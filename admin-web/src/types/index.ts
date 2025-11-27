// Auth types
export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  role: 'ADMIN' | 'MANAGER'
  expiresIn: number
}

export interface RefreshTokenRequest {
  refreshToken: string
}

export interface RefreshTokenResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export interface ForgotPasswordRequest {
  email: string
}

export interface ResetPasswordRequest {
  email: string
  code: string
  newPassword: string
}

export interface UserInfo {
  email: string
  role: 'ADMIN' | 'MANAGER'
  restaurants: Restaurant[]
}

// Restaurant types
export interface Restaurant {
  id: number
  name: string
  address?: string
  phone?: string
  whatsapp?: string
  instagram?: string
  description?: string
  latitude?: number
  longitude?: number
  workingHours?: string
  managerLanguageCode?: string
  logoImageId?: number | null
  bgImageId?: number | null
  isActive: boolean
  subscription?: Subscription
}

export interface Subscription {
  id?: number
  restaurantId: number
  status?: string
  paymentReference?: string
  externalTransactionId?: string
  subscriptionType?: SubscriptionType
  startDate?: string
  endDate?: string
  isActive: boolean
  daysRemaining: number
  isExpiringSoon: boolean
  description?: string
  createdAt?: string
  updatedAt?: string
}

export interface SubscriptionType {
  id: number
  code: string
  name: string
  description?: string
  price: number
}

// Menu types
export interface MenuCategory {
  id: number
  name: string
  description?: string
  displayOrder: number
}

export interface MenuItem {
  id: number
  name: string
  description?: string
  ingredients?: string
  price: number
  discountPercent: number
  spicinessLevel: number
  hasSugar: boolean
  imageId?: number | null
  menuCategoryId: number
  displayOrder: number
}

// Floor, Room, Table types
export interface Floor {
  id: number
  floorNumber: string
  restaurantId: number
  roomsCount?: number
}

export interface Room {
  id: number
  name: string
  floorId: number
  description?: string
  isSmoking: boolean
  isOutdoor: boolean
  imageId?: number | null
  tableCount?: number
}

export interface Table {
  id: number
  tableNumber: string
  roomId: number
  capacity: number
  description?: string
  imageId?: number | null
  depositAmount?: string | null
  depositNote?: string | null
  positionX1?: number | null
  positionY1?: number | null
  positionX2?: number | null
  positionY2?: number | null
}

// Booking types
export interface Booking {
  id: number
  restaurantId: number
  tableId: number
  clientPhone: string
  clientName?: string
  bookingDate: string
  numberOfPersons: number
  status: BookingStatus
  specialRequests?: string
  preOrderItemsCount: number
}

export type BookingStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED_BY_CLIENT' | 'CANCELLED_BY_MANAGER'

export interface PreOrderItem {
  id: number
  menuItemId: number
  menuItemName: string
  quantity: number
  price: number
  specialRequests?: string
}

// Client types
export interface Client {
  id: number
  phone: string
  name?: string
  bookingsCount: number
  preOrdersCount: number
  lastVisitDate?: string
}

// Promotion types
export interface Promotion {
  id: number
  title: string
  description?: string
  promotionTypeId?: number  // Для формы создания/редактирования
  promotionType?: {
    id: number
    code: string
    name: string
  }  // Из API ответа
  startDate: string
  endDate?: string | null
  imageId?: number | null
  isRecurring: boolean
  recurrenceType?: 'WEEKLY' | 'MONTHLY' | 'DAILY' | null
  recurrenceDaysOfWeek?: number[] | null
}

export interface PromotionType {
  id: number
  code: string
  name: string
  description?: string
}

// Analytics types
export interface AnalyticsOverview {
  bookings: number
  preOrders: number
  revenue: number
  newClients: number
}

// Pagination types
export interface PaginationResponse<T> {
  data: T
  total: number
  limit: number
  offset: number
}

// Room Layout types
export interface RoomLayout {
  room: Room
  tables: Table[]
  imageUrl?: string
}

export interface UpdateTablePositionRequest {
  tableId: number
  positionX1?: number | null
  positionY1?: number | null
  positionX2?: number | null
  positionY2?: number | null
}

// Error types
export interface ApiError {
  exceptionName: string
  message: string
}

