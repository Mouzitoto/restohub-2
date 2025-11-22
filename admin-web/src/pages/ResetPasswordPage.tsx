import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { authService } from '../services/authService'

const resetPasswordSchema = z.object({
  email: z.string().email('Введите корректный email'),
  code: z.string().regex(/^\d{6}$/, 'Код должен состоять из 6 цифр'),
  newPassword: z.string().min(8, 'Пароль должен содержать минимум 8 символов'),
})

type ResetPasswordFormData = z.infer<typeof resetPasswordSchema>

export default function ResetPasswordPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)

  const emailFromQuery = searchParams.get('email') || ''

  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
  } = useForm<ResetPasswordFormData>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      email: emailFromQuery,
    },
  })

  useEffect(() => {
    if (emailFromQuery) {
      setValue('email', emailFromQuery)
    }
  }, [emailFromQuery, setValue])

  const onSubmit = async (data: ResetPasswordFormData) => {
    setIsLoading(true)
    setError(null)

    try {
      await authService.resetPassword(data)
      setSuccess(true)
      setTimeout(() => {
        navigate('/login')
      }, 2000)
    } catch (err: any) {
      const exceptionName = err.response?.data?.exceptionName
      let errorMessage = err.response?.data?.message || 'Ошибка сброса пароля'

      if (exceptionName === 'INVALID_RESET_CODE') {
        errorMessage = 'Неверный код восстановления'
      } else if (exceptionName === 'RESET_CODE_EXPIRED') {
        errorMessage = 'Код истек. Запросите новый код.'
      } else if (exceptionName === 'RESET_CODE_ALREADY_USED') {
        errorMessage = 'Код уже использован. Запросите новый код.'
      } else if (exceptionName === 'INVALID_PASSWORD') {
        errorMessage = 'Пароль должен содержать минимум 8 символов'
      }

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
          <p>Пароль успешно изменен</p>
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

          <div style={{ marginBottom: '1rem' }}>
            <label htmlFor="code" style={{ display: 'block', marginBottom: '0.5rem' }}>
              Код восстановления
            </label>
            <input
              id="code"
              type="text"
              maxLength={6}
              {...register('code')}
              style={{
                width: '100%',
                padding: '0.5rem',
                border: errors.code ? '1px solid red' : '1px solid #ccc',
                borderRadius: '4px',
              }}
            />
            {errors.code && (
              <div style={{ color: 'red', fontSize: '0.875rem', marginTop: '0.25rem' }}>
                {errors.code.message}
              </div>
            )}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label htmlFor="newPassword" style={{ display: 'block', marginBottom: '0.5rem' }}>
              Новый пароль
            </label>
            <input
              id="newPassword"
              type="password"
              {...register('newPassword')}
              style={{
                width: '100%',
                padding: '0.5rem',
                border: errors.newPassword ? '1px solid red' : '1px solid #ccc',
                borderRadius: '4px',
              }}
            />
            {errors.newPassword && (
              <div style={{ color: 'red', fontSize: '0.875rem', marginTop: '0.25rem' }}>
                {errors.newPassword.message}
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
            {isLoading ? 'Изменение...' : 'Изменить пароль'}
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

