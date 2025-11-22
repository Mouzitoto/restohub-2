import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useApp } from '../../context/AppContext'
import { authService } from '../../services/authService'

export default function UserMenu() {
  const { user, role } = useApp()
  const navigate = useNavigate()
  const [isOpen, setIsOpen] = useState(false)

  const handleLogout = () => {
    authService.logout()
    navigate('/login')
  }

  return (
    <div style={{ position: 'relative' }}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        style={{
          padding: '0.5rem 1rem',
          backgroundColor: '#f5f5f5',
          border: '1px solid #ddd',
          borderRadius: '4px',
          cursor: 'pointer',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem',
        }}
      >
        <span>👤</span>
        {user?.email}
        <span>▼</span>
      </button>

      {isOpen && (
        <>
          <div
            style={{
              position: 'fixed',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              zIndex: 998,
            }}
            onClick={() => setIsOpen(false)}
          />
          <div
            style={{
              position: 'absolute',
              top: '100%',
              right: 0,
              marginTop: '0.5rem',
              backgroundColor: 'white',
              border: '1px solid #ddd',
              borderRadius: '4px',
              boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
              zIndex: 999,
              minWidth: '200px',
            }}
          >
            <div style={{ padding: '0.75rem 1rem', borderBottom: '1px solid #eee' }}>
              <div style={{ fontWeight: 'bold' }}>{user?.email}</div>
              <div style={{ fontSize: '0.875rem', color: '#666' }}>
                {role === 'ADMIN' ? 'Администратор' : 'Менеджер'}
              </div>
            </div>
            <button
              onClick={handleLogout}
              style={{
                display: 'block',
                width: '100%',
                padding: '0.75rem 1rem',
                textAlign: 'left',
                border: 'none',
                backgroundColor: 'white',
                cursor: 'pointer',
                color: '#d32f2f',
              }}
            >
              Выход
            </button>
          </div>
        </>
      )}
    </div>
  )
}

