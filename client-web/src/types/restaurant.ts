// API Response Types (from phase-5)
export interface ApiRestaurant {
  id: number;
  name: string;
  address?: string;
  phone?: string;
  email?: string;
  description?: string;
  logoId?: number;
  backgroundId?: number;
  primaryColor?: string;
  latitude?: number;
  longitude?: number;
  instagram?: string;
  whatsapp?: string;
  website?: string;
  cuisineType?: string;
  establishmentType?: string;
}

export interface ApiMenuCategory {
  id: number;
  name: string;
  description?: string;
  displayOrder: number;
  imageId?: number;
  items: ApiMenuItem[];
}

export interface ApiMenuItem {
  id: number;
  name: string;
  description?: string;
  ingredients?: string;
  price: number;
  discountPercent?: number;
  spicinessLevel?: number;
  hasSugar?: boolean;
  imageId?: number;
  displayOrder?: number;
  weight?: string;
  isNew?: boolean;
  hasPromo?: boolean;
}

export interface ApiPromotion {
  id: number;
  title: string;
  description?: string;
  imageId?: number;
  promotionType: {
    id: number;
    code: string;
    name: string;
  };
  startDate: string;
  endDate?: string;
  isRecurring: boolean;
  recurrenceType?: 'DAILY' | 'WEEKLY' | 'MONTHLY';
  recurrenceDayOfWeek?: number;
}

export interface ApiFloor {
  id: number;
  floorNumber: string;
}

export interface ApiRoom {
  id: number;
  name: string;
  floorId: number;
  isSmoking: boolean;
  isOutdoor: boolean;
  imageId?: number;
  description?: string;
}

export interface ApiTable {
  id: number;
  tableNumber: string;
  roomId: number;
  capacity: number;
  description?: string;
  imageId?: number;
  depositAmount?: number;
  depositNote?: string;
}

// Component Types (adapted from design-from-figma)
export interface Restaurant {
  id: string;
  name: string;
  logoUrl?: string;
  backgroundUrl?: string;
  address?: string;
  description?: string;
  phone?: string;
  email?: string;
  cuisineType?: string;
  rating?: number; // Optional - may not be in API
  distance?: number; // Optional - calculated on frontend
  hasPromotions: boolean;
  isOutdoor: boolean;
  isSmoking: boolean;
  lat?: number;
  lng?: number;
  primaryColor: string; // Default if not provided
  menuCategories?: MenuCategory[];
  establishmentType?: string;
  instagram?: string;
  whatsapp?: string;
  website?: string;
}

export interface MenuCategory {
  id: string;
  name: string;
  imageUrl?: string;
  dishes: Dish[];
}

export interface Dish {
  id: string;
  name: string;
  description: string;
  price: number;
  imageUrl?: string;
  weight?: string;
  isNew?: boolean;
  hasPromo?: boolean;
}

export interface Promotion {
  id: string;
  title: string;
  description?: string;
  imageUrl?: string;
  type: 'PROMOTION' | 'THEME_NIGHT' | 'NEW';
  startDate: string;
  endDate?: string;
  isRecurring: boolean;
  recurrenceType?: 'DAILY' | 'WEEKLY' | 'MONTHLY';
  recurrenceDayOfWeek?: number;
}

export interface Floor {
  id: string;
  name: string;
  number: number;
}

export interface Room {
  id: string;
  name: string;
  floorId: string;
  isSmoking: boolean;
  isOutdoor: boolean;
  imageId?: number;
  mapImageUrl?: string;
  description?: string;
}

export interface Table {
  id: string;
  tableNumber: string;
  roomId: string;
  capacity: number;
  description?: string;
  imageUrl?: string;
  images?: string[];
  deposit?: number;
  // Coordinates not stored in DB - will be calculated or simplified
  x?: number;
  y?: number;
}

export interface BookingRequest {
  tableId: number;
  date: string; // YYYY-MM-DD
  time: string; // HH:mm:ss
  personCount: number;
  clientFirstName?: string;
  clientName?: string;
  specialRequests?: string;
  preOrderItems?: PreOrderItem[];
}

export interface PreOrderItem {
  menuItemId: number;
  quantity: number;
  specialRequests?: string;
}

export interface BookingResponse {
  id: number;
  restaurantId: number;
  tableId: number;
  date: string;
  time: string;
  personCount: number;
  clientName?: string;
  specialRequests?: string;
  status: {
    code: string;
    name: string;
  };
  whatsappUrl: string;
  message: string;
  createdAt: string;
}

