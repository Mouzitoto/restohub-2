import { useEffect, useState } from 'react'
import { useApp } from '../context/AppContext'
import { apiClient } from '../services/apiClient'
import Modal from '../components/common/Modal'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useToast } from '../context/ToastContext'
import type { Subscription } from '../types'

const subscriptionSchema = z.object({
  subscriptionTypeId: z.number().optional(),
  startDate: z.string().optional(),
  endDate: z.string().min(1, 'Дата окончания обязательна'),
  isActive: z.boolean().optional(),
  description: z.string().max(10000).optional(),
})

type SubscriptionFormData = z.infer<typeof subscriptionSchema>

export default function SubscriptionPage() {
  const { currentRestaurant, role } = useApp()
  const [subscription, setSubscription] = useState<Subscription | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const toast = useToast()

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<SubscriptionFormData>({
    resolver: zodResolver(subscriptionSchema),
  })

  useEffect(() => {
    if (currentRestaurant) {
      loadSubscription()
    }
  }, [currentRestaurant])

  const loadSubscription = async () => {
    if (!currentRestaurant) return

    try {
      const response = await apiClient.instance.get<Subscription>(
        `/admin-api/r/${currentRestaurant.id}/subscription`
      )
      setSubscription(response.data)
    } catch (error: any) {
      if (error.response?.status !== 404) {
        toast.error('Не удалось загрузить информацию о подписке')
      }
    }
  }

  const onSubmit = async (data: SubscriptionFormData) => {
    if (!currentRestaurant) return

    setIsLoading(true)
    try {
      await apiClient.instance.put(`/admin-api/r/${currentRestaurant.id}/subscription`, data)
      toast.success('Подписка обновлена')
      setIsModalOpen(false)
      reset()
      loadSubscription()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка сохранения')
    } finally {
      setIsLoading(false)
    }
  }

  if (!currentRestaurant) {
    return <div>Ресторан не выбран</div>
  }

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Подписка</h1>

      <div style={{ backgroundColor: 'white', padding: '2rem', borderRadius: '8px', marginBottom: '2rem' }}>
        {subscription ? (
          <div>
            <h2>Информация о подписке</h2>
            <p>Статус: {subscription.isActive ? 'Активна' : 'Неактивна'}</p>
            <p>Дата окончания: {new Date(subscription.endDate).toLocaleDateString('ru-RU')}</p>
            <p>Дней осталось: {subscription.daysRemaining}</p>
            {subscription.isExpiringSoon && (
              <div style={{ color: '#ff9800', marginTop: '1rem' }}>
                ⚠️ Подписка истекает скоро
              </div>
            )}
          </div>
        ) : (
          <div>
            <p>Подписка не активирована</p>
            {role === 'ADMIN' && (
              <button
                onClick={() => {
                  reset()
                  setIsModalOpen(true)
                }}
                style={{
                  padding: '0.75rem 1.5rem',
                  backgroundColor: '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  marginTop: '1rem',
                }}
              >
                Создать подписку
              </button>
            )}
          </div>
        )}

        {role === 'ADMIN' && subscription && (
          <button
            onClick={() => {
              reset({
                endDate: subscription.endDate,
                isActive: subscription.isActive,
              })
              setIsModalOpen(true)
            }}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              marginTop: '1rem',
            }}
          >
            Редактировать подписку
          </button>
        )}
      </div>

      {role === 'ADMIN' && (
        <Modal
          isOpen={isModalOpen}
          onClose={() => {
            setIsModalOpen(false)
            reset()
          }}
          title={subscription ? 'Редактировать подписку' : 'Создать подписку'}
        >
          <form onSubmit={handleSubmit(onSubmit)}>
            <div style={{ marginBottom: '1rem' }}>
              <label>Дата окончания *</label>
              <input
                type="date"
                {...register('endDate')}
                style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
              />
              {errors.endDate && <div style={{ color: 'red' }}>{errors.endDate.message}</div>}
            </div>

            <div style={{ marginBottom: '1rem' }}>
              <label>
                <input type="checkbox" {...register('isActive')} style={{ marginRight: '0.5rem' }} />
                Активна
              </label>
            </div>

            <div style={{ marginBottom: '1rem' }}>
              <label>Описание</label>
              <textarea {...register('description')} rows={3} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            </div>

            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
              <button
                type="button"
                onClick={() => {
                  setIsModalOpen(false)
                  reset()
                }}
                style={{ padding: '0.5rem 1rem', cursor: 'pointer' }}
              >
                Отмена
              </button>
              <button
                type="submit"
                disabled={isLoading}
                style={{
                  padding: '0.5rem 1rem',
                  backgroundColor: '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: isLoading ? 'not-allowed' : 'pointer',
                }}
              >
                {isLoading ? 'Сохранение...' : 'Сохранить'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}

