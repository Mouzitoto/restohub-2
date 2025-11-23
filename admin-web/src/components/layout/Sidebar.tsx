import { NavLink } from 'react-router-dom'
import { useApp } from '../../context/AppContext'

export default function Sidebar() {
  const { role } = useApp()

  const menuItems = [
    { path: '/dashboard', label: 'Dashboard', icon: '📊' },
    { path: '/restaurant', label: 'Мой ресторан', icon: '🏢' },
    { path: '/menu/categories', label: 'Категории меню', icon: '📁' },
    { path: '/menu/items', label: 'Меню', icon: '🍽️' },
    { path: '/floors', label: 'Этажи', icon: '🏗️' },
    { path: '/rooms', label: 'Залы', icon: '🏛️' },
    { path: '/tables', label: 'Столы', icon: '🪑' },
    { path: '/bookings', label: 'Бронирования', icon: '📅' },
    { path: '/subscription', label: 'Подписка', icon: '💳' },
    { path: '/promotions', label: 'Акции', icon: '🎉' },
    { path: '/analytics', label: 'Аналитика', icon: '📈' },
    { path: '/clients', label: 'Клиенты', icon: '👥' },
  ]

  const adminMenuItems = [
    { path: '/admin/restaurants', label: 'Управление ресторанами', icon: '🏢' },
    { path: '/admin/users', label: 'Управление пользователями', icon: '👤' },
    { path: '/admin/settings', label: 'Системные настройки', icon: '⚙️' },
  ]

  return (
    <aside
      style={{
        width: '250px',
        backgroundColor: '#2c3e50',
        color: 'white',
        padding: '1.5rem 0',
        minHeight: '100vh',
      }}
    >
      <div style={{ padding: '0 1.5rem', marginBottom: '2rem' }}>
        <h2 style={{ margin: 0, fontSize: '1.5rem' }}>RestoHub</h2>
      </div>

      <nav>
        {menuItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            style={({ isActive }) => ({
              display: 'block',
              padding: '0.75rem 1.5rem',
              color: 'white',
              textDecoration: 'none',
              backgroundColor: isActive ? '#34495e' : 'transparent',
              borderLeft: isActive ? '3px solid #3498db' : '3px solid transparent',
            })}
          >
            <span style={{ marginRight: '0.5rem' }}>{item.icon}</span>
            {item.label}
          </NavLink>
        ))}

        {(role === 'ADMIN' || role === 'MANAGER') && (
          <>
            <div
              style={{
                margin: '1rem 0',
                padding: '0.5rem 1.5rem',
                fontSize: '0.75rem',
                color: '#95a5a6',
                textTransform: 'uppercase',
              }}
            >
              {role === 'ADMIN' ? 'Администрирование' : 'Управление'}
            </div>
            {/* Менеджеры видят только "Управление ресторанами" */}
            {(role === 'ADMIN' ? adminMenuItems : adminMenuItems.filter(item => item.path === '/admin/restaurants')).map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                style={({ isActive }) => ({
                  display: 'block',
                  padding: '0.75rem 1.5rem',
                  color: 'white',
                  textDecoration: 'none',
                  backgroundColor: isActive ? '#34495e' : 'transparent',
                  borderLeft: isActive ? '3px solid #3498db' : '3px solid transparent',
                })}
              >
                <span style={{ marginRight: '0.5rem' }}>{item.icon}</span>
                {role === 'MANAGER' && item.path === '/admin/restaurants' ? 'Мои рестораны' : item.label}
              </NavLink>
            ))}
          </>
        )}
      </nav>
    </aside>
  )
}

