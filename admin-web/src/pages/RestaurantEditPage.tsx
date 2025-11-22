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
      await apiClient.instance.put(`/admin-api/r/${currentRestaurant.id}`, {
        ...data,
        logoImageId,
        bgImageId,
      })
      toast.success('Настройки сохранены')
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
            <label style={{ display: 'block', marginBottom: '0.5rem' }}>Логотип ресторана</label>
            <ImageUpload
              currentImageId={logoImageId}
              onImageUploaded={setLogoImageId}
              onImageRemoved={() => setLogoImageId(null)}
              type="logo"
              recommendedSize="200x200px - 500x500px"
            />
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem' }}>Фоновое изображение</label>
            <ImageUpload
              currentImageId={bgImageId}
              onImageUploaded={setBgImageId}
              onImageRemoved={() => setBgImageId(null)}
              type="background"
              recommendedSize="минимум 1920x1080px"
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

