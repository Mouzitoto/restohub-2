import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { ArrowLeft, ZoomIn, ZoomOut } from 'lucide-react';
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
  
  // Map interaction states
  const [zoom, setZoom] = useState(1);
  const [pan, setPan] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  const [lastPinchDistance, setLastPinchDistance] = useState<number | null>(null);
  const [lastPinchZoom, setLastPinchZoom] = useState(1);
  
  const mapContainerRef = useRef<HTMLDivElement>(null);

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

  // Zoom handlers
  const handleZoomIn = () => {
    setZoom(prev => Math.min(prev + 0.2, 3));
  };

  const handleZoomOut = () => {
    setZoom(prev => Math.max(prev - 0.2, 0.5));
  };

  // Mouse drag handlers
  const handleMouseDown = (e: React.MouseEvent) => {
    if (e.button !== 0) return; // Only left mouse button
    setIsDragging(true);
    setDragStart({ x: e.clientX - pan.x, y: e.clientY - pan.y });
  };

  const handleMouseMove = (e: React.MouseEvent) => {
    if (isDragging) {
      setPan({
        x: e.clientX - dragStart.x,
        y: e.clientY - dragStart.y
      });
    }
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  // Touch handlers for single touch (panning)
  const handleTouchStart = (e: React.TouchEvent) => {
    if (e.touches.length === 1) {
      setIsDragging(true);
      setDragStart({
        x: e.touches[0].clientX - pan.x,
        y: e.touches[0].clientY - pan.y
      });
      setLastPinchDistance(null);
    } else if (e.touches.length === 2) {
      // Pinch to zoom
      const touch1 = e.touches[0];
      const touch2 = e.touches[1];
      const distance = Math.hypot(
        touch2.clientX - touch1.clientX,
        touch2.clientY - touch1.clientY
      );
      setLastPinchDistance(distance);
      setLastPinchZoom(zoom);
      setIsDragging(false);
    }
  };

  const handleTouchMove = (e: React.TouchEvent) => {
    if (e.touches.length === 1 && isDragging) {
      // Single touch - panning
      setPan({
        x: e.touches[0].clientX - dragStart.x,
        y: e.touches[0].clientY - dragStart.y
      });
    } else if (e.touches.length === 2 && lastPinchDistance !== null) {
      // Two touches - pinch to zoom
      e.preventDefault();
      const touch1 = e.touches[0];
      const touch2 = e.touches[1];
      const distance = Math.hypot(
        touch2.clientX - touch1.clientX,
        touch2.clientY - touch1.clientY
      );
      
      const scale = distance / lastPinchDistance;
      const newZoom = Math.max(0.5, Math.min(3, lastPinchZoom * scale));
      setZoom(newZoom);
    }
  };

  const handleTouchEnd = () => {
    setIsDragging(false);
    setLastPinchDistance(null);
  };

  const handleTableClick = (tableId: string, e: React.MouseEvent | React.TouchEvent) => {
    e.stopPropagation();
    if (!isDragging) {
      setSelectedTableId(tableId);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col h-screen">
      {/* Back Button - выше заголовка когда выбран стол */}
      {selectedTable && (
        <div className="sticky top-0 z-30 bg-white border-b p-4">
          <Button 
            variant="ghost" 
            size="sm"
            onClick={() => setSelectedTableId('')}
            className="gap-2"
          >
            <ArrowLeft className="w-4 h-4" />
            Вернуться к карте столов
          </Button>
        </div>
      )}
      
      {/* Header */}
      <div className={`bg-white border-b sticky ${selectedTable ? 'top-[65px]' : 'top-0'} z-20`}>
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
              <h1 className="text-xl font-semibold">
                {selectedTable ? `Стол ${selectedTable.tableNumber}` : 'Выберите стол'}
              </h1>
              <p className="text-gray-600 text-sm">{room.name}</p>
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
        <div className="flex-1 overflow-hidden relative">
          {/* Floating info panel */}
          <div className="absolute bottom-6 left-1/2 transform -translate-x-1/2 z-30 bg-white rounded-lg shadow-lg border border-gray-200 px-6 py-4 max-w-md">
            <p className="text-center text-gray-700 text-sm">
              Кликните на стол на схеме, чтобы сделать бронирование
            </p>
          </div>

          {/* Zoom controls */}
          <div className="absolute top-4 right-4 z-30 flex flex-col gap-2 bg-white rounded-lg shadow-lg p-2">
            <Button
              variant="outline"
              size="icon"
              onClick={handleZoomIn}
              disabled={zoom >= 3}
              className="h-10 w-10"
            >
              <ZoomIn className="w-5 h-5" />
            </Button>
            <Button
              variant="outline"
              size="icon"
              onClick={handleZoomOut}
              disabled={zoom <= 0.5}
              className="h-10 w-10"
            >
              <ZoomOut className="w-5 h-5" />
            </Button>
          </div>

          {/* Interactive map container */}
          {room.mapImageUrl ? (
            <div
              ref={mapContainerRef}
              className={`w-full h-full overflow-hidden bg-gray-100 ${isDragging ? 'cursor-grabbing' : 'cursor-grab'}`}
              onMouseDown={handleMouseDown}
              onMouseMove={handleMouseMove}
              onMouseUp={handleMouseUp}
              onMouseLeave={handleMouseUp}
              onTouchStart={handleTouchStart}
              onTouchMove={handleTouchMove}
              onTouchEnd={handleTouchEnd}
            >
              <div
                className="relative origin-center"
                style={{
                  transform: `translate(${pan.x}px, ${pan.y}px) scale(${zoom})`,
                  transition: isDragging || lastPinchDistance !== null ? 'none' : 'transform 0.1s ease-out',
                  width: '100%',
                  height: '100%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <div className="relative" style={{ maxWidth: '100%', maxHeight: '100%' }}>
                  <ImageWithFallback
                    src={room.mapImageUrl}
                    alt={`План зала ${room.name}`}
                    className="max-w-full max-h-full object-contain"
                    draggable={false}
                  />
                  {/* Tables overlay */}
                  {tables.filter(table => 
                    table.positionX1 != null && 
                    table.positionY1 != null && 
                    table.positionX2 != null && 
                    table.positionY2 != null
                  ).map(table => {
                    const x1 = Math.min(table.positionX1!, table.positionX2!);
                    const x2 = Math.max(table.positionX1!, table.positionX2!);
                    const y1 = Math.min(table.positionY1!, table.positionY2!);
                    const y2 = Math.max(table.positionY1!, table.positionY2!);
                    
                    return (
                      <div
                        key={table.id}
                        onClick={(e) => handleTableClick(table.id, e)}
                        onTouchEnd={(e) => handleTableClick(table.id, e)}
                        className="absolute bg-transparent cursor-pointer hover:bg-blue-200 hover:bg-opacity-20 transition-colors"
                        style={{
                          left: `${x1}%`,
                          top: `${y1}%`,
                          width: `${x2 - x1}%`,
                          height: `${y2 - y1}%`,
                        }}
                        title={`Стол ${table.tableNumber} - Нажмите для бронирования`}
                      />
                    );
                  })}
                </div>
              </div>
            </div>
          ) : (
            <div className="flex items-center justify-center h-full">
              <div className="text-center text-gray-500">
                {tables.length === 0 ? (
                  <p>Столы не найдены</p>
                ) : (
                  <p>Схема зала не загружена</p>
                )}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default TableSelectionPage;

