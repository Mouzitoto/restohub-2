import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useApp } from '../../context/AppContext'
import { apiClient } from '../../services/apiClient'
import Modal from '../../components/common/Modal'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useToast } from '../../context/ToastContext'
import InputMask from 'react-input-mask'
import type { Restaurant, PaginationResponse, UserInfo } from '../../types'

// Функция для преобразования телефона из маски в формат +7XXXXXXXXXX или 8XXXXXXXXXX
const normalizePhone = (value: string): string => {
  if (!value) return ''
  // Убираем все символы кроме цифр
  const digits = value.replace(/\D/g, '')
  // Если начинается с 7, добавляем +
  if (digits.startsWith('7') && digits.length === 11) {
    return `+${digits}`
  }
  // Если начинается с 8 и 11 цифр, оставляем как есть
  if (digits.startsWith('8') && digits.length === 11) {
    return digits
  }
  return value
}

// Функция для форматирования телефона в формат маски +7 (999) 123-45-67
const formatPhoneForMask = (value: string): string => {
  if (!value) return ''
  // Убираем все символы кроме цифр
  const digits = value.replace(/\D/g, '')
  // Если 11 цифр, форматируем
  if (digits.length === 11) {
    return `+7 (${digits.slice(1, 4)}) ${digits.slice(4, 7)}-${digits.slice(7, 9)}-${digits.slice(9, 11)}`
  }
  return value
}

const restaurantSchema = z.object({
  name: z.string().min(1, 'Название обязательно').max(255),
  address: z.string().min(1, 'Адрес обязателен').max(500),
  phone: z.string()
    .min(1, 'Телефон обязателен')
    .refine((val) => {
      const normalized = normalizePhone(val)
      return /^(\+7|8)\d{10}$/.test(normalized)
    }, { message: 'Введите корректный номер телефона (формат: +7 (999) 123-45-67)' }),
  whatsapp: z.string()
    .optional()
    .or(z.literal(''))
    .refine((val) => {
      if (!val || val === '') return true
      const normalized = normalizePhone(val)
      return /^(\+7|8)\d{10}$/.test(normalized)
    }, { message: 'Введите корректный номер телефона (формат: +7 (999) 123-45-67)' }),
  instagram: z.string().max(255).optional().or(z.literal('')),
  description: z.string().max(10000).optional().or(z.literal('')),
  latitude: z.number().min(-90).max(90).optional(),
  longitude: z.number().min(-180).max(180).optional(),
  workingHours: z.string().max(1000).optional().or(z.literal('')),
  managerLanguageCode: z.string().max(10).optional().refine(
    (val) => !val || val === '' || /^[a-z]{2}$/.test(val),
    { message: 'Код языка должен состоять из 2 букв в нижнем регистре' }
  ),
})

type RestaurantFormData = z.infer<typeof restaurantSchema>

