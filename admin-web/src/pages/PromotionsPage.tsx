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
import type { Promotion } from '../types'

const promotionSchema = z.object({
  title: z.string().min(1, 'Заголовок обязателен').max(255),
  description: z.string().max(10000).optional(),
  promotionTypeId: z.number().min(1, 'Выберите тип промо-события'),
  startDate: z.string().min(1, 'Дата начала обязательна'),
  endDate: z.string().optional(),
  isRecurring: z.boolean().optional(),
  recurrenceType: z.enum(['WEEKLY', 'MONTHLY', 'DAILY']).optional(),
  recurrenceDayOfWeek: z.number().min(1).max(7).optional(),
})

type PromotionFormData = z.infer<typeof promotionSchema>

export default function PromotionsPage() {
  const { currentRestaurant } = useApp()
  const [promotions, setPromotions] = useState<Promotion[]>([])
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingPromotion, setEditingPromotion] = useState<Promotion | null>(null)
  const [imageId, setImageId] = useState<number | null>(null)
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const toast = useToast()

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useForm<PromotionFormData>({
    resolver: zodResolver(promotionSchema),
    defaultValues: {
      isRecurring: false,
    },
  })

  const isRecurring = watch('isRecurring')
  const recurrenceType = watch('recurrenceType')

  useEffect(() => {
    if (currentRestaurant) {
      loadPromotions()
    }
  }, [currentRestaurant])

  const loadPromotions = async () => {
    if (!currentRestaurant) return

    try {
      const response = await apiClient.instance.get<{ data: Promotion[] }>(
        `/admin-api/r/${currentRestaurant.id}/promotion`
      )
      setPromotions(Array.isArray(response.data?.data) ? response.data.data : [])
    } catch (error) {
      toast.error('Не удалось загрузить акции')
      setPromotions([])
    }
  }

  const handleImageUpload = async (file: File) => {
    if (!currentRestaurant || !editingPromotion) return

    setIsLoading(true)
    try {
      const formData = new FormData()
      formData.append('file', file)

      const response = await apiClient.instance.post<Promotion>(
        `/admin-api/r/${currentRestaurant.id}/promotion/${editingPromotion.id}/image`,
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
    if (!currentRestaurant || !editingPromotion) return

    setIsLoading(true)
    try {
      const response = await apiClient.instance.delete<Promotion>(
        `/admin-api/r/${currentRestaurant.id}/promotion/${editingPromotion.id}/image`
      )

      setImageId(response.data.imageId ?? null)
      toast.success('Изображение удалено')
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка удаления изображения')
    } finally {
      setIsLoading(false)
    }
  }

  const onSubmit = async (data: PromotionFormData) => {
    if (!currentRestaurant) return

    setIsLoading(true)
    try {
      // Создаем или обновляем сущность без изображения
      let promotionId: number
      if (editingPromotion) {
        await apiClient.instance.put(`/admin-api/r/${currentRestaurant.id}/promotion/${editingPromotion.id}`, data)
        promotionId = editingPromotion.id
        toast.success('Акция обновлена')
      } else {
        const response = await apiClient.instance.post<Promotion>(
          `/admin-api/r/${currentRestaurant.id}/promotion`,
          data
        )
        promotionId = response.data.id
        toast.success('Акция создана')
      }

      // Если есть файл изображения, загружаем его
      if (imageFile) {
        const formData = new FormData()
        formData.append('file', imageFile)

        await apiClient.instance.post(
          `/admin-api/r/${currentRestaurant.id}/promotion/${promotionId}/image`,
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
      setEditingPromotion(null)
      setImageId(null)
      setImageFile(null)
      loadPromotions()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка сохранения')
    } finally {
      setIsLoading(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!currentRestaurant) return
    if (!confirm('Вы уверены, что хотите удалить эту акцию?')) return

    try {
      await apiClient.instance.delete(`/admin-api/r/${currentRestaurant.id}/promotion/${id}`)
      toast.success('Акция удалена')
      loadPromotions()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка удаления')
    }
  }

  if (!currentRestaurant) {
    return <div>Ресторан не выбран</div>
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>Акции и события</h1>
        <button
          onClick={() => {
            setEditingPromotion(null)
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
          Создать акцию
        </button>
      </div>

      <div style={{ backgroundColor: 'white', borderRadius: '8px', overflow: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '1400px' }}>
          <thead>
            <tr style={{ backgroundColor: '#f5f5f5' }}>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Изображение</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Заголовок</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Описание</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Тип промо</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Дата начала</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Дата окончания</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Повторяющееся</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Тип повторения</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>День недели</th>
              <th style={{ padding: '1rem' }}>Действия</th>
            </tr>
          </thead>
          <tbody>
            {promotions.map((promotion) => (
              <tr key={promotion.id} style={{ borderTop: '1px solid #eee' }}>
                <td style={{ padding: '1rem' }}>
                  <ImagePreview imageId={promotion.imageId ?? null} size="small" />
                </td>
                <td style={{ padding: '1rem' }}>{promotion.title}</td>
                <td style={{ padding: '1rem', maxWidth: '200px' }}>
                  <div 
                    style={{ 
                      overflow: 'hidden', 
                      textOverflow: 'ellipsis', 
                      whiteSpace: 'nowrap',
                    }}
                    title={promotion.description || ''}
                  >
                    {promotion.description || '-'}
                  </div>
                </td>
                <td style={{ padding: '1rem' }}>
                  {promotion.promotionTypeId}
                </td>
                <td style={{ padding: '1rem' }}>
                  {new Date(promotion.startDate).toLocaleDateString('ru-RU')}
                </td>
                <td style={{ padding: '1rem' }}>
                  {promotion.endDate
                    ? new Date(promotion.endDate).toLocaleDateString('ru-RU')
                    : '-'}
                </td>
                <td style={{ padding: '1rem' }}>
                  {promotion.isRecurring ? 'Да' : 'Нет'}
                </td>
                <td style={{ padding: '1rem' }}>
                  {promotion.recurrenceType 
                    ? promotion.recurrenceType === 'DAILY' ? 'Ежедневно' 
                      : promotion.recurrenceType === 'WEEKLY' ? 'Еженедельно'
                      : promotion.recurrenceType === 'MONTHLY' ? 'Ежемесячно'
                      : promotion.recurrenceType
                    : '-'}
                </td>
                <td style={{ padding: '1rem' }}>
                  {promotion.recurrenceDayOfWeek || '-'}
                </td>
                <td style={{ padding: '1rem' }}>
                  <button
                    onClick={() => {
                      setEditingPromotion(promotion)
                      setImageId(promotion.imageId || null)
                      setImageFile(null)
                      reset({
                        title: promotion.title,
                        description: promotion.description || '',
                        promotionTypeId: promotion.promotionTypeId,
                        startDate: promotion.startDate.split('T')[0],
                        endDate: promotion.endDate ? promotion.endDate.split('T')[0] : '',
                        isRecurring: promotion.isRecurring,
                        recurrenceType: promotion.recurrenceType || undefined,
                        recurrenceDayOfWeek: promotion.recurrenceDayOfWeek || undefined,
                      })
                      setIsModalOpen(true)
                    }}
                    style={{ marginRight: '0.5rem', padding: '0.25rem 0.5rem', cursor: 'pointer' }}
                  >
                    Редактировать
                  </button>
                  <button
                    onClick={() => handleDelete(promotion.id)}
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
          setEditingPromotion(null)
          setImageId(null)
          setImageFile(null)
        }}
        title={editingPromotion ? 'Редактировать акцию' : 'Создать акцию'}
        size="large"
      >
        <form onSubmit={handleSubmit(onSubmit)}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Заголовок *</label>
            <input {...register('title')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {errors.title && <div style={{ color: 'red' }}>{errors.title.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Описание</label>
            <textarea {...register('description')} rows={3} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Тип промо-события *</label>
            <input type="number" {...register('promotionTypeId', { valueAsNumber: true })} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {errors.promotionTypeId && <div style={{ color: 'red' }}>{errors.promotionTypeId.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Дата начала *</label>
            <input type="date" {...register('startDate')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {errors.startDate && <div style={{ color: 'red' }}>{errors.startDate.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Дата окончания</label>
            <input type="date" {...register('endDate')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>
              <input type="checkbox" {...register('isRecurring')} style={{ marginRight: '0.5rem' }} />
              Повторяющееся событие
            </label>
          </div>

          {isRecurring && (
            <>
              <div style={{ marginBottom: '1rem' }}>
                <label>Тип повторения</label>
                <select {...register('recurrenceType')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}>
                  <option value="">Выберите тип</option>
                  <option value="DAILY">Ежедневно</option>
                  <option value="WEEKLY">Еженедельно</option>
                  <option value="MONTHLY">Ежемесячно</option>
                </select>
              </div>

              {recurrenceType === 'WEEKLY' && (
                <div style={{ marginBottom: '1rem' }}>
                  <label>День недели (1-7, где 1=понедельник)</label>
                  <input type="number" min="1" max="7" {...register('recurrenceDayOfWeek', { valueAsNumber: true })} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
                </div>
              )}
            </>
          )}

          <div style={{ marginBottom: '1rem' }}>
            <label>Изображение</label>
            <ImageUpload
              currentImageId={imageId}
              onImageUploaded={editingPromotion ? handleImageUpload : (file: File) => setImageFile(file)}
              onImageRemoved={editingPromotion ? handleImageRemove : () => {
                setImageId(null)
                setImageFile(null)
              }}
              type="promotion"
              recommendedSize="1200x600px - 1920x1080px"
              uploadToEntity={true}
            />
          </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
            <button
              type="button"
              onClick={() => {
                setIsModalOpen(false)
                reset()
                setEditingPromotion(null)
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

