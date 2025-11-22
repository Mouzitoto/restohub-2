import { useEffect, useState } from 'react'
import { useApp } from '../../context/AppContext'
import { apiClient } from '../../services/apiClient'
import StatCard from '../../components/StatCard'

export default function AdminDashboardPage() {
  const { role } = useApp()
  const [stats, setStats] = useState({
    restaurants: 0,
    activeSubscriptions: 0,
    users: 0,
    newRestaurants: 0,
  })
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    if (role === 'ADMIN') {
      loadStats()
    }
  }, [role])

  const loadStats = async () => {
    setIsLoading(true)
    try {
      const [restaurantsRes, subscriptionsRes, usersRes] = await Promise.all([
        apiClient.instance.get('/admin-api/r'),
        apiClient.instance.get('/admin-api/subscription'),
        apiClient.instance.get('/admin-api/user'),
      ])

      setStats({
        restaurants: restaurantsRes.data?.total || 0,
        activeSubscriptions: subscriptionsRes.data?.filter((s: any) => s.isActive).length || 0,
        users: usersRes.data?.total || 0,
        newRestaurants: 0, // TODO: calculate from date
      })
    } catch (error) {
      console.error('Failed to load stats:', error)
    } finally {
      setIsLoading(false)
    }
  }

  if (role !== 'ADMIN') {
    return <div>Доступ запрещен</div>
  }

  if (isLoading) {
    return <div>Загрузка...</div>
  }

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Панель администратора</h1>

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
          gap: '1.5rem',
        }}
      >
        <StatCard title="Ресторанов" value={stats.restaurants} icon="🏢" />
        <StatCard title="Активных подписок" value={stats.activeSubscriptions} icon="💳" />
        <StatCard title="Пользователей" value={stats.users} icon="👤" />
        <StatCard title="Новых ресторанов" value={stats.newRestaurants} icon="🆕" />
      </div>
    </div>
  )
}

