import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import AuthRedirectHandler from './AuthRedirectHandler'

// Мокаем useNavigate
const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

describe('AuthRedirectHandler', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should listen to auth:logout event and navigate to login', () => {
    // Arrange
    render(
      <BrowserRouter>
        <AuthRedirectHandler />
      </BrowserRouter>
    )

    // Act - диспатчим событие auth:logout
    const event = new CustomEvent('auth:logout', {
      detail: { redirectTo: '/login' },
    })
    window.dispatchEvent(event)

    // Assert
    expect(mockNavigate).toHaveBeenCalledWith('/login', { replace: true })
  })

  it('should use default redirect path if not provided', () => {
    // Arrange
    render(
      <BrowserRouter>
        <AuthRedirectHandler />
      </BrowserRouter>
    )

    // Act - диспатчим событие без detail
    const event = new CustomEvent('auth:logout')
    window.dispatchEvent(event)

    // Assert
    expect(mockNavigate).toHaveBeenCalledWith('/login', { replace: true })
  })

  it('should clean up event listener on unmount', () => {
    // Arrange
    const removeEventListenerSpy = vi.spyOn(window, 'removeEventListener')
    const { unmount } = render(
      <BrowserRouter>
        <AuthRedirectHandler />
      </BrowserRouter>
    )

    // Act
    unmount()

    // Assert
    expect(removeEventListenerSpy).toHaveBeenCalledWith(
      'auth:logout',
      expect.any(Function)
    )
    removeEventListenerSpy.mockRestore()
  })
})

