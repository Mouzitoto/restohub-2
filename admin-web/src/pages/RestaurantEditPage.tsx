import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useApp } from '../context/AppContext'
import { apiClient } from '../services/apiClient'
import ImageUpload from '../components/common/ImageUpload'
import ImagePreview from '../components/common/ImagePreview'
import { useToast } from '../context/ToastContext'
import { getImageUploadErrorMessage } from '../utils/imageUploadError'
import type { Restaurant } from '../types'

const restaurantSchema = z.object({
  name: z.string().min(1, 'Название ресторана обязательно').max(255),
  address: z.string().max(500).optional(),
  phone: z.string().regex(/^(\+7|8)\d{10}$/, 'Введите корректный номер телефона').optional(),
  whatsapp: z.string().regex(/^(\+7|8)\d{10}$/, 'Введите корректный номер телефона').optional(),
  instagram: z.string().regex(/^https?:\/\/(www\.)?instagram\.com\/[\w.]+$/, 'Введите корректную ссылку на Instagram').optional(),
  description: z.string().max(10000).optional(),
  latitude: z.number().min(-90).max(90).optional(),
  longitude: z.number().min(-180).max(180).optional(),
  workingHours: z.string().optional(),
  managerLanguageCode: z.string().regex(/^[a-z]{2}$/, 'Введите корректный код языка').optional(),
  isActive: z.boolean(),
})

type RestaurantFormData = z.infer<typeof restaurantSchema>

