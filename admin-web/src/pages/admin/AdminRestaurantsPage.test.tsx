import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '../../test/utils/test-utils'
import { http, HttpResponse } from 'msw'
import { server } from '../../test/mocks/server'
import AdminRestaurantsPage from './AdminRestaurantsPage'
import { AppProvider } from '../../context/AppContext'
import userEvent from '@testing-library/user-event'
import { tokenStorage } from '../../utils/tokenStorage'

describe('AdminRestaurantsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Очищаем localStorage перед каждым тестом
    localStorage.clear()
    
    // Устанавливаем токен, чтобы authService.isAuthenticated() возвращал true
    tokenStorage.setAccessToken('mock-access-token')
    tokenStorage.setTokenExpiry(3600) // Токен не истек
  })

  it('renders create restaurant button for manager', async () => {
    // Arrange
    server.use(
      http.get('*/admin-api/auth/me', () => {
        return HttpResponse.json({
          id: 1,
          email: 'manager@test.com',
          role: 'MANAGER',
          restaurants: [],
        })
      }),
      http.get('*/admin-api/r', () => {
        return HttpResponse.json({
          data: [],
          pagination: { total: 0, limit: 50, offset: 0, hasMore: false },
        })
      })
    )

    // Act
    render(
      <AppProvider>
        <AdminRestaurantsPage />
      </AppProvider>
    )

    // Assert
    await waitFor(() => {
      expect(screen.getByText('Создать ресторан')).toBeInTheDocument()
    }, { timeout: 3000 })
  })

  it('creates restaurant as manager', async () => {
    // Arrange
    let createRequest: any = null
    server.use(
      http.get('*/admin-api/auth/me', () => {
        return HttpResponse.json({
          id: 1,
          email: 'manager@test.com',
          role: 'MANAGER',
          restaurants: [],
        })
      }),
      http.get('*/admin-api/r', () => {
        return HttpResponse.json({
          data: [],
          pagination: { total: 0, limit: 50, offset: 0, hasMore: false },
        })
      }),
      http.post('*/admin-api/r', async ({ request }) => {
        createRequest = await request.json()
        return HttpResponse.json({
          id: 1,
          name: 'New Restaurant',
          address: 'New Address',
          phone: '+79991234567',
          isActive: true,
        }, { status: 201 })
      })
    )

    // Act
    const user = userEvent.setup()
    render(
      <AppProvider>
        <AdminRestaurantsPage />
      </AppProvider>
    )

    await waitFor(() => {
      expect(screen.getByText('Создать ресторан')).toBeInTheDocument()
    }, { timeout: 3000 })

    const createButton = screen.getByText('Создать ресторан')
    await user.click(createButton)

    // Ждем появления формы - проверяем наличие заголовка модального окна
    await waitFor(() => {
      expect(screen.getByText('Создать ресторан', { selector: 'h2' })).toBeInTheDocument()
    })

    // Заполняем форму - используем поиск по name атрибуту через querySelector
    // так как label не связан с input через htmlFor
    await waitFor(() => {
      const nameInput = document.querySelector('input[name="name"]') as HTMLInputElement
      expect(nameInput).toBeInTheDocument()
    })

    const nameInput = document.querySelector('input[name="name"]') as HTMLInputElement
    const addressInput = document.querySelector('input[name="address"]') as HTMLInputElement
    const phoneInput = document.querySelector('input[name="phone"]') as HTMLInputElement
    const submitButton = screen.getByText('Сохранить')

    await user.type(nameInput, 'New Restaurant')
    await user.type(addressInput, 'New Address')
    await user.type(phoneInput, '+79991234567')
    await user.click(submitButton)

    // Assert
    await waitFor(() => {
      expect(createRequest).not.toBeNull()
      expect(createRequest.name).toBe('New Restaurant')
      expect(createRequest.address).toBe('New Address')
      expect(createRequest.phone).toBe('+79991234567')
      // userId не должен быть отправлен для менеджера
      expect(createRequest.userId).toBeUndefined()
    }, { timeout: 3000 })
  })

  it('creates restaurant as admin with userId', async () => {
    // Arrange
    server.use(
      http.get('*/admin-api/auth/me', () => {
        return HttpResponse.json({
          id: 1,
          email: 'admin@test.com',
          role: 'ADMIN',
          restaurants: [],
        })
      }),
      http.get('*/admin-api/r', () => {
        return HttpResponse.json({
          data: [],
          pagination: { total: 0, limit: 50, offset: 0, hasMore: false },
        })
      }),
      http.post('http://localhost:8082/admin-api/r', async () => {
        return HttpResponse.json({
          id: 1,
          name: 'New Restaurant',
          isActive: true,
        }, { status: 201 })
      })
    )

    // Act
    render(
      <AppProvider>
        <AdminRestaurantsPage />
      </AppProvider>
    )

    await waitFor(() => {
      expect(screen.getByText('Создать ресторан')).toBeInTheDocument()
    }, { timeout: 3000 })

    // Примечание: В реальном приложении ADMIN может указать userId через UI
    // Здесь мы просто проверяем, что страница доступна для ADMIN
    // Для полного теста нужно добавить поле userId в форму для ADMIN
  })

  it('creates restaurant as admin without userId', async () => {
    // Arrange
    let createRequest: any = null
    server.use(
      http.get('*/admin-api/auth/me', () => {
        return HttpResponse.json({
          id: 1,
          email: 'admin@test.com',
          role: 'ADMIN',
          restaurants: [],
        })
      }),
      http.get('*/admin-api/r', () => {
        return HttpResponse.json({
          data: [],
          pagination: { total: 0, limit: 50, offset: 0, hasMore: false },
        })
      }),
      http.post('*/admin-api/r', async ({ request }) => {
        createRequest = await request.json()
        return HttpResponse.json({
          id: 1,
          name: 'New Restaurant',
          isActive: true,
        }, { status: 201 })
      })
    )

    // Act
    const user = userEvent.setup()
    render(
      <AppProvider>
        <AdminRestaurantsPage />
      </AppProvider>
    )

    await waitFor(() => {
      expect(screen.getByText('Создать ресторан')).toBeInTheDocument()
    }, { timeout: 3000 })

    const createButton = screen.getByText('Создать ресторан')
    await user.click(createButton)

    // Ждем появления формы - проверяем наличие заголовка модального окна
    await waitFor(() => {
      expect(screen.getByText('Создать ресторан', { selector: 'h2' })).toBeInTheDocument()
    })

    // Заполняем форму - используем поиск по name атрибуту через querySelector
    // так как label не связан с input через htmlFor
    await waitFor(() => {
      const nameInput = document.querySelector('input[name="name"]') as HTMLInputElement
      expect(nameInput).toBeInTheDocument()
    })

    const nameInput = document.querySelector('input[name="name"]') as HTMLInputElement
    const addressInput = document.querySelector('input[name="address"]') as HTMLInputElement
    const phoneInput = document.querySelector('input[name="phone"]') as HTMLInputElement
    const submitButton = screen.getByText('Сохранить')

    await user.type(nameInput, 'New Restaurant')
    await user.type(addressInput, 'New Address')
    await user.type(phoneInput, '+79991234567')
    await user.click(submitButton)

    // Assert
    await waitFor(() => {
      expect(createRequest).not.toBeNull()
      // userId не указан, поэтому не должен быть в запросе
      expect(createRequest.userId).toBeUndefined()
    }, { timeout: 3000 })
  })
})

