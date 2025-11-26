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
import type { MenuItem, MenuCategory, PaginationResponse } from '../types'

const menuItemSchema = z.object({
  name: z.string().min(1, 'Название блюда обязательно').max(255),
  description: z.string().max(10000).optional(),
  ingredients: z.string().max(10000).optional(),
  price: z.number().min(0.01, 'Цена должна быть больше 0'),
  menuCategoryId: z.number().min(1, 'Выберите категорию'),
  discountPercent: z.number().min(0).max(100).optional(),
  spicinessLevel: z.number().min(0).max(5).optional(),
  hasSugar: z.boolean().optional(),
  displayOrder: z.number().min(0).optional(),
})

type MenuItemFormData = z.infer<typeof menuItemSchema>

export default function MenuItemsPage() {
  const { currentRestaurant } = useApp()
  const [items, setItems] = useState<MenuItem[]>([])
  const [categories, setCategories] = useState<MenuCategory[]>([])
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<MenuItem | null>(null)
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
  } = useForm<MenuItemFormData>({
    resolver: zodResolver(menuItemSchema),
    defaultValues: {
      discountPercent: 0,
      spicinessLevel: 0,
      hasSugar: false,
    },
  })

  const price = watch('price') || 0
  const discountPercent = watch('discountPercent') || 0
  const finalPrice = price * (1 - discountPercent / 100)

  useEffect(() => {
    if (currentRestaurant) {
      loadCategories()
      loadItems()
    }
  }, [currentRestaurant])

  const loadCategories = async () => {
    try {
      const response = await apiClient.instance.get<PaginationResponse<MenuCategory[]>>(
        '/admin-api/menu-category'
      )
      setCategories(response.data.data)
    } catch (error) {
      toast.error('Не удалось загрузить категории')
    }
  }

  const loadItems = async () => {
    if (!currentRestaurant) return

    try {
      const response = await apiClient.instance.get<PaginationResponse<MenuItem[]>>(
        `/admin-api/r/${currentRestaurant.id}/menu-item?sortBy=displayOrder&sortOrder=asc`
      )
      setItems(response.data.data)
    } catch (error) {
      toast.error('Не удалось загрузить блюда')
    }
  }

  const handleImageUpload = async (file: File) => {
    if (!currentRestaurant || !editingItem) return

    setIsLoading(true)
    try {
      const formData = new FormData()
      formData.append('file', file)

      const response = await apiClient.instance.post<MenuItem>(
        `/admin-api/r/${currentRestaurant.id}/menu-item/${editingItem.id}/image`,
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
    if (!currentRestaurant || !editingItem) return

    setIsLoading(true)
    try {
      const response = await apiClient.instance.delete<MenuItem>(
        `/admin-api/r/${currentRestaurant.id}/menu-item/${editingItem.id}/image`
      )

      setImageId(response.data.imageId ?? null)
      toast.success('Изображение удалено')
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка удаления изображения')
    } finally {
      setIsLoading(false)
    }
  }

  const onSubmit = async (data: MenuItemFormData) => {
    if (!currentRestaurant) return

    setIsLoading(true)
    try {
      // Создаем или обновляем сущность без изображения
      let itemId: number
      if (editingItem) {
        await apiClient.instance.put(`/admin-api/r/${currentRestaurant.id}/menu-item/${editingItem.id}`, data)
        itemId = editingItem.id
        toast.success('Блюдо обновлено')
      } else {
        const response = await apiClient.instance.post<MenuItem>(
          `/admin-api/r/${currentRestaurant.id}/menu-item`,
          data
        )
        itemId = response.data.id
        toast.success('Блюдо создано')
      }

      // Если есть файл изображения, загружаем его
      if (imageFile) {
        const formData = new FormData()
        formData.append('file', imageFile)

        await apiClient.instance.post(
          `/admin-api/r/${currentRestaurant.id}/menu-item/${itemId}/image`,
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
      setEditingItem(null)
      setImageId(null)
      setImageFile(null)
      loadItems()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка сохранения')
    } finally {
      setIsLoading(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!currentRestaurant) return
    if (!confirm('Вы уверены, что хотите удалить это блюдо?')) return

    try {
      await apiClient.instance.delete(`/admin-api/r/${currentRestaurant.id}/menu-item/${id}`)
      toast.success('Блюдо удалено')
      loadItems()
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
        <h1>Блюда меню</h1>
        <button
        onClick={() => {
          setEditingItem(null)
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
          Добавить блюдо
        </button>
      </div>

      <div style={{ backgroundColor: 'white', borderRadius: '8px', overflow: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '1200px' }}>
          <thead>
            <tr style={{ backgroundColor: '#f5f5f5' }}>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Фото</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Название</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Описание</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Ингредиенты</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Цена</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Скидка</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Категория</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Острота</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Сахар</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Порядок</th>
              <th style={{ padding: '1rem' }}>Действия</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => {
              const finalPrice = item.price * (1 - item.discountPercent / 100)
              return (
                <tr key={item.id} style={{ borderTop: '1px solid #eee' }}>
                  <td style={{ padding: '1rem' }}>
                    <ImagePreview imageId={item.imageId ?? null} size="small" />
                  </td>
                  <td style={{ padding: '1rem' }}>{item.name}</td>
                  <td style={{ padding: '1rem', maxWidth: '200px' }}>
                    <div 
                      style={{ 
                        overflow: 'hidden', 
                        textOverflow: 'ellipsis', 
                        whiteSpace: 'nowrap',
                      }}
                      title={item.description || ''}
                    >
                      {item.description || '-'}
                    </div>
                  </td>
                  <td style={{ padding: '1rem', maxWidth: '200px' }}>
                    <div 
                      style={{ 
                        overflow: 'hidden', 
                        textOverflow: 'ellipsis', 
                        whiteSpace: 'nowrap',
                      }}
                      title={item.ingredients || ''}
                    >
                      {item.ingredients || '-'}
                    </div>
                  </td>
                  <td style={{ padding: '1rem' }}>
                    {item.discountPercent > 0 ? (
                      <div>
                        <span style={{ textDecoration: 'line-through', color: '#999' }}>
                          {item.price.toFixed(2)} ₽
                        </span>
                        <span style={{ color: '#f44336', marginLeft: '0.5rem', fontWeight: 'bold' }}>
                          {finalPrice.toFixed(2)} ₽
                        </span>
                      </div>
                    ) : (
                      <span>{item.price.toFixed(2)} ₽</span>
                    )}
                  </td>
                  <td style={{ padding: '1rem' }}>
                    {item.discountPercent > 0 ? (
                      <span style={{ color: '#f44336', fontWeight: 'bold' }}>
                        {item.discountPercent}%
                      </span>
                    ) : (
                      <span>-</span>
                    )}
                  </td>
                  <td style={{ padding: '1rem' }}>
                    {categories.find((c) => c.id === item.menuCategoryId)?.name || '-'}
                  </td>
                  <td style={{ padding: '1rem' }}>
                    {item.spicinessLevel > 0 ? (
                      <span>{item.spicinessLevel}/5</span>
                    ) : (
                      <span>-</span>
                    )}
                  </td>
                  <td style={{ padding: '1rem' }}>
                    {item.hasSugar ? 'Да' : 'Нет'}
                  </td>
                  <td style={{ padding: '1rem' }}>
                    {item.displayOrder}
                  </td>
                  <td style={{ padding: '1rem' }}>
                    <button
                      onClick={() => {
                        setEditingItem(item)
                        setImageId(item.imageId || null)
                        setImageFile(null)
                        reset({
                          name: item.name,
                          description: item.description || '',
                          ingredients: item.ingredients || '',
                          price: item.price,
                          menuCategoryId: item.menuCategoryId,
                          discountPercent: item.discountPercent,
                          spicinessLevel: item.spicinessLevel,
                          hasSugar: item.hasSugar,
                          displayOrder: item.displayOrder,
                        })
                        setIsModalOpen(true)
                      }}
                      style={{ marginRight: '0.5rem', padding: '0.25rem 0.5rem', cursor: 'pointer' }}
                    >
                      Редактировать
                    </button>
                    <button
                      onClick={() => handleDelete(item.id)}
                      style={{ padding: '0.25rem 0.5rem', cursor: 'pointer', color: '#f44336' }}
                    >
                      Удалить
                    </button>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>

      <Modal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false)
          reset()
          setEditingItem(null)
          setImageId(null)
          setImageFile(null)
        }}
        title={editingItem ? 'Редактировать блюдо' : 'Добавить блюдо'}
        size="large"
      >
        <form onSubmit={handleSubmit(onSubmit)}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Название *</label>
            <input {...register('name')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {errors.name && <div style={{ color: 'red' }}>{errors.name.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Описание</label>
            <textarea {...register('description')} rows={3} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Ингредиенты</label>
            <textarea {...register('ingredients')} rows={3} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Категория *</label>
            <select {...register('menuCategoryId', { valueAsNumber: true })} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}>
              <option value="">Выберите категорию</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.name}
                </option>
              ))}
            </select>
            {errors.menuCategoryId && <div style={{ color: 'red' }}>{errors.menuCategoryId.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Цена *</label>
            <input type="number" step="0.01" {...register('price', { valueAsNumber: true })} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {errors.price && <div style={{ color: 'red' }}>{errors.price.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Процент скидки</label>
            <input type="number" min="0" max="100" {...register('discountPercent', { valueAsNumber: true })} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {discountPercent > 0 && (
              <div style={{ marginTop: '0.5rem' }}>
                Финальная цена: <strong>{finalPrice.toFixed(2)} ₽</strong>
              </div>
            )}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Уровень остроты (0-5)</label>
            <input type="number" min="0" max="5" {...register('spicinessLevel', { valueAsNumber: true })} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>
              <input type="checkbox" {...register('hasSugar')} style={{ marginRight: '0.5rem' }} />
              Содержит сахар
            </label>
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Изображение</label>
            <ImageUpload
              currentImageId={imageId}
              onImageUploaded={editingItem ? handleImageUpload : (file: File) => setImageFile(file)}
              onImageRemoved={editingItem ? handleImageRemove : () => {
                setImageId(null)
                setImageFile(null)
              }}
              type="dish"
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
                setEditingItem(null)
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

