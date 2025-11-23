import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useApp } from '../context/AppContext'
import { apiClient } from '../services/apiClient'
import StatCard from '../components/StatCard'
import type { AnalyticsOverview } from '../types'
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts'

export default function DashboardPage() {
  const { currentRestaurant } = useApp()
  const navigate = useNavigate()
  const [overview, setOverview] = useState<AnalyticsOverview | null>(null)
  const [bookingsData, setBookingsData] = useState<any[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    if (!currentRestaurant) return

    const loadData = async () => {
      setIsLoading(true)
      try {
        const [overviewRes, bookingsRes] = await Promise.all([
          apiClient.instance.get<any>(
            `/admin-api/r/${currentRestaurant.id}/analytics/overview`
          ),
          apiClient.instance.get<any>(
            `/admin-api/r/${currentRestaurant.id}/analytics/booking?groupBy=day&dateFrom=${new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]}&dateTo=${new Date().toISOString().split('T')[0]}`
          ),
        ])

        // Преобразуем структуру ответа API в формат, ожидаемый компонентом
        const overviewData = overviewRes.data
        const bookingsResponse = bookingsRes.data
        
        // Преобразуем overview в нужный формат
        setOverview({
          bookings: overviewData?.bookings?.total || 0,
          preOrders: overviewData?.preOrders?.total || 0,
          revenue: overviewData?.preOrders?.totalRevenue ? Number(overviewData.preOrders.totalRevenue) : 0,
          newClients: overviewData?.clients?.newClients || 0,
        })
        
        // Преобразуем chart данные для графика
        const chartData = bookingsResponse?.chart || []
        setBookingsData(chartData.map((item: any) => ({
          date: item.period,
          approved: item.byStatus?.APPROVED || 0,
          pending: item.byStatus?.PENDING || 0,
          rejected: item.byStatus?.REJECTED || 0,
        })))
      } catch (error) {
        console.error('Failed to load dashboard data:', error)
      } finally {
        setIsLoading(false)
      }
    }

    loadData()
  }, [currentRestaurant])

  if (isLoading) {
    return <div>Загрузка...</div>
  }

  if (!currentRestaurant) {
    return <div>Ресторан не выбран</div>
  }

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Панель управления</h1>

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
          gap: '1.5rem',
          marginBottom: '2rem',
        }}
      >
        <StatCard
          title="Бронирования"
          value={overview?.bookings || 0}
          icon="📅"
          onClick={() => navigate('/bookings')}
        />
        <StatCard
          title="Предзаказы"
          value={overview?.preOrders || 0}
          icon="🛒"
          onClick={() => navigate('/bookings')}
        />
        <StatCard
          title="Выручка"
          value={`${(overview?.revenue || 0).toLocaleString('ru-RU')} ₽`}
          icon="💰"
          onClick={() => navigate('/analytics')}
        />
        <StatCard
          title="Новые клиенты"
          value={overview?.newClients || 0}
          icon="👥"
          onClick={() => navigate('/clients')}
        />
      </div>

      <div style={{ backgroundColor: 'white', padding: '1.5rem', borderRadius: '8px', marginBottom: '2rem' }}>
        <h2 style={{ marginBottom: '1rem' }}>График бронирований</h2>
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={bookingsData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="date" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Line type="monotone" dataKey="approved" stroke="#4caf50" name="Подтверждено" />
            <Line type="monotone" dataKey="pending" stroke="#ff9800" name="Ожидает" />
            <Line type="monotone" dataKey="rejected" stroke="#f44336" name="Отклонено" />
          </LineChart>
        </ResponsiveContainer>
      </div>

      <div style={{ backgroundColor: 'white', padding: '1.5rem', borderRadius: '8px' }}>
        <h2 style={{ marginBottom: '1rem' }}>Быстрые действия</h2>
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <button
            onClick={() => navigate('/menu/items/new')}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
            }}
          >
            Добавить блюдо
          </button>
          <button
            onClick={() => navigate('/promotions/new')}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: '#28a745',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
            }}
          >
            Создать акцию
          </button>
          <button
            onClick={() => navigate('/tables')}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: '#17a2b8',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
            }}
          >
            Управление столами
          </button>
          <button
            onClick={() => navigate('/analytics')}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
            }}
          >
            Просмотр статистики
          </button>
        </div>
      </div>
    </div>
  )
}

