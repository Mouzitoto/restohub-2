import { useState, useRef } from 'react'
import type { DragEvent } from 'react'
import ImagePreview from './ImagePreview'
import { useToast } from '../../context/ToastContext'
import { getImageUploadErrorMessage } from '../../utils/imageUploadError'

interface ImageUploadProps {
  currentImageId: number | null
  onImageUploaded: (file: File) => void | Promise<void>
  onImageRemoved?: () => void
  accept?: string
  maxSize?: number
  type?: string
  recommendedSize?: string
  aspectRatio?: number
  showCrop?: boolean
  className?: string
  disabled?: boolean
  uploadToEntity?: boolean
  entityId?: number
  entityType?: string
}

export default function ImageUpload({
  currentImageId,
  onImageUploaded,
  onImageRemoved,
  accept = 'image/jpeg,image/png,image/webp',
  maxSize = 5 * 1024 * 1024, // 5MB
  type: _type,
  recommendedSize,
  className,
  disabled = false,
  uploadToEntity: _uploadToEntity,
  entityId: _entityId,
  entityType: _entityType,
}: ImageUploadProps) {
  const [isDragging, setIsDragging] = useState(false)
  const [isUploading, setIsUploading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const toast = useToast()

  const validateFile = (file: File): string | null => {
    // Проверка формата
    if (!accept.split(',').some((format) => file.type.includes(format.trim().split('/')[1]))) {
      return 'Разрешены только JPG, PNG, WebP'
    }

    // Проверка размера
    if (file.size > maxSize) {
      return `Максимальный размер файла: ${Math.round(maxSize / 1024 / 1024)}MB`
    }

    return null
  }

  const handleFile = async (file: File) => {
    const validationError = validateFile(file)
    if (validationError) {
      setError(validationError)
      toast.error(validationError)
      return
    }

    setIsUploading(true)
    setError(null)

    try {
      // Загрузка к конкретной сущности, передаем File в callback
      await onImageUploaded(file)
    } catch (err: any) {
      const errorMessage = getImageUploadErrorMessage(err)
      setError(errorMessage)
      toast.error(errorMessage)
    } finally {
      setIsUploading(false)
    }
  }

  const handleDragOver = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault()
    if (!disabled) {
      setIsDragging(true)
    }
  }

  const handleDragLeave = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault()
    setIsDragging(false)
  }

  const handleDrop = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault()
    setIsDragging(false)

    if (disabled) return

    const file = e.dataTransfer.files[0]
    if (file) {
      handleFile(file)
    }
  }

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      handleFile(file)
    }
  }

  const handleRemove = () => {
    if (onImageRemoved) {
      onImageRemoved()
    }
  }

  return (
    <div className={className}>
      {currentImageId ? (
        <div>
          <ImagePreview imageId={currentImageId} size="large" />
          {!disabled && (
            <button
              onClick={handleRemove}
              style={{
                marginTop: '0.5rem',
                padding: '0.5rem 1rem',
                backgroundColor: '#f44336',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
              }}
            >
              Удалить
            </button>
          )}
        </div>
      ) : (
        <div
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          style={{
            border: `2px dashed ${isDragging ? '#007bff' : '#ccc'}`,
            borderRadius: '8px',
            padding: '2rem',
            textAlign: 'center',
            backgroundColor: isDragging ? '#f0f8ff' : '#fafafa',
            cursor: disabled ? 'not-allowed' : 'pointer',
            opacity: disabled ? 0.6 : 1,
          }}
          onClick={() => !disabled && fileInputRef.current?.click()}
        >
          <input
            ref={fileInputRef}
            type="file"
            accept={accept}
            onChange={handleFileSelect}
            style={{ display: 'none' }}
            disabled={disabled}
          />
          {isUploading ? (
            <div>Загрузка...</div>
          ) : (
            <>
              <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>📁</div>
              <div>Перетащите изображение сюда или нажмите для выбора</div>
              {recommendedSize && (
                <div style={{ fontSize: '0.875rem', color: '#666', marginTop: '0.5rem' }}>
                  Рекомендуемый размер: {recommendedSize}
                </div>
              )}
            </>
          )}
        </div>
      )}

      {error && (
        <div style={{ color: 'red', fontSize: '0.875rem', marginTop: '0.5rem' }}>{error}</div>
      )}
    </div>
  )
}

