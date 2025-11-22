import { useEffect, useState } from 'react'
import { useApp } from '../../context/AppContext'
import { apiClient } from '../../services/apiClient'
import { useForm } from 'react-hook-form'
import { useToast } from '../../context/ToastContext'

export default function AdminSettingsPage() {
  const { role } = useApp()
  const [isLoading, setIsLoading] = useState(false)
  const toast = useToast()

  const { register, handleSubmit, reset } = useForm()

  useEffect(() => {
    if (role === 'ADMIN') {
      loadSettings()
    }
  }, [role])

  const loadSettings = async () => {
    try {
      const response = await apiClient.instance.get('/admin-api/settings')
      reset(response.data)
    } catch (error) {
      console.error('Failed to load settings:', error)
    }
  }

  const onSubmit = async (data: any) => {
    setIsLoading(true)
    try {
      await apiClient.instance.put('/admin-api/settings', data)
      toast.success('Настройки сохранены')
      loadSettings()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Ошибка сохранения')
    } finally {
      setIsLoading(false)
    }
  }

  if (role !== 'ADMIN') {
    return <div>Доступ запрещен</div>
  }

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Системные настройки</h1>

      <form onSubmit={handleSubmit(onSubmit)} style={{ backgroundColor: 'white', padding: '2rem', borderRadius: '8px' }}>
        <h2 style={{ marginBottom: '1.5rem' }}>Общие настройки</h2>

        <div style={{ marginBottom: '1rem' }}>
          <label>Название системы</label>
          <input {...register('systemName')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
        </div>

        <h2 style={{ marginTop: '2rem', marginBottom: '1.5rem' }}>Интеграции</h2>

        <div style={{ marginBottom: '1rem' }}>
          <label>WhatsApp бот - номер</label>
          <input {...register('whatsappNumber')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label>WhatsApp бот - токен</label>
          <input type="password" {...register('whatsappToken')} style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }} />
        </div>

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
            marginTop: '1rem',
          }}
        >
          {isLoading ? 'Сохранение...' : 'Сохранить'}
        </button>
      </form>
    </div>
  )
}

