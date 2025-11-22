import { useNavigate } from 'react-router-dom'
import { useApp } from '../../context/AppContext'

export default function SubscriptionWarningBanner() {
  const { role, subscription } = useApp()
  const navigate = useNavigate()

  // Показываем только для MANAGER и если подписка истекает скоро
  if (
    role !== 'MANAGER' ||
    !subscription ||
    !subscription.isActive ||
    !subscription.isExpiringSoon
  ) {
    return null
  }

  const daysText =
    subscription.daysRemaining === 1
      ? 'день'
      : subscription.daysRemaining < 5
      ? 'дня'
      : 'дней'

  return (
    <div
      style={{
        backgroundColor: '#ff9800',
        color: 'white',
        padding: '1rem 2rem',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
      }}
    >
      <div>
        <strong>Внимание!</strong> Подписка истекает через {subscription.daysRemaining}{' '}
        {daysText}. Пожалуйста, продлите подписку.
      </div>
      <button
        onClick={() => navigate('/subscription')}
        style={{
          padding: '0.5rem 1rem',
          backgroundColor: 'white',
          color: '#ff9800',
          border: 'none',
          borderRadius: '4px',
          cursor: 'pointer',
          fontWeight: 'bold',
        }}
      >
        Продлить подписку
      </button>
    </div>
  )
}

