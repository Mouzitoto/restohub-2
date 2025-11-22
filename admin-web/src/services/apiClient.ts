import axios, { AxiosError } from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import { tokenStorage } from '../utils/tokenStorage'
import type { ApiError } from '../types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8082'

class ApiClient {
  private client: AxiosInstance
  private isRefreshing = false
  private failedQueue: Array<{
    resolve: (value?: any) => void
    reject: (error?: any) => void
  }> = []

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    })

    this.setupInterceptors()
  }

  private setupInterceptors(): void {
    // Request interceptor - добавляем токен
    this.client.interceptors.request.use(
      (config) => {
        const token = tokenStorage.getAccessToken()
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }

        // Проверяем, нужно ли обновить токен
        if (tokenStorage.isTokenExpiringSoon(5)) {
          this.refreshTokenIfNeeded()
        }

        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )

    // Response interceptor - обрабатываем ошибки и обновляем токен
    this.client.interceptors.response.use(
      (response) => response,
      async (error: AxiosError<ApiError>) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

        if (!originalRequest) {
          return Promise.reject(error)
        }

        // Если ошибка 401 и это не запрос на логин/refresh
        if (error.response?.status === 401 && !originalRequest._retry) {
          const exceptionName = error.response.data?.exceptionName

          // Если токен истек - пытаемся обновить
          if (exceptionName === 'TOKEN_EXPIRED') {
            if (this.isRefreshing) {
              // Если уже обновляем токен - добавляем в очередь
              return new Promise((resolve, reject) => {
                this.failedQueue.push({ resolve, reject })
              })
                .then(() => {
                  return this.client(originalRequest)
                })
                .catch((err) => {
                  return Promise.reject(err)
                })
            }

            originalRequest._retry = true
            this.isRefreshing = true

            try {
              const refreshed = await this.refreshToken()
              if (refreshed) {
                this.processQueue(null)
                return this.client(originalRequest)
              } else {
                this.processQueue(new Error('Token refresh failed'))
                this.handleAuthError()
                return Promise.reject(error)
              }
            } catch (refreshError) {
              this.processQueue(refreshError)
              this.handleAuthError()
              return Promise.reject(refreshError)
            } finally {
              this.isRefreshing = false
            }
          }

          // Для других ошибок авторизации - редирект на логин
          if (
            exceptionName === 'REFRESH_TOKEN_EXPIRED' ||
            exceptionName === 'INVALID_REFRESH_TOKEN' ||
            exceptionName === 'INVALID_TOKEN' ||
            exceptionName === 'UNAUTHORIZED'
          ) {
            this.handleAuthError()
            return Promise.reject(error)
          }
        }

        return Promise.reject(error)
      }
    )
  }

  private async refreshTokenIfNeeded(): Promise<void> {
    if (this.isRefreshing) return

    const refreshToken = tokenStorage.getRefreshToken()
    if (!refreshToken) return

    if (tokenStorage.isTokenExpiringSoon(5)) {
      await this.refreshToken()
    }
  }

  private async refreshToken(): Promise<boolean> {
    const refreshToken = tokenStorage.getRefreshToken()
    if (!refreshToken) return false

    try {
      const response = await axios.post<{
        accessToken: string
        refreshToken: string
        expiresIn: number
      }>(`${API_BASE_URL}/admin-api/auth/refresh`, {
        refreshToken,
      })

      tokenStorage.setAccessToken(response.data.accessToken)
      tokenStorage.setRefreshToken(response.data.refreshToken)
      tokenStorage.setTokenExpiry(response.data.expiresIn)

      return true
    } catch (error) {
      const axiosError = error as AxiosError<ApiError>
      const exceptionName = axiosError.response?.data?.exceptionName

      if (
        exceptionName === 'REFRESH_TOKEN_EXPIRED' ||
        exceptionName === 'INVALID_REFRESH_TOKEN'
      ) {
        tokenStorage.clearTokens()
      }

      return false
    }
  }

  private processQueue(error: any): void {
    this.failedQueue.forEach((promise) => {
      if (error) {
        promise.reject(error)
      } else {
        promise.resolve()
      }
    })
    this.failedQueue = []
  }

  private handleAuthError(): void {
    tokenStorage.clearTokens()
    window.location.href = '/login'
  }

  get instance(): AxiosInstance {
    return this.client
  }
}

export const apiClient = new ApiClient()

