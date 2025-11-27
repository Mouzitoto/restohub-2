import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { ArrowLeft, Cigarette, CigaretteOff, Sun } from 'lucide-react';
import { Badge } from '../components/ui/badge';
import { restaurantApi } from '../services/api';
import { mapFloor, mapRoom, mapRestaurant } from '../utils/mappers';
import { Floor, Room, Restaurant } from '../types/restaurant';
import { toast } from 'sonner';

export function RoomSelectionPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
  const [floors, setFloors] = useState<Floor[]>([]);
  const [selectedFloorId, setSelectedFloorId] = useState<string>('');
  const [rooms, setRooms] = useState<Room[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      loadData();
    }
  }, [id]);

  useEffect(() => {
    if (selectedFloorId && id) {
      loadRooms();
    }
  }, [selectedFloorId, id]);

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
      
      // Set first floor as selected
      if (mappedFloors.length > 0) {
        setSelectedFloorId(mappedFloors[0].id);
      }
    } catch (err: any) {
      console.error('Error loading data:', err);
      setError('Ошибка при загрузке данных');
      toast.error('Не удалось загрузить данные');
    } finally {
      setIsLoading(false);
    }
  };

  const loadRooms = async () => {
    if (!id || !selectedFloorId) return;
    
    try {
      const restaurantId = parseInt(id, 10);
      const floorId = parseInt(selectedFloorId, 10);
      
      const apiRooms = await restaurantApi.getRooms(restaurantId, { floorId });
      const mappedRooms = apiRooms.map(mapRoom);
      setRooms(mappedRooms);
    } catch (err: any) {
      console.error('Error loading rooms:', err);
      toast.error('Не удалось загрузить залы');
      setRooms([]);
    }
  };

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
    <div className="min-h-screen bg-gray-50 pb-24">
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
        {rooms.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            <p>Залы не найдены</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-4">
            {rooms.map(room => (
              <div
                key={room.id}
                onClick={() => navigate(`/r/${id}/booking/tables/${room.id}`)}
                className="bg-white rounded-xl p-6 border-2 border-gray-200 cursor-pointer transition-all active:scale-[0.98] hover:border-rose-300"
              >
                <div className="flex items-start justify-between">
                  <h2 className="flex-1">{room.name}</h2>
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
                  </div>
                </div>
                {room.description && (
                  <p className="text-gray-600 mt-2">{room.description}</p>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Floor Selector */}
      {floors.length > 1 && (
        <div className="fixed bottom-0 left-0 right-0 bg-white border-t p-4 z-10">
          <div className="flex justify-center gap-2 overflow-x-auto">
            {floors.map(floor => (
              <Button
                key={floor.id}
                variant={selectedFloorId === floor.id ? 'default' : 'outline'}
                onClick={() => setSelectedFloorId(floor.id)}
                style={
                  selectedFloorId === floor.id
                    ? { backgroundColor: restaurant.primaryColor }
                    : {}
                }
                className="flex-shrink-0"
              >
                {floor.name}
              </Button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

export default RoomSelectionPage;

