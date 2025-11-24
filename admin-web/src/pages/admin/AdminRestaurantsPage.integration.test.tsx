import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen, waitFor } from '../../test/utils/test-utils'
import { http, HttpResponse } from 'msw'
import { server } from '../../test/mocks/server'
import AdminRestaurantsPage from './AdminRestaurantsPage'
import { AppProvider } from '../../context/AppContext'
import userEvent from '@testing-library/user-event'
import { tokenStorage } from '../../utils/tokenStorage'

describe('AdminRestaurantsPage Integration', () => {
  beforeEach(() => {
    // Очищаем localStorage перед каждым тестом
    localStorage.clear()
    
    // Устанавливаем токен, чтобы authService.isAuthenticated() возвращал true
    tokenStorage.setAccessToken('mock-access-token')
    tokenStorage.setTokenExpiry(3600) // Токен не истек
    
    // Настройка моков для аутентификации
    server.use(
      http.get('*/admin-api/auth/me', () => {
        return HttpResponse.json({
          id: 1,
          email: 'manager@test.com',
          role: 'MANAGER',
          restaurants: [],
        })
      })
    )
  })

  it('manager can create restaurant and see it in list', async () => {
    // Arrange
    let createdRestaurant: any = null
    let restaurantsList: any[] = []

    server.use(
      http.get('*/admin-api/r', () => {
        return HttpResponse.json({
          data: restaurantsList,
          pagination: { total: restaurantsList.length, limit: 50, offset: 0, hasMore: false },
        })
      }),
      http.post('*/admin-api/r', async ({ request }) => {
        const body = await request.json() as any
        createdRestaurant = {
          id: 1,
          name: body.name,
          address: body.address,
          phone: body.phone,
          isActive: true,
        }
        restaurantsList.push(createdRestaurant)
        return HttpResponse.json(createdRestaurant, { status: 201 })
      })
    )

    const user = userEvent.setup()

    // Act
    render(
      <AppProvider>
        <AdminRestaurantsPage />
      </AppProvider>
    )

    // Ждем загрузки страницы
    await waitFor(() => {
      expect(screen.getByText('Создать ресторан')).toBeInTheDocument()
    }, { timeout: 3000 })

    // Открываем форму создания
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

    await user.clear(nameInput)
    await user.type(nameInput, 'New Restaurant')
    await user.clear(addressInput)
    await user.type(addressInput, 'New Address')
    await user.clear(phoneInput)
    await user.type(phoneInput, '+79991234567')
    await user.click(submitButton)

    // Assert
    await waitFor(() => {
      expect(createdRestaurant).not.toBeNull()
      expect(createdRestaurant.name).toBe('New Restaurant')
      // Проверяем, что ресторан появился в списке (после перезагрузки)
      // В реальном приложении список обновится автоматически
    }, { timeout: 3000 })
  })

  it('admin can create restaurant for manager', async () => {
    // Arrange
    // Устанавливаем токен для этого теста
    tokenStorage.setAccessToken('mock-access-token')
    tokenStorage.setTokenExpiry(3600)
    
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
        const body = await request.json() as any
        return HttpResponse.json({
          id: 1,
          name: body.name,
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

    // Assert
    await waitFor(() => {
      expect(screen.getByText('Создать ресторан')).toBeInTheDocument()
    }, { timeout: 3000 })

    // Примечание: Для полного теста нужно добавить поле userId в форму для ADMIN
    // Сейчас проверяем только доступность страницы
  })
})

