import { createContext, useContext, useState, useEffect } from 'react'
import type { ReactNode } from 'react'
import { authService } from '../services/authService'
import { tokenStorage } from '../utils/tokenStorage'
import type { Restaurant, UserInfo, Subscription } from '../types'

interface AppContextType {
  user: UserInfo | null
  currentRestaurant: Restaurant | null
  restaurants: Restaurant[]
  subscription: Subscription | null
  role: 'ADMIN' | 'MANAGER' | null
  isLoading: boolean
  setCurrentRestaurant: (restaurant: Restaurant) => void
  refreshUserInfo: () => Promise<void>
  refreshRestaurants: () => Promise<void>
}

const AppContext = createContext<AppContextType | undefined>(undefined)

export function AppProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserInfo | null>(null)
  const [currentRestaurant, setCurrentRestaurantState] = useState<Restaurant | null>(null)
  const [restaurants, setRestaurants] = useState<Restaurant[]>([])
  const [subscription, setSubscription] = useState<Subscription | null>(null)
  const [role, setRole] = useState<'ADMIN' | 'MANAGER' | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const loadUserInfo = async () => {
    try {
      const userInfo = await authService.getCurrentUser()
      setUser(userInfo)
      setRole(userInfo.role)
      setRestaurants(userInfo.restaurants || [])

      // Выбираем ресторан
      const savedRestaurantId = tokenStorage.getSelectedRestaurantId()
      if (savedRestaurantId) {
        const savedRestaurant = userInfo.restaurants?.find((r) => r.id === savedRestaurantId)
        if (savedRestaurant) {
          setCurrentRestaurantState(savedRestaurant)
          setSubscription(savedRestaurant.subscription || null)
          return
        }
      }

      // Если сохраненного ресторана нет, выбираем первый
      if (userInfo.restaurants && userInfo.restaurants.length > 0) {
        const firstRestaurant = userInfo.restaurants[0]
        setCurrentRestaurantState(firstRestaurant)
        setSubscription(firstRestaurant.subscription || null)
        tokenStorage.setSelectedRestaurantId(firstRestaurant.id)
      }
    } catch (error) {
      console.error('Failed to load user info:', error)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    if (authService.isAuthenticated()) {
      loadUserInfo()
    } else {
      setIsLoading(false)
    }
  }, [])

  const setCurrentRestaurant = (restaurant: Restaurant) => {
    setCurrentRestaurantState(restaurant)
    setSubscription(restaurant.subscription || null)
    tokenStorage.setSelectedRestaurantId(restaurant.id)
  }

  const refreshUserInfo = async () => {
    await loadUserInfo()
  }

  const refreshRestaurants = async () => {
    await loadUserInfo()
  }

  return (
    <AppContext.Provider
      value={{
        user,
        currentRestaurant,
        restaurants,
        subscription,
        role,
        isLoading,
        setCurrentRestaurant,
        refreshUserInfo,
        refreshRestaurants,
      }}
    >
      {children}
    </AppContext.Provider>
  )
}

export function useApp() {
  const context = useContext(AppContext)
  if (context === undefined) {
    throw new Error('useApp must be used within an AppProvider')
  }
  return context
}

