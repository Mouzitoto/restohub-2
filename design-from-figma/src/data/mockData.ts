export interface Restaurant {
  id: string;
  name: string;
  logoUrl: string;
  backgroundUrl: string;
  address: string;
  description: string;
  phone: string;
  email: string;
  cuisineType: string;
  rating: number;
  distance: number;
  hasPromotions: boolean;
  isOutdoor: boolean;
  isSmoking: boolean;
  lat: number;
  lng: number;
  primaryColor: string;
  menuCategories: MenuCategory[];
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
  imageUrl: string;
  weight?: string;
  isNew?: boolean;
  hasPromo?: boolean;
}

export interface Promotion {
  id: string;
  title: string;
  description: string;
  imageUrl: string;
  type: 'PROMOTION' | 'THEME_NIGHT' | 'NEW';
  startDate: string;
  endDate?: string;
  isRecurring: boolean;
  recurrenceType?: 'DAILY' | 'WEEKLY' | 'MONTHLY';
  recurrenceDayOfWeek?: number;
}

export const mockRestaurants: Restaurant[] = [
  {
    id: '1',
    name: 'Bella Italia',
    logoUrl: 'https://images.unsplash.com/photo-1514933651103-005eec06c04b?w=400',
    backgroundUrl: 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=1200',
    address: 'ул. Пушкина, 15',
    description: 'Аутентичная итальянская кухня в сердце города',
    phone: '+7 (495) 123-45-67',
    email: 'info@bellaitalia.ru',
    cuisineType: 'Итальянская',
    rating: 4.8,
    distance: 0.5,
    hasPromotions: true,
    isOutdoor: true,
    isSmoking: false,
    lat: 55.7558,
    lng: 37.6173,
    primaryColor: '#D32F2F',
    establishmentType: 'restaurant',
    instagram: 'bellaitalia',
    whatsapp: '79951234567',
    website: 'https://bellaitalia.ru',
    menuCategories: [
      {
        id: 'appetizers',
        name: 'Закуски',
        imageUrl: 'https://images.unsplash.com/photo-1608897013039-887f21d8c804?w=400',
        dishes: [
          {
            id: 'bruschetta',
            name: 'Брускетта',
            description: 'Поджаренный хлеб с томатами и базиликом',
            price: 450,
            weight: '150 г',
            isNew: true,
            imageUrl: 'https://images.unsplash.com/photo-1572695157366-5e585ab2b69f?w=400'
          },
          {
            id: 'caprese',
            name: 'Капрезе',
            description: 'Моцарелла, томаты, базилик, оливковое масло',
            price: 650,
            weight: '200 г',
            imageUrl: 'https://images.unsplash.com/photo-1608897013039-887f21d8c804?w=400'
          }
        ]
      },
      {
        id: 'pasta',
        name: 'Паста',
        imageUrl: 'https://images.unsplash.com/photo-1612874742237-6526221588e3?w=400',
        dishes: [
          {
            id: 'carbonara',
            name: 'Карбонара',
            description: 'Спагетти с беконом, яйцом и пармезаном',
            price: 890,
            weight: '300 г',
            imageUrl: 'https://images.unsplash.com/photo-1612874742237-6526221588e3?w=400'
          },
          {
            id: 'bolognese',
            name: 'Болоньезе',
            description: 'Паста с мясным соусом',
            price: 850,
            weight: '320 г',
            imageUrl: 'https://images.unsplash.com/photo-1627662168781-c5e1c9a80d0c?w=400'
          }
        ]
      },
      {
        id: 'pizza',
        name: 'Пицца',
        imageUrl: 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400',
        dishes: [
          {
            id: 'margherita',
            name: 'Маргарита',
            description: 'Томатный соус, моцарелла, базилик',
            price: 790,
            weight: '450 г',
            hasPromo: true,
            imageUrl: 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400'
          }
        ]
      }
    ]
  },
  {
    id: '2',
    name: 'Суши Мастер',
    logoUrl: 'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=400',
    backgroundUrl: 'https://images.unsplash.com/photo-1552566626-52f8b828add9?w=1200',
    address: 'пр. Мира, 32',
    description: 'Японская кухня премиум класса',
    phone: '+7 (495) 234-56-78',
    email: 'info@sushimaster.ru',
    cuisineType: 'Японская',
    rating: 4.9,
    distance: 1.2,
    hasPromotions: true,
    isOutdoor: false,
    isSmoking: true,
    lat: 55.7658,
    lng: 37.6273,
    primaryColor: '#1976D2',
    establishmentType: 'restaurant',
    menuCategories: [
      {
        id: 'rolls',
        name: 'Роллы',
        imageUrl: 'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=400',
        dishes: [
          {
            id: 'california',
            name: 'Калифорния',
            description: 'Краб, авокадо, огурец, икра тобико',
            price: 690,
            weight: '8 шт',
            imageUrl: 'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=400'
          },
          {
            id: 'philadelphia',
            name: 'Филадельфия',
            description: 'Лосось, сливочный сыр, огурец',
            price: 890,
            weight: '8 шт',
            imageUrl: 'https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?w=400'
          }
        ]
      }
    ]
  },
  {
    id: '3',
    name: 'Грузинский Дворик',
    logoUrl: 'https://images.unsplash.com/photo-1544148103-0773bf10d330?w=400',
    backgroundUrl: 'https://images.unsplash.com/photo-1559339352-11d035aa65de?w=1200',
    address: 'ул. Арбат, 7',
    description: 'Настоящие грузинские хинкали и хачапури',
    phone: '+7 (495) 345-67-89',
    email: 'info@georgian.ru',
    cuisineType: 'Грузинская',
    rating: 4.7,
    distance: 2.0,
    hasPromotions: false,
    isOutdoor: true,
    isSmoking: true,
    lat: 55.7458,
    lng: 37.6073,
    primaryColor: '#C62828',
    establishmentType: 'restaurant',
    menuCategories: [
      {
        id: 'khinkali',
        name: 'Хинкали',
        imageUrl: 'https://images.unsplash.com/photo-1626200419199-391ae4be7a41?w=400',
        dishes: [
          {
            id: 'khinkali-meat',
            name: 'Хинкали с мясом',
            description: 'Традиционные грузинские хинкали',
            price: 120,
            weight: '1 шт',
            imageUrl: 'https://images.unsplash.com/photo-1626200419199-391ae4be7a41?w=400'
          }
        ]
      }
    ]
  },
  {
    id: '4',
    name: 'Веранда',
    logoUrl: 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=400',
    backgroundUrl: 'https://images.unsplash.com/photo-1466978913421-dad2ebd01d17?w=1200',
    address: 'Парковая ул., 24',
    description: 'Европейская кухня с видом на парк',
    phone: '+7 (495) 456-78-90',
    email: 'info@veranda.ru',
    cuisineType: 'Европейская',
    rating: 4.6,
    distance: 0.8,
    hasPromotions: true,
    isOutdoor: true,
    isSmoking: false,
    lat: 55.7358,
    lng: 37.5973,
    primaryColor: '#388E3C',
    establishmentType: 'cafe',
    menuCategories: []
  }
];

