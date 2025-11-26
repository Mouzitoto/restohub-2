import { useEffect, useState } from 'react'
import { useApp } from '../context/AppContext'
import { apiClient } from '../services/apiClient'
import Modal from '../components/common/Modal'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useToast } from '../context/ToastContext'
import type { Subscription, SubscriptionType } from '../types'

const subscriptionSchema = z.object({
  subscriptionTypeId: z.number().optional(),
  startDate: z.string().optional(),
  endDate: z.string().min(1, 'Дата окончания обязательна'),
  isActive: z.boolean().optional(),
  description: z.string().max(10000).optional(),
})

const createSubscriptionSchema = z.object({
  subscriptionTypeId: z.number().min(1, 'Выберите тип подписки'),
})

type SubscriptionFormData = z.infer<typeof subscriptionSchema>
type CreateSubscriptionFormData = z.infer<typeof createSubscriptionSchema>

const SUBSCRIPTION_TYPES: SubscriptionType[] = [
  { id: 1, code: 'STANDARD', name: 'Стандарт', description: 'Базовый тариф с основными функциями', price: 10000 },
  { id: 2, code: 'PRO', name: 'Про', description: 'Расширенный тариф с дополнительными возможностями', price: 20000 },
  { id: 3, code: 'PREMIUM', name: 'Премиум', description: 'Премиальный тариф с полным доступом ко всем функциям', price: 30000 },
]

const getStatusLabel = (status?: string) => {
  switch (status) {
    case 'DRAFT':
      return 'Не оплачено'
    case 'PENDING':
      return 'Ожидает оплаты'
    case 'ACTIVATED':
      return 'Активирована'
    case 'EXPIRED':
      return 'Просрочено'
    case 'CANCELLED':
      return 'Отменена'
    default:
      return 'Неизвестно'
  }
}

const getStatusColor = (status?: string) => {
  switch (status) {
    case 'DRAFT':
    case 'PENDING':
      return '#ff9800'
    case 'ACTIVATED':
      return '#4caf50'
    case 'EXPIRED':
    case 'CANCELLED':
      return '#f44336'
    default:
      return '#757575'
  }
}

interface SubscriptionListItem {
  id: number
  status?: string
  paymentReference?: string
  subscriptionType?: {
    id: number
    code: string
    name: string
  }
  startDate?: string
  endDate?: string
  isActive: boolean
  daysRemaining?: number
  isExpiringSoon?: boolean
}

