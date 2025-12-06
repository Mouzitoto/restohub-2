import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authService } from '../services/authService'
import TermsModal from '../components/TermsModal'
import './RegisterPage.css'

type Step = 1 | 2 | 3

export default function RegisterPage() {
  const navigate = useNavigate()
  const [step, setStep] = useState<Step>(1)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [agreeToTerms, setAgreeToTerms] = useState(false)
  const [code, setCode] = useState('')
  
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isTermsModalOpen, setIsTermsModalOpen] = useState(false)
  const [resendTimer, setResendTimer] = useState(0)

  const validateStep1 = (): boolean => {
    const newErrors: Record<string, string> = {}

    if (!email) {
      newErrors.email = 'Email обязателен'
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      newErrors.email = 'Неверный формат email'
    }

    if (!password) {
      newErrors.password = 'Пароль обязателен'
    } else if (password.length < 8) {
      newErrors.password = 'Пароль должен содержать минимум 8 символов'
    }

    if (!confirmPassword) {
      newErrors.confirmPassword = 'Подтверждение пароля обязательно'
    } else if (password !== confirmPassword) {
      newErrors.confirmPassword = 'Пароли не совпадают'
    }

    if (!agreeToTerms) {
      newErrors.agreeToTerms = 'Необходимо согласие с офертой'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const validateStep2 = (): boolean => {
    const newErrors: Record<string, string> = {}

    if (!code) {
      newErrors.code = 'Код обязателен'
    } else if (!/^\d{4}$/.test(code)) {
      newErrors.code = 'Код должен состоять из 4 цифр'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleStep1Submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErrorMessage(null)

    if (!validateStep1()) {
      return
    }

    setIsLoading(true)
    try {
      await authService.registerPartner({
        email,
        password,
        confirmPassword,
        agreeToTerms,
      })
      setStep(2)
      startResendTimer()
    } catch (err: any) {
      setErrorMessage(err.response?.data?.message || err.message || 'Ошибка при регистрации')
    } finally {
      setIsLoading(false)
    }
  }

  const handleStep2Submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErrorMessage(null)

    if (!validateStep2()) {
      return
    }

    setIsLoading(true)
    try {
      await authService.verifyEmail({ email, code })
      setStep(3)
    } catch (err: any) {
      setErrorMessage(err.response?.data?.message || err.message || 'Ошибка при подтверждении email')
    } finally {
      setIsLoading(false)
    }
  }

  const handleResendCode = async () => {
    if (resendTimer > 0) return

    setIsLoading(true)
    setErrorMessage(null)
    try {
      await authService.resendVerificationCode({ email })
      startResendTimer()
    } catch (err: any) {
      setErrorMessage(err.response?.data?.message || err.message || 'Ошибка при отправке кода')
    } finally {
      setIsLoading(false)
    }
  }

  const startResendTimer = () => {
    setResendTimer(60)
    const interval = setInterval(() => {
      setResendTimer((prev) => {
        if (prev <= 1) {
          clearInterval(interval)
          return 0
        }
        return prev - 1
      })
    }, 1000)
  }

  return (
    <div className="register-page">
      <div className="register-container">
        <div className="registration-progress">
          <div className={`progress-step ${step >= 1 ? 'active' : ''}`}>
            <div className="step-number">1</div>
            <div className="step-label">Данные</div>
          </div>
          <div className={`progress-line ${step >= 2 ? 'active' : ''}`}></div>
          <div className={`progress-step ${step >= 2 ? 'active' : ''}`}>
            <div className="step-number">2</div>
            <div className="step-label">Подтверждение</div>
          </div>
          <div className={`progress-line ${step >= 3 ? 'active' : ''}`}></div>
          <div className={`progress-step ${step >= 3 ? 'active' : ''}`}>
            <div className="step-number">3</div>
            <div className="step-label">Готово</div>
          </div>
        </div>

        {step === 1 && (
          <form onSubmit={handleStep1Submit} className="registration-step">
            <h2>Регистрация партнера</h2>
            
            {errorMessage && (
              <div className="error-message">{errorMessage}</div>
            )}

            <div className="form-group">
              <label htmlFor="email">Email *</label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className={errors.email ? 'error' : ''}
                disabled={isLoading}
                style={{
                  width: '100%',
                  padding: '0.5rem',
                  border: errors.email ? '1px solid red' : '1px solid #ccc',
                  borderRadius: '4px',
                }}
              />
              {errors.email && <div className="field-error">{errors.email}</div>}
            </div>

            <div className="form-group">
              <label htmlFor="password">Пароль *</label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className={errors.password ? 'error' : ''}
                disabled={isLoading}
                style={{
                  width: '100%',
                  padding: '0.5rem',
                  border: errors.password ? '1px solid red' : '1px solid #ccc',
                  borderRadius: '4px',
                }}
              />
              {errors.password && <div className="field-error">{errors.password}</div>}
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword">Подтверждение пароля *</label>
              <input
                id="confirmPassword"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className={errors.confirmPassword ? 'error' : ''}
                disabled={isLoading}
                style={{
                  width: '100%',
                  padding: '0.5rem',
                  border: errors.confirmPassword ? '1px solid red' : '1px solid #ccc',
                  borderRadius: '4px',
                }}
              />
              {errors.confirmPassword && (
                <div className="field-error">{errors.confirmPassword}</div>
              )}
            </div>

            <div className="form-group">
              <label className="checkbox-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <input
                  type="checkbox"
                  checked={agreeToTerms}
                  onChange={(e) => setAgreeToTerms(e.target.checked)}
                  disabled={isLoading}
                />
                <span>
                  Я согласен с{' '}
                  <button
                    type="button"
                    onClick={() => setIsTermsModalOpen(true)}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: '#007bff',
                      cursor: 'pointer',
                      textDecoration: 'underline',
                    }}
                  >
                    публичной офертой
                  </button>
                  {' '}*
                </span>
              </label>
              {errors.agreeToTerms && (
                <div className="field-error">{errors.agreeToTerms}</div>
              )}
            </div>

            <div className="form-actions" style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
              <button 
                type="button" 
                onClick={() => navigate('/login')} 
                disabled={isLoading}
                style={{
                  padding: '0.75rem 1.5rem',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: isLoading ? 'not-allowed' : 'pointer',
                }}
              >
                Отменить
              </button>
              <button 
                type="submit" 
                disabled={isLoading}
                style={{
                  padding: '0.75rem 1.5rem',
                  backgroundColor: isLoading ? '#ccc' : '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: isLoading ? 'not-allowed' : 'pointer',
                }}
              >
                {isLoading ? 'Отправка...' : 'Далее'}
              </button>
            </div>
          </form>
        )}

        {step === 2 && (
          <form onSubmit={handleStep2Submit} className="registration-step">
            <h2>Подтверждение email</h2>
            
            <p style={{ marginBottom: '1rem' }}>
              На вашу почту <strong>{email}</strong> отправлено письмо с кодом подтверждения.
            </p>
            <p style={{ marginBottom: '1rem' }}>Введите код из письма:</p>

            {errorMessage && (
              <div className="error-message">{errorMessage}</div>
            )}

            <div className="form-group">
              <label htmlFor="code">Код подтверждения *</label>
              <input
                id="code"
                type="text"
                value={code}
                onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 4))}
                className={errors.code ? 'error' : ''}
                placeholder="1234"
                maxLength={4}
                disabled={isLoading}
                style={{
                  width: '100%',
                  padding: '0.5rem',
                  border: errors.code ? '1px solid red' : '1px solid #ccc',
                  borderRadius: '4px',
                }}
              />
              {errors.code && <div className="field-error">{errors.code}</div>}
            </div>

            <div className="form-actions" style={{ display: 'flex', gap: '1rem', justifyContent: 'space-between' }}>
              <button 
                type="button" 
                onClick={() => setStep(1)} 
                disabled={isLoading}
                style={{
                  padding: '0.75rem 1.5rem',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: isLoading ? 'not-allowed' : 'pointer',
                }}
              >
                Назад
              </button>
              <div style={{ display: 'flex', gap: '1rem' }}>
                <button
                  type="button"
                  onClick={handleResendCode}
                  disabled={isLoading || resendTimer > 0}
                  style={{
                    padding: '0.75rem 1.5rem',
                    backgroundColor: resendTimer > 0 ? '#ccc' : '#6c757d',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: (isLoading || resendTimer > 0) ? 'not-allowed' : 'pointer',
                  }}
                >
                  {resendTimer > 0
                    ? `Отправить повторно (${resendTimer}с)`
                    : 'Отправить код повторно'}
                </button>
                <button 
                  type="submit" 
                  disabled={isLoading}
                  style={{
                    padding: '0.75rem 1.5rem',
                    backgroundColor: isLoading ? '#ccc' : '#007bff',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: isLoading ? 'not-allowed' : 'pointer',
                  }}
                >
                  {isLoading ? 'Проверка...' : 'Далее'}
                </button>
              </div>
            </div>
          </form>
        )}

        {step === 3 && (
          <div className="registration-step success-step">
            <div style={{ fontSize: '4rem', color: '#28a745', marginBottom: '1rem' }}>✓</div>
            <h2>Регистрация завершена!</h2>
            <p>Вы успешно зарегистрировались в системе Resto-Hub</p>
            <button 
              onClick={() => navigate('/login')} 
              style={{
                marginTop: '1rem',
                padding: '0.75rem 1.5rem',
                backgroundColor: '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '1rem',
              }}
            >
              Перейти на страницу входа
            </button>
          </div>
        )}
      </div>

      <TermsModal
        isOpen={isTermsModalOpen}
        onClose={() => setIsTermsModalOpen(false)}
      />
    </div>
  )
}

