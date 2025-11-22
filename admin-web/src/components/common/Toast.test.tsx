import { describe, it, expect } from 'vitest'
import { render, screen } from '../../test/utils/test-utils'
import { ToastProvider } from '../../context/ToastContext'

const TestComponent = () => {
  return (
    <div>
      <button>Show Toast</button>
    </div>
  )
}

describe('Toast', () => {
  it('should render toast provider', () => {
    render(
      <ToastProvider>
        <TestComponent />
      </ToastProvider>
    )

    expect(screen.getByText('Show Toast')).toBeInTheDocument()
  })
})

