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
import { getImageUploadErrorMessage } from '../utils/imageUploadError'
import type { Promotion, PromotionType } from '../types'

const promotionSchema = z.object({
  title: z.string().min(1, 'Заголовок обязателен').max(255),
  description: z.string().max(10000).optional(),
  promotionTypeId: z.number().min(1, 'Выберите тип промо-события'),
  startDate: z.string().min(1, 'Дата начала обязательна'),
  endDate: z.string().optional(),
  isRecurring: z.boolean().optional(),
  recurrenceType: z.enum(['WEEKLY', 'MONTHLY', 'DAILY']).optional(),
  recurrenceDaysOfWeek: z.array(z.number().min(1).max(7)).optional(),
})

type PromotionFormData = z.infer<typeof promotionSchema>

export default function PromotionsPage() {
  const { currentRestaurant } = useApp()
  const [promotions, setPromotions] = useState<Promotion[]>([])
  const [promotionTypes, setPromotionTypes] = useState<PromotionType[]>([])
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
    loadPromotionTypes()
    if (currentRestaurant) {
      loadPromotions()
    }
  }, [currentRestaurant])
  
  const loadPromotionTypes = async () => {
    try {
      // Типы промо-событий одинаковые для всех ресторанов, используем id=1 как fallback
      const restaurantId = currentRestaurant?.id || 1
      const response = await apiClient.instance.get<PromotionType[]>(
        `/admin-api/r/${restaurantId}/promotion/types`
      )
      setPromotionTypes(Array.isArray(response.data) ? response.data : [])
    } catch (error) {
      toast.error('Не удалось загрузить типы промо-событий')
      setPromotionTypes([])
    }
  }

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
      toast.error(getImageUploadErrorMessage(error))
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
                  {promotion.promotionType?.name || promotionTypes.find(t => t.id === promotion.promotionTypeId)?.name || '-'}
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
                  {promotion.recurrenceDaysOfWeek && promotion.recurrenceDaysOfWeek.length > 0
                    ? promotion.recurrenceDaysOfWeek
                        .sort((a, b) => a - b)
                        .map((day) => {
                          const dayNames = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс']
                          return dayNames[day - 1]
                        })
                        .join(', ')
                    : '-'}
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
                        promotionTypeId: promotion.promotionType?.id || promotion.promotionTypeId,
                        startDate: promotion.startDate.split('T')[0],
                        endDate: promotion.endDate ? promotion.endDate.split('T')[0] : '',
                        isRecurring: promotion.isRecurring,
                        recurrenceType: promotion.recurrenceType || undefined,
                        recurrenceDaysOfWeek: promotion.recurrenceDaysOfWeek || undefined,
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
            <select {...register('promotionTypeId', { valueAsNumber: true })} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}>
              <option value="">Выберите тип</option>
              {promotionTypes.map((type) => (
                <option key={type.id} value={type.id}>
                  {type.name}
                </option>
              ))}
            </select>
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

              {recurrenceType && (
                <div style={{ marginBottom: '1rem', padding: '0.75rem', backgroundColor: '#f5f5f5', borderRadius: '4px' }}>
                  {recurrenceType === 'DAILY' && (
                    <div style={{ fontSize: '0.9rem', color: '#666' }}>
                      <strong>Ежедневно:</strong> Событие будет повторяться каждый день в период действия акции.
                    </div>
                  )}
                  {recurrenceType === 'WEEKLY' && (
                    <div style={{ fontSize: '0.9rem', color: '#666' }}>
                      <strong>Еженедельно:</strong> Событие будет повторяться в выбранные дни недели каждую неделю в период действия акции.
                    </div>
                  )}
                  {recurrenceType === 'MONTHLY' && (
                    <div style={{ fontSize: '0.9rem', color: '#666' }}>
                      <strong>Ежемесячно:</strong> Событие будет повторяться каждый месяц в тот же день месяца, что указан в дате начала акции.
                    </div>
                  )}
                </div>
              )}

              {recurrenceType === 'WEEKLY' && (
                <div style={{ marginBottom: '1rem' }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem' }}>Дни недели *</label>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                    {[
                      { value: 1, label: 'Пн' },
                      { value: 2, label: 'Вт' },
                      { value: 3, label: 'Ср' },
                      { value: 4, label: 'Чт' },
                      { value: 5, label: 'Пт' },
                      { value: 6, label: 'Сб' },
                      { value: 7, label: 'Вс' },
                    ].map((day) => {
                      const isSelected = watch('recurrenceDaysOfWeek')?.includes(day.value) || false
                      return (
                        <button
                          key={day.value}
                          type="button"
                          onClick={() => {
                            const currentDays = watch('recurrenceDaysOfWeek') || []
                            const newDays = isSelected
                              ? currentDays.filter((d: number) => d !== day.value)
                              : [...currentDays, day.value]
                            reset({
                              ...watch(),
                              recurrenceDaysOfWeek: newDays.length > 0 ? newDays : undefined,
                            })
                          }}
                          style={{
                            padding: '0.5rem 1rem',
                            minWidth: '50px',
                            border: `2px solid ${isSelected ? '#007bff' : '#ddd'}`,
                            borderRadius: '4px',
                            backgroundColor: isSelected ? '#007bff' : 'white',
                            color: isSelected ? 'white' : '#333',
                            cursor: 'pointer',
                            fontWeight: isSelected ? 'bold' : 'normal',
                            transition: 'all 0.2s',
                          }}
                          onMouseEnter={(e) => {
                            if (!isSelected) {
                              e.currentTarget.style.backgroundColor = '#f0f0f0'
                            }
                          }}
                          onMouseLeave={(e) => {
                            if (!isSelected) {
                              e.currentTarget.style.backgroundColor = 'white'
                            }
                          }}
                        >
                          {day.label}
                        </button>
                      )
                    })}
                  </div>
                  {errors.recurrenceDaysOfWeek && (
                    <div style={{ color: 'red', marginTop: '0.25rem' }}>{errors.recurrenceDaysOfWeek.message}</div>
                  )}
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

