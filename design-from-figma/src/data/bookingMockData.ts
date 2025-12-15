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
  imageId?: string;
  mapImageUrl?: string;
}

export interface Table {
  id: string;
  tableNumber: string;
  roomId: string;
  capacity: number;
  description?: string;
  imageUrl?: string;
  images?: string[]; // Массив фотографий стола
  deposit?: number; // Депозит в тенге
  // Координаты на карте (в процентах)
  x: number;
  y: number;
}

export const mockFloors: Floor[] = [
  { id: '1', name: '1 этаж', number: 1 },
  { id: '2', name: '2 этаж', number: 2 },
  { id: '3', name: 'Крыша', number: 3 }
];

export const mockRooms: Room[] = [
  {
    id: '1',
    name: 'Главный зал',
    floorId: '1',
    isSmoking: false,
    isOutdoor: false,
    mapImageUrl: 'https://pekinperm.ru/storage/tiny/%D0%A1%D1%85%D0%B5%D0%BC%D0%B02.svg'
  },
  {
    id: '2',
    name: 'VIP зона',
    floorId: '1',
    isSmoking: true,
    isOutdoor: false,
    mapImageUrl: 'https://images.unsplash.com/photo-1559339352-11d035aa65de?w=1200'
  },
  {
    id: '3',
    name: 'Летняя терраса',
    floorId: '1',
    isSmoking: false,
    isOutdoor: true,
    mapImageUrl: 'https://images.unsplash.com/photo-1466978913421-dad2ebd01d17?w=1200'
  },
  {
    id: '4',
    name: 'Банкетный зал',
    floorId: '2',
    isSmoking: false,
    isOutdoor: false,
    mapImageUrl: 'https://images.unsplash.com/photo-1552566626-52f8b828add9?w=1200'
  },
  {
    id: '5',
    name: 'Лаунж бар',
    floorId: '2',
    isSmoking: true,
    isOutdoor: false,
    mapImageUrl: 'https://images.unsplash.com/photo-1514933651103-005eec06c04b?w=1200'
  },
  {
    id: '6',
    name: 'Панорамный зал',
    floorId: '3',
    isSmoking: false,
    isOutdoor: true,
    mapImageUrl: 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=1200'
  }
];

