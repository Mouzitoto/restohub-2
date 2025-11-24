import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

/**
 * Компонент для обработки событий редиректа при ошибках авторизации.
 * Использует React Router навигацию вместо window.location.href,
 * что предотвращает полную перезагрузку страницы.
 */
export default function AuthRedirectHandler() {
  const navigate = useNavigate()

  useEffect(() => {
    const handleAuthLogout = (event: CustomEvent<{ redirectTo: string }>) => {
      const redirectTo = event.detail?.redirectTo || '/login'
      navigate(redirectTo, { replace: true })
    }

    window.addEventListener('auth:logout', handleAuthLogout as EventListener)

    return () => {
      window.removeEventListener('auth:logout', handleAuthLogout as EventListener)
    }
  }, [navigate])

  return null
}

