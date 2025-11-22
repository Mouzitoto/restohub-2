import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { authService } from '../services/authService'

interface AuthGuardProps {
  children: ReactNode
}

export default function AuthGuard({ children }: AuthGuardProps) {
  const [isChecking, setIsChecking] = useState(true)
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  useEffect(() => {
    const checkAuth = async () => {
      if (authService.isAuthenticated()) {
        setIsAuthenticated(true)
      } else {
        // Попытка обновить токен
        const refreshToken = localStorage.getItem('admin_refresh_token')
        if (refreshToken) {
          try {
            await authService.refresh(refreshToken)
            setIsAuthenticated(true)
          } catch {
            setIsAuthenticated(false)
          }
        } else {
          setIsAuthenticated(false)
        }
      }
      setIsChecking(false)
    }

    checkAuth()
  }, [])

  if (isChecking) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <div>Загрузка...</div>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
}

