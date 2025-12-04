import type {
  ApiRestaurant,
  ApiMenuCategory,
  ApiMenuItem,
  ApiPromotion,
  ApiFloor,
  ApiRoom,
  ApiTable,
  Restaurant,
  MenuCategory,
  Dish,
  Promotion,
  Floor,
  Room,
  Table,
} from '../types/restaurant';
import { getImageUrl } from '../services/api';

// Default values for missing fields
const DEFAULT_PRIMARY_COLOR = '#D32F2F'; // Rose color
const DEFAULT_RATING = 4.5;
const DEFAULT_DISTANCE = 0;

// Map promotion type code to component type
const mapPromotionType = (code: string): 'PROMOTION' | 'THEME_NIGHT' | 'NEW' => {
  switch (code.toUpperCase()) {
    case 'PROMOTION':
      return 'PROMOTION';
    case 'THEME_NIGHT':
      return 'THEME_NIGHT';
    case 'NEW':
      return 'NEW';
    default:
      return 'PROMOTION';
  }
};

// Convert API Restaurant to Component Restaurant
export const mapRestaurant = (api: ApiRestaurant, hasPromotions = false): Restaurant => {
  // Check if restaurant has outdoor or smoking rooms (would need separate API call or include in response)
  // For now, defaulting to false - this should be calculated from rooms data
  const isOutdoor = false;
  const isSmoking = false;

  return {
    id: api.id.toString(),
    name: api.name,
    logoUrl: getImageUrl(api.logoId),
    backgroundUrl: getImageUrl(api.backgroundId),
    address: api.address,
    description: api.description,
    phone: api.phone,
    email: api.email,
    cuisineType: api.cuisineType,
    rating: DEFAULT_RATING, // Not in API - using default
    distance: DEFAULT_DISTANCE, // Not in API - will be calculated on frontend if needed
    hasPromotions,
    isOutdoor,
    isSmoking,
    lat: api.latitude,
    lng: api.longitude,
    primaryColor: api.primaryColor || DEFAULT_PRIMARY_COLOR,
    establishmentType: api.establishmentType,
    instagram: api.instagram,
    whatsapp: api.whatsapp,
    website: api.website,
  };
};

// Convert API MenuCategory to Component MenuCategory
export const mapMenuCategory = (api: ApiMenuCategory): MenuCategory => {
  return {
    id: api.id.toString(),
    name: api.name,
    imageUrl: getImageUrl(api.imageId),
    dishes: (api.items || []).map(mapMenuItem),
  };
};

// Convert API MenuItem to Component Dish
export const mapMenuItem = (api: ApiMenuItem): Dish => {
  return {
    id: api.id.toString(),
    name: api.name,
    description: api.description || '',
    price: api.price,
    imageUrl: getImageUrl(api.imageId, true), // Используем превью для карточек
    weight: api.weight,
    isNew: api.isNew,
    hasPromo: api.hasPromo,
  };
};

// Convert API Promotion to Component Promotion
export const mapPromotion = (api: ApiPromotion): Promotion => {
  return {
    id: api.id.toString(),
    title: api.title,
    description: api.description,
    imageUrl: getImageUrl(api.imageId),
    type: mapPromotionType(api.promotionType.code),
    startDate: api.startDate,
    endDate: api.endDate,
    isRecurring: api.isRecurring,
    recurrenceType: api.recurrenceType,
    recurrenceDayOfWeek: api.recurrenceDayOfWeek,
  };
};

// Convert API Floor to Component Floor
export const mapFloor = (api: ApiFloor): Floor => {
  // Try to extract number from floorNumber string
  const numberMatch = api.floorNumber.match(/\d+/);
  const number = numberMatch ? parseInt(numberMatch[0], 10) : 0;

  return {
    id: api.id.toString(),
    name: api.floorNumber,
    number,
  };
};

// Convert API Room to Component Room
export const mapRoom = (api: ApiRoom): Room => {
  return {
    id: api.id.toString(),
    name: api.name,
    floorId: api.floorId.toString(),
    isSmoking: api.isSmoking,
    isOutdoor: api.isOutdoor,
    imageId: api.imageId,
    mapImageUrl: getImageUrl(api.imageId),
    description: api.description,
  };
};

// Convert API Table to Component Table
export const mapTable = (api: ApiTable): Table => {
  // Note: x, y coordinates are not stored in DB
  // They will need to be calculated or simplified visualization used
  return {
    id: api.id.toString(),
    tableNumber: api.tableNumber,
    roomId: api.roomId.toString(),
    capacity: api.capacity,
    description: api.description,
    imageUrl: getImageUrl(api.imageId),
    images: api.imageId ? [getImageUrl(api.imageId)!] : undefined,
    deposit: api.depositAmount,
    // x, y coordinates not available - will be handled in TableSelectionPage
  };
};

// Helper to check if restaurant has outdoor or smoking rooms
export const checkRestaurantFeatures = (rooms: ApiRoom[]): { isOutdoor: boolean; isSmoking: boolean } => {
  return {
    isOutdoor: rooms.some(room => room.isOutdoor),
    isSmoking: rooms.some(room => room.isSmoking),
  };
};

