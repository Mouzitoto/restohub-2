import { useEffect, useState } from 'react'
import { useApp } from '../context/AppContext'
import { apiClient } from '../services/apiClient'
import Modal from '../components/common/Modal'
import ImageUpload from '../components/common/ImageUpload'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useToast } from '../context/ToastContext'
import type { Room, Floor } from '../types'

const roomSchema = z.object({
  name: z.string().min(1, 'Название зала обязательно').max(255),
  floorId: z.number().min(1, 'Выберите этаж'),
  description: z.string().max(10000).optional(),
  isSmoking: z.boolean().optional(),
  isOutdoor: z.boolean().optional(),
})

type RoomFormData = z.infer<typeof roomSchema>

export default function RoomsPage() {
  const { currentRestaurant } = useApp()
  const [rooms, setRooms] = useState<Room[]>([])
  const [floors, setFloors] = useState<Floor[]>([])
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingRoom, setEditingRoom] = useState<Room | null>(null)
  const [imageId, setImageId] = useState<number | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const toast = useToast()

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<RoomFormData>({
    resolver: zodResolver(roomSchema),
    defaultValues: {
      isSmoking: false,
      isOutdoor: false,
    },
  })

  useEffect(() => {
    if (currentRestaurant) {
      loadFloors()
      loadRooms()
    }
  }, [currentRestaurant])

  const loadFloors = async () => {
    if (!currentRestaurant) return

    try {
      const response = await apiClient.instance.get<Floor[]>(
        `/admin-api/r/${currentRestaurant.id}/floor`
      )
      setFloors(response.data)
    } catch (error) {
      toast.error('Не удалось загрузить этажи')
    }
  }

  const loadRooms = async () => {
    if (!currentRestaurant) return

    try {
      const response = await apiClient.instance.get<Room[]>(
        `/admin-api/r/${currentRestaurant.id}/room`
      )
      setRooms(response.data)
    } catch (error) {
      toast.error('Не удалось загрузить залы')
    }
  }

  const onSubmit = async (data: RoomFormData) => {
    if (!currentRestaurant) return

    setIsLoading(true)
    try {
      const payload = { ...data, imageId }
      if (editingRoom) {
        await apiClient.instance.put(`/admin-api/r/${currentRestaurant.id}/room/${editingRoom.id}`, payload)
        toast.success('Зал обновлен')
      } else {
        await apiClient.instance.post(`/admin-api/r/${currentRestaurant.id}/room`, payload)
        toast.success('Зал создан')
      }
      setIsModalOpen(false)
      reset()
      setEditingRoom(null)
      setImageId(null)
      loadRooms()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка сохранения')
    } finally {
      setIsLoading(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!currentRestaurant) return
    if (!confirm('Вы уверены, что хотите удалить этот зал?')) return

    try {
      await apiClient.instance.delete(`/admin-api/r/${currentRestaurant.id}/room/${id}`)
      toast.success('Зал удален')
      loadRooms()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Нельзя удалить зал: в зале есть активные столы')
    }
  }

  if (!currentRestaurant) {
    return <div>Ресторан не выбран</div>
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>Залы</h1>
        <button
          onClick={() => {
            setEditingRoom(null)
            reset()
            setImageId(null)
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
          Добавить зал
        </button>
      </div>

      <div style={{ backgroundColor: 'white', borderRadius: '8px', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: '#f5f5f5' }}>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Название</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Этаж</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Количество столов</th>
              <th style={{ padding: '1rem' }}>Действия</th>
            </tr>
          </thead>
          <tbody>
            {rooms.map((room) => (
              <tr key={room.id} style={{ borderTop: '1px solid #eee' }}>
                <td style={{ padding: '1rem' }}>{room.name}</td>
                <td style={{ padding: '1rem' }}>
                  {floors.find((f) => f.id === room.floorId)?.floorNumber || '-'}
                </td>
                <td style={{ padding: '1rem' }}>{room.tableCount || 0}</td>
                <td style={{ padding: '1rem' }}>
                  <button
                    onClick={() => {
                      setEditingRoom(room)
                      setImageId(room.imageId || null)
                      reset({
                        name: room.name,
                        floorId: room.floorId,
                        description: room.description || '',
                        isSmoking: room.isSmoking,
                        isOutdoor: room.isOutdoor,
                      })
                      setIsModalOpen(true)
                    }}
                    style={{ marginRight: '0.5rem', padding: '0.25rem 0.5rem', cursor: 'pointer' }}
                  >
                    Редактировать
                  </button>
                  <button
                    onClick={() => handleDelete(room.id)}
                    disabled={!!(room.tableCount && room.tableCount > 0)}
                    style={{
                      padding: '0.25rem 0.5rem',
                      cursor: !!(room.tableCount && room.tableCount > 0) ? 'not-allowed' : 'pointer',
                      color: room.tableCount && room.tableCount > 0 ? '#999' : '#f44336',
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
          setEditingRoom(null)
          setImageId(null)
        }}
        title={editingRoom ? 'Редактировать зал' : 'Создать зал'}
        size="medium"
      >
        <form onSubmit={handleSubmit(onSubmit)}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Название *</label>
            <input {...register('name')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {errors.name && <div style={{ color: 'red' }}>{errors.name.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Этаж *</label>
            <select {...register('floorId', { valueAsNumber: true })} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}>
              <option value="">Выберите этаж</option>
              {floors.map((floor) => (
                <option key={floor.id} value={floor.id}>
                  {floor.floorNumber}
                </option>
              ))}
            </select>
            {errors.floorId && <div style={{ color: 'red' }}>{errors.floorId.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Описание</label>
            <textarea {...register('description')} rows={3} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>
              <input type="checkbox" {...register('isSmoking')} style={{ marginRight: '0.5rem' }} />
              Курение разрешено
            </label>
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>
              <input type="checkbox" {...register('isOutdoor')} style={{ marginRight: '0.5rem' }} />
              На открытом воздухе
            </label>
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>План помещения</label>
            <ImageUpload
              currentImageId={imageId}
              onImageUploaded={setImageId}
              onImageRemoved={() => setImageId(null)}
              type="room"
              recommendedSize="минимум 1920x1080px"
            />
          </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
            <button
              type="button"
              onClick={() => {
                setIsModalOpen(false)
                reset()
                setEditingRoom(null)
                setImageId(null)
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

