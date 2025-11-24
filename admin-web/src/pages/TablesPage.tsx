import { useEffect, useState } from 'react'
import { useApp } from '../context/AppContext'
import { apiClient } from '../services/apiClient'
import Modal from '../components/common/Modal'
import ImageUpload from '../components/common/ImageUpload'
import ImagePreview from '../components/common/ImagePreview'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useToast } from '../context/ToastContext'
import type { Table, Room } from '../types'

const tableSchema = z.object({
  tableNumber: z.string().min(1, 'Номер стола обязателен').max(50),
  roomId: z.number().min(1, 'Выберите зал'),
  capacity: z.number().min(1, 'Количество мест должно быть больше 0').max(100),
  description: z.string().max(10000).optional(),
  depositAmount: z.string().optional(),
  depositNote: z.string().optional(),
})

type TableFormData = z.infer<typeof tableSchema>

export default function TablesPage() {
  const { currentRestaurant } = useApp()
  const [tables, setTables] = useState<Table[]>([])
  const [rooms, setRooms] = useState<Room[]>([])
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingTable, setEditingTable] = useState<Table | null>(null)
  const [imageId, setImageId] = useState<number | null>(null)
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const toast = useToast()

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<TableFormData>({
    resolver: zodResolver(tableSchema),
  })

  useEffect(() => {
    if (currentRestaurant) {
      loadRooms()
      loadTables()
    }
  }, [currentRestaurant])

  const loadRooms = async () => {
    if (!currentRestaurant) return

    try {
      const response = await apiClient.instance.get<{ data: Room[] }>(
        `/admin-api/r/${currentRestaurant.id}/room`
      )
      setRooms(Array.isArray(response.data?.data) ? response.data.data : [])
    } catch (error) {
      toast.error('Не удалось загрузить залы')
      setRooms([])
    }
  }

  const loadTables = async () => {
    if (!currentRestaurant) return

    try {
      const response = await apiClient.instance.get<{ data: Table[] }>(
        `/admin-api/r/${currentRestaurant.id}/table`
      )
      setTables(Array.isArray(response.data?.data) ? response.data.data : [])
    } catch (error) {
      toast.error('Не удалось загрузить столы')
      setTables([])
    }
  }

  const handleImageUpload = async (file: File) => {
    if (!currentRestaurant || !editingTable) return

    setIsLoading(true)
    try {
      const formData = new FormData()
      formData.append('file', file)

      const response = await apiClient.instance.post<Table>(
        `/admin-api/r/${currentRestaurant.id}/table/${editingTable.id}/image`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      )

      setImageId(response.data.imageId ?? null)
      toast.success('Изображение успешно загружено')
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка загрузки изображения')
    } finally {
      setIsLoading(false)
    }
  }

  const handleImageRemove = async () => {
    if (!currentRestaurant || !editingTable) return

    setIsLoading(true)
    try {
      const response = await apiClient.instance.delete<Table>(
        `/admin-api/r/${currentRestaurant.id}/table/${editingTable.id}/image`
      )

      setImageId(response.data.imageId ?? null)
      toast.success('Изображение удалено')
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка удаления изображения')
    } finally {
      setIsLoading(false)
    }
  }

  const onSubmit = async (data: TableFormData) => {
    if (!currentRestaurant) return

    setIsLoading(true)
    try {
      // Создаем или обновляем сущность без изображения
      let tableId: number
      if (editingTable) {
        await apiClient.instance.put(`/admin-api/r/${currentRestaurant.id}/table/${editingTable.id}`, data)
        tableId = editingTable.id
        toast.success('Стол обновлен')
      } else {
        const response = await apiClient.instance.post<Table>(
          `/admin-api/r/${currentRestaurant.id}/table`,
          data
        )
        tableId = response.data.id
        toast.success('Стол создан')
      }

      // Если есть файл изображения, загружаем его
      if (imageFile) {
        const formData = new FormData()
        formData.append('file', imageFile)

        await apiClient.instance.post(
          `/admin-api/r/${currentRestaurant.id}/table/${tableId}/image`,
          formData,
          {
            headers: {
              'Content-Type': 'multipart/form-data',
            },
          }
        )
      }

      setIsModalOpen(false)
      reset()
      setEditingTable(null)
      setImageId(null)
      setImageFile(null)
      loadTables()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка сохранения')
    } finally {
      setIsLoading(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!currentRestaurant) return
    if (!confirm('Вы уверены, что хотите удалить этот стол?')) return

    try {
      await apiClient.instance.delete(`/admin-api/r/${currentRestaurant.id}/table/${id}`)
      toast.success('Стол удален')
      loadTables()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Нельзя удалить стол: у стола есть активные бронирования')
    }
  }

  if (!currentRestaurant) {
    return <div>Ресторан не выбран</div>
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>Столы</h1>
        <button
          onClick={() => {
            setEditingTable(null)
            reset()
            setImageId(null)
            setImageFile(null)
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
          Добавить стол
        </button>
      </div>

      <div style={{ backgroundColor: 'white', borderRadius: '8px', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: '#f5f5f5' }}>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Фото</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Номер стола</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Зал</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Количество мест</th>
              <th style={{ padding: '1rem' }}>Действия</th>
            </tr>
          </thead>
          <tbody>
            {tables.map((table) => (
              <tr key={table.id} style={{ borderTop: '1px solid #eee' }}>
                <td style={{ padding: '1rem' }}>
                  <ImagePreview imageId={table.imageId ?? null} size="small" />
                </td>
                <td style={{ padding: '1rem' }}>{table.tableNumber}</td>
                <td style={{ padding: '1rem' }}>
                  {rooms.find((r) => r.id === table.roomId)?.name || '-'}
                </td>
                <td style={{ padding: '1rem' }}>{table.capacity}</td>
                <td style={{ padding: '1rem' }}>
                  <button
                    onClick={() => {
                      setEditingTable(table)
                      setImageId(table.imageId || null)
                      setImageFile(null)
                      reset({
                        tableNumber: table.tableNumber,
                        roomId: table.roomId,
                        capacity: table.capacity,
                        description: table.description || '',
                        depositAmount: table.depositAmount || '',
                        depositNote: table.depositNote || '',
                      })
                      setIsModalOpen(true)
                    }}
                    style={{ marginRight: '0.5rem', padding: '0.25rem 0.5rem', cursor: 'pointer' }}
                  >
                    Редактировать
                  </button>
                  <button
                    onClick={() => handleDelete(table.id)}
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
          setEditingTable(null)
          setImageId(null)
          setImageFile(null)
        }}
        title={editingTable ? 'Редактировать стол' : 'Создать стол'}
        size="medium"
      >
        <form onSubmit={handleSubmit(onSubmit)}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Номер стола *</label>
            <input {...register('tableNumber')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {errors.tableNumber && <div style={{ color: 'red' }}>{errors.tableNumber.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Зал *</label>
            <select {...register('roomId', { valueAsNumber: true })} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}>
              <option value="">Выберите зал</option>
              {rooms.map((room) => (
                <option key={room.id} value={room.id}>
                  {room.name}
                </option>
              ))}
            </select>
            {errors.roomId && <div style={{ color: 'red' }}>{errors.roomId.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Количество мест *</label>
            <input type="number" min="1" max="100" {...register('capacity', { valueAsNumber: true })} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {errors.capacity && <div style={{ color: 'red' }}>{errors.capacity.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Описание</label>
            <textarea {...register('description')} rows={3} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Минимальная сумма депозита</label>
            <input {...register('depositAmount')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Примечание о депозите</label>
            <input {...register('depositNote')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Фото стола</label>
            <ImageUpload
              currentImageId={imageId}
              onImageUploaded={editingTable ? handleImageUpload : (file: File) => setImageFile(file)}
              onImageRemoved={editingTable ? handleImageRemove : () => {
                setImageId(null)
                setImageFile(null)
              }}
              type="table"
              recommendedSize="800x600px - 1200x900px"
              uploadToEntity={true}
            />
          </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
            <button
              type="button"
              onClick={() => {
                setIsModalOpen(false)
                reset()
                setEditingTable(null)
                setImageId(null)
                setImageFile(null)
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

