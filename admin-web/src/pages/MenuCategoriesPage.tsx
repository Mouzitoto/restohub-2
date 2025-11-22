import { useEffect, useState } from 'react'
import { useApp } from '../context/AppContext'
import { apiClient } from '../services/apiClient'
import Modal from '../components/common/Modal'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useToast } from '../context/ToastContext'
import type { MenuCategory, PaginationResponse } from '../types'

const categorySchema = z.object({
  name: z.string().min(1, 'Название категории обязательно').max(255),
  description: z.string().max(10000).optional(),
  displayOrder: z.number().min(0).optional(),
})

type CategoryFormData = z.infer<typeof categorySchema>

export default function MenuCategoriesPage() {
  const { role } = useApp()
  const [categories, setCategories] = useState<MenuCategory[]>([])
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingCategory, setEditingCategory] = useState<MenuCategory | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const toast = useToast()

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<CategoryFormData>({
    resolver: zodResolver(categorySchema),
  })

  useEffect(() => {
    loadCategories()
  }, [])

  const loadCategories = async () => {
    try {
      const response = await apiClient.instance.get<PaginationResponse<MenuCategory[]>>(
        '/admin-api/menu-category?sortBy=displayOrder&sortOrder=asc'
      )
      setCategories(response.data.data)
    } catch (error) {
      toast.error('Не удалось загрузить категории')
    }
  }

  const onSubmit = async (data: CategoryFormData) => {
    setIsLoading(true)
    try {
      if (editingCategory) {
        await apiClient.instance.put(`/admin-api/menu-category/${editingCategory.id}`, data)
        toast.success('Категория обновлена')
      } else {
        await apiClient.instance.post('/admin-api/menu-category', data)
        toast.success('Категория создана')
      }
      setIsModalOpen(false)
      reset()
      setEditingCategory(null)
      loadCategories()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка сохранения')
    } finally {
      setIsLoading(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Вы уверены, что хотите удалить эту категорию?')) return

    try {
      await apiClient.instance.delete(`/admin-api/menu-category/${id}`)
      toast.success('Категория удалена')
      loadCategories()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Нельзя удалить категорию. В категории есть активные блюда.')
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>Категории меню</h1>
        {role === 'ADMIN' && (
          <button
            onClick={() => {
              setEditingCategory(null)
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
            Создать категорию
          </button>
        )}
      </div>

      <p style={{ color: '#666', marginBottom: '1rem' }}>
        Категории меню являются глобальными и используются всеми ресторанами
      </p>

      <div style={{ backgroundColor: 'white', borderRadius: '8px', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: '#f5f5f5' }}>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Название</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Описание</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Порядок</th>
              {role === 'ADMIN' && <th style={{ padding: '1rem' }}>Действия</th>}
            </tr>
          </thead>
          <tbody>
            {categories.map((category) => (
              <tr key={category.id} style={{ borderTop: '1px solid #eee' }}>
                <td style={{ padding: '1rem' }}>{category.name}</td>
                <td style={{ padding: '1rem', color: '#666' }}>
                  {category.description || '-'}
                </td>
                <td style={{ padding: '1rem' }}>{category.displayOrder}</td>
                {role === 'ADMIN' && (
                  <td style={{ padding: '1rem' }}>
                    <button
                      onClick={() => {
                        setEditingCategory(category)
                        reset({
                          name: category.name,
                          description: category.description || '',
                          displayOrder: category.displayOrder,
                        })
                        setIsModalOpen(true)
                      }}
                      style={{ marginRight: '0.5rem', padding: '0.25rem 0.5rem', cursor: 'pointer' }}
                    >
                      Редактировать
                    </button>
                    <button
                      onClick={() => handleDelete(category.id)}
                      style={{ padding: '0.25rem 0.5rem', cursor: 'pointer', color: '#f44336' }}
                    >
                      Удалить
                    </button>
                  </td>
                )}
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
          setEditingCategory(null)
        }}
        title={editingCategory ? 'Редактировать категорию' : 'Создать категорию'}
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
            <label>Порядок отображения</label>
            <input type="number" {...register('displayOrder', { valueAsNumber: true })} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
          </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
            <button
              type="button"
              onClick={() => {
                setIsModalOpen(false)
                reset()
                setEditingCategory(null)
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

