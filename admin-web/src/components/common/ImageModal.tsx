import { useState, useEffect, useRef } from 'react'
import Modal from './Modal'
import { apiClient } from '../../services/apiClient'

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
  const blobUrlRef = useRef<string | null>(null)

  useEffect(() => {
    if (isOpen && imageId) {
      setIsLoading(true)
      setError(null)
      
      // Загружаем изображение через apiClient с авторизацией
      apiClient.instance
        .get(`/admin-api/image?id=${imageId}&isPreview=false`, {
          responseType: 'blob',
        })
        .then((response) => {
          const blob = response.data
          // Очищаем предыдущий blob URL
          if (blobUrlRef.current) {
            URL.revokeObjectURL(blobUrlRef.current)
          }
          const url = URL.createObjectURL(blob)
          blobUrlRef.current = url
          setImageUrl(url)
          setIsLoading(false)
        })
        .catch((err) => {
          console.error('Failed to load image:', err)
          setError('Не удалось загрузить изображение')
          setIsLoading(false)
        })
    } else {
      setImageUrl(null)
    }

    // Очищаем blob URL при размонтировании или изменении imageId
    return () => {
      if (blobUrlRef.current) {
        URL.revokeObjectURL(blobUrlRef.current)
        blobUrlRef.current = null
      }
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

