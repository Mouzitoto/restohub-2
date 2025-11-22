import { useEffect, useState } from 'react'
import { useApp } from '../context/AppContext'
import { apiClient } from '../services/apiClient'
import Modal from '../components/common/Modal'
import type { Client, Booking, PreOrderItem } from '../types'

export default function ClientsPage() {
  const { currentRestaurant } = useApp()
  const [clients, setClients] = useState<Client[]>([])
  const [selectedClient, setSelectedClient] = useState<Client | null>(null)
  const [clientBookings, setClientBookings] = useState<Booking[]>([])
  const [clientPreOrders, setClientPreOrders] = useState<PreOrderItem[]>([])
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    if (currentRestaurant) {
      loadClients()
    }
  }, [currentRestaurant])

  const loadClients = async () => {
    if (!currentRestaurant) return

    try {
      const response = await apiClient.instance.get<{ data: Client[]; total: number }>(
        `/admin-api/r/${currentRestaurant.id}/client`
      )
      setClients(response.data.data)
    } catch (error) {
      console.error('Failed to load clients:', error)
    }
  }

  const loadClientDetails = async (clientId: number) => {
    if (!currentRestaurant) return

    setIsLoading(true)
    try {
      const [bookingsRes, preOrdersRes] = await Promise.all([
        apiClient.instance.get<Booking[]>(
          `/admin-api/r/${currentRestaurant.id}/client/${clientId}/booking`
        ),
        apiClient.instance.get<PreOrderItem[]>(
          `/admin-api/r/${currentRestaurant.id}/client/${clientId}/pre-order`
        ),
      ])

      setClientBookings(bookingsRes.data)
      setClientPreOrders(preOrdersRes.data)
    } catch (error) {
      console.error('Failed to load client details:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const handleClientClick = async (client: Client) => {
    setSelectedClient(client)
    await loadClientDetails(client.id)
  }

  if (!currentRestaurant) {
    return <div>Ресторан не выбран</div>
  }

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Клиенты</h1>

      <div style={{ backgroundColor: 'white', borderRadius: '8px', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: '#f5f5f5' }}>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Телефон</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Имя</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Бронирований</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Предзаказов</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Последний визит</th>
              <th style={{ padding: '1rem' }}>Действия</th>
            </tr>
          </thead>
          <tbody>
            {clients.map((client) => (
              <tr key={client.id} style={{ borderTop: '1px solid #eee' }}>
                <td style={{ padding: '1rem' }}>{client.phone}</td>
                <td style={{ padding: '1rem' }}>{client.name || '-'}</td>
                <td style={{ padding: '1rem' }}>{client.bookingsCount}</td>
                <td style={{ padding: '1rem' }}>{client.preOrdersCount}</td>
                <td style={{ padding: '1rem' }}>
                  {client.lastVisitDate
                    ? new Date(client.lastVisitDate).toLocaleDateString('ru-RU')
                    : '-'}
                </td>
                <td style={{ padding: '1rem' }}>
                  <button
                    onClick={() => handleClientClick(client)}
                    style={{ padding: '0.25rem 0.5rem', cursor: 'pointer' }}
                  >
                    Подробнее
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selectedClient && (
        <Modal
          isOpen={!!selectedClient}
          onClose={() => {
            setSelectedClient(null)
            setClientBookings([])
            setClientPreOrders([])
          }}
          title="Детальная информация о клиенте"
          size="large"
        >
          {isLoading ? (
            <div>Загрузка...</div>
          ) : (
            <div>
              <h3>Базовая информация</h3>
              <p>Телефон: {selectedClient.phone}</p>
              <p>Имя: {selectedClient.name || '-'}</p>
              <p>Бронирований: {selectedClient.bookingsCount}</p>
              <p>Предзаказов: {selectedClient.preOrdersCount}</p>

              <h3 style={{ marginTop: '2rem' }}>История бронирований</h3>
              <table style={{ width: '100%', marginTop: '1rem' }}>
                <thead>
                  <tr>
                    <th>Дата</th>
                    <th>Стол</th>
                    <th>Статус</th>
                  </tr>
                </thead>
                <tbody>
                  {clientBookings.map((booking) => (
                    <tr key={booking.id}>
                      <td>{new Date(booking.bookingDate).toLocaleString('ru-RU')}</td>
                      <td>#{booking.tableId}</td>
                      <td>{booking.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>

              <h3 style={{ marginTop: '2rem' }}>История предзаказов</h3>
              <table style={{ width: '100%', marginTop: '1rem' }}>
                <thead>
                  <tr>
                    <th>Блюдо</th>
                    <th>Количество</th>
                    <th>Цена</th>
                  </tr>
                </thead>
                <tbody>
                  {clientPreOrders.map((item) => (
                    <tr key={item.id}>
                      <td>{item.menuItemName}</td>
                      <td>{item.quantity}</td>
                      <td>{item.price.toFixed(2)} ₽</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Modal>
      )}
    </div>
  )
}

