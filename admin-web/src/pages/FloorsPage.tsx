import { useEffect, useState } from 'react'
import { useApp } from '../context/AppContext'
import { apiClient } from '../services/apiClient'
import Modal from '../components/common/Modal'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useToast } from '../context/ToastContext'
import type { Floor } from '../types'

const floorSchema = z.object({
  floorNumber: z.string().min(1, 'Номер этажа обязателен').max(50),
})

type FloorFormData = z.infer<typeof floorSchema>

export default function FloorsPage() {
  const { currentRestaurant } = useApp()
  const [floors, setFloors] = useState<Floor[]>([])
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingFloor, setEditingFloor] = useState<Floor | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const toast = useToast()

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<FloorFormData>({
    resolver: zodResolver(floorSchema),
  })

  useEffect(() => {
    if (currentRestaurant) {
      loadFloors()
    }
  }, [currentRestaurant])

  const loadFloors = async () => {
    if (!currentRestaurant) return

    try {
      const response = await apiClient.instance.get<Floor[]>(
        `/admin-api/r/${currentRestaurant.id}/floor?sortBy=floorNumber&sortOrder=asc`
      )
      setFloors(response.data)
    } catch (error) {
      toast.error('Не удалось загрузить этажи')
    }
  }

  const onSubmit = async (data: FloorFormData) => {
    if (!currentRestaurant) return

    setIsLoading(true)
    try {
      if (editingFloor) {
        await apiClient.instance.put(`/admin-api/r/${currentRestaurant.id}/floor/${editingFloor.id}`, data)
        toast.success('Этаж обновлен')
      } else {
        await apiClient.instance.post(`/admin-api/r/${currentRestaurant.id}/floor`, data)
        toast.success('Этаж создан')
      }
      setIsModalOpen(false)
      reset()
      setEditingFloor(null)
      loadFloors()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка сохранения')
    } finally {
      setIsLoading(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!currentRestaurant) return
    if (!confirm('Вы уверены, что хотите удалить этот этаж?')) return

    try {
      await apiClient.instance.delete(`/admin-api/r/${currentRestaurant.id}/floor/${id}`)
      toast.success('Этаж удален')
      loadFloors()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Нельзя удалить этаж: на этаже есть активные залы')
    }
  }

  if (!currentRestaurant) {
    return <div>Ресторан не выбран</div>
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>Этажи</h1>
        <button
          onClick={() => {
            setEditingFloor(null)
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
          Добавить этаж
        </button>
      </div>

      <div style={{ backgroundColor: 'white', borderRadius: '8px', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: '#f5f5f5' }}>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Номер этажа</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Количество залов</th>
              <th style={{ padding: '1rem' }}>Действия</th>
            </tr>
          </thead>
          <tbody>
            {floors.map((floor) => (
              <tr key={floor.id} style={{ borderTop: '1px solid #eee' }}>
                <td style={{ padding: '1rem' }}>{floor.floorNumber}</td>
                <td style={{ padding: '1rem' }}>{floor.roomsCount || 0}</td>
                <td style={{ padding: '1rem' }}>
                  <button
                    onClick={() => {
                      setEditingFloor(floor)
                      reset({ floorNumber: floor.floorNumber })
                      setIsModalOpen(true)
                    }}
                    style={{ marginRight: '0.5rem', padding: '0.25rem 0.5rem', cursor: 'pointer' }}
                  >
                    Редактировать
                  </button>
                  <button
                    onClick={() => handleDelete(floor.id)}
                    disabled={!!(floor.roomsCount && floor.roomsCount > 0)}
                    style={{
                      padding: '0.25rem 0.5rem',
                      cursor: floor.roomsCount && floor.roomsCount > 0 ? 'not-allowed' : 'pointer',
                      color: floor.roomsCount && floor.roomsCount > 0 ? '#999' : '#f44336',
                    }}
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
          setEditingFloor(null)
        }}
        title={editingFloor ? 'Редактировать этаж' : 'Создать этаж'}
      >
        <form onSubmit={handleSubmit(onSubmit)}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Номер этажа *</label>
            <input {...register('floorNumber')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {errors.floorNumber && <div style={{ color: 'red' }}>{errors.floorNumber.message}</div>}
          </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
            <button
              type="button"
              onClick={() => {
                setIsModalOpen(false)
                reset()
                setEditingFloor(null)
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

