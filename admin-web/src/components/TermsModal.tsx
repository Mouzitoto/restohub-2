import { useEffect, useState } from 'react'
import { authService } from '../services/authService'
import './TermsModal.css'

interface TermsModalProps {
  isOpen: boolean
  onClose: () => void
}

export default function TermsModal({ isOpen, onClose }: TermsModalProps) {
  const [terms, setTerms] = useState<string>('')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (isOpen) {
      loadTerms()
    }
  }, [isOpen])

  const loadTerms = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await authService.getTerms()
      setTerms(response.terms)
    } catch (err) {
      setError('Не удалось загрузить текст оферты')
      console.error('Failed to load terms:', err)
    } finally {
      setIsLoading(false)
    }
  }

  if (!isOpen) return null

  return (
    <div className="terms-modal-overlay" onClick={onClose}>
      <div className="terms-modal" onClick={(e) => e.stopPropagation()}>
        <div className="terms-modal-header">
          <h2>Публичная оферта</h2>
          <button className="terms-modal-close" onClick={onClose}>
            ×
          </button>
        </div>
        <div className="terms-modal-content">
          {isLoading && <div className="terms-loading">Загрузка...</div>}
          {error && <div className="terms-error">{error}</div>}
          {!isLoading && !error && (
            <div className="terms-text">
              {terms || 'Текст оферты загружается...'}
            </div>
          )}
        </div>
        <div className="terms-modal-footer">
          <button className="terms-modal-button" onClick={onClose}>
            Закрыть
          </button>
        </div>
      </div>
    </div>
  )
}

