import { describe, it, expect, beforeEach } from 'vitest'
import { tokenStorage } from './tokenStorage'

describe('tokenStorage', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('should set and get access token', () => {
    tokenStorage.setAccessToken('access-token')
    expect(tokenStorage.getAccessToken()).toBe('access-token')
  })

  it('should set and get refresh token', () => {
    tokenStorage.setRefreshToken('refresh-token')
    expect(tokenStorage.getRefreshToken()).toBe('refresh-token')
  })

  it('should clear tokens', () => {
    tokenStorage.setAccessToken('access-token')
    tokenStorage.setRefreshToken('refresh-token')
    tokenStorage.clearTokens()
    expect(tokenStorage.getAccessToken()).toBeNull()
    expect(tokenStorage.getRefreshToken()).toBeNull()
  })

  it('should set and get selected restaurant ID', () => {
    tokenStorage.setSelectedRestaurantId(1)
    expect(tokenStorage.getSelectedRestaurantId()).toBe(1)
  })

  it('should clear selected restaurant ID', () => {
    tokenStorage.setSelectedRestaurantId(1)
    tokenStorage.clearSelectedRestaurantId()
    expect(tokenStorage.getSelectedRestaurantId()).toBeNull()
  })

  it('should check if token is expired', () => {
    tokenStorage.setTokenExpiry(300) // expires in 300 seconds
    expect(tokenStorage.isTokenExpired()).toBe(false)
  })

  it('should check if token is expiring soon', () => {
    tokenStorage.setTokenExpiry(60) // expires in 60 seconds
    expect(tokenStorage.isTokenExpiringSoon(5)).toBe(true)
  })
})