export default function AdminRestaurantsPage() {
  const { role, setCurrentRestaurant, refreshRestaurants, refreshUserInfo } = useApp()
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
    control,
  } = useForm<RestaurantFormData>({
    resolver: zodResolver(restaurantSchema),
  })

  useEffect(() => {
    // ADMIN и MANAGER могут видеть и создавать рестораны
    if (role !== 'ADMIN' && role !== 'MANAGER') {
      navigate('/dashboard')
      return
    }
    loadRestaurants()
  }, [role, navigate])

  useEffect(() => {
    if (isModalOpen && editingRestaurant) {
      // Загружаем данные ресторана для редактирования
      apiClient.instance.get<Restaurant>(`/admin-api/r/${editingRestaurant.id}`)
        .then((response) => {
          const data = response.data
          reset({
            name: data.name,
            address: data.address || '',
            phone: data.phone ? formatPhoneForMask(data.phone) : '',
            whatsapp: data.whatsapp ? formatPhoneForMask(data.whatsapp) : '',
            instagram: data.instagram || '',
            description: data.description || '',
            latitude: data.latitude || undefined,
            longitude: data.longitude || undefined,
            workingHours: data.workingHours || '',
            managerLanguageCode: data.managerLanguageCode || '',
          })
        })
        .catch(() => {
          toast.error('Не удалось загрузить данные ресторана')
        })
    } else if (isModalOpen && !editingRestaurant) {
      // Сброс формы при создании нового ресторана
      reset()
    }
  }, [isModalOpen, editingRestaurant, reset, toast])

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
      // Нормализуем телефоны перед отправкой
      const normalizedData = {
        ...data,
        phone: normalizePhone(data.phone),
        whatsapp: data.whatsapp ? normalizePhone(data.whatsapp) : '',
      }
      
      if (editingRestaurant) {
        await apiClient.instance.put(`/admin-api/r/${editingRestaurant.id}`, normalizedData)
        toast.success('Ресторан обновлен')
      } else {
        const response = await apiClient.instance.post<Restaurant>('/admin-api/r', normalizedData)
        toast.success('Ресторан создан')
        
        // Если это менеджер и создан новый ресторан, обновляем список ресторанов и выбираем новый
        if (role === 'MANAGER') {
          // Обновляем список ресторанов в контексте
          await refreshRestaurants()
          // Находим новый ресторан в обновленном списке и выбираем его
          if (response.data) {
            const updatedRestaurants = await apiClient.instance.get<PaginationResponse<Restaurant[]>>('/admin-api/r')
            const newRestaurant = updatedRestaurants.data.data.find(r => r.id === response.data.id)
            if (newRestaurant) {
              setCurrentRestaurant(newRestaurant)
            }
          }
        }
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


  // ADMIN и MANAGER могут видеть и создавать рестораны
  if (role !== 'ADMIN' && role !== 'MANAGER') {
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
                    onClick={async () => {
                      // Обновляем данные ресторанов из API, чтобы получить актуальную информацию
                      await refreshUserInfo()
                      // Получаем обновленные данные ресторанов
                      const response = await apiClient.instance.get<UserInfo>('/admin-api/auth/me')
                      const updatedRestaurant = response.data.restaurants?.find((r: Restaurant) => r.id === restaurant.id)
                      if (updatedRestaurant) {
                        // Выбираем ресторан (как будто выбрали в дропдауне)
                        setCurrentRestaurant(updatedRestaurant)
                        // Переходим на страницу "Мой ресторан"
                        navigate('/restaurant')
                      } else {
                        toast.error('Не удалось загрузить информацию о ресторане')
                      }
                    }}
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
                    Переключиться
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
            <label>Адрес *</label>
            <input {...register('address')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {errors.address && <div style={{ color: 'red' }}>{errors.address.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Телефон *</label>
              <Controller
                name="phone"
                control={control}
                render={({ field }) => (
                  <InputMask
                    mask="+7 (999) 999-99-99"
                    maskChar={null}
                    value={field.value || ''}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                      const value = e.target.value
                      field.onChange(value)
                    }}
                    placeholder="+7 (999) 123-45-67"
                    style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
                  />
                )}
              />
            {errors.phone && <div style={{ color: 'red' }}>{errors.phone.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>WhatsApp</label>
              <Controller
                name="whatsapp"
                control={control}
                render={({ field }) => (
                  <InputMask
                    mask="+7 (999) 999-99-99"
                    maskChar={null}
                    value={field.value || ''}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                      const value = e.target.value
                      field.onChange(value)
                    }}
                    placeholder="+7 (999) 123-45-67"
                    style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
                  />
                )}
              />
            {errors.whatsapp && <div style={{ color: 'red' }}>{errors.whatsapp.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Instagram</label>
            <input {...register('instagram')} placeholder="restaurant_name" style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {errors.instagram && <div style={{ color: 'red' }}>{errors.instagram.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Описание</label>
            <textarea {...register('description')} rows={4} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem', resize: 'vertical' }} />
            {errors.description && <div style={{ color: 'red' }}>{errors.description.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Рабочие часы</label>
            <input {...register('workingHours')} placeholder="Пн-Пт: 10:00-22:00" style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {errors.workingHours && <div style={{ color: 'red' }}>{errors.workingHours.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Код языка менеджера</label>
            <input {...register('managerLanguageCode')} placeholder="ru" style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {errors.managerLanguageCode && <div style={{ color: 'red' }}>{errors.managerLanguageCode.message}</div>}
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

