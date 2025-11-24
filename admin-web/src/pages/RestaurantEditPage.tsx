import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useApp } from '../context/AppContext'
import { apiClient } from '../services/apiClient'
import ImageUpload from '../components/common/ImageUpload'
import { useToast } from '../context/ToastContext'
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
  const { currentRestaurant, role } = useApp()
  const [logoImageId, setLogoImageId] = useState<number | null>(null)
  const [bgImageId, setBgImageId] = useState<number | null>(null)
  const [isLoading, setIsLoading] = useState(false)
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

      setValue('name', data.name)
      setValue('address', data.address || '')
      setValue('phone', data.phone || '')
      setValue('whatsapp', data.whatsapp || '')
      setValue('instagram', data.instagram || '')
      setValue('description', data.description || '')
      setValue('latitude', data.latitude || undefined)
      setValue('longitude', data.longitude || undefined)
      setValue('workingHours', data.workingHours || '')
      setValue('managerLanguageCode', data.managerLanguageCode || 'ru')
      setValue('isActive', data.isActive)

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
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка сохранения')
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
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка загрузки изображения')
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
      <h1 style={{ marginBottom: '2rem' }}>Редактирование ресторана</h1>

      <form onSubmit={handleSubmit(onSubmit)}>
        <div style={{ backgroundColor: 'white', padding: '2rem', borderRadius: '8px', marginBottom: '2rem' }}>
          <h2 style={{ marginBottom: '1.5rem' }}>Основная информация</h2>

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
        </div>

        <div style={{ backgroundColor: 'white', padding: '2rem', borderRadius: '8px', marginBottom: '2rem' }}>
          <h2 style={{ marginBottom: '1.5rem' }}>Дизайн страницы</h2>

          <div style={{ marginBottom: '2rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
              <label style={{ display: 'block', fontWeight: '500' }}>Логотип ресторана</label>
              <div style={{ 
                display: 'flex', 
                alignItems: 'center', 
                gap: '0.5rem',
                padding: '0.25rem 0.75rem',
                borderRadius: '12px',
                backgroundColor: logoImageId ? '#e8f5e9' : '#fff3e0',
                color: logoImageId ? '#2e7d32' : '#e65100',
                fontSize: '0.875rem',
                fontWeight: '500'
              }}>
                <span style={{ 
                  width: '8px', 
                  height: '8px', 
                  borderRadius: '50%', 
                  backgroundColor: logoImageId ? '#4caf50' : '#ff9800',
                  display: 'inline-block'
                }}></span>
                {logoImageId ? 'Загружено' : 'Не загружено'}
              </div>
            </div>
            
            <ImageUpload
              currentImageId={logoImageId}
              onImageUploaded={(file: File) => handleImageUpload(file, 'logo')}
              onImageRemoved={() => handleImageRemove('logo')}
              type="logo"
              recommendedSize="200x200px - 500x500px"
              uploadToEntity={true}
            />
          </div>

          <div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
              <label style={{ display: 'block', fontWeight: '500' }}>Фоновое изображение</label>
              <div style={{ 
                display: 'flex', 
                alignItems: 'center', 
                gap: '0.5rem',
                padding: '0.25rem 0.75rem',
                borderRadius: '12px',
                backgroundColor: bgImageId ? '#e8f5e9' : '#fff3e0',
                color: bgImageId ? '#2e7d32' : '#e65100',
                fontSize: '0.875rem',
                fontWeight: '500'
              }}>
                <span style={{ 
                  width: '8px', 
                  height: '8px', 
                  borderRadius: '50%', 
                  backgroundColor: bgImageId ? '#4caf50' : '#ff9800',
                  display: 'inline-block'
                }}></span>
                {bgImageId ? 'Загружено' : 'Не загружено'}
              </div>
            </div>
            
            <ImageUpload
              currentImageId={bgImageId}
              onImageUploaded={(file: File) => handleImageUpload(file, 'background')}
              onImageRemoved={() => handleImageRemove('background')}
              type="background"
              recommendedSize="минимум 1920x1080px"
              uploadToEntity={true}
            />
          </div>
        </div>

        {role === 'ADMIN' && (
          <div style={{ backgroundColor: 'white', padding: '2rem', borderRadius: '8px', marginBottom: '2rem' }}>
            <h2 style={{ marginBottom: '1.5rem' }}>Управление подпиской</h2>
            <p>Управление подпиской доступно на странице подписки</p>
          </div>
        )}

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
      </form>
    </div>
  )
}

