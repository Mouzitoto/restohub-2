import { describe, it, expect } from 'vitest'
import { render, screen } from '../test/utils/test-utils'
import LoginPage from './LoginPage'

describe('LoginPage', () => {
  it('should render login form', () => {
    render(<LoginPage />)

    expect(screen.getByText('Email')).toBeInTheDocument()
    expect(screen.getByText('Пароль')).toBeInTheDocument()
    expect(screen.getByRole('textbox', { name: /email/i })).toBeInTheDocument()
  })
})

