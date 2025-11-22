import { useEffect, useState } from 'react'

export type ToastType = 'success' | 'error' | 'info' | 'warning'

interface ToastProps {
  message: string
  type: ToastType
  duration?: number
  onClose: () => void
}

export default function Toast({ message, type, duration = 5000, onClose }: ToastProps) {
  const [isVisible, setIsVisible] = useState(true)

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsVisible(false)
      setTimeout(onClose, 300) // Ждем завершения анимации
    }, duration)

    return () => clearTimeout(timer)
  }, [duration, onClose])

  const typeStyles = {
    success: { backgroundColor: '#4caf50', color: 'white' },
    error: { backgroundColor: '#f44336', color: 'white' },
    info: { backgroundColor: '#2196f3', color: 'white' },
    warning: { backgroundColor: '#ff9800', color: 'white' },
  }

  return (
    <div
      style={{
        ...typeStyles[type],
        padding: '1rem 1.5rem',
        borderRadius: '4px',
        marginBottom: '0.5rem',
        boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
        opacity: isVisible ? 1 : 0,
        transition: 'opacity 0.3s',
        minWidth: '300px',
        maxWidth: '500px',
      }}
    >
      {message}
    </div>
  )
}

interface ToastContainerProps {
  toasts: Array<{ id: string; message: string; type: ToastType }>
  onRemove: (id: string) => void
}

export function ToastContainer({ toasts, onRemove }: ToastContainerProps) {
  return (
    <div
      style={{
        position: 'fixed',
        top: '20px',
        right: '20px',
        zIndex: 10000,
      }}
    >
      {toasts.map((toast) => (
        <Toast
          key={toast.id}
          message={toast.message}
          type={toast.type}
          onClose={() => onRemove(toast.id)}
        />
      ))}
    </div>
  )
}


