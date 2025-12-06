import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { authService } from '../services/authService'
import { useApp } from '../context/AppContext'

const loginSchema = z.object({
  email: z.string().email('Введите корректный email'),
  password: z.string().min(1, 'Пароль обязателен'),
})

type LoginFormData = z.infer<typeof loginSchema>

export default function LoginPage() {
  const navigate = useNavigate()
  const { refreshUserInfo } = useApp()
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  })

  const onSubmit = async (data: LoginFormData) => {
    setIsLoading(true)
    setError(null)

    try {
      await authService.login(data)
      await refreshUserInfo()
      navigate('/dashboard')
    } catch (err: any) {
      const errorMessage =
        err.response?.data?.message || 'Неверный email или пароль'
      setError(errorMessage)
    } finally {
      setIsLoading(false)
    }
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
        <h1 style={{ textAlign: 'center', marginBottom: '2rem' }}>Вход в систему</h1>
        
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
            <label htmlFor="password" style={{ display: 'block', marginBottom: '0.5rem' }}>
              Пароль
            </label>
            <input
              id="password"
              type="password"
              {...register('password')}
              style={{
                width: '100%',
                padding: '0.5rem',
                border: errors.password ? '1px solid red' : '1px solid #ccc',
                borderRadius: '4px',
              }}
            />
            {errors.password && (
              <div style={{ color: 'red', fontSize: '0.875rem', marginTop: '0.25rem' }}>
                {errors.password.message}
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
            {isLoading ? 'Вход...' : 'Войти'}
          </button>
        </form>

        <div style={{ marginTop: '1rem', textAlign: 'center', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          <Link 
            to="/forgot-password" 
            style={{ color: '#007bff', textDecoration: 'none' }}
          >
            Забыли пароль?
          </Link>
          <div style={{ marginTop: '0.5rem' }}>
            <Link 
              to="/register" 
              style={{ color: '#007bff', textDecoration: 'none', fontWeight: '500' }}
            >
              Зарегистрироваться
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}

