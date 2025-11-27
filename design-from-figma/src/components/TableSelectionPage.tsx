import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { mockRestaurants } from '../data/mockData';
import { mockRooms, mockTables, Table } from '../data/bookingMockData';
import { Button } from './ui/button';
import { ArrowLeft, ZoomIn, ZoomOut, Maximize2 } from 'lucide-react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { BookingForm } from './BookingForm';
import { ImageWithFallback } from './figma/ImageWithFallback';

export function TableSelectionPage() {
  const { id, roomId } = useParams<{ id: string; roomId: string }>();
  const navigate = useNavigate();
  const restaurant = mockRestaurants.find(r => r.id === id);
  const room = mockRooms.find(r => r.id === roomId);
  
  const [tables, setTables] = useState<Table[]>([]);
  const [selectedTableId, setSelectedTableId] = useState<string>('hide');
  const [zoom, setZoom] = useState(1);
  const [pan, setPan] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  
  const mapRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // Simulate API call to get tables for the room
    const roomTables = mockTables.filter(table => table.roomId === roomId);
    setTables(roomTables);
  }, [roomId]);

  if (!restaurant || !room) {
    return (
      <div className="p-4">
        <p>Зал не найден</p>
        <Button onClick={() => navigate('/')} className="mt-4">
          На главную
        </Button>
      </div>
    );
  }

  const selectedTable = tables.find(t => t.id === selectedTableId);

  const handleZoomIn = () => {
    setZoom(prev => Math.min(prev + 0.2, 3));
  };

  const handleZoomOut = () => {
    setZoom(prev => Math.max(prev - 0.2, 0.5));
  };

  const handleResetZoom = () => {
    setZoom(1);
    setPan({ x: 0, y: 0 });
  };

  const handleMouseDown = (e: React.MouseEvent) => {
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

  const handleTouchStart = (e: React.TouchEvent) => {
    if (e.touches.length === 1) {
      setIsDragging(true);
      setDragStart({
        x: e.touches[0].clientX - pan.x,
        y: e.touches[0].clientY - pan.y
      });
    }
  };

  const handleTouchMove = (e: React.TouchEvent) => {
    if (isDragging && e.touches.length === 1) {
      setPan({
        x: e.touches[0].clientX - dragStart.x,
        y: e.touches[0].clientY - dragStart.y
      });
    }
  };

  const handleTouchEnd = () => {
    setIsDragging(false);
  };

  const handleTableClick = (tableId: string, e: React.MouseEvent | React.TouchEvent) => {
    e.stopPropagation();
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

      {/* Map Container - показывается когда стол не выбран */}
      {!selectedTable && (
        <div className="flex-1 relative overflow-hidden bg-gray-100">
          {/* Legend - горизонтально наверху */}
          <div className="absolute top-4 left-4 right-4 z-10 flex justify-center">
            <div className="bg-white rounded-lg shadow-lg px-4 py-2 flex items-center gap-4">
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 rounded-full bg-blue-500"></div>
                <span className="text-gray-700">Доступный стол</span>
              </div>
            </div>
          </div>

          <div
            ref={mapRef}
            className={`w-full h-full ${isDragging ? 'cursor-grabbing' : 'cursor-grab'}`}
            onMouseDown={handleMouseDown}
            onMouseMove={handleMouseMove}
            onMouseUp={handleMouseUp}
            onMouseLeave={handleMouseUp}
            onTouchStart={handleTouchStart}
            onTouchMove={handleTouchMove}
            onTouchEnd={handleTouchEnd}
          >
            <div
              style={{
                transform: `translate(${pan.x}px, ${pan.y}px) scale(${zoom})`,
                transformOrigin: 'center center',
                transition: isDragging ? 'none' : 'transform 0.1s ease-out',
                width: '800px',
                height: '600px',
                position: 'absolute',
                left: '50%',
                top: '50%',
                marginLeft: '-400px',
                marginTop: '-300px'
              }}
            >
              {/* Background Image */}
              {room.mapImageUrl && (
                <div className="absolute inset-0 flex items-center justify-center">
                  <ImageWithFallback
                    src={room.mapImageUrl}
                    alt={room.name}
                    className="w-full h-full object-cover opacity-30 rounded-lg"
                  />
                </div>
              )}

              {/* Tables */}
              <svg 
                className="absolute inset-0 w-full h-full"
                viewBox="0 0 100 100"
                preserveAspectRatio="none"
              >
                {tables.map(table => {
                  return (
                    <g 
                      key={table.id}
                      style={{ cursor: 'pointer' }}
                      onMouseDown={(e) => {
                        e.stopPropagation();
                      }}
                      onClick={(e) => {
                        e.stopPropagation();
                        handleTableClick(table.id, e);
                      }}
                    >
                      <circle
                        cx={table.x}
                        cy={table.y}
                        r={zoom > 1.5 ? '4' : '3'}
                        fill="#3B82F6"
                        fillOpacity="0.7"
                        stroke="#1E40AF"
                        strokeWidth="0.5"
                      />
                      <text
                        x={table.x}
                        y={table.y}
                        textAnchor="middle"
                        dominantBaseline="middle"
                        fill="white"
                        fontSize={zoom > 1.5 ? '2.5' : '2'}
                        fontWeight="bold"
                        style={{ pointerEvents: 'none' }}
                      >
                        {table.tableNumber}
                      </text>
                    </g>
                  );
                })}
              </svg>
            </div>
          </div>

          {/* Zoom Controls */}
          <div className="absolute top-20 right-4 flex flex-col gap-2 bg-white rounded-lg shadow-lg p-2">
            <Button
              variant="outline"
              size="icon"
              onClick={handleZoomIn}
              disabled={zoom >= 3}
            >
              <ZoomIn className="w-4 h-4" />
            </Button>
            <Button
              variant="outline"
              size="icon"
              onClick={handleZoomOut}
              disabled={zoom <= 0.5}
            >
              <ZoomOut className="w-4 h-4" />
            </Button>
            <Button
              variant="outline"
              size="icon"
              onClick={handleResetZoom}
            >
              <Maximize2 className="w-4 h-4" />
            </Button>
          </div>

          {/* Instruction overlay */}
          <div className="absolute inset-x-0 bottom-4 mx-4 bg-white rounded-lg shadow-lg p-4 border-2 border-blue-300">
            <p className="text-center text-blue-900">
              👆 Нажмите на стол на карте для бронирования
            </p>
          </div>
        </div>
      )}

      {/* Booking Form (appears when table is selected) */}
      {selectedTable && selectedTableId !== 'hide' && (
        <BookingForm
          restaurant={restaurant}
          room={room}
          table={selectedTable}
          onClose={() => setSelectedTableId('hide')}
          onBack={() => setSelectedTableId('hide')}
        />
      )}
    </div>
  );
}