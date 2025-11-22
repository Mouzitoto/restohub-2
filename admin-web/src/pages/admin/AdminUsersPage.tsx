import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useApp } from '../../context/AppContext'
import { apiClient } from '../../services/apiClient'
import Modal from '../../components/common/Modal'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useToast } from '../../context/ToastContext'
import type { PaginationResponse, Restaurant } from '../../types'

interface UserListItem {
  id: number
  email: string
  roleId: number
  roleName: string
  isActive: boolean
  restaurants: Restaurant[]
  createdAt: string
}

const userSchema = z.object({
  email: z.string().email('Введите корректный email'),
  password: z.string().min(8, 'Пароль должен содержать минимум 8 символов').optional(),
  roleId: z.number().min(1, 'Выберите роль'),
  restaurantIds: z.array(z.number()).optional(),
})

type UserFormData = z.infer<typeof userSchema>

const resetPasswordSchema = z.object({
  password: z.string().min(8, 'Пароль должен содержать минимум 8 символов'),
  confirmPassword: z.string(),
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Пароли не совпадают',
  path: ['confirmPassword'],
})

type ResetPasswordFormData = z.infer<typeof resetPasswordSchema>

export default function AdminUsersPage() {
  const { role } = useApp()
  const navigate = useNavigate()
  const [users, setUsers] = useState<UserListItem[]>([])
  const [roles, setRoles] = useState<any[]>([])
  const [restaurants, setRestaurants] = useState<Restaurant[]>([])
  const [isUserModalOpen, setIsUserModalOpen] = useState(false)
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false)
  const [editingUser, setEditingUser] = useState<UserListItem | null>(null)
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const toast = useToast()

  const {
    register: registerUser,
    handleSubmit: handleSubmitUser,
    formState: { errors: userErrors },
    reset: resetUser,
    watch: watchUser,
  } = useForm<UserFormData>({
    resolver: zodResolver(userSchema),
  })

  const {
    register: registerPassword,
    handleSubmit: handleSubmitPassword,
    formState: { errors: passwordErrors },
    reset: resetPassword,
  } = useForm<ResetPasswordFormData>({
    resolver: zodResolver(resetPasswordSchema),
  })

  const selectedRoleId = watchUser('roleId')

  useEffect(() => {
    if (role !== 'ADMIN') {
      navigate('/dashboard')
      return
    }
    loadUsers()
    loadRoles()
    loadRestaurants()
  }, [role, navigate])

  const loadUsers = async () => {
    try {
      const response = await apiClient.instance.get<PaginationResponse<UserListItem[]>>(
        '/admin-api/user'
      )
      setUsers(response.data.data)
    } catch (error) {
      toast.error('Не удалось загрузить пользователей')
    }
  }

  const loadRoles = async () => {
    try {
      const response = await apiClient.instance.get('/admin-api/role')
      setRoles(response.data)
    } catch (error) {
      toast.error('Не удалось загрузить роли')
    }
  }

  const loadRestaurants = async () => {
    try {
      const response = await apiClient.instance.get<PaginationResponse<Restaurant[]>>('/admin-api/r')
      setRestaurants(response.data.data)
    } catch (error) {
      toast.error('Не удалось загрузить рестораны')
    }
  }

  const onSubmitUser = async (data: UserFormData) => {
    setIsLoading(true)
    try {
      const payload: any = { ...data }
      if (!data.password && editingUser) {
        delete payload.password
      }

      if (editingUser) {
        await apiClient.instance.put(`/admin-api/user/${editingUser.id}`, payload)
        toast.success('Пользователь обновлен')
      } else {
        await apiClient.instance.post('/admin-api/user', payload)
        toast.success('Пользователь создан')
      }
      setIsUserModalOpen(false)
      resetUser()
      setEditingUser(null)
      loadUsers()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка сохранения')
    } finally {
      setIsLoading(false)
    }
  }

  const onSubmitPassword = async (data: ResetPasswordFormData) => {
    if (!selectedUserId) return

    setIsLoading(true)
    try {
      await apiClient.instance.put(`/admin-api/user/${selectedUserId}/password`, {
        password: data.password
      })
      toast.success('Пароль успешно изменен')
      setIsPasswordModalOpen(false)
      resetPassword()
      setSelectedUserId(null)
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка сброса пароля')
    } finally {
      setIsLoading(false)
    }
  }

  const handleActivate = async (userId: number, isActive: boolean) => {
    if (!confirm(`Вы уверены, что хотите ${isActive ? 'деактивировать' : 'активировать'} пользователя?`)) return

    try {
      await apiClient.instance.put(`/admin-api/user/${userId}/activate`, { isActive: !isActive })
      toast.success(`Пользователь ${isActive ? 'деактивирован' : 'активирован'}`)
      loadUsers()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка изменения статуса')
    }
  }

  const handleDelete = async (userId: number) => {
    if (!confirm('Вы уверены, что хотите удалить пользователя? Это действие нельзя отменить.')) return

    try {
      await apiClient.instance.delete(`/admin-api/user/${userId}`)
      toast.success('Пользователь удален')
      loadUsers()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка удаления')
    }
  }

  if (role !== 'ADMIN') {
    return null
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>Управление пользователями</h1>
        <button
          onClick={() => {
            setEditingUser(null)
            resetUser()
            setIsUserModalOpen(true)
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
          Создать пользователя
        </button>
      </div>

      <div style={{ backgroundColor: 'white', borderRadius: '8px', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: '#f5f5f5' }}>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Email</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Роль</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Рестораны</th>
              <th style={{ padding: '1rem', textAlign: 'left' }}>Статус</th>
              <th style={{ padding: '1rem' }}>Действия</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id} style={{ borderTop: '1px solid #eee' }}>
                <td style={{ padding: '1rem' }}>{user.email}</td>
                <td style={{ padding: '1rem' }}>
                  <span
                    style={{
                      padding: '0.25rem 0.5rem',
                      borderRadius: '4px',
                      backgroundColor: user.roleId === 1 ? '#9c27b0' : '#2196f3',
                      color: 'white',
                      fontSize: '0.875rem',
                    }}
                  >
                    {user.roleName}
                  </span>
                </td>
                <td style={{ padding: '1rem' }}>
                  {user.restaurants?.map((r) => r.name).join(', ') || '-'}
                </td>
                <td style={{ padding: '1rem' }}>
                  <span
                    style={{
                      padding: '0.25rem 0.5rem',
                      borderRadius: '4px',
                      backgroundColor: user.isActive ? '#4caf50' : '#999',
                      color: 'white',
                      fontSize: '0.875rem',
                    }}
                  >
                    {user.isActive ? 'Активен' : 'Неактивен'}
                  </span>
                </td>
                <td style={{ padding: '1rem' }}>
                  <button
                    onClick={() => {
                      setEditingUser(user)
                      resetUser({
                        email: user.email,
                        roleId: user.roleId,
                        restaurantIds: user.restaurants?.map((r) => r.id) || [],
                      })
                      setIsUserModalOpen(true)
                    }}
                    style={{ marginRight: '0.5rem', padding: '0.25rem 0.5rem', cursor: 'pointer' }}
                  >
                    Редактировать
                  </button>
                  <button
                    onClick={() => {
                      setSelectedUserId(user.id)
                      resetPassword()
                      setIsPasswordModalOpen(true)
                    }}
                    style={{ marginRight: '0.5rem', padding: '0.25rem 0.5rem', cursor: 'pointer' }}
                  >
                    Сбросить пароль
                  </button>
                  <button
                    onClick={() => handleActivate(user.id, user.isActive)}
                    style={{ marginRight: '0.5rem', padding: '0.25rem 0.5rem', cursor: 'pointer' }}
                  >
                    {user.isActive ? 'Деактивировать' : 'Активировать'}
                  </button>
                  <button
                    onClick={() => handleDelete(user.id)}
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
        isOpen={isUserModalOpen}
        onClose={() => {
          setIsUserModalOpen(false)
          resetUser()
          setEditingUser(null)
        }}
        title={editingUser ? 'Редактировать пользователя' : 'Создать пользователя'}
        size="medium"
      >
        <form onSubmit={handleSubmitUser(onSubmitUser)}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Email *</label>
            <input {...registerUser('email')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
            {userErrors.email && <div style={{ color: 'red' }}>{userErrors.email.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Пароль {!editingUser && '*'}</label>
            <input
              type="password"
              {...registerUser('password')}
              style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
            />
            {userErrors.password && <div style={{ color: 'red' }}>{userErrors.password.message}</div>}
            {editingUser && <div style={{ fontSize: '0.875rem', color: '#666' }}>Оставьте пустым, чтобы не менять</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Роль *</label>
            <select {...registerUser('roleId', { valueAsNumber: true })} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}>
              <option value="">Выберите роль</option>
              {roles.map((role) => (
                <option key={role.id} value={role.id}>
                  {role.name}
                </option>
              ))}
            </select>
            {userErrors.roleId && <div style={{ color: 'red' }}>{userErrors.roleId.message}</div>}
          </div>

          {selectedRoleId === 2 && (
            <div style={{ marginBottom: '1rem' }}>
              <label>Привязанные рестораны</label>
              <select
                multiple
                {...registerUser('restaurantIds', { valueAsNumber: true })}
                style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem', minHeight: '100px' }}
              >
                {restaurants.map((restaurant) => (
                  <option key={restaurant.id} value={restaurant.id}>
                    {restaurant.name}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
            <button
              type="button"
              onClick={() => {
                setIsUserModalOpen(false)
                resetUser()
                setEditingUser(null)
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

      <Modal
        isOpen={isPasswordModalOpen}
        onClose={() => {
          setIsPasswordModalOpen(false)
          resetPassword()
          setSelectedUserId(null)
        }}
        title="Сброс пароля"
      >
        <form onSubmit={handleSubmitPassword(onSubmitPassword)}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Новый пароль *</label>
            <input
              type="password"
              {...registerPassword('password')}
              style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
            />
            {passwordErrors.password && <div style={{ color: 'red' }}>{passwordErrors.password.message}</div>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Подтверждение пароля *</label>
            <input
              type="password"
              {...registerPassword('confirmPassword')}
              style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
            />
            {passwordErrors.confirmPassword && <div style={{ color: 'red' }}>{passwordErrors.confirmPassword.message}</div>}
          </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
            <button
              type="button"
              onClick={() => {
                setIsPasswordModalOpen(false)
                resetPassword()
                setSelectedUserId(null)
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
              {isLoading ? 'Сброс...' : 'Сбросить'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

