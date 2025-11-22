import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { authService } from '../services/authService'

const forgotPasswordSchema = z.object({
  email: z.string().email('Введите корректный email'),
})

type ForgotPasswordFormData = z.infer<typeof forgotPasswordSchema>

export default function ForgotPasswordPage() {
  const navigate = useNavigate()
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordFormData>({
    resolver: zodResolver(forgotPasswordSchema),
  })

  const onSubmit = async (data: ForgotPasswordFormData) => {
    setIsLoading(true)
    setError(null)

    try {
      await authService.forgotPassword(data)
      setSuccess(true)
      setTimeout(() => {
        navigate(`/reset-password?email=${encodeURIComponent(data.email)}`)
      }, 2000)
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Ошибка отправки кода'
      setError(errorMessage)
    } finally {
      setIsLoading(false)
    }
  }

  if (success) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        minHeight: '100vh',
        backgroundColor: '#f5f5f5'
      }}>
        <div style={{
          backgroundColor: 'white',
          padding: '2rem',
          borderRadius: '8px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
          width: '100%',
          maxWidth: '400px',
          textAlign: 'center'
        }}>
          <p>Код восстановления отправлен на email. Проверьте почту.</p>
        </div>
      </div>
    )
  }

  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      minHeight: '100vh',
      backgroundColor: '#f5f5f5'
    }}>
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '8px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
        width: '100%',
        maxWidth: '400px'
      }}>
        <h1 style={{ textAlign: 'center', marginBottom: '2rem' }}>Восстановление пароля</h1>
        
        <form onSubmit={handleSubmit(onSubmit)}>
          <div style={{ marginBottom: '1rem' }}>
            <label htmlFor="email" style={{ display: 'block', marginBottom: '0.5rem' }}>
              Email
            </label>
            <input
              id="email"
              type="email"
              {...register('email')}
              style={{
                width: '100%',
                padding: '0.5rem',
                border: errors.email ? '1px solid red' : '1px solid #ccc',
                borderRadius: '4px',
              }}
            />
            {errors.email && (
              <div style={{ color: 'red', fontSize: '0.875rem', marginTop: '0.25rem' }}>
                {errors.email.message}
              </div>
            )}
          </div>

          {error && (
            <div style={{ 
              color: 'red', 
              fontSize: '0.875rem', 
              marginBottom: '1rem',
              padding: '0.5rem',
              backgroundColor: '#fee',
              borderRadius: '4px'
            }}>
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={isLoading}
            style={{
              width: '100%',
              padding: '0.75rem',
              backgroundColor: isLoading ? '#ccc' : '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              fontSize: '1rem',
            }}
          >
            {isLoading ? 'Отправка...' : 'Отправить код'}
          </button>
        </form>

        <div style={{ marginTop: '1rem', textAlign: 'center' }}>
          <a 
            href="/login" 
            style={{ color: '#007bff', textDecoration: 'none' }}
          >
            Вернуться к входу
          </a>
        </div>
      </div>
    </div>
  )
}

