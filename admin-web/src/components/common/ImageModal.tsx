import { useState, useEffect } from 'react'
import Modal from './Modal'

interface ImageModalProps {
  imageId: number | null
  isOpen: boolean
  onClose: () => void
  alt?: string
}

export default function ImageModal({ imageId, isOpen, onClose, alt = 'Image' }: ImageModalProps) {
  const [imageUrl, setImageUrl] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (isOpen && imageId) {
      setIsLoading(true)
      setError(null)
      const url = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8082'}/admin-api/image?id=${imageId}&isPreview=false`
      setImageUrl(url)
      setIsLoading(false)
    } else {
      setImageUrl(null)
    }
  }, [isOpen, imageId])

  if (!isOpen || !imageId) return null

  return (
    <Modal isOpen={isOpen} onClose={onClose} size="large">
      <div style={{ textAlign: 'center' }}>
        {isLoading && <div>Загрузка...</div>}
        {error && <div style={{ color: 'red' }}>{error}</div>}
        {imageUrl && !isLoading && (
          <img
            src={imageUrl}
            alt={alt}
            style={{
              maxWidth: '100%',
              maxHeight: '80vh',
              objectFit: 'contain',
            }}
            onError={() => {
              setError('Не удалось загрузить изображение')
              setIsLoading(false)
            }}
            onLoad={() => setIsLoading(false)}
          />
        )}
      </div>
    </Modal>
  )
}

