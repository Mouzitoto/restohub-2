import { describe, it, expect, beforeEach, vi } from 'vitest'
import { tokenStorage } from '../utils/tokenStorage'
import { server } from '../test/mocks/server'
import { http, HttpResponse } from 'msw'

// Мокаем tokenStorage
vi.mock('../utils/tokenStorage', () => ({
  tokenStorage: {
    getAccessToken: vi.fn(),
    getRefreshToken: vi.fn(),
    setAccessToken: vi.fn(),
    setRefreshToken: vi.fn(),
    setTokenExpiry: vi.fn(),
    clearTokens: vi.fn(),
    isTokenExpired: vi.fn(),
    isTokenExpiringSoon: vi.fn(),
  },
}))

// Импортируем apiClient
import { apiClient } from './apiClient'

// Мокаем window.location (для обратной совместимости, если где-то еще используется)
const mockLocation = {
  href: '',
}
Object.defineProperty(window, 'location', {
  value: mockLocation,
  writable: true,
})

// Мокаем window.dispatchEvent для проверки событий
const mockDispatchEvent = vi.fn()
window.dispatchEvent = mockDispatchEvent

describe('ApiClient - Token Refresh Logic', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    server.resetHandlers()
    mockLocation.href = ''
    mockDispatchEvent.mockClear()
  })

  describe('Token expiration handling', () => {
    it('should automatically refresh token when receiving 401 with TOKEN_EXPIRED', async () => {
      // Arrange
      const expiredAccessToken = 'expired-access-token'
      const refreshToken = 'valid-refresh-token'
      const newAccessToken = 'new-access-token'
      const newRefreshToken = 'new-refresh-token'

      vi.mocked(tokenStorage.getAccessToken).mockReturnValue(expiredAccessToken)
      vi.mocked(tokenStorage.getRefreshToken).mockReturnValue(refreshToken)

      let requestCount = 0

      // Первый запрос возвращает 401 с TOKEN_EXPIRED
      // Используем паттерн с * для перехвата запросов с любым origin, как в handlers.ts
      console.log('[TEST] Setting up MSW handlers')
      server.use(
        http.get('*/admin-api/menu-category', ({ request }) => {
          console.log('[MSW] GET /admin-api/menu-category intercepted')
          console.log('[MSW] Request URL:', request.url)
          console.log('[MSW] Request method:', request.method)
          requestCount++
          console.log('[MSW] Request count:', requestCount)
          if (requestCount === 1) {
            console.log('[MSW] Returning 401 TOKEN_EXPIRED')
            return HttpResponse.json(
              { exceptionName: 'TOKEN_EXPIRED', message: 'Токен истек' },
              { status: 401 }
            )
          }
          // После обновления токена запрос должен пройти успешно
          console.log('[MSW] Returning 200 success')
          return HttpResponse.json({ data: [] })
        }),
        http.post('*/admin-api/auth/refresh', async ({ request }) => {
          console.log('[MSW] POST /admin-api/auth/refresh intercepted')
          console.log('[MSW] Request URL:', request.url)
          const body = await request.json() as { refreshToken: string }
          console.log('[MSW] Refresh token from request:', body.refreshToken)
          console.log('[MSW] Expected refresh token:', refreshToken)
          if (body.refreshToken === refreshToken) {
            console.log('[MSW] Refresh token matches, returning new tokens')
            return HttpResponse.json({
              accessToken: newAccessToken,
              refreshToken: newRefreshToken,
              expiresIn: 300,
            })
          }
          console.log('[MSW] Refresh token mismatch, returning 401')
          return HttpResponse.json({ error: 'Invalid refresh token' }, { status: 401 })
        })
      )
      console.log('[TEST] MSW handlers set up')

      // Act - используем относительный путь, axios добавит baseURL
      // MSW должен перехватить запрос через паттерн */admin-api/menu-category
      console.log('[TEST] Before request')
      console.log('[TEST] apiClient.instance.defaults.baseURL:', apiClient.instance.defaults.baseURL)
      console.log('[TEST] apiClient.instance.defaults:', JSON.stringify(apiClient.instance.defaults, null, 2))
      console.log('[TEST] import.meta.env.VITE_API_BASE_URL:', import.meta.env.VITE_API_BASE_URL)
      console.log('[TEST] Making request to: /admin-api/menu-category')
      
      // Используем относительный путь - axios автоматически добавит baseURL
      // MSW должен перехватить полный URL через паттерн */admin-api/menu-category
      const response = await apiClient.instance.get('/admin-api/menu-category')
      console.log('[TEST] Request successful, status:', response.status)

      // Assert
      expect(response.status).toBe(200)
      expect(requestCount).toBe(2) // Первый запрос + повторный запрос после обновления
      expect(tokenStorage.setAccessToken).toHaveBeenCalledWith(newAccessToken)
      expect(tokenStorage.setRefreshToken).toHaveBeenCalledWith(newRefreshToken)
      expect(tokenStorage.setTokenExpiry).toHaveBeenCalledWith(300)
    })

    it('should retry original request after successful token refresh', async () => {
      // Arrange
      const expiredAccessToken = 'expired-access-token'
      const refreshToken = 'valid-refresh-token'
      const newAccessToken = 'new-access-token'
      const newRefreshToken = 'new-refresh-token'

      vi.mocked(tokenStorage.getAccessToken).mockReturnValue(expiredAccessToken)
      vi.mocked(tokenStorage.getRefreshToken).mockReturnValue(refreshToken)

      let originalRequestCount = 0
      const expectedData = { id: 1, name: 'Test Restaurant' }

      server.use(
        http.get('*/admin-api/r/1', () => {
          originalRequestCount++
          if (originalRequestCount === 1) {
            return HttpResponse.json(
              { exceptionName: 'TOKEN_EXPIRED', message: 'Токен истек' },
              { status: 401 }
            )
          }
          // После обновления токена возвращаем данные
          return HttpResponse.json(expectedData)
        }),
        http.post('*/admin-api/auth/refresh', async ({ request }) => {
          const body = await request.json() as { refreshToken: string }
          if (body.refreshToken === refreshToken) {
            return HttpResponse.json({
              accessToken: newAccessToken,
              refreshToken: newRefreshToken,
              expiresIn: 300,
            })
          }
          return HttpResponse.json({ error: 'Invalid refresh token' }, { status: 401 })
        })
      )

      // Act
      const response = await apiClient.instance.get('/admin-api/r/1')

      // Assert
      expect(response.status).toBe(200)
      expect(response.data).toEqual(expectedData)
      expect(originalRequestCount).toBe(2) // Первый запрос + повторный после обновления
    })

    it('should dispatch auth:logout event when refresh token is expired', async () => {
      // Arrange
      const expiredAccessToken = 'expired-access-token'
      const expiredRefreshToken = 'expired-refresh-token'

      vi.mocked(tokenStorage.getAccessToken).mockReturnValue(expiredAccessToken)
      vi.mocked(tokenStorage.getRefreshToken).mockReturnValue(expiredRefreshToken)

      server.use(
        http.get('*/admin-api/menu-category', () => {
          return HttpResponse.json(
            { exceptionName: 'TOKEN_EXPIRED', message: 'Токен истек' },
            { status: 401 }
          )
        }),
        http.post('*/admin-api/auth/refresh', () => {
          return HttpResponse.json(
            { exceptionName: 'REFRESH_TOKEN_EXPIRED', message: 'Refresh token истек' },
            { status: 401 }
          )
        })
      )

      // Act
      try {
        await apiClient.instance.get('/admin-api/menu-category')
        expect.fail('Should have thrown an error')
      } catch (error) {
        // Assert
        // clearTokens вызывается в refreshToken при REFRESH_TOKEN_EXPIRED
        // handleAuthError также вызывается, но может быть асинхронно
        // Проверяем, что clearTokens был вызван хотя бы один раз
        await new Promise(resolve => setTimeout(resolve, 50))
        expect(tokenStorage.clearTokens).toHaveBeenCalled()
        // Проверяем, что было отправлено событие auth:logout
        expect(mockDispatchEvent).toHaveBeenCalled()
        const lastCall = mockDispatchEvent.mock.calls[mockDispatchEvent.mock.calls.length - 1]
        expect(lastCall[0]).toBeInstanceOf(CustomEvent)
        const event = lastCall[0] as CustomEvent<{ redirectTo: string }>
        expect(event.type).toBe('auth:logout')
        expect(event.detail?.redirectTo).toBe('/login')
      }
    })

    it('should queue multiple requests when token refresh is in progress', async () => {
      // Arrange
      const expiredAccessToken = 'expired-access-token'
      const refreshToken = 'valid-refresh-token'
      const newAccessToken = 'new-access-token'
      const newRefreshToken = 'new-refresh-token'

      vi.mocked(tokenStorage.getAccessToken).mockReturnValue(expiredAccessToken)
      vi.mocked(tokenStorage.getRefreshToken).mockReturnValue(refreshToken)

      let refreshCallCount = 0
      let request1Count = 0
      let request2Count = 0

      server.use(
        http.get('*/admin-api/menu-category', () => {
          request1Count++
          if (request1Count === 1) {
            return HttpResponse.json(
              { exceptionName: 'TOKEN_EXPIRED', message: 'Токен истек' },
              { status: 401 }
            )
          }
          return HttpResponse.json({ data: [] })
        }),
        http.get('*/admin-api/r', () => {
          request2Count++
          if (request2Count === 1) {
            return HttpResponse.json(
              { exceptionName: 'TOKEN_EXPIRED', message: 'Токен истек' },
              { status: 401 }
            )
          }
          return HttpResponse.json({ data: [] })
        }),
        http.post('*/admin-api/auth/refresh', async () => {
          refreshCallCount++
          // Имитируем задержку обновления токена
          await new Promise((resolve) => setTimeout(resolve, 100))
          return HttpResponse.json({
            accessToken: newAccessToken,
            refreshToken: newRefreshToken,
            expiresIn: 300,
          })
        })
      )

      // Act - запускаем два запроса одновременно
      const [response1, response2] = await Promise.all([
        apiClient.instance.get('/admin-api/menu-category'),
        apiClient.instance.get('/admin-api/r'),
      ])

      // Assert
      expect(response1.status).toBe(200)
      expect(response2.status).toBe(200)
      // Обновление токена должно произойти только один раз
      expect(refreshCallCount).toBe(1)
      // Оба запроса должны быть повторены после обновления
      expect(request1Count).toBe(2)
      expect(request2Count).toBe(2)
    })

    it('should dispatch auth:logout event for 401 errors with UNAUTHORIZED', async () => {
      // Arrange
      const accessToken = 'valid-access-token'

      vi.mocked(tokenStorage.getAccessToken).mockReturnValue(accessToken)
      vi.mocked(tokenStorage.getRefreshToken).mockReturnValue(null)

      server.use(
        http.get('*/admin-api/menu-category', () => {
          return HttpResponse.json(
            { exceptionName: 'UNAUTHORIZED', message: 'Требуется аутентификация' },
            { status: 401 }
          )
        })
      )

      // Act
      try {
        await apiClient.instance.get('/admin-api/menu-category')
        expect.fail('Should have thrown an error')
      } catch (error: any) {
        // Assert
        // Проверяем, что ошибка была выброшена
        expect(error).toBeDefined()
        // Проверяем статус ответа, если он есть
        if (error.response) {
          expect(error.response.status).toBe(401)
          expect(error.response.data?.exceptionName).toBe('UNAUTHORIZED')
        }
        // При UNAUTHORIZED сразу вызывается handleAuthError, который вызывает clearTokens
        // Ждем немного, чтобы handleAuthError успел выполниться
        await new Promise(resolve => setTimeout(resolve, 50))
        expect(tokenStorage.clearTokens).toHaveBeenCalled()
        // Проверяем, что было отправлено событие auth:logout
        expect(mockDispatchEvent).toHaveBeenCalled()
        const lastCall = mockDispatchEvent.mock.calls[mockDispatchEvent.mock.calls.length - 1]
        expect(lastCall[0]).toBeInstanceOf(CustomEvent)
        const event = lastCall[0] as CustomEvent<{ redirectTo: string }>
        expect(event.type).toBe('auth:logout')
        expect(event.detail?.redirectTo).toBe('/login')
      }
    })

    it('should attempt token refresh for 401 errors without exceptionName', async () => {
      // Arrange
      const expiredAccessToken = 'expired-access-token'
      const refreshToken = 'valid-refresh-token'
      const newAccessToken = 'new-access-token'
      const newRefreshToken = 'new-refresh-token'

      vi.mocked(tokenStorage.getAccessToken).mockReturnValue(expiredAccessToken)
      vi.mocked(tokenStorage.getRefreshToken).mockReturnValue(refreshToken)

      let requestCount = 0

      server.use(
        http.get('*/admin-api/menu-category', () => {
          requestCount++
          if (requestCount === 1) {
            // Возвращаем 401 без exceptionName (как будто токен просто истек)
            return HttpResponse.json(
              { message: 'Unauthorized' },
              { status: 401 }
            )
          }
          return HttpResponse.json({ data: [] })
        }),
        http.post('*/admin-api/auth/refresh', async ({ request }) => {
          const body = await request.json() as { refreshToken: string }
          if (body.refreshToken === refreshToken) {
            return HttpResponse.json({
              accessToken: newAccessToken,
              refreshToken: newRefreshToken,
              expiresIn: 300,
            })
          }
          return HttpResponse.json({ error: 'Invalid refresh token' }, { status: 401 })
        })
      )

      // Act
      const response = await apiClient.instance.get('/admin-api/menu-category')

      // Assert
      expect(response.status).toBe(200)
      expect(requestCount).toBe(2) // Первый запрос + повторный после обновления
      expect(tokenStorage.setAccessToken).toHaveBeenCalledWith(newAccessToken)
      expect(tokenStorage.setRefreshToken).toHaveBeenCalledWith(newRefreshToken)
    })

    it('should add Authorization header to requests', async () => {
      // Arrange
      const accessToken = 'test-access-token'

      vi.mocked(tokenStorage.getAccessToken).mockReturnValue(accessToken)

      let capturedHeaders: Headers | null = null

      server.use(
        http.get('*/admin-api/menu-category', ({ request }) => {
          capturedHeaders = request.headers
          return HttpResponse.json({ data: [] })
        })
      )

      // Act
      await apiClient.instance.get('/admin-api/menu-category')

      // Assert
      expect(capturedHeaders).not.toBeNull()
      expect(capturedHeaders!.get('Authorization')).toBe(`Bearer ${accessToken}`)
    })

    it('should not add Authorization header for public endpoints', async () => {
      // Arrange
      const accessToken = 'test-access-token'

      vi.mocked(tokenStorage.getAccessToken).mockReturnValue(accessToken)

      let capturedHeaders: Headers | null = null

      server.use(
        http.post('*/admin-api/auth/login', async ({ request }) => {
          capturedHeaders = request.headers
          return HttpResponse.json({
            accessToken: 'new-token',
            refreshToken: 'new-refresh-token',
            expiresIn: 300,
          })
        })
      )

      // Act
      await apiClient.instance.post('/admin-api/auth/login', {
        email: 'test@example.com',
        password: 'password123',
      })

      // Assert
      expect(capturedHeaders).not.toBeNull()
      // Токен не должен быть добавлен для публичного эндпоинта
      expect(capturedHeaders!.get('Authorization')).toBeNull()
    })

    it('should not add Authorization header for refresh endpoint', async () => {
      // Arrange
      const accessToken = 'test-access-token'
      const refreshToken = 'test-refresh-token'

      vi.mocked(tokenStorage.getAccessToken).mockReturnValue(accessToken)
      vi.mocked(tokenStorage.getRefreshToken).mockReturnValue(refreshToken)

      let capturedHeaders: Headers | null = null

      server.use(
        http.post('*/admin-api/auth/refresh', async ({ request }) => {
          capturedHeaders = request.headers
          return HttpResponse.json({
            accessToken: 'new-token',
            refreshToken: 'new-refresh-token',
            expiresIn: 300,
          })
        })
      )

      // Act
      await apiClient.instance.post('/admin-api/auth/refresh', {
        refreshToken,
      })

      // Assert
      expect(capturedHeaders).not.toBeNull()
      // Токен не должен быть добавлен для публичного эндпоинта
      expect(capturedHeaders!.get('Authorization')).toBeNull()
    })
  })
})