export const mockTables: Table[] = [
  // Главный зал
  { 
    id: '1', 
    tableNumber: '1', 
    roomId: '1', 
    capacity: 4, 
    x: 20, 
    y: 20,
    images: [
      'https://images.unsplash.com/photo-1640703935937-5e6ec134977d?w=800',
      'https://images.unsplash.com/photo-1758977403438-1b8546560d31?w=800',
      'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800'
    ]
  },
  { 
    id: '2', 
    tableNumber: '2', 
    roomId: '1', 
    capacity: 2, 
    x: 40, 
    y: 25,
    images: [
      'https://images.unsplash.com/photo-1640703935937-5e6ec134977d?w=800',
      'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800'
    ]
  },
  { 
    id: '3', 
    tableNumber: '3', 
    roomId: '1', 
    capacity: 6, 
    x: 60, 
    y: 30,
    images: [
      'https://images.unsplash.com/photo-1758977403438-1b8546560d31?w=800',
      'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800'
    ]
  },
  { 
    id: '4', 
    tableNumber: '4', 
    roomId: '1', 
    capacity: 4, 
    x: 80, 
    y: 20,
    images: [
      'https://images.unsplash.com/photo-1640703935937-5e6ec134977d?w=800'
    ]
  },
  { 
    id: '5', 
    tableNumber: '5', 
    roomId: '1', 
    capacity: 2, 
    x: 30, 
    y: 60,
    images: [
      'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800'
    ]
  },
  { 
    id: '6', 
    tableNumber: '6', 
    roomId: '1', 
    capacity: 8, 
    x: 70, 
    y: 65,
    images: [
      'https://images.unsplash.com/photo-1758977403438-1b8546560d31?w=800',
      'https://images.unsplash.com/photo-1640703935937-5e6ec134977d?w=800'
    ]
  },
  
  // VIP зона
  { 
    id: '7', 
    tableNumber: '1', 
    roomId: '2', 
    capacity: 6, 
    x: 30, 
    y: 40, 
    description: 'Премиум стол у окна',
    deposit: 5000,
    images: [
      'https://images.unsplash.com/photo-1708517194326-6077b788f04b?w=800',
      'https://images.unsplash.com/photo-1758977403438-1b8546560d31?w=800',
      'https://images.unsplash.com/photo-1559339352-11d035aa65de?w=800'
    ]
  },
  { 
    id: '8', 
    tableNumber: '2', 
    roomId: '2', 
    capacity: 8, 
    x: 70, 
    y: 50, 
    description: 'Большой стол для компании',
    deposit: 7000,
    images: [
      'https://images.unsplash.com/photo-1708517194326-6077b788f04b?w=800',
      'https://images.unsplash.com/photo-1758977403438-1b8546560d31?w=800'
    ]
  },
  
  // Летняя терраса
  { 
    id: '9', 
    tableNumber: '1', 
    roomId: '3', 
    capacity: 4, 
    x: 25, 
    y: 35,
    images: [
      'https://images.unsplash.com/photo-1466978913421-dad2ebd01d17?w=800',
      'https://images.unsplash.com/photo-1640703935937-5e6ec134977d?w=800'
    ]
  },
  { 
    id: '10', 
    tableNumber: '2', 
    roomId: '3', 
    capacity: 4, 
    x: 50, 
    y: 40,
    images: [
      'https://images.unsplash.com/photo-1466978913421-dad2ebd01d17?w=800'
    ]
  },
  { 
    id: '11', 
    tableNumber: '3', 
    roomId: '3', 
    capacity: 2, 
    x: 75, 
    y: 35,
    images: [
      'https://images.unsplash.com/photo-1466978913421-dad2ebd01d17?w=800',
      'https://images.unsplash.com/photo-1640703935937-5e6ec134977d?w=800'
    ]
  },
  
  // Банкетный зал
  { 
    id: '12', 
    tableNumber: '1', 
    roomId: '4', 
    capacity: 10, 
    x: 50, 
    y: 50, 
    description: 'Центральный банкетный стол',
    deposit: 10000,
    images: [
      'https://images.unsplash.com/photo-1552566626-52f8b828add9?w=800',
      'https://images.unsplash.com/photo-1758977403438-1b8546560d31?w=800',
      'https://images.unsplash.com/photo-1640703935937-5e6ec134977d?w=800'
    ]
  },
  
  // Лаунж бар
  { 
    id: '13', 
    tableNumber: '1', 
    roomId: '5', 
    capacity: 4, 
    x: 30, 
    y: 30,
    images: [
      'https://images.unsplash.com/photo-1514933651103-005eec06c04b?w=800',
      'https://images.unsplash.com/photo-1640703935937-5e6ec134977d?w=800'
    ]
  },
  { 
    id: '14', 
    tableNumber: '2', 
    roomId: '5', 
    capacity: 4, 
    x: 70, 
    y: 30,
    images: [
      'https://images.unsplash.com/photo-1514933651103-005eec06c04b?w=800'
    ]
  },
  { 
    id: '15', 
    tableNumber: '3', 
    roomId: '5', 
    capacity: 2, 
    x: 50, 
    y: 70,
    images: [
      'https://images.unsplash.com/photo-1514933651103-005eec06c04b?w=800',
      'https://images.unsplash.com/photo-1640703935937-5e6ec134977d?w=800'
    ]
  },
  
  // Панорамный зал
  { 
    id: '16', 
    tableNumber: '1', 
    roomId: '6', 
    capacity: 6, 
    x: 35, 
    y: 45, 
    description: 'Стол с видом на город',
    deposit: 3000,
    images: [
      'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800',
      'https://images.unsplash.com/photo-1758977403438-1b8546560d31?w=800',
      'https://images.unsplash.com/photo-1640703935937-5e6ec134977d?w=800'
    ]
  },
  { 
    id: '17', 
    tableNumber: '2', 
    roomId: '6', 
    capacity: 4, 
    x: 65, 
    y: 45, 
    description: 'Романтический столик',
    deposit: 2000,
    images: [
      'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800',
      'https://images.unsplash.com/photo-1640703935937-5e6ec134977d?w=800'
    ]
  }
];