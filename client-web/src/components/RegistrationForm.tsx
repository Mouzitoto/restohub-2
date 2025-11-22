import { useState } from 'react'
import { partnerApi } from '../services/partnerApi'
import TermsModal from './TermsModal'
import './RegistrationForm.css'

interface RegistrationFormProps {
  onSuccess?: () => void
  onCancel: () => void
}

type Step = 1 | 2 | 3

export default function RegistrationForm({ onSuccess, onCancel }: RegistrationFormProps) {
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
      await partnerApi.registerPartner({
        email,
        password,
        confirmPassword,
        agreeToTerms,
      })
      setStep(2)
      startResendTimer()
    } catch (err: any) {
      setErrorMessage(err.message || 'Ошибка при регистрации')
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
      await partnerApi.verifyEmail({ email, code })
      setStep(3)
      if (onSuccess) {
        onSuccess()
      }
    } catch (err: any) {
      setErrorMessage(err.message || 'Ошибка при подтверждении email')
    } finally {
      setIsLoading(false)
    }
  }

  const handleResendCode = async () => {
    if (resendTimer > 0) return

    setIsLoading(true)
    setErrorMessage(null)
    try {
      await partnerApi.resendVerificationCode({ email })
      startResendTimer()
    } catch (err: any) {
      setErrorMessage(err.message || 'Ошибка при отправке кода')
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

  const handleGoToLogin = () => {
    window.location.href = 'http://localhost:3001/login'
  }

  return (
    <>
      <div className="registration-form">
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
              />
              {errors.confirmPassword && (
                <div className="field-error">{errors.confirmPassword}</div>
              )}
            </div>

            <div className="form-group">
              <label className="checkbox-label">
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
                    className="link-button"
                    onClick={() => setIsTermsModalOpen(true)}
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

            <div className="form-actions">
              <button type="button" onClick={onCancel} disabled={isLoading}>
                Отменить
              </button>
              <button type="submit" disabled={isLoading}>
                {isLoading ? 'Отправка...' : 'Далее'}
              </button>
            </div>
          </form>
        )}

        {step === 2 && (
          <form onSubmit={handleStep2Submit} className="registration-step">
            <h2>Подтверждение email</h2>
            
            <p className="info-text">
              На вашу почту <strong>{email}</strong> отправлено письмо с кодом подтверждения.
            </p>
            <p className="info-text">Введите код из письма:</p>

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
              />
              {errors.code && <div className="field-error">{errors.code}</div>}
              <div className="hint-text">
                <strong>Заглушка:</strong> Используйте код <strong>1234</strong>
              </div>
            </div>

            <div className="form-actions">
              <button type="button" onClick={() => setStep(1)} disabled={isLoading}>
                Назад
              </button>
              <button
                type="button"
                onClick={handleResendCode}
                disabled={isLoading || resendTimer > 0}
                className="resend-button"
              >
                {resendTimer > 0
                  ? `Отправить повторно (${resendTimer}с)`
                  : 'Отправить код повторно'}
              </button>
              <button type="submit" disabled={isLoading}>
                {isLoading ? 'Проверка...' : 'Далее'}
              </button>
            </div>
          </form>
        )}

        {step === 3 && (
          <div className="registration-step success-step">
            <div className="success-icon">✓</div>
            <h2>Регистрация завершена!</h2>
            <p>Вы успешно зарегистрировались в системе Resto-Hub</p>
            <button onClick={handleGoToLogin} className="success-button">
              Перейти на страницу входа
            </button>
          </div>
        )}
      </div>

      <TermsModal
        isOpen={isTermsModalOpen}
        onClose={() => setIsTermsModalOpen(false)}
      />
    </>
  )
}

