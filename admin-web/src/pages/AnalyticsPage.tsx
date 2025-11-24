import { useEffect, useState } from 'react'
import { useApp } from '../context/AppContext'
import { apiClient } from '../services/apiClient'
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts'

export default function AnalyticsPage() {
  const { currentRestaurant } = useApp()
  const [bookingsData, setBookingsData] = useState<any[]>([])
  const [preOrdersData, setPreOrdersData] = useState<any[]>([])
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    if (currentRestaurant) {
      loadData()
    }
  }, [currentRestaurant])

  const loadData = async () => {
    if (!currentRestaurant) return

    setIsLoading(true)
    try {
      const [bookingsRes, preOrdersRes] = await Promise.all([
        apiClient.instance.get(`/admin-api/r/${currentRestaurant.id}/analytics/booking?groupBy=day`),
        apiClient.instance.get(`/admin-api/r/${currentRestaurant.id}/analytics/pre-order`),
      ])

      // Преобразуем данные для графика бронирований
      const bookingsResponse = bookingsRes.data
      const chartData = bookingsResponse?.chart
      if (Array.isArray(chartData)) {
        setBookingsData(chartData.map((item: any) => ({
          date: item.period,
          approved: item.byStatus?.APPROVED || 0,
          pending: item.byStatus?.PENDING || 0,
          rejected: item.byStatus?.REJECTED || 0,
        })))
      } else {
        setBookingsData([])
      }

      // Преобразуем данные для графика предзаказов
      const preOrdersResponse = preOrdersRes.data
      const preOrdersChartData = preOrdersResponse?.chart
      if (Array.isArray(preOrdersChartData)) {
        setPreOrdersData(preOrdersChartData.map((item: any) => ({
          name: item.period,
          count: item.count || 0,
        })))
      } else {
        // Если chart нет, используем popularItems
        const popularItems = preOrdersResponse?.popularItems
        if (Array.isArray(popularItems)) {
          setPreOrdersData(popularItems.map((item: any) => ({
            name: item.menuItemName,
            count: item.quantity || 0,
          })))
        } else {
          setPreOrdersData([])
        }
      }
    } catch (error) {
      console.error('Failed to load analytics:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const handleExport = async (type: string, format: string) => {
    if (!currentRestaurant) return

    try {
      const response = await apiClient.instance.get(
        `/admin-api/r/${currentRestaurant.id}/analytics/export?type=${type}&format=${format}`,
        { responseType: 'blob' }
      )

      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `analytics-${type}-${new Date().toISOString()}.${format}`)
      document.body.appendChild(link)
      link.click()
      link.remove()
    } catch (error) {
      console.error('Export failed:', error)
    }
  }

  if (!currentRestaurant) {
    return <div>Ресторан не выбран</div>
  }

  if (isLoading) {
    return <div>Загрузка...</div>
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>Аналитика</h1>
        <div>
          <button
            onClick={() => handleExport('booking', 'csv')}
            style={{ marginRight: '0.5rem', padding: '0.5rem 1rem', cursor: 'pointer' }}
          >
            Экспорт CSV
          </button>
          <button
            onClick={() => handleExport('booking', 'xlsx')}
            style={{ padding: '0.5rem 1rem', cursor: 'pointer' }}
          >
            Экспорт Excel
          </button>
        </div>
      </div>

      <div style={{ backgroundColor: 'white', padding: '1.5rem', borderRadius: '8px', marginBottom: '2rem' }}>
        <h2 style={{ marginBottom: '1rem' }}>Статистика бронирований</h2>
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
        <h2 style={{ marginBottom: '1rem' }}>Топ популярных блюд</h2>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={preOrdersData} layout="vertical">
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis type="number" />
            <YAxis dataKey="name" type="category" width={150} />
            <Tooltip />
            <Legend />
            <Bar dataKey="count" fill="#2196f3" name="Количество заказов" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}

