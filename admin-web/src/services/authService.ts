import { apiClient } from './apiClient'
import { tokenStorage } from '../utils/tokenStorage'
import type {
  LoginRequest,
  LoginResponse,
  RefreshTokenRequest,
  RefreshTokenResponse,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  UserInfo,
} from '../types'

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiClient.instance.post<LoginResponse>(
      '/admin-api/auth/login',
      credentials
    )

    // Сохраняем токены
    tokenStorage.setAccessToken(response.data.accessToken)
    tokenStorage.setRefreshToken(response.data.refreshToken)
    tokenStorage.setRole(response.data.role)
    tokenStorage.setTokenExpiry(response.data.expiresIn)

    return response.data
  },

  async refresh(refreshToken: string): Promise<RefreshTokenResponse> {
    const response = await apiClient.instance.post<RefreshTokenResponse>(
      '/admin-api/auth/refresh',
      { refreshToken } as RefreshTokenRequest
    )

    tokenStorage.setAccessToken(response.data.accessToken)
    tokenStorage.setRefreshToken(response.data.refreshToken)
    tokenStorage.setTokenExpiry(response.data.expiresIn)

    return response.data
  },

  async getCurrentUser(): Promise<UserInfo> {
    const response = await apiClient.instance.get<UserInfo>('/admin-api/auth/me')
    return response.data
  },

  async forgotPassword(data: ForgotPasswordRequest): Promise<void> {
    await apiClient.instance.post('/admin-api/auth/forgot-password', data)
  },

  async resetPassword(data: ResetPasswordRequest): Promise<void> {
    await apiClient.instance.post('/admin-api/auth/reset-password', data)
  },

  async registerPartner(data: {
    email: string
    password: string
    confirmPassword: string
    agreeToTerms: boolean
  }): Promise<{ message: string; email: string }> {
    const response = await apiClient.instance.post<{ message: string; email: string }>(
      '/admin-api/auth/register',
      data
    )
    return response.data
  },

  async verifyEmail(data: { email: string; code: string }): Promise<{ message: string; userId: number }> {
    const response = await apiClient.instance.post<{ message: string; userId: number }>(
      '/admin-api/auth/verify-email',
      data
    )
    return response.data
  },

  async resendVerificationCode(data: { email: string }): Promise<{ message: string; email: string }> {
    const response = await apiClient.instance.post<{ message: string; email: string }>(
      '/admin-api/auth/resend-verification-code',
      data
    )
    return response.data
  },

  async getTerms(): Promise<{ terms: string }> {
    const response = await apiClient.instance.get<{ terms: string }>('/admin-api/auth/terms')
    return response.data
  },

  logout(): void {
    const refreshToken = tokenStorage.getRefreshToken()
    if (refreshToken) {
      // Отправляем запрос на сервер для инвалидации токена (опционально)
      apiClient.instance.post('/admin-api/auth/logout', { refreshToken }).catch(() => {
        // Игнорируем ошибки при logout
      })
    }
    tokenStorage.clearTokens()
    tokenStorage.clearSelectedRestaurantId()
  },

  isAuthenticated(): boolean {
    const token = tokenStorage.getAccessToken()
    if (!token) return false

    if (tokenStorage.isTokenExpired()) {
      return false
    }

    return true
  },
}

