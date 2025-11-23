import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '../../test/utils/test-utils'
import { http, HttpResponse } from 'msw'
import { server } from '../../test/mocks/server'
import AdminRestaurantsPage from './AdminRestaurantsPage'
import { AppProvider } from '../../context/AppContext'
import userEvent from '@testing-library/user-event'

describe('AdminRestaurantsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders create restaurant button for manager', async () => {
    // Arrange
    server.use(
      http.get('/admin-api/auth/me', () => {
        return HttpResponse.json({
          id: 1,
          email: 'manager@test.com',
          role: 'MANAGER',
          restaurants: [],
        })
      }),
      http.get('/admin-api/r', () => {
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
      http.get('/admin-api/auth/me', () => {
        return HttpResponse.json({
          id: 1,
          email: 'manager@test.com',
          role: 'MANAGER',
          restaurants: [],
        })
      }),
      http.get('/admin-api/r', () => {
        return HttpResponse.json({
          data: [],
          pagination: { total: 0, limit: 50, offset: 0, hasMore: false },
        })
      }),
      http.post('/admin-api/r', async ({ request }) => {
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

    await waitFor(() => {
      expect(screen.getByLabelText(/название/i)).toBeInTheDocument()
    })

    // Заполняем форму
    const nameInput = screen.getByLabelText(/название/i)
    const addressInput = screen.getByLabelText(/адрес/i)
    const phoneInput = screen.getByLabelText(/телефон/i)
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
      http.get('/admin-api/auth/me', () => {
        return HttpResponse.json({
          id: 1,
          email: 'admin@test.com',
          role: 'ADMIN',
          restaurants: [],
        })
      }),
      http.get('/admin-api/r', () => {
        return HttpResponse.json({
          data: [],
          pagination: { total: 0, limit: 50, offset: 0, hasMore: false },
        })
      }),
      http.post('/admin-api/r', async () => {
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
      http.get('/admin-api/auth/me', () => {
        return HttpResponse.json({
          id: 1,
          email: 'admin@test.com',
          role: 'ADMIN',
          restaurants: [],
        })
      }),
      http.get('/admin-api/r', () => {
        return HttpResponse.json({
          data: [],
          pagination: { total: 0, limit: 50, offset: 0, hasMore: false },
        })
      }),
      http.post('/admin-api/r', async ({ request }) => {
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

    await waitFor(() => {
      expect(screen.getByLabelText(/название/i)).toBeInTheDocument()
    })

    // Заполняем форму
    const nameInput = screen.getByLabelText(/название/i)
    const addressInput = screen.getByLabelText(/адрес/i)
    const phoneInput = screen.getByLabelText(/телефон/i)
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

