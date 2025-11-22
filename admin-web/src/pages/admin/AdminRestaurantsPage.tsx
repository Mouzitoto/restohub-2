import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useApp } from '../../context/AppContext'
import { apiClient } from '../../services/apiClient'
import Modal from '../../components/common/Modal'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useToast } from '../../context/ToastContext'
import type { Restaurant, PaginationResponse } from '../../types'

const restaurantSchema = z.object({
  name: z.string().min(1, 'Название обязательно').max(255),
  address: z.string().max(500).optional(),
})

type RestaurantFormData = z.infer<typeof restaurantSchema>

export default function AdminRestaurantsPage() {
  const { role } = useApp()
  const navigate = useNavigate()
  const [restaurants, setRestaurants] = useState<Restaurant[]>([])
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingRestaurant, setEditingRestaurant] = useState<Restaurant | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const toast = useToast()

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<RestaurantFormData>({
    resolver: zodResolver(restaurantSchema),
  })

  useEffect(() => {
    if (role !== 'ADMIN') {
      navigate('/dashboard')
      return
    }
    loadRestaurants()
  }, [role, navigate])

  const loadRestaurants = async () => {
    try {
      const response = await apiClient.instance.get<PaginationResponse<Restaurant[]>>(
        '/admin-api/r'
      )
      setRestaurants(response.data.data)
    } catch (error) {
      toast.error('Не удалось загрузить рестораны')
    }
  }

  const onSubmit = async (data: RestaurantFormData) => {
    setIsLoading(true)
    try {
      if (editingRestaurant) {
        await apiClient.instance.put(`/admin-api/r/${editingRestaurant.id}`, data)
        toast.success('Ресторан обновлен')
      } else {
        await apiClient.instance.post('/admin-api/r', data)
        toast.success('Ресторан создан')
      }
      setIsModalOpen(false)
      reset()
      setEditingRestaurant(null)
      loadRestaurants()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка сохранения')
    } finally {
      setIsLoading(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Вы уверены, что хотите удалить этот ресторан?')) return

    try {
      await apiClient.instance.delete(`/admin-api/r/${id}`)
      toast.success('Ресторан удален')
      loadRestaurants()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка удаления')
    }
  }

  if (role !== 'ADMIN') {
    return null
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>Управление ресторанами</h1>
        <button
          onClick={() => {
            setEditingRestaurant(null)
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
          }}
        >
          Создать ресторан
        </button>
      </div>

      <div style={{ backgroundColor: 'white', borderRadius: '8px', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: '#f5f5f5' }}>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Название</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Адрес</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Статус</th>
              <th style={{ padding: '1rem' }}>Действия</th>
            </tr>
          </thead>
          <tbody>
            {restaurants.map((restaurant) => (
              <tr key={restaurant.id} style={{ borderTop: '1px solid #eee' }}>
                <td style={{ padding: '1rem' }}>{restaurant.name}</td>
                <td style={{ padding: '1rem' }}>{restaurant.address || '-'}</td>
                <td style={{ padding: '1rem' }}>
                  <span
                    style={{
                      padding: '0.25rem 0.5rem',
                      borderRadius: '4px',
                      backgroundColor: restaurant.isActive ? '#4caf50' : '#999',
                      color: 'white',
                      fontSize: '0.875rem',
                    }}
                  >
                    {restaurant.isActive ? 'Активен' : 'Неактивен'}
                  </span>
                </td>
                <td style={{ padding: '1rem' }}>
                  <button
                    onClick={() => {
                      setEditingRestaurant(restaurant)
                      reset({
                        name: restaurant.name,
                        address: restaurant.address || '',
                      })
                      setIsModalOpen(true)
                    }}
                    style={{ marginRight: '0.5rem', padding: '0.25rem 0.5rem', cursor: 'pointer' }}
                  >
                    Редактировать
                  </button>
                  <button
                    onClick={() => handleDelete(restaurant.id)}
                    style={{ padding: '0.25rem 0.5rem', cursor: 'pointer', color: '#f44336' }}
                  >
                    Удалить
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false)
          reset()
          setEditingRestaurant(null)
        }}
        title={editingRestaurant ? 'Редактировать ресторан' : 'Создать ресторан'}
      >
        <form onSubmit={handleSubmit(onSubmit)}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Название *</label>
            <input {...register('name')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {errors.name && <div style={{ color: 'red' }}>{errors.name.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Адрес</label>
            <input {...register('address')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
          </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
            <button
              type="button"
              onClick={() => {
                setIsModalOpen(false)
                reset()
                setEditingRestaurant(null)
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
    </div>
  )
}

