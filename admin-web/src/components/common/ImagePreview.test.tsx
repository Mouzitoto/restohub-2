import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import ImagePreview from './ImagePreview'
import { apiClient } from '../../services/apiClient'

// Мокаем apiClient
vi.mock('../../services/apiClient', () => ({
  apiClient: {
    instance: {
      get: vi.fn(),
    },
  },
}))

describe('ImagePreview', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Мокаем URL.createObjectURL и URL.revokeObjectURL
    globalThis.URL.createObjectURL = vi.fn(() => 'blob:mock-url') as any
    globalThis.URL.revokeObjectURL = vi.fn() as any
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('отображает placeholder когда imageId отсутствует', () => {
    render(<ImagePreview imageId={null} />)
    expect(screen.getByText('Изображение не загружено')).toBeInTheDocument()
  })

  it('загружает изображение через apiClient когда imageId предоставлен', async () => {
    const mockBlob = new Blob(['mock image data'], { type: 'image/png' })
    const mockResponse = { data: mockBlob }
    
    vi.mocked(apiClient.instance.get).mockResolvedValue(mockResponse as any)

    render(<ImagePreview imageId={123} />)

    await waitFor(() => {
      expect(apiClient.instance.get).toHaveBeenCalledWith(
        '/admin-api/image?id=123&isPreview=true',
        { responseType: 'blob' }
      )
    })
  })

  it('создает blob URL после успешной загрузки', async () => {
    const mockBlob = new Blob(['mock image data'], { type: 'image/png' })
    const mockResponse = { data: mockBlob }
    
    vi.mocked(apiClient.instance.get).mockResolvedValue(mockResponse as any)

    render(<ImagePreview imageId={123} />)

    await waitFor(() => {
      expect(globalThis.URL.createObjectURL).toHaveBeenCalledWith(mockBlob)
    })
  })

  it('отображает ошибку при неудачной загрузке', async () => {
    vi.mocked(apiClient.instance.get).mockRejectedValue(new Error('Network error'))

    render(<ImagePreview imageId={123} />)

    await waitFor(() => {
      expect(screen.getByText('Изображение не загружено')).toBeInTheDocument()
    })
  })

  it('очищает blob URL при размонтировании', async () => {
    const mockBlob = new Blob(['mock image data'], { type: 'image/png' })
    const mockResponse = { data: mockBlob }
    
    vi.mocked(apiClient.instance.get).mockResolvedValue(mockResponse as any)

    const { unmount } = render(<ImagePreview imageId={123} />)

    await waitFor(() => {
      expect(globalThis.URL.createObjectURL).toHaveBeenCalled()
    })

    unmount()

    expect(globalThis.URL.revokeObjectURL).toHaveBeenCalledWith('blob:mock-url')
  })

  it('очищает предыдущий blob URL при изменении imageId', async () => {
    const mockBlob = new Blob(['mock image data'], { type: 'image/png' })
    const mockResponse = { data: mockBlob }
    
    vi.mocked(apiClient.instance.get).mockResolvedValue(mockResponse as any)

    const { rerender } = render(<ImagePreview imageId={123} />)

    await waitFor(() => {
      expect(globalThis.URL.createObjectURL).toHaveBeenCalled()
    })

    rerender(<ImagePreview imageId={456} />)

    await waitFor(() => {
      expect(globalThis.URL.revokeObjectURL).toHaveBeenCalled()
    })
  })
})

