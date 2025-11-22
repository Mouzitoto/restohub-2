const ACCESS_TOKEN_KEY = 'admin_access_token'
const REFRESH_TOKEN_KEY = 'admin_refresh_token'
const ROLE_KEY = 'admin_role'
const TOKEN_EXPIRY_KEY = 'admin_token_expiry'
const SELECTED_RESTAURANT_KEY = 'admin_selected_restaurant_id'

export const tokenStorage = {
  getAccessToken: (): string | null => {
    return localStorage.getItem(ACCESS_TOKEN_KEY)
  },

  setAccessToken: (token: string): void => {
    localStorage.setItem(ACCESS_TOKEN_KEY, token)
  },

  getRefreshToken: (): string | null => {
    return localStorage.getItem(REFRESH_TOKEN_KEY)
  },

  setRefreshToken: (token: string): void => {
    localStorage.setItem(REFRESH_TOKEN_KEY, token)
  },

  getRole: (): 'ADMIN' | 'MANAGER' | null => {
    const role = localStorage.getItem(ROLE_KEY)
    return role as 'ADMIN' | 'MANAGER' | null
  },

  setRole: (role: 'ADMIN' | 'MANAGER'): void => {
    localStorage.setItem(ROLE_KEY, role)
  },

  getTokenExpiry: (): number | null => {
    const expiry = localStorage.getItem(TOKEN_EXPIRY_KEY)
    return expiry ? parseInt(expiry, 10) : null
  },

  setTokenExpiry: (expiresIn: number): void => {
    const expiry = Date.now() + expiresIn * 1000
    localStorage.setItem(TOKEN_EXPIRY_KEY, expiry.toString())
  },

  isTokenExpired: (): boolean => {
    const expiry = tokenStorage.getTokenExpiry()
    if (!expiry) return true
    return Date.now() >= expiry
  },

  isTokenExpiringSoon: (minutes: number = 5): boolean => {
    const expiry = tokenStorage.getTokenExpiry()
    if (!expiry) return true
    const threshold = Date.now() + minutes * 60 * 1000
    return expiry <= threshold
  },

  clearTokens: (): void => {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    localStorage.removeItem(ROLE_KEY)
    localStorage.removeItem(TOKEN_EXPIRY_KEY)
  },

  getSelectedRestaurantId: (): number | null => {
    const id = localStorage.getItem(SELECTED_RESTAURANT_KEY)
    return id ? parseInt(id, 10) : null
  },

  setSelectedRestaurantId: (id: number): void => {
    localStorage.setItem(SELECTED_RESTAURANT_KEY, id.toString())
  },

  clearSelectedRestaurantId: (): void => {
    localStorage.removeItem(SELECTED_RESTAURANT_KEY)
  },
}

