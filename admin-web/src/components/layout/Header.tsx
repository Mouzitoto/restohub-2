import RestaurantSelector from './RestaurantSelector'
import UserMenu from './UserMenu'
import { useApp } from '../../context/AppContext'

export default function Header() {
  const { role } = useApp()

  return (
    <header
      style={{
        backgroundColor: 'white',
        padding: '1rem 2rem',
        borderBottom: '1px solid #e0e0e0',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
      }}
    >
      <div>
        <h1 style={{ margin: 0, fontSize: '1.5rem' }}>Панель управления</h1>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        {role === 'MANAGER' && <RestaurantSelector />}
        <UserMenu />
      </div>
    </header>
  )
}

