import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import ImageModal from './ImageModal'
import { apiClient } from '../../services/apiClient'

// Мокаем apiClient
vi.mock('../../services/apiClient', () => ({
  apiClient: {
    instance: {
      get: vi.fn(),
    },
  },
}))

// Мокаем Modal
vi.mock('./Modal', () => ({
  default: ({ isOpen, children }: { isOpen: boolean; children: React.ReactNode }) => {
    if (!isOpen) return null
    return <div data-testid="modal">{children}</div>
  },
}))

describe('ImageModal', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Мокаем URL.createObjectURL и URL.revokeObjectURL
    globalThis.URL.createObjectURL = vi.fn(() => 'blob:mock-url') as any
    globalThis.URL.revokeObjectURL = vi.fn() as any
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('не отображается когда isOpen=false', () => {
    render(<ImageModal imageId={123} isOpen={false} onClose={vi.fn()} />)
    expect(screen.queryByAltText('Image')).not.toBeInTheDocument()
  })

  it('не отображается когда imageId отсутствует', () => {
    render(<ImageModal imageId={null} isOpen={true} onClose={vi.fn()} />)
    expect(screen.queryByAltText('Image')).not.toBeInTheDocument()
  })

  it('загружает изображение через apiClient когда модальное окно открыто', async () => {
    const mockBlob = new Blob(['mock image data'], { type: 'image/png' })
    const mockResponse = { data: mockBlob }
    
    vi.mocked(apiClient.instance.get).mockResolvedValue(mockResponse as any)

    render(<ImageModal imageId={123} isOpen={true} onClose={vi.fn()} />)

    await waitFor(() => {
      expect(apiClient.instance.get).toHaveBeenCalledWith(
        '/admin-api/image?id=123&isPreview=false',
        { responseType: 'blob' }
      )
    })
  })

  it('создает blob URL после успешной загрузки', async () => {
    const mockBlob = new Blob(['mock image data'], { type: 'image/png' })
    const mockResponse = { data: mockBlob }
    
    vi.mocked(apiClient.instance.get).mockResolvedValue(mockResponse as any)

    render(<ImageModal imageId={123} isOpen={true} onClose={vi.fn()} />)

    await waitFor(() => {
      expect(globalThis.URL.createObjectURL).toHaveBeenCalledWith(mockBlob)
    })
  })

  it('отображает ошибку при неудачной загрузке', async () => {
    vi.mocked(apiClient.instance.get).mockRejectedValue(new Error('Network error'))

    render(<ImageModal imageId={123} isOpen={true} onClose={vi.fn()} />)

    await waitFor(() => {
      expect(screen.getByText('Не удалось загрузить изображение')).toBeInTheDocument()
    })
  })

  it('очищает blob URL при размонтировании', async () => {
    const mockBlob = new Blob(['mock image data'], { type: 'image/png' })
    const mockResponse = { data: mockBlob }
    
    vi.mocked(apiClient.instance.get).mockResolvedValue(mockResponse as any)

    const { unmount } = render(<ImageModal imageId={123} isOpen={true} onClose={vi.fn()} />)

    await waitFor(() => {
      expect(globalThis.URL.createObjectURL).toHaveBeenCalled()
    })

    unmount()

    expect(globalThis.URL.revokeObjectURL).toHaveBeenCalledWith('blob:mock-url')
  })
})

