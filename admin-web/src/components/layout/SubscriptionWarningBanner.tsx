import { useNavigate } from 'react-router-dom'
import { useApp } from '../../context/AppContext'
import { apiClient } from '../../services/apiClient'
import { useToast } from '../../context/ToastContext'
import { useState } from 'react'

export default function SubscriptionWarningBanner() {
  const { role, subscription, currentRestaurant, refreshUserInfo } = useApp()
  const navigate = useNavigate()
  const toast = useToast()
  const [isActivating, setIsActivating] = useState(false)

  // Показываем только для MANAGER и ADMIN
  if (role !== 'MANAGER' && role !== 'ADMIN') {
    return null
  }

  if (!currentRestaurant) {
    return null
  }

  const notifications: Array<{
    type: 'warning' | 'error'
    message: string
    actionText: string
    action: () => void
  }> = []

  // 1. Подписка кончается (осталось ≤ 5 дней)
  if (subscription && subscription.isActive && subscription.isExpiringSoon && subscription.daysRemaining <= 5) {
    const daysText =
      subscription.daysRemaining === 1
        ? 'день'
        : subscription.daysRemaining < 5
        ? 'дня'
        : 'дней'

    notifications.push({
      type: 'warning',
      message: `Подписка истекает через ${subscription.daysRemaining} ${daysText}. Продлите подписку, чтобы ресторан оставался видимым для пользователей.`,
      actionText: 'Продлить подписку',
      action: () => navigate('/subscription'),
    })
  }

  // 2. Нет активной подписки
  if (!subscription || !subscription.isActive) {
    notifications.push({
      type: 'error',
      message: 'У ресторана нет активной подписки. Ресторан не виден пользователям. Активируйте подписку, чтобы восстановить доступ.',
      actionText: 'Перейти к подписке',
      action: () => navigate('/subscription'),
    })
  }

  // 3. Ресторан деактивирован
  if (!currentRestaurant.isActive) {
    const handleActivate = async () => {
      if (!currentRestaurant) return

      setIsActivating(true)
      try {
        await apiClient.instance.put(`/admin-api/r/${currentRestaurant.id}/activate`)
        toast.success('Ресторан активирован')
        // Обновляем контекст, чтобы уведомления обновились
        await refreshUserInfo()
      } catch (error: any) {
        const errorMessage = error.response?.data?.exceptionName === 'NO_ACTIVE_SUBSCRIPTION'
          ? 'Нельзя активировать ресторан без активной подписки'
          : error.response?.data?.message || 'Ошибка активации ресторана'
        toast.error(errorMessage)
      } finally {
        setIsActivating(false)
      }
    }

    notifications.push({
      type: 'error',
      message: 'Ресторан деактивирован. Ресторан не виден пользователям. Активируйте ресторан, чтобы восстановить доступ.',
      actionText: 'Активировать ресторан',
      action: handleActivate,
    })
  }

  if (notifications.length === 0) {
    return null
  }

  return (
    <>
      {notifications.map((notification, index) => (
        <div
          key={index}
          style={{
            backgroundColor: notification.type === 'error' ? '#dc3545' : '#ff9800',
            color: 'white',
            padding: '1rem 2rem',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <div>{notification.message}</div>
          <button
            onClick={notification.action}
            disabled={isActivating}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: 'white',
              color: notification.type === 'error' ? '#dc3545' : '#ff9800',
              border: 'none',
              borderRadius: '4px',
              cursor: isActivating ? 'not-allowed' : 'pointer',
              fontWeight: 'bold',
              opacity: isActivating ? 0.6 : 1,
            }}
          >
            {notification.actionText}
          </button>
        </div>
      ))}
    </>
  )
}

