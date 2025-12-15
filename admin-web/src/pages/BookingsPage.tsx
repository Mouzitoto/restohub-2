import { useEffect, useState } from 'react'
import { useApp } from '../context/AppContext'
import { apiClient } from '../services/apiClient'
import Modal from '../components/common/Modal'
import { useToast } from '../context/ToastContext'
import type { Booking, PreOrderItem } from '../types'

export default function BookingsPage() {
  const { currentRestaurant } = useApp()
  const [bookings, setBookings] = useState<Booking[]>([])
  const [selectedBooking, setSelectedBooking] = useState<Booking | null>(null)
  const [preOrderItems, setPreOrderItems] = useState<PreOrderItem[]>([])
  const [expandedBookingId, setExpandedBookingId] = useState<number | null>(null)
  const toast = useToast()

  useEffect(() => {
    if (currentRestaurant) {
      loadBookings()
    }
  }, [currentRestaurant])

  const loadBookings = async () => {
    if (!currentRestaurant) return

    try {
      const response = await apiClient.instance.get<{ data: Booking[]; total: number }>(
        `/admin-api/r/${currentRestaurant.id}/booking`
      )
      setBookings(Array.isArray(response.data?.data) ? response.data.data : [])
    } catch (error) {
      toast.error('Не удалось загрузить бронирования')
    }
  }

  const loadPreOrderItems = async (bookingId: number) => {
    if (!currentRestaurant) return

    try {
      const response = await apiClient.instance.get<PreOrderItem[] | { data: PreOrderItem[] }>(
        `/admin-api/r/${currentRestaurant.id}/booking/${bookingId}/pre-order-items`
      )
      // Проверяем, является ли ответ массивом или объектом с data
      if (Array.isArray(response.data)) {
        setPreOrderItems(response.data)
      } else if (response.data && 'data' in response.data && Array.isArray(response.data.data)) {
        setPreOrderItems(response.data.data)
      } else {
        setPreOrderItems([])
      }
    } catch (error) {
      toast.error('Не удалось загрузить предзаказ')
      setPreOrderItems([])
    }
  }

  const handleBookingClick = (booking: Booking) => {
    if (expandedBookingId === booking.id) {
      setExpandedBookingId(null)
      setPreOrderItems([])
    } else {
      setExpandedBookingId(booking.id)
      if (booking.preOrderItemsCount > 0) {
        loadPreOrderItems(booking.id)
      }
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return '#999'
      case 'APPROVED':
        return '#4caf50'
      case 'REJECTED':
        return '#f44336'
      case 'CANCELLED_BY_CLIENT':
      case 'CANCELLED_BY_MANAGER':
        return '#ff9800'
      default:
        return '#999'
    }
  }

  if (!currentRestaurant) {
    return <div>Ресторан не выбран</div>
  }

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Бронирования</h1>

      <div style={{ backgroundColor: 'white', borderRadius: '8px', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: '#f5f5f5' }}>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Дата и время</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Клиент</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Стол</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Персон</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Предзаказ</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Статус</th>
              <th style={{ padding: '1rem' }}>Действия</th>
            </tr>
          </thead>
          <tbody>
            {bookings.map((booking) => (
              <>
                <tr
                  key={booking.id}
                  style={{
                    borderTop: '1px solid #eee',
                    cursor: booking.preOrderItemsCount > 0 ? 'pointer' : 'default',
                    backgroundColor: expandedBookingId === booking.id ? '#f0f8ff' : 'white',
                  }}
                  onClick={() => booking.preOrderItemsCount > 0 && handleBookingClick(booking)}
                >
                  <td style={{ padding: '1rem' }}>
                    {new Date(booking.bookingDate).toLocaleString('ru-RU')}
                  </td>
                  <td style={{ padding: '1rem' }}>
                    <div>{booking.clientName || '-'}</div>
                    <div style={{ fontSize: '0.875rem', color: '#666' }}>{booking.clientPhone}</div>
                  </td>
                  <td style={{ padding: '1rem' }}>Стол #{booking.tableId}</td>
                  <td style={{ padding: '1rem' }}>{booking.numberOfPersons}</td>
                  <td style={{ padding: '1rem' }}>
                    {booking.preOrderItemsCount > 0 ? (
                      <span style={{ backgroundColor: '#e3f2fd', padding: '0.25rem 0.5rem', borderRadius: '4px' }}>
                        {booking.preOrderItemsCount}
                      </span>
                    ) : (
                      '-'
                    )}
                  </td>
                  <td style={{ padding: '1rem' }}>
                    <span
                      style={{
                        padding: '0.25rem 0.5rem',
                        borderRadius: '4px',
                        backgroundColor: getStatusColor(booking.status),
                        color: 'white',
                        fontSize: '0.875rem',
                      }}
                    >
                      {booking.status}
                    </span>
                  </td>
                  <td style={{ padding: '1rem' }}>
                    <button
                      onClick={() => setSelectedBooking(booking)}
                      style={{ padding: '0.25rem 0.5rem', cursor: 'pointer' }}
                    >
                      Подробнее
                    </button>
                  </td>
                </tr>
                {expandedBookingId === booking.id && booking.preOrderItemsCount > 0 && (
                  <tr>
                    <td colSpan={7} style={{ padding: '1rem', backgroundColor: '#f9f9f9' }}>
                      <div>
                        <h4>Предзаказанные блюда:</h4>
                        {preOrderItems.length > 0 ? (
                          <table style={{ width: '100%', marginTop: '0.5rem' }}>
                            <thead>
                              <tr>
                                <th>Блюдо</th>
                                <th>Количество</th>
                                <th>Цена</th>
                                <th>Итого</th>
                              </tr>
                            </thead>
                            <tbody>
                              {preOrderItems.map((item) => (
                                <tr key={item.id}>
                                  <td>{item.menuItemName}</td>
                                  <td>{item.quantity}</td>
                                  <td>{item.price.toFixed(2)} ₸</td>
                                  <td>{(item.quantity * item.price).toFixed(2)} ₸</td>
                                </tr>
                              ))}
                            </tbody>
                            <tfoot>
                              <tr>
                                <td colSpan={3} style={{ fontWeight: 'bold' }}>
                                  Итого:
                                </td>
                                <td style={{ fontWeight: 'bold' }}>
                                  {preOrderItems.reduce((sum, item) => sum + item.quantity * item.price, 0).toFixed(2)} ₸
                                </td>
                              </tr>
                            </tfoot>
                          </table>
                        ) : (
                          <div>Загрузка...</div>
                        )}
                      </div>
                    </td>
                  </tr>
                )}
              </>
            ))}
          </tbody>
        </table>
      </div>

      {selectedBooking && (
        <Modal
          isOpen={!!selectedBooking}
          onClose={() => setSelectedBooking(null)}
          title="Детальная информация о бронировании"
        >
          <div>
            <h3>Клиент</h3>
            <p>Имя: {selectedBooking.clientName || '-'}</p>
            <p>Телефон: {selectedBooking.clientPhone}</p>

            <h3>Бронирование</h3>
            <p>Дата и время: {new Date(selectedBooking.bookingDate).toLocaleString('ru-RU')}</p>
            <p>Стол: #{selectedBooking.tableId}</p>
            <p>Количество персон: {selectedBooking.numberOfPersons}</p>
            <p>Статус: {selectedBooking.status}</p>
            {selectedBooking.specialRequests && <p>Особые пожелания: {selectedBooking.specialRequests}</p>}

            {selectedBooking.preOrderItemsCount > 0 && (
              <div>
                <h3>Предзаказ блюд</h3>
                <p>Количество позиций: {selectedBooking.preOrderItemsCount}</p>
              </div>
            )}
          </div>
        </Modal>
      )}
    </div>
  )
}