export const mockPromotions: Record<string, Promotion[]> = {
  '1': [
    {
      id: 'p1',
      title: 'Счастливые часы',
      description: 'Скидка 30% на все напитки с 15:00 до 18:00',
      imageUrl: 'https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?w=800',
      type: 'PROMOTION',
      startDate: '2025-01-01',
      endDate: '2025-12-31',
      isRecurring: true,
      recurrenceType: 'DAILY'
    },
    {
      id: 'p2',
      title: 'Итальянская ночь',
      description: 'Живая музыка, традиционные танцы и специальное меню',
      imageUrl: 'https://images.unsplash.com/photo-1511795409834-ef04bbd61622?w=800',
      type: 'THEME_NIGHT',
      startDate: '2025-11-01',
      isRecurring: true,
      recurrenceType: 'WEEKLY',
      recurrenceDayOfWeek: 5
    }
  ],
  '2': [
    {
      id: 'p3',
      title: 'Новинка: Суши-сет "Токио"',
      description: 'Попробуйте наш новый авторский сет из 32 роллов',
      imageUrl: 'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=800',
      type: 'NEW',
      startDate: '2025-11-10',
      endDate: '2025-12-31',
      isRecurring: false
    }
  ],
  '4': [
    {
      id: 'p4',
      title: 'Бранч по выходным',
      description: 'Специальное меню на завтрак с 10:00 до 14:00',
      imageUrl: 'https://images.unsplash.com/photo-1533777419517-239796f920ed?w=800',
      type: 'PROMOTION',
      startDate: '2025-11-01',
      isRecurring: true,
      recurrenceType: 'WEEKLY',
      recurrenceDayOfWeek: 6
    }
  ]
};

export const cuisineTypes = ['Итальянская', 'Японская', 'Грузинская', 'Европейская'];