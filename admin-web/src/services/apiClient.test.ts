import { describe, it, expect, beforeEach, vi } from 'vitest'
import { apiClient } from './apiClient'
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

// Мокаем window.location
const mockLocation = {
  href: '',
}
Object.defineProperty(window, 'location', {
  value: mockLocation,
  writable: true,
})

describe('ApiClient - Token Refresh Logic', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    server.resetHandlers()
    mockLocation.href = ''
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
      server.use(
        http.get('http://localhost:8082/admin-api/menu-category', () => {
          requestCount++
          if (requestCount === 1) {
            return HttpResponse.json(
              { exceptionName: 'TOKEN_EXPIRED', message: 'Токен истек' },
              { status: 401 }
            )
          }
          // После обновления токена запрос должен пройти успешно
          return HttpResponse.json({ data: [] })
        }),
        http.post('http://localhost:8082/admin-api/auth/refresh', async ({ request }) => {
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
        http.get('http://localhost:8082/admin-api/r/1', () => {
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
        http.post('http://localhost:8082/admin-api/auth/refresh', async ({ request }) => {
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

    it('should redirect to login when refresh token is expired', async () => {
      // Arrange
      const expiredAccessToken = 'expired-access-token'
      const expiredRefreshToken = 'expired-refresh-token'

      vi.mocked(tokenStorage.getAccessToken).mockReturnValue(expiredAccessToken)
      vi.mocked(tokenStorage.getRefreshToken).mockReturnValue(expiredRefreshToken)

      server.use(
        http.get('http://localhost:8082/admin-api/menu-category', () => {
          return HttpResponse.json(
            { exceptionName: 'TOKEN_EXPIRED', message: 'Токен истек' },
            { status: 401 }
          )
        }),
        http.post('http://localhost:8082/admin-api/auth/refresh', () => {
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
        expect(tokenStorage.clearTokens).toHaveBeenCalled()
        expect(mockLocation.href).toBe('/login')
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
        http.get('http://localhost:8082/admin-api/menu-category', () => {
          request1Count++
          if (request1Count === 1) {
            return HttpResponse.json(
              { exceptionName: 'TOKEN_EXPIRED', message: 'Токен истек' },
              { status: 401 }
            )
          }
          return HttpResponse.json({ data: [] })
        }),
        http.get('http://localhost:8082/admin-api/r', () => {
          request2Count++
          if (request2Count === 1) {
            return HttpResponse.json(
              { exceptionName: 'TOKEN_EXPIRED', message: 'Токен истек' },
              { status: 401 }
            )
          }
          return HttpResponse.json({ data: [] })
        }),
        http.post('http://localhost:8082/admin-api/auth/refresh', async () => {
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

    it('should not refresh token for 401 errors without TOKEN_EXPIRED', async () => {
      // Arrange
      const accessToken = 'valid-access-token'

      vi.mocked(tokenStorage.getAccessToken).mockReturnValue(accessToken)
      vi.mocked(tokenStorage.getRefreshToken).mockReturnValue(null)

      server.use(
        http.get('http://localhost:8082/admin-api/menu-category', () => {
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
        expect(error.response?.status).toBe(401)
        expect(error.response?.data?.exceptionName).toBe('UNAUTHORIZED')
        // Не должно быть попыток обновить токен
        expect(tokenStorage.getRefreshToken).not.toHaveBeenCalled()
        expect(mockLocation.href).toBe('/login')
      }
    })

    it('should add Authorization header to requests', async () => {
      // Arrange
      const accessToken = 'test-access-token'

      vi.mocked(tokenStorage.getAccessToken).mockReturnValue(accessToken)

      let capturedHeaders: Headers | null = null

      server.use(
        http.get('http://localhost:8082/admin-api/menu-category', ({ request }) => {
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
  })
})

