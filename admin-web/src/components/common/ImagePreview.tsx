import { useState, useEffect } from 'react'
import ImageModal from './ImageModal'

interface ImagePreviewProps {
  imageId: number | null
  alt?: string
  className?: string
  size?: 'small' | 'medium' | 'large'
  placeholder?: string
}

export default function ImagePreview({
  imageId,
  alt = 'Preview',
  className,
  size = 'medium',
  placeholder = 'Изображение не загружено',
}: ImagePreviewProps) {
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [imageUrl, setImageUrl] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState(false)

  const sizeStyles = {
    small: { width: '60px', height: '60px' },
    medium: { width: '150px', height: '150px' },
    large: { width: '300px', height: '300px' },
  }

  useEffect(() => {
    if (imageId) {
      setIsLoading(true)
      setError(false)
      const url = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8082'}/admin-api/image?id=${imageId}&isPreview=true`
      setImageUrl(url)
      setIsLoading(false)
    } else {
      setImageUrl(null)
    }
  }, [imageId])

  if (!imageId) {
    return (
      <div
        style={{
          ...sizeStyles[size],
          backgroundColor: '#f5f5f5',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          border: '1px solid #ddd',
          borderRadius: '4px',
          fontSize: '0.875rem',
          color: '#666',
          textAlign: 'center',
          padding: '0.5rem',
        }}
        className={className}
      >
        {placeholder}
      </div>
    )
  }

  return (
    <>
      <div
        style={{
          ...sizeStyles[size],
          cursor: 'pointer',
          position: 'relative',
          overflow: 'hidden',
          border: '1px solid #ddd',
          borderRadius: '4px',
        }}
        className={className}
        onClick={() => setIsModalOpen(true)}
      >
        {isLoading && (
          <div
            style={{
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              backgroundColor: '#f5f5f5',
            }}
          >
            Загрузка...
          </div>
        )}
        {error && (
          <div
            style={{
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              backgroundColor: '#f5f5f5',
              fontSize: '0.875rem',
              color: '#666',
              padding: '0.5rem',
            }}
          >
            {placeholder}
          </div>
        )}
        {imageUrl && !isLoading && !error && (
          <img
            src={imageUrl}
            alt={alt}
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'cover',
            }}
            onError={() => {
              setError(true)
              setIsLoading(false)
            }}
            onLoad={() => setIsLoading(false)}
          />
        )}
      </div>
      <ImageModal
        imageId={imageId}
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        alt={alt}
      />
    </>
  )
}

