import type {
  ApiRestaurant,
  ApiMenuCategory,
  ApiPromotion,
  ApiFloor,
  ApiRoom,
  ApiTable,
  BookingRequest,
  BookingResponse,
} from '../types/restaurant';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';

export const apiClient = {
  get: async <T>(endpoint: string): Promise<T> => {
    const response = await fetch(`${API_BASE_URL}${endpoint}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },
  post: async <T>(endpoint: string, data: unknown): Promise<T> => {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },
};

// Restaurant API
export const restaurantApi = {
  // GET /client-api/r - список ресторанов
  getRestaurants: async (params?: {
    limit?: number;
    offset?: number;
    status?: string;
  }): Promise<ApiRestaurant[]> => {
    const queryParams = new URLSearchParams();
    if (params?.limit) queryParams.append('limit', params.limit.toString());
    if (params?.offset) queryParams.append('offset', params.offset.toString());
    if (params?.status) queryParams.append('status', params.status);
    
    const query = queryParams.toString();
    return apiClient.get<ApiRestaurant[]>(`/client-api/r${query ? `?${query}` : ''}`);
  },

  // GET /client-api/r/:id - информация о ресторане
  getRestaurant: async (id: number): Promise<ApiRestaurant> => {
    return apiClient.get<ApiRestaurant>(`/client-api/r/${id}`);
  },

  // GET /client-api/r/:id/menu - меню ресторана
  getMenu: async (id: number): Promise<ApiMenuCategory[]> => {
    const response = await apiClient.get<{ categories: ApiMenuCategory[] }>(`/client-api/r/${id}/menu`);
    return response.categories || [];
  },

  // GET /client-api/r/:id/floor - список этажей
  getFloors: async (id: number): Promise<ApiFloor[]> => {
    return apiClient.get<ApiFloor[]>(`/client-api/r/${id}/floor`);
  },

  // GET /client-api/r/:id/room - список залов
  getRooms: async (id: number, params?: { floorId?: number }): Promise<ApiRoom[]> => {
    const queryParams = new URLSearchParams();
    if (params?.floorId) queryParams.append('floorId', params.floorId.toString());
    
    const query = queryParams.toString();
    return apiClient.get<ApiRoom[]>(`/client-api/r/${id}/room${query ? `?${query}` : ''}`);
  },

  // GET /client-api/r/:id/room/:roomId - информация о зале
  getRoom: async (id: number, roomId: number): Promise<ApiRoom> => {
    return apiClient.get<ApiRoom>(`/client-api/r/${id}/room/${roomId}`);
  },

  // GET /client-api/r/:id/table - список столов
  getTables: async (id: number, params?: { roomId?: number; floorId?: number }): Promise<ApiTable[]> => {
    const queryParams = new URLSearchParams();
    if (params?.roomId) queryParams.append('roomId', params.roomId.toString());
    if (params?.floorId) queryParams.append('floorId', params.floorId.toString());
    
    const query = queryParams.toString();
    return apiClient.get<ApiTable[]>(`/client-api/r/${id}/table${query ? `?${query}` : ''}`);
  },

  // GET /client-api/r/:id/table/map - карта столов (иерархическая структура)
  getTableMap: async (id: number, params?: { floorId?: number; roomId?: number }): Promise<{
    floors: Array<{
      id: number;
      floorNumber: string;
      rooms: Array<{
        id: number;
        name: string;
        tables: ApiTable[];
        imageId?: number;
      }>;
    }>;
  }> => {
    const queryParams = new URLSearchParams();
    if (params?.floorId) queryParams.append('floorId', params.floorId.toString());
    if (params?.roomId) queryParams.append('roomId', params.roomId.toString());
    
    const query = queryParams.toString();
    return apiClient.get(`/client-api/r/${id}/table/map${query ? `?${query}` : ''}`);
  },

  // GET /client-api/r/:id/promotion - промо-события
  getPromotions: async (id: number, params?: {
    promotionTypeId?: number;
    isCurrent?: boolean;
    limit?: number;
    offset?: number;
  }): Promise<ApiPromotion[]> => {
    const queryParams = new URLSearchParams();
    if (params?.promotionTypeId) queryParams.append('promotionTypeId', params.promotionTypeId.toString());
    if (params?.isCurrent !== undefined) queryParams.append('isCurrent', params.isCurrent.toString());
    if (params?.limit) queryParams.append('limit', params.limit.toString());
    if (params?.offset) queryParams.append('offset', params.offset.toString());
    
    const query = queryParams.toString();
    return apiClient.get<ApiPromotion[]>(`/client-api/r/${id}/promotion${query ? `?${query}` : ''}`);
  },

  // GET /client-api/r/search - поиск ресторанов
  searchRestaurants: async (params: {
    q?: string;
    menu_item?: string;
    promotion?: string;
    promotion_type?: string;
    isOutdoor?: boolean;
    isSmoking?: boolean;
    lat?: number;
    lng?: number;
    radius?: number;
    limit?: number;
    offset?: number;
  }): Promise<ApiRestaurant[]> => {
    const queryParams = new URLSearchParams();
    if (params.q) queryParams.append('q', params.q);
    if (params.menu_item) queryParams.append('menu_item', params.menu_item);
    if (params.promotion) queryParams.append('promotion', params.promotion);
    if (params.promotion_type) queryParams.append('promotion_type', params.promotion_type);
    if (params.isOutdoor !== undefined) queryParams.append('isOutdoor', params.isOutdoor.toString());
    if (params.isSmoking !== undefined) queryParams.append('isSmoking', params.isSmoking.toString());
    if (params.lat) queryParams.append('lat', params.lat.toString());
    if (params.lng) queryParams.append('lng', params.lng.toString());
    if (params.radius) queryParams.append('radius', params.radius.toString());
    if (params.limit) queryParams.append('limit', params.limit.toString());
    if (params.offset) queryParams.append('offset', params.offset.toString());
    
    return apiClient.get<ApiRestaurant[]>(`/client-api/r/search?${queryParams.toString()}`);
  },

  // POST /client-api/r/:id/booking - создание бронирования
  createBooking: async (id: number, booking: BookingRequest): Promise<BookingResponse> => {
    return apiClient.post<BookingResponse>(`/client-api/r/${id}/booking`, booking);
  },
};

// Helper function to get image URL from imageId
// TODO: Replace with actual image service endpoint when available
export const getImageUrl = (imageId?: number): string | undefined => {
  if (!imageId) return undefined;
  // Placeholder - should be replaced with actual image service
  return `${API_BASE_URL}/client-api/images/${imageId}`;
};
