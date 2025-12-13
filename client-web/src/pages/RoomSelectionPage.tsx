import { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { ArrowLeft, Cigarette, CigaretteOff, Sun, Music } from 'lucide-react';
import { Badge } from '../components/ui/badge';
import { restaurantApi } from '../services/api';
import { mapFloor, mapRoom, mapRestaurant } from '../utils/mappers';
import type { Floor, Room, Restaurant } from '../types/restaurant';
import { toast } from 'sonner';

export function RoomSelectionPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
  const [floors, setFloors] = useState<Floor[]>([]);
  const [rooms, setRooms] = useState<Room[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      loadData();
    }
  }, [id]);

  const loadData = async () => {
    if (!id) return;
    
    setIsLoading(true);
    setError(null);
    
    try {
      const restaurantId = parseInt(id, 10);
      if (isNaN(restaurantId)) {
        setError('Неверный ID ресторана');
        return;
      }

      // Load restaurant
      const apiRestaurant = await restaurantApi.getRestaurant(restaurantId);
      const mappedRestaurant = mapRestaurant(apiRestaurant, false);
      setRestaurant(mappedRestaurant);

      // Load floors
      const apiFloors = await restaurantApi.getFloors(restaurantId);
      const mappedFloors = apiFloors.map(mapFloor);
      setFloors(mappedFloors);
      
      // Load all rooms (without floor filter)
      await loadRooms(restaurantId);
    } catch (err: any) {
      console.error('Error loading data:', err);
      setError('Ошибка при загрузке данных');
      toast.error('Не удалось загрузить данные');
    } finally {
      setIsLoading(false);
    }
  };

  const loadRooms = async (restaurantId?: number) => {
    const targetRestaurantId = restaurantId || (id ? parseInt(id, 10) : null);
    if (!targetRestaurantId) return;
    
    try {
      // Load all rooms without floor filter
      const apiRooms = await restaurantApi.getRooms(targetRestaurantId);
      const mappedRooms = apiRooms.map(mapRoom);
      setRooms(mappedRooms);
    } catch (err: any) {
      console.error('Error loading rooms:', err);
      toast.error('Не удалось загрузить залы');
      setRooms([]);
    }
  };

  // Sort rooms by floor number (ascending)
  const sortedRooms = useMemo(() => {
    return [...rooms].sort((a, b) => {
      const floorA = floors.find(f => f.id === a.floorId);
      const floorB = floors.find(f => f.id === b.floorId);
      
      // If floor not found, put at the end
      if (!floorA && !floorB) return 0;
      if (!floorA) return 1;
      if (!floorB) return -1;
      
      // Compare by floor number
      return floorA.number - floorB.number;
    });
  }, [rooms, floors]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-pulse text-gray-500">Загрузка...</div>
        </div>
      </div>
    );
  }

  if (error || !restaurant) {
    return (
      <div className="min-h-screen bg-gray-50 p-4">
        <div className="max-w-md mx-auto mt-8 text-center">
          <p className="text-gray-500 mb-4">{error || 'Ресторан не найден'}</p>
          <Button onClick={() => navigate(`/r/${id}`)} className="mt-4">
            Назад
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b sticky top-0 z-10">
        <div className="p-4 flex items-center gap-3">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => navigate(`/r/${id}`)}
          >
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div className="flex-1">
            <h1>Выберите зал</h1>
            <p className="text-gray-600">{restaurant.name}</p>
          </div>
        </div>
      </div>

      {/* Rooms Grid */}
      <div className="p-4">
        {sortedRooms.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            <p>Залы не найдены</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-4">
            {sortedRooms.map(room => {
              const floor = floors.find(f => f.id === room.floorId);
              return (
                <div
                  key={room.id}
                  onClick={() => navigate(`/r/${id}/booking/tables/${room.id}`)}
                  className="bg-white rounded-xl p-6 border-2 border-gray-200 cursor-pointer transition-all active:scale-[0.98] hover:border-rose-300"
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h2 className="mb-1">{room.name}</h2>
                      {floor && (
                        <p className="text-sm text-gray-500">Этаж {floor.name}</p>
                      )}
                    </div>
                    <div className="flex gap-2 flex-wrap justify-end">
                      {room.isSmoking ? (
                        <Badge variant="outline" className="border-amber-300">
                          <Cigarette className="w-4 h-4 mr-1 text-amber-600" />
                          Курящий
                        </Badge>
                      ) : (
                        <Badge variant="outline" className="border-green-300">
                          <CigaretteOff className="w-4 h-4 mr-1 text-green-600" />
                          Некурящий
                        </Badge>
                      )}
                      {room.isOutdoor && (
                        <Badge variant="outline" className="border-blue-300">
                          <Sun className="w-4 h-4 mr-1 text-blue-600" />
                          Открытый воздух
                        </Badge>
                      )}
                      {room.isLiveMusic && (
                        <Badge variant="outline" className="border-purple-300">
                          <Music className="w-4 h-4 mr-1 text-purple-600" />
                          Живая музыка
                        </Badge>
                      )}
                    </div>
                  </div>
                  {room.description && (
                    <p className="text-gray-600 mt-2">{room.description}</p>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>

    </div>
  );
}

export default RoomSelectionPage;

