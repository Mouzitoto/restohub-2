import { useEffect } from 'react'
import type { ReactNode } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import Sidebar from './Sidebar'
import Header from './Header'
import SubscriptionWarningBanner from './SubscriptionWarningBanner'
import { useApp } from '../../context/AppContext'

interface AdminLayoutProps {
  children: ReactNode
}

// Страницы, которые требуют выбранный ресторан
const RESTAURANT_REQUIRED_PATHS = [
  '/dashboard',
  '/restaurant',
  '/menu/categories',
  '/menu/items',
  '/floors',
  '/rooms',
  '/tables',
  '/bookings',
  '/subscription',
  '/promotions',
  '/analytics',
  '/clients',
]

export default function AdminLayout({ children }: AdminLayoutProps) {
  const { role, currentRestaurant, restaurants } = useApp()
  const location = useLocation()
  const navigate = useNavigate()

  useEffect(() => {
    // Для менеджеров: если пытаются попасть на страницу, требующую ресторан,
    // но ресторан не выбран или нет ресторанов, редиректим на страницу "Мои рестораны"
    if (role === 'MANAGER') {
      const isRestaurantRequired = RESTAURANT_REQUIRED_PATHS.some(path => 
        location.pathname === path || location.pathname.startsWith(path + '/')
      )
      const isManagerWithoutRestaurant = !currentRestaurant || restaurants.length === 0

      if (isRestaurantRequired && isManagerWithoutRestaurant) {
        navigate('/admin/restaurants', { replace: true })
      }
    }
  }, [role, currentRestaurant, restaurants, location.pathname, navigate])

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <Header />
        <SubscriptionWarningBanner />
        <main style={{ flex: 1, padding: '2rem', backgroundColor: '#f5f5f5' }}>
          {children}
        </main>
      </div>
    </div>
  )
}

