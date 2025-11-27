import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { ArrowLeft } from 'lucide-react';
import { BookingForm } from '../components/BookingForm';
import { ImageWithFallback } from '../components/figma/ImageWithFallback';
import { restaurantApi } from '../services/api';
import { mapRestaurant, mapRoom, mapTable } from '../utils/mappers';
import type { Restaurant, Room, Table } from '../types/restaurant';
import { toast } from 'sonner';

export function TableSelectionPage() {
  const { id, roomId } = useParams<{ id: string; roomId: string }>();
  const navigate = useNavigate();
  
  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
  const [room, setRoom] = useState<Room | null>(null);
  const [tables, setTables] = useState<Table[]>([]);
  const [selectedTableId, setSelectedTableId] = useState<string>('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id && roomId) {
      loadData();
    }
  }, [id, roomId]);

  const loadData = async () => {
    if (!id || !roomId) return;
    
    setIsLoading(true);
    setError(null);
    
    try {
      const restaurantId = parseInt(id, 10);
      const roomIdNum = parseInt(roomId, 10);
      
      if (isNaN(restaurantId) || isNaN(roomIdNum)) {
        setError('Неверный ID');
        return;
      }

      // Load restaurant
      const apiRestaurant = await restaurantApi.getRestaurant(restaurantId);
      const mappedRestaurant = mapRestaurant(apiRestaurant, false);
      setRestaurant(mappedRestaurant);

      // Load room
      const apiRoom = await restaurantApi.getRoom(restaurantId, roomIdNum);
      const mappedRoom = mapRoom(apiRoom);
      setRoom(mappedRoom);

      // Load tables
      const apiTables = await restaurantApi.getTables(restaurantId, { roomId: roomIdNum });
      const mappedTables = apiTables.map(mapTable);
      setTables(mappedTables);
    } catch (err: any) {
      console.error('Error loading data:', err);
      setError('Ошибка при загрузке данных');
      toast.error('Не удалось загрузить данные');
    } finally {
      setIsLoading(false);
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

  if (error || !restaurant || !room) {
    return (
      <div className="min-h-screen bg-gray-50 p-4">
        <div className="max-w-md mx-auto mt-8 text-center">
          <p className="text-gray-500 mb-4">{error || 'Данные не найдены'}</p>
          <Button onClick={() => navigate(`/r/${id}/booking/rooms`)} className="mt-4">
            Назад
          </Button>
        </div>
      </div>
    );
  }

  const selectedTable = tables.find(t => t.id === selectedTableId);

  // Simplified table visualization - since coordinates are not stored in DB
  // We'll show a simple grid/list of tables
  const handleTableClick = (tableId: string) => {
    setSelectedTableId(tableId);
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col h-screen">
      {/* Header */}
      <div className="bg-white border-b sticky top-0 z-20">
        <div className="p-4">
          <div className="flex items-center gap-3">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => navigate(`/r/${id}/booking/rooms`)}
            >
              <ArrowLeft className="w-5 h-5" />
            </Button>
            <div className="flex-1">
              <h1>{selectedTable ? `Стол ${selectedTable.tableNumber}` : 'Выберите стол'}</h1>
              <p className="text-gray-600">{room.name}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Content */}
      {selectedTable ? (
        <BookingForm
          restaurant={restaurant}
          room={room}
          table={selectedTable}
          onClose={() => setSelectedTableId('')}
          onBack={() => setSelectedTableId('')}
        />
      ) : (
        <div className="flex-1 overflow-y-auto p-4">
          {/* Room map image if available */}
          {room.mapImageUrl && (
            <div className="mb-4 rounded-lg overflow-hidden bg-gray-100">
              <ImageWithFallback
                src={room.mapImageUrl}
                alt={`План зала ${room.name}`}
                className="w-full h-auto"
              />
            </div>
          )}

          {/* Tables list/grid */}
          {tables.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              <p>Столы не найдены</p>
            </div>
          ) : (
            <div className="grid grid-cols-2 gap-4">
              {tables.map(table => (
                <div
                  key={table.id}
                  onClick={() => handleTableClick(table.id)}
                  className="bg-white rounded-xl p-4 border-2 border-gray-200 cursor-pointer transition-all active:scale-[0.98] hover:border-rose-300"
                >
                  <div className="text-center">
                    <div 
                      className="w-16 h-16 rounded-full mx-auto mb-2 flex items-center justify-center text-white text-xl font-semibold"
                      style={{ backgroundColor: restaurant.primaryColor }}
                    >
                      {table.tableNumber}
                    </div>
                    <h3 className="mb-1">Стол {table.tableNumber}</h3>
                    <p className="text-gray-600 text-sm">Вместимость: {table.capacity} чел.</p>
                    {table.description && (
                      <p className="text-gray-500 text-xs mt-1">{table.description}</p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default TableSelectionPage;

