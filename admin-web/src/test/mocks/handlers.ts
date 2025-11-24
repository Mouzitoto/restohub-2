import { http, HttpResponse } from 'msw'

// Используем паттерн для перехвата запросов с любым origin
export const handlers = [
  // Auth handlers
  http.post('*/admin-api/auth/login', () => {
    return HttpResponse.json({
      accessToken: 'mock-access-token',
      refreshToken: 'mock-refresh-token',
      role: 'MANAGER',
      expiresIn: 300,
    })
  }),

  http.post('*/admin-api/auth/refresh', () => {
    return HttpResponse.json({
      accessToken: 'new-mock-access-token',
      refreshToken: 'new-mock-refresh-token',
      expiresIn: 300,
    })
  }),

  http.get('*/admin-api/auth/me', () => {
    return HttpResponse.json({
      email: 'test@example.com',
      role: 'MANAGER',
      restaurants: [
        {
          id: 1,
          name: 'Test Restaurant',
          subscription: {
            isActive: true,
            endDate: '2024-12-31',
            daysRemaining: 30,
            isExpiringSoon: false,
          },
        },
      ],
    })
  }),

  // Restaurant handlers
  http.get('*/admin-api/r', () => {
    return HttpResponse.json({
      data: [
        {
          id: 1,
          name: 'Test Restaurant',
          address: 'Test Address',
        },
      ],
      pagination: {
        total: 1,
        limit: 50,
        offset: 0,
        hasMore: false,
      },
    })
  }),

  http.get('*/admin-api/r/:id', () => {
    return HttpResponse.json({
      id: 1,
      name: 'Test Restaurant',
      address: 'Test Address',
      phone: '+79991234567',
      isActive: true,
    })
  }),

  http.post('*/admin-api/r', async ({ request }) => {
    const body = await request.json() as any
    return HttpResponse.json({
      id: 1,
      name: body.name,
      address: body.address,
      phone: body.phone,
      isActive: true,
    }, { status: 201 })
  }),

  // Menu category handlers
  http.get('*/admin-api/menu-category', () => {
    return HttpResponse.json({
      data: [],
      total: 0,
    })
  }),

  // Analytics handlers
  http.get('*/admin-api/r/:id/analytics/overview', () => {
    return HttpResponse.json({
      bookings: 10,
      preOrders: 5,
      revenue: 50000,
      newClients: 3,
    })
  }),

  // Image handlers
  http.get('*/admin-api/image', async () => {
    // Создаем простой blob для тестирования (1x1 PNG)
    const pngBase64 = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=='
    const blob = Uint8Array.from(atob(pngBase64), c => c.charCodeAt(0))
    
    return HttpResponse.arrayBuffer(blob.buffer, {
      headers: {
        'Content-Type': 'image/png',
      },
    })
  }),
]

