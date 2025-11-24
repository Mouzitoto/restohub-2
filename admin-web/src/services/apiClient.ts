import axios, { AxiosError } from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import { tokenStorage } from '../utils/tokenStorage'
import type { ApiError } from '../types'

// Устанавливаем базовый URL API, используя переменную окружения или дефолтное значение
const getApiBaseUrl = (): string => {
  const envUrl = import.meta.env.VITE_API_BASE_URL
  console.log('[apiClient] getApiBaseUrl - envUrl:', envUrl, 'type:', typeof envUrl)
  if (envUrl && typeof envUrl === 'string' && envUrl.trim() !== '') {
    const result = envUrl.trim()
    console.log('[apiClient] getApiBaseUrl - using envUrl:', result)
    return result
  }
  // Дефолтное значение для разработки и тестов
  const defaultUrl = 'http://localhost:8082'
  console.log('[apiClient] getApiBaseUrl - using default:', defaultUrl)
  return defaultUrl
}

const API_BASE_URL = getApiBaseUrl()
console.log('[apiClient] API_BASE_URL constant:', API_BASE_URL)

class ApiClient {
  private client: AxiosInstance
  private isRefreshing = false
  private failedQueue: Array<{
    resolve: (value?: any) => void
    reject: (error?: any) => void
  }> = []

  constructor() {
    // Убеждаемся, что baseURL всегда установлен
    const baseURL = API_BASE_URL || 'http://localhost:8082'
    console.log('[apiClient] constructor - baseURL:', baseURL)
    console.log('[apiClient] constructor - API_BASE_URL:', API_BASE_URL)
    console.log('[apiClient] constructor - import.meta.env.VITE_API_BASE_URL:', import.meta.env.VITE_API_BASE_URL)
    
    this.client = axios.create({
      baseURL: baseURL,
      headers: {
        'Content-Type': 'application/json',
      },
    })

    console.log('[apiClient] constructor - client.defaults.baseURL:', this.client.defaults.baseURL)
    console.log('[apiClient] constructor - client created successfully')

    this.setupInterceptors()
  }

  private setupInterceptors(): void {
    // Request interceptor - добавляем токен
    this.client.interceptors.request.use(
      (config) => {
        console.log('[apiClient] request interceptor - config.url:', config.url)
        console.log('[apiClient] request interceptor - config.baseURL:', config.baseURL)
        console.log('[apiClient] request interceptor - full URL will be:', `${config.baseURL}${config.url}`)
        
        // Публичные эндпоинты, для которых не нужно добавлять токен
        const publicEndpoints = [
          '/auth/login',
          '/auth/refresh',
          '/auth/forgot-password',
          '/auth/reset-password',
          '/auth/register',
          '/auth/verify-email',
          '/auth/resend-verification-code',
          '/auth/terms',
        ]
        
        const isPublicEndpoint = publicEndpoints.some(endpoint => 
          config.url?.includes(endpoint)
        )
        
        // Не добавляем токен для публичных эндпоинтов
        if (!isPublicEndpoint) {
          const token = tokenStorage.getAccessToken()
          if (token) {
            // Устанавливаем заголовок Authorization
            // Используем оба способа для совместимости с разными версиями axios
            if (config.headers) {
              if (typeof config.headers.set === 'function') {
                // AxiosHeaders (axios >= 1.0)
                config.headers.set('Authorization', `Bearer ${token}`)
              } else {
                // Обычный объект (старые версии)
                config.headers['Authorization'] = `Bearer ${token}`
              }
            } else {
              config.headers = { Authorization: `Bearer ${token}` } as any
            }
          }
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
          const isLoginOrRefreshRequest = 
            originalRequest.url?.includes('/auth/login') || 
            originalRequest.url?.includes('/auth/refresh')

          // Пропускаем обработку для запросов на логин/refresh
          if (isLoginOrRefreshRequest) {
            return Promise.reject(error)
          }

          // Если токен истек или exceptionName не указан (возможно, токен просто истек) - пытаемся обновить
          if (exceptionName === 'TOKEN_EXPIRED' || !exceptionName) {
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
    // Используем кастомное событие для SPA-навигации вместо window.location.href
    // Это предотвращает полную перезагрузку страницы и потерю истории логов
    window.dispatchEvent(new CustomEvent('auth:logout', { detail: { redirectTo: '/login' } }))
  }

  get instance(): AxiosInstance {
    return this.client
  }
}

export const apiClient = new ApiClient()