export default function RestaurantEditPage() {
  const { currentRestaurant, role, refreshUserInfo } = useApp()
  const [logoImageId, setLogoImageId] = useState<number | null>(null)
  const [bgImageId, setBgImageId] = useState<number | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [isEditingInfo, setIsEditingInfo] = useState(false)
  const [isEditingLogo, setIsEditingLogo] = useState(false)
  const [isEditingBackground, setIsEditingBackground] = useState(false)
  const [restaurantData, setRestaurantData] = useState<RestaurantFormData | null>(null)
  const toast = useToast()

  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
  } = useForm<RestaurantFormData>({
    resolver: zodResolver(restaurantSchema),
    defaultValues: {
      isActive: true,
      managerLanguageCode: 'ru',
    },
  })

  useEffect(() => {
    if (currentRestaurant) {
      loadRestaurant()
    }
  }, [currentRestaurant])

  const loadRestaurant = async () => {
    if (!currentRestaurant) return

    try {
      const response = await apiClient.instance.get<Restaurant>(
        `/admin-api/r/${currentRestaurant.id}`
      )
      const data = response.data

      const formData: RestaurantFormData = {
        name: data.name,
        address: data.address || '',
        phone: data.phone || '',
        whatsapp: data.whatsapp || '',
        instagram: data.instagram || '',
        description: data.description || '',
        latitude: data.latitude || undefined,
        longitude: data.longitude || undefined,
        workingHours: data.workingHours || '',
        managerLanguageCode: data.managerLanguageCode || 'ru',
        isActive: data.isActive,
      }

      setRestaurantData(formData)
      setValue('name', formData.name)
      setValue('address', formData.address)
      setValue('phone', formData.phone)
      setValue('whatsapp', formData.whatsapp)
      setValue('instagram', formData.instagram)
      setValue('description', formData.description)
      setValue('latitude', formData.latitude)
      setValue('longitude', formData.longitude)
      setValue('workingHours', formData.workingHours)
      setValue('managerLanguageCode', formData.managerLanguageCode)
      setValue('isActive', formData.isActive)

      setLogoImageId(data.logoImageId ?? null)
      setBgImageId(data.bgImageId ?? null)
    } catch (error) {
      toast.error('Не удалось загрузить данные ресторана')
    }
  }

  const onSubmit = async (data: RestaurantFormData) => {
    if (!currentRestaurant) return

    setIsLoading(true)
    try {
      // Теперь logoImageId и bgImageId не нужно отправлять, так как они загружаются отдельно
      await apiClient.instance.put(`/admin-api/r/${currentRestaurant.id}`, data)
      toast.success('Настройки сохранены')
      
      // Перезагружаем данные ресторана после сохранения
      await loadRestaurant()
      setIsEditingInfo(false)
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка сохранения')
    } finally {
      setIsLoading(false)
    }
  }

  const handleCancelEdit = () => {
    if (restaurantData) {
      setValue('name', restaurantData.name)
      setValue('address', restaurantData.address)
      setValue('phone', restaurantData.phone)
      setValue('whatsapp', restaurantData.whatsapp)
      setValue('instagram', restaurantData.instagram)
      setValue('description', restaurantData.description)
      setValue('latitude', restaurantData.latitude)
      setValue('longitude', restaurantData.longitude)
      setValue('workingHours', restaurantData.workingHours)
      setValue('managerLanguageCode', restaurantData.managerLanguageCode)
      setValue('isActive', restaurantData.isActive)
    }
    setIsEditingInfo(false)
  }

  const handleActivate = async () => {
    if (!currentRestaurant) return

    setIsLoading(true)
    try {
      await apiClient.instance.put<Restaurant>(
        `/admin-api/r/${currentRestaurant.id}/activate`
      )
      toast.success('Ресторан активирован')
      await loadRestaurant()
      // Обновляем контекст, чтобы уведомления обновились
      await refreshUserInfo()
    } catch (error: any) {
      const errorMessage = error.response?.data?.exceptionName === 'NO_ACTIVE_SUBSCRIPTION'
        ? 'Нельзя активировать ресторан без активной подписки'
        : error.response?.data?.message || 'Ошибка активации ресторана'
      toast.error(errorMessage)
    } finally {
      setIsLoading(false)
    }
  }

  const handleDeactivate = async () => {
    if (!currentRestaurant) return

    if (!confirm('Вы уверены, что хотите деактивировать ресторан? Ресторан станет невидимым для пользователей.')) {
      return
    }

    setIsLoading(true)
    try {
      await apiClient.instance.put<Restaurant>(
        `/admin-api/r/${currentRestaurant.id}/deactivate`
      )
      toast.success('Ресторан деактивирован')
      await loadRestaurant()
      // Обновляем контекст, чтобы уведомления обновились
      await refreshUserInfo()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка деактивации ресторана')
    } finally {
      setIsLoading(false)
    }
  }

  const handleImageUpload = async (file: File, imageType: 'logo' | 'background') => {
    if (!currentRestaurant) return

    setIsLoading(true)
    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('type', imageType)

      const response = await apiClient.instance.post<Restaurant>(
        `/admin-api/r/${currentRestaurant.id}/image`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      )

      // Обновляем состояние из ответа
      setLogoImageId(response.data.logoImageId ?? null)
      setBgImageId(response.data.bgImageId ?? null)
      
      toast.success('Изображение успешно загружено')
      setIsEditingLogo(false)
      setIsEditingBackground(false)
    } catch (error: any) {
      toast.error(getImageUploadErrorMessage(error))
    } finally {
      setIsLoading(false)
    }
  }

  const handleImageRemove = async (imageType: 'logo' | 'background') => {
    if (!currentRestaurant) return

    setIsLoading(true)
    try {
      const response = await apiClient.instance.delete<Restaurant>(
        `/admin-api/r/${currentRestaurant.id}/image?type=${imageType}`
      )

      // Обновляем состояние из ответа
      setLogoImageId(response.data.logoImageId ?? null)
      setBgImageId(response.data.bgImageId ?? null)
      
      toast.success('Изображение удалено')
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка удаления изображения')
    } finally {
      setIsLoading(false)
    }
  }

  if (!currentRestaurant) {
    return <div>Ресторан не выбран</div>
  }

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Мой ресторан</h1>

      {/* Основная информация */}
      <div style={{ backgroundColor: 'white', padding: '2rem', borderRadius: '8px', marginBottom: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <h2 style={{ margin: 0 }}>Основная информация</h2>
          {!isEditingInfo && (
            <button
              type="button"
              onClick={() => setIsEditingInfo(true)}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '0.875rem',
              }}
            >
              Редактировать
            </button>
          )}
        </div>

        {isEditingInfo ? (
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

            <div style={{ marginBottom: '1rem' }}>
              <label>Телефон</label>
              <input {...register('phone')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
              {errors.phone && <div style={{ color: 'red' }}>{errors.phone.message}</div>}
            </div>

            <div style={{ marginBottom: '1rem' }}>
              <label>WhatsApp</label>
              <input {...register('whatsapp')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            </div>

            <div style={{ marginBottom: '1rem' }}>
              <label>Instagram</label>
              <input {...register('instagram')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            </div>

            <div style={{ marginBottom: '1rem' }}>
              <label>Описание</label>
              <textarea {...register('description')} rows={5} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            </div>

            <div style={{ marginBottom: '1rem' }}>
              <label>Рабочие часы</label>
              <input {...register('workingHours')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            </div>

            <div style={{ marginBottom: '1rem' }}>
              <label>
                <input type="checkbox" {...register('isActive')} style={{ marginRight: '0.5rem' }} />
                Активен
              </label>
            </div>

            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button
                type="submit"
                disabled={isLoading}
                style={{
                  padding: '0.75rem 2rem',
                  backgroundColor: '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: isLoading ? 'not-allowed' : 'pointer',
                  fontSize: '1rem',
                }}
              >
                {isLoading ? 'Сохранение...' : 'Сохранить'}
              </button>
              <button
                type="button"
                onClick={handleCancelEdit}
                disabled={isLoading}
                style={{
                  padding: '0.75rem 2rem',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: isLoading ? 'not-allowed' : 'pointer',
                  fontSize: '1rem',
                }}
              >
                Отмена
              </button>
            </div>
          </form>
        ) : (
          <div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Название:</strong> {restaurantData?.name || '-'}
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Адрес:</strong> {restaurantData?.address || '-'}
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Телефон:</strong> {restaurantData?.phone || '-'}
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>WhatsApp:</strong> {restaurantData?.whatsapp || '-'}
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Instagram:</strong> {restaurantData?.instagram || '-'}
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Описание:</strong> {restaurantData?.description || '-'}
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Рабочие часы:</strong> {restaurantData?.workingHours || '-'}
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <strong>Активен:</strong> {restaurantData?.isActive ? 'Да' : 'Нет'}
            </div>
            {(role === 'ADMIN' || role === 'MANAGER') && (
              <div style={{ marginTop: '1.5rem', display: 'flex', gap: '0.5rem' }}>
                {!restaurantData?.isActive && (
                  <button
                    type="button"
                    onClick={handleActivate}
                    disabled={isLoading}
                    style={{
                      padding: '0.75rem 1.5rem',
                      backgroundColor: '#28a745',
                      color: 'white',
                      border: 'none',
                      borderRadius: '4px',
                      cursor: isLoading ? 'not-allowed' : 'pointer',
                      fontSize: '1rem',
                      fontWeight: 'bold',
                    }}
                  >
                    Активировать
                  </button>
                )}
                {restaurantData?.isActive && (
                  <button
                    type="button"
                    onClick={handleDeactivate}
                    disabled={isLoading}
                    style={{
                      padding: '0.75rem 1.5rem',
                      backgroundColor: '#dc3545',
                      color: 'white',
                      border: 'none',
                      borderRadius: '4px',
                      cursor: isLoading ? 'not-allowed' : 'pointer',
                      fontSize: '1rem',
                      fontWeight: 'bold',
                    }}
                  >
                    Деактивировать
                  </button>
                )}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Логотип */}
      <div style={{ backgroundColor: 'white', padding: '2rem', borderRadius: '8px', marginBottom: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <h2 style={{ margin: 0 }}>Логотип ресторана</h2>
          {!isEditingLogo && (
            <button
              type="button"
              onClick={() => setIsEditingLogo(true)}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '0.875rem',
              }}
            >
              Редактировать
            </button>
          )}
        </div>

        {isEditingLogo ? (
          <div>
            <ImageUpload
              currentImageId={logoImageId}
              onImageUploaded={(file: File) => handleImageUpload(file, 'logo')}
              onImageRemoved={() => handleImageRemove('logo')}
              type="logo"
              recommendedSize="200x200px - 500x500px"
              uploadToEntity={true}
            />
            <button
              type="button"
              onClick={() => setIsEditingLogo(false)}
              style={{
                marginTop: '1rem',
                padding: '0.5rem 1rem',
                backgroundColor: '#6c757d',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '0.875rem',
              }}
            >
              Отмена
            </button>
          </div>
        ) : (
          <div>
            {logoImageId ? (
              <ImagePreview imageId={logoImageId} size="large" />
            ) : (
              <div style={{ 
                padding: '2rem', 
                textAlign: 'center', 
                backgroundColor: '#f5f5f5', 
                borderRadius: '8px',
                color: '#666'
              }}>
                Логотип не загружен
              </div>
            )}
          </div>
        )}
      </div>

      {/* Фоновое изображение */}
      <div style={{ backgroundColor: 'white', padding: '2rem', borderRadius: '8px', marginBottom: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <h2 style={{ margin: 0 }}>Фоновое изображение</h2>
          {!isEditingBackground && (
            <button
              type="button"
              onClick={() => setIsEditingBackground(true)}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '0.875rem',
              }}
            >
              Редактировать
            </button>
          )}
        </div>

        {isEditingBackground ? (
          <div>
            <ImageUpload
              currentImageId={bgImageId}
              onImageUploaded={(file: File) => handleImageUpload(file, 'background')}
              onImageRemoved={() => handleImageRemove('background')}
              type="background"
              recommendedSize="минимум 1920x1080px"
              uploadToEntity={true}
            />
            <button
              type="button"
              onClick={() => setIsEditingBackground(false)}
              style={{
                marginTop: '1rem',
                padding: '0.5rem 1rem',
                backgroundColor: '#6c757d',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '0.875rem',
              }}
            >
              Отмена
            </button>
          </div>
        ) : (
          <div>
            {bgImageId ? (
              <ImagePreview imageId={bgImageId} size="large" />
            ) : (
              <div style={{ 
                padding: '2rem', 
                textAlign: 'center', 
                backgroundColor: '#f5f5f5', 
                borderRadius: '8px',
                color: '#666'
              }}>
                Фоновое изображение не загружено
              </div>
            )}
          </div>
        )}
      </div>

      {role === 'ADMIN' && (
        <div style={{ backgroundColor: 'white', padding: '2rem', borderRadius: '8px', marginBottom: '2rem' }}>
          <h2 style={{ marginBottom: '1.5rem' }}>Управление подпиской</h2>
          <p>Управление подпиской доступно на странице подписки</p>
        </div>
      )}
    </div>
  )
}

