import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { mockRestaurants } from '../data/mockData';
import { mockFloors, mockRooms, Room } from '../data/bookingMockData';
import { Button } from './ui/button';
import { ArrowLeft, Cigarette, CigaretteOff, Sun } from 'lucide-react';
import { Badge } from './ui/badge';

export function RoomSelectionPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const restaurant = mockRestaurants.find(r => r.id === id);
  
  const [selectedFloorId, setSelectedFloorId] = useState(mockFloors[0].id);
  const [rooms, setRooms] = useState<Room[]>([]);

  useEffect(() => {
    // Simulate API call to get rooms for selected floor
    const filteredRooms = mockRooms.filter(room => room.floorId === selectedFloorId);
    setRooms(filteredRooms);
  }, [selectedFloorId]);

  if (!restaurant) {
    return (
      <div className="p-4">
        <p>Ресторан не найден</p>
        <Button onClick={() => navigate('/')} className="mt-4">
          На главную
        </Button>
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
            </div>
          ))}
        </div>
      </div>

      {/* Floor Selector */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t p-4 z-10">
        <div className="flex justify-center gap-2">
          {mockFloors.map(floor => (
            <Button
              key={floor.id}
              variant={selectedFloorId === floor.id ? 'default' : 'outline'}
              onClick={() => setSelectedFloorId(floor.id)}
              style={
                selectedFloorId === floor.id
                  ? { backgroundColor: restaurant.primaryColor }
                  : {}
              }
            >
              {floor.name}
            </Button>
          ))}
        </div>
      </div>
    </div>
  );
}