export default function SubscriptionPage() {
  const { currentRestaurant, role } = useApp()
  const [subscription, setSubscription] = useState<Subscription | null>(null)
  const [subscriptions, setSubscriptions] = useState<SubscriptionListItem[]>([])
  const [isLoadingSubscriptions, setIsLoadingSubscriptions] = useState(false)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [isCreating, setIsCreating] = useState(false)
  const toast = useToast()

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<SubscriptionFormData>({
    resolver: zodResolver(subscriptionSchema),
  })

  const {
    register: registerCreate,
    handleSubmit: handleSubmitCreate,
    formState: { errors: errorsCreate },
    reset: resetCreate,
  } = useForm<CreateSubscriptionFormData>({
    resolver: zodResolver(createSubscriptionSchema),
  })

  useEffect(() => {
    if (currentRestaurant) {
      loadSubscription()
      loadSubscriptions()
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

  const loadSubscriptions = async () => {
    if (!currentRestaurant) return

    setIsLoadingSubscriptions(true)
    try {
      const response = await apiClient.instance.get<SubscriptionListItem[]>(
        `/admin-api/r/${currentRestaurant.id}/subscriptions`
      )
      setSubscriptions(response.data)
    } catch (error: any) {
      toast.error('Не удалось загрузить список подписок')
    } finally {
      setIsLoadingSubscriptions(false)
    }
  }

  const onCreateSubscription = async (data: CreateSubscriptionFormData) => {
    if (!currentRestaurant) return

    setIsCreating(true)
    try {
      const response = await apiClient.instance.post<Subscription>(
        `/admin-api/r/${currentRestaurant.id}/subscription`,
        { subscriptionTypeId: data.subscriptionTypeId }
      )
      toast.success('Подписка создана')
      setIsCreateModalOpen(false)
      resetCreate()
      setSubscription(response.data)
      loadSubscriptions()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка создания подписки')
    } finally {
      setIsCreating(false)
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

  const downloadInvoiceForSubscription = async (subscriptionId: number, type: 'invoice' | 'paid-invoice', paymentRef?: string) => {
    if (!currentRestaurant) return

    try {
      const endpoint = type === 'invoice' ? 'invoice' : 'paid-invoice'
      const response = await apiClient.instance.get(
        `/admin-api/r/${currentRestaurant.id}/subscriptions/${subscriptionId}/${endpoint}`,
        { responseType: 'blob' }
      )

      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `${type === 'invoice' ? 'invoice' : 'paid-invoice'}-${paymentRef || subscriptionId}.pdf`)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
    } catch (error: any) {
      toast.error('Не удалось скачать счет')
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>Подписки</h1>
        {(role === 'ADMIN' || role === 'MANAGER') && (
          <button
            onClick={() => {
              resetCreate()
              setIsCreateModalOpen(true)
            }}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '1rem',
            }}
          >
            Купить подписку
          </button>
        )}
      </div>

      <div style={{ backgroundColor: 'white', padding: '2rem', borderRadius: '8px', marginBottom: '2rem' }}>
        <h2 style={{ marginBottom: '1rem' }}>Список подписок</h2>
        {isLoadingSubscriptions ? (
          <div>Загрузка...</div>
        ) : subscriptions.length === 0 ? (
          <div>Подписки не найдены</div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #ddd', backgroundColor: '#f5f5f5' }}>
                  <th style={{ padding: '0.75rem', textAlign: 'left' }}>Статус</th>
                  <th style={{ padding: '0.75rem', textAlign: 'left' }}>Тип подписки</th>
                  <th style={{ padding: '0.75rem', textAlign: 'left' }}>Номер (payment_ref)</th>
                  <th style={{ padding: '0.75rem', textAlign: 'left' }}>Дата начала</th>
                  <th style={{ padding: '0.75rem', textAlign: 'left' }}>Дата окончания</th>
                  <th style={{ padding: '0.75rem', textAlign: 'left' }}>Активность</th>
                  <th style={{ padding: '0.75rem', textAlign: 'left' }}>Действия</th>
                </tr>
              </thead>
              <tbody>
                {subscriptions.map((sub) => (
                  <tr key={sub.id} style={{ borderBottom: '1px solid #ddd' }}>
                    <td style={{ padding: '0.75rem' }}>
                      <span style={{ 
                        color: getStatusColor(sub.status), 
                        fontWeight: 'bold',
                        padding: '0.25rem 0.5rem',
                        borderRadius: '4px',
                        backgroundColor: getStatusColor(sub.status) + '20'
                      }}>
                        {getStatusLabel(sub.status)}
                      </span>
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      {sub.subscriptionType?.name || '-'}
                    </td>
                    <td style={{ padding: '0.75rem', fontFamily: 'monospace' }}>
                      {sub.paymentReference || '-'}
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      {sub.startDate ? new Date(sub.startDate).toLocaleDateString('ru-RU') : '-'}
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      {sub.endDate ? new Date(sub.endDate).toLocaleDateString('ru-RU') : '-'}
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      <span style={{ 
                        color: sub.isActive ? '#4caf50' : '#757575',
                        fontWeight: 'bold'
                      }}>
                        {sub.isActive ? 'Активна' : 'Неактивна'}
                      </span>
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                        {sub.status === 'DRAFT' && (
                          <button
                            onClick={() => downloadInvoiceForSubscription(sub.id, 'invoice', sub.paymentReference)}
                            style={{
                              padding: '0.25rem 0.5rem',
                              backgroundColor: '#28a745',
                              color: 'white',
                              border: 'none',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '0.875rem',
                            }}
                          >
                            Счет
                          </button>
                        )}
                        {sub.status === 'ACTIVATED' && (
                          <button
                            onClick={() => downloadInvoiceForSubscription(sub.id, 'paid-invoice', sub.paymentReference)}
                            style={{
                              padding: '0.25rem 0.5rem',
                              backgroundColor: '#28a745',
                              color: 'white',
                              border: 'none',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '0.875rem',
                            }}
                          >
                            Оплаченный счет
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {(role === 'ADMIN' || role === 'MANAGER') && (
        <Modal
          isOpen={isCreateModalOpen}
          onClose={() => {
            setIsCreateModalOpen(false)
            resetCreate()
          }}
          title="Купить подписку"
        >
          <form onSubmit={handleSubmitCreate(onCreateSubscription)}>
            <div style={{ marginBottom: '1rem' }}>
              <label>Тип подписки *</label>
              <select
                {...registerCreate('subscriptionTypeId', { valueAsNumber: true })}
                style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
              >
                <option value="">Выберите тип подписки</option>
                {SUBSCRIPTION_TYPES.map((type) => (
                  <option key={type.id} value={type.id}>
                    {type.name} - {type.price.toLocaleString('ru-RU')} ₸/месяц
                  </option>
                ))}
              </select>
              {errorsCreate.subscriptionTypeId && (
                <div style={{ color: 'red' }}>{errorsCreate.subscriptionTypeId.message}</div>
              )}
            </div>

            <div style={{ marginBottom: '1rem', padding: '1rem', backgroundColor: '#e7f3ff', borderRadius: '4px' }}>
              <strong>Информация:</strong>
              <ul style={{ marginTop: '0.5rem', paddingLeft: '1.5rem' }}>
                <li>После создания подписки вы получите номер для оплаты</li>
                <li>Укажите этот номер в комментарии к платежу</li>
                <li>Подписка будет активирована после обработки платежа</li>
              </ul>
            </div>

            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
              <button
                type="button"
                onClick={() => {
                  setIsCreateModalOpen(false)
                  resetCreate()
                }}
                style={{ padding: '0.5rem 1rem', cursor: 'pointer' }}
              >
                Отмена
              </button>
              <button
                type="submit"
                disabled={isCreating}
                style={{
                  padding: '0.5rem 1rem',
                  backgroundColor: '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: isCreating ? 'not-allowed' : 'pointer',
                }}
              >
                {isCreating ? 'Создание...' : 'Создать подписку'}
              </button>
            </div>
          </form>
        </Modal>
      )}

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
