import { useState, useRef, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import type { Restaurant, Room, Table } from '../types/restaurant';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Card } from './ui/card';
import { Calendar as CalendarIcon, Clock, Users, ShoppingCart } from 'lucide-react';
import { Calendar } from './ui/calendar';
import { ImageWithFallback } from './figma/ImageWithFallback';
import { PreOrderModal } from './PreOrderModal';
import { ImageModal } from './ImageModal';
import { restaurantApi } from '../services/api';
import { toast } from 'sonner';

interface BookingFormProps {
  restaurant: Restaurant;
  room: Room;
  table: Table;
  onClose: () => void;
  onBack?: () => void;
}

interface PreOrderItem {
  menuItemId: string;
  name: string;
  quantity: number;
  price: number;
  specialRequests: string | null;
}

export function BookingForm({ restaurant, table }: BookingFormProps) {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  
  const [formData, setFormData] = useState({
    date: '',
    time: '',
    personCount: 2,
    clientFirstName: '',
    clientName: '',
    specialRequests: ''
  });
  
  const [preOrderItems, setPreOrderItems] = useState<PreOrderItem[]>([]);
  const [showPreOrderModal, setShowPreOrderModal] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showImageModal, setShowImageModal] = useState(false);
  const [showTimePicker, setShowTimePicker] = useState(false);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [showPersonCountPicker, setShowPersonCountPicker] = useState(false);
  const timePickerRef = useRef<HTMLDivElement>(null);
  const timeInputRef = useRef<HTMLInputElement>(null);
  const dateInputRef = useRef<HTMLInputElement>(null);
  const datePickerRef = useRef<HTMLDivElement>(null);
  const personCountInputRef = useRef<HTMLInputElement>(null);
  const personCountPickerRef = useRef<HTMLDivElement>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (formData.personCount > table.capacity) {
      toast.error(`Максимальная вместимость стола: ${table.capacity} человек`);
      return;
    }

    if (!formData.date || !formData.time) {
      toast.error('Пожалуйста, выберите дату и время');
      return;
    }

    setIsSubmitting(true);
    
    try {
      const restaurantId = parseInt(id!, 10);
      const tableId = parseInt(table.id, 10);
      
      // Format date and time for API
      const bookingDate = formData.date; // YYYY-MM-DD
      const bookingTime = formData.time; // HH:mm:ss
      
      const bookingResponse = await restaurantApi.createBooking(restaurantId, {
        tableId,
        date: bookingDate,
        time: bookingTime,
        personCount: formData.personCount,
        clientFirstName: formData.clientFirstName || undefined,
        clientName: formData.clientName || undefined,
        specialRequests: formData.specialRequests || undefined,
        preOrderItems: preOrderItems.map(item => ({
          menuItemId: parseInt(item.menuItemId, 10),
          quantity: item.quantity,
          specialRequests: item.specialRequests || undefined,
        })),
      });

      toast.success('Бронирование создано!');
      navigate(`/r/${id}/booking/confirm/${bookingResponse.id}`, {
        state: { bookingResponse }
      });
    } catch (err: any) {
      console.error('Error creating booking:', err);
      toast.error('Не удалось создать бронирование');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleAddPreOrder = (items: PreOrderItem[]) => {
    setPreOrderItems(items);
    setShowPreOrderModal(false);
  };

  const totalPreOrderCost = preOrderItems.reduce((sum, item) => sum + item.price * item.quantity, 0);

  // Get today's date in YYYY-MM-DD format
  const today = new Date().toISOString().split('T')[0];

  // Generate hours (8-23), filtered if today is selected
  const getAvailableHours = () => {
    const allHours = Array.from({ length: 16 }, (_, i) => i + 8);
    if (formData.date === today) {
      const currentHour = new Date().getHours();
      return allHours.filter(hour => hour >= currentHour);
    }
    return allHours;
  };
  const hours = getAvailableHours();
  
  // Generate minutes (00, 15, 30, 45)
  const minutes = [0, 15, 30, 45];

  // Close pickers when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        timePickerRef.current &&
        !timePickerRef.current.contains(event.target as Node) &&
        timeInputRef.current &&
        !timeInputRef.current.contains(event.target as Node)
      ) {
        setShowTimePicker(false);
      }
      if (
        datePickerRef.current &&
        !datePickerRef.current.contains(event.target as Node) &&
        dateInputRef.current &&
        !dateInputRef.current.contains(event.target as Node)
      ) {
        setShowDatePicker(false);
      }
      if (
        personCountPickerRef.current &&
        !personCountPickerRef.current.contains(event.target as Node) &&
        personCountInputRef.current &&
        !personCountInputRef.current.contains(event.target as Node)
      ) {
        setShowPersonCountPicker(false);
      }
    };

    if (showTimePicker || showDatePicker || showPersonCountPicker) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => {
        document.removeEventListener('mousedown', handleClickOutside);
      };
    }
  }, [showTimePicker, showDatePicker, showPersonCountPicker]);

  const handleTimeInputClick = (e?: React.MouseEvent | React.FocusEvent) => {
    if (e) {
      e.preventDefault();
    }
    setShowTimePicker(true);
    setShowDatePicker(false);
    setShowPersonCountPicker(false);
  };

  const handleTimeSelect = (hour: number, minute: number) => {
    const timeString = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
    setFormData({ ...formData, time: timeString });
  };

  const handleDateInputClick = (e: React.MouseEvent | React.FocusEvent) => {
    e.preventDefault();
    setShowDatePicker(true);
    setShowTimePicker(false);
    setShowPersonCountPicker(false);
  };

  const handleOtherFieldFocus = () => {
    setShowTimePicker(false);
    setShowDatePicker(false);
    setShowPersonCountPicker(false);
  };

  const handlePersonCountInputClick = (e?: React.MouseEvent | React.FocusEvent) => {
    if (e) {
      e.preventDefault();
    }
    setShowPersonCountPicker(true);
    setShowTimePicker(false);
    setShowDatePicker(false);
  };

  const handlePersonCountSelect = (count: number) => {
    setFormData({ ...formData, personCount: count });
    setShowPersonCountPicker(false);
  };

  return (
    <>
      <div className="flex-1 bg-white overflow-y-auto pb-20">
        <div className="p-4">
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Table Photos */}
            {table.imageUrl && (
              <div className="space-y-2">
                <div 
                  className="aspect-video rounded-lg overflow-hidden bg-gray-100 cursor-pointer"
                  onClick={() => setShowImageModal(true)}
                >
                  <ImageWithFallback
                    src={table.imageUrl}
                    alt={`Стол ${table.tableNumber}`}
                    className="w-full h-full object-cover"
                  />
                </div>
              </div>
            )}

            {/* Table Info */}
            <Card className="px-4 py-[10px] gap-[10px]">
              <h3 className="mb-1">Информация о столе</h3>
              <div className="space-y-2 text-gray-700">
                <div className="flex justify-between">
                  <span>Номер стола:</span>
                  <span className="font-semibold">№ {table.tableNumber}</span>
                </div>
                <div className="flex justify-between">
                  <span>Вместимость:</span>
                  <span>{table.capacity} человек</span>
                </div>
                {table.description && (
                  <p className="text-gray-600 mt-2">{table.description}</p>
                )}
              </div>
            </Card>

            {/* Date and Time */}
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2 relative">
                <Label htmlFor="date">
                  <CalendarIcon className="w-4 h-4 inline mr-2" />
                  Дата
                </Label>
                <Input
                  ref={dateInputRef}
                  id="date"
                  type="text"
                  readOnly
                  value={formData.date ? (() => {
                    const [year, month, day] = formData.date.split('-').map(Number);
                    return new Date(year, month - 1, day).toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit', year: 'numeric' });
                  })() : ''}
                  onClick={handleDateInputClick}
                  onFocus={handleDateInputClick}
                  placeholder="Выберите дату"
                  required
                />
                {/* Date Picker Panel */}
                {showDatePicker && (
                  <div
                    ref={datePickerRef}
                    className="absolute top-full left-0 mt-2 bg-white border border-gray-200 rounded-lg shadow-lg z-50 p-4 w-auto"
                  >
                    <Calendar
                      mode="single"
                      selected={formData.date ? (() => {
                        const [year, month, day] = formData.date.split('-').map(Number);
                        return new Date(year, month - 1, day);
                      })() : undefined}
                      onSelect={(date) => {
                        if (date) {
                          const year = date.getFullYear();
                          const month = String(date.getMonth() + 1).padStart(2, '0');
                          const day = String(date.getDate()).padStart(2, '0');
                          const dateString = `${year}-${month}-${day}`;
                          setFormData({ ...formData, date: dateString });
                          setShowDatePicker(false);
                        }
                      }}
                      disabled={(date) => {
                        const todayDate = new Date(today);
                        todayDate.setHours(0, 0, 0, 0);
                        const checkDate = new Date(date);
                        checkDate.setHours(0, 0, 0, 0);
                        return checkDate < todayDate;
                      }}
                      className="w-full"
                    />
                  </div>
                )}
              </div>
              <div className="space-y-2 relative">
                <Label htmlFor="time">
                  <Clock className="w-4 h-4 inline mr-2" />
                  Время
                </Label>
                <Input
                  ref={timeInputRef}
                  id="time"
                  type="text"
                  readOnly
                  value={formData.time || ''}
                  onClick={handleTimeInputClick}
                  onFocus={handleTimeInputClick}
                  placeholder="Выберите время"
                  required
                />
                {/* Quick Time Picker Panel */}
                {showTimePicker && (
                  <div
                    ref={timePickerRef}
                    className="absolute top-full right-0 mt-2 bg-white border border-gray-200 rounded-lg shadow-lg z-50 p-4 w-auto min-w-[320px]"
                  >
                    <div className="grid grid-cols-2 gap-4">
                      {/* Hours Column */}
                      <div className="pr-4 border-r border-gray-200">
                        <div className="text-sm font-semibold mb-2 text-gray-700">Часы</div>
                        <div className="grid grid-cols-3 gap-2 max-h-48 overflow-y-auto [&::-webkit-scrollbar]:hidden [-ms-overflow-style:none] [scrollbar-width:none]">
                          {hours.map((hour) => (
                            <button
                              key={hour}
                              type="button"
                              onClick={() => {
                                const currentMinute = formData.time ? parseInt(formData.time.split(':')[1] || '0') : 0;
                                handleTimeSelect(hour, currentMinute);
                              }}
                              className="px-3 py-2 text-sm rounded-md border border-gray-200 hover:bg-gray-100 hover:border-gray-300 transition-colors flex items-center justify-center"
                            >
                              {hour.toString().padStart(2, '0')}
                            </button>
                          ))}
                        </div>
                      </div>
                      {/* Minutes Column */}
                      <div className="pl-4">
                        <div className="text-sm font-semibold mb-2 text-gray-700">Минуты</div>
                        <div className="grid grid-cols-2 gap-2">
                          {minutes.map((minute) => (
                            <button
                              key={minute}
                              type="button"
                              onClick={() => {
                                const currentHour = formData.time ? parseInt(formData.time.split(':')[0] || '0') : 0;
                                handleTimeSelect(currentHour, minute);
                              }}
                              className="px-3 py-2 text-sm rounded-md border border-gray-200 hover:bg-gray-100 hover:border-gray-300 transition-colors"
                            >
                              {minute.toString().padStart(2, '0')}
                            </button>
                          ))}
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* Person Count and Client Name */}
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2 relative">
                <Label htmlFor="personCount">
                  <Users className="w-4 h-4 inline mr-2" />
                  Количество гостей
                </Label>
                <Input
                  ref={personCountInputRef}
                  id="personCount"
                  type="text"
                  readOnly
                  value={formData.personCount}
                  onClick={handlePersonCountInputClick}
                  onFocus={handlePersonCountInputClick}
                  required
                />
                <p className="text-sm text-gray-500">
                  Максимум: {table.capacity} человек
                </p>
                {/* Person Count Picker Panel */}
                {showPersonCountPicker && (
                  <div
                    ref={personCountPickerRef}
                    className="absolute top-full left-0 mt-2 bg-white border border-gray-200 rounded-lg shadow-lg z-50 p-4 w-auto min-w-[200px]"
                  >
                    <div className="text-sm font-semibold mb-2 text-gray-700">Количество гостей</div>
                    <div className="grid grid-cols-4 gap-2 max-h-48 overflow-y-auto [&::-webkit-scrollbar]:hidden [-ms-overflow-style:none] [scrollbar-width:none]">
                      {Array.from({ length: table.capacity }, (_, i) => i + 1).map((count) => (
                        <button
                          key={count}
                          type="button"
                          onClick={() => handlePersonCountSelect(count)}
                          className={`px-3 py-2 text-sm rounded-md border border-gray-200 hover:bg-gray-100 hover:border-gray-300 transition-colors flex items-center justify-center ${
                            formData.personCount === count ? 'bg-primary text-primary-foreground border-primary' : ''
                          }`}
                        >
                          {count}
                        </button>
                      ))}
                    </div>
                  </div>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="clientName">Имя (необязательно)</Label>
                <Input
                  id="clientName"
                  type="text"
                  placeholder="Ваше имя"
                  value={formData.clientName}
                  onChange={(e) => setFormData({ ...formData, clientName: e.target.value })}
                  onFocus={handleOtherFieldFocus}
                />
              </div>
            </div>

            {/* Special Requests */}
            <div className="space-y-2">
              <Label htmlFor="specialRequests">Особые пожелания (необязательно)</Label>
              <Input
                id="specialRequests"
                type="text"
                placeholder="Например, детский стульчик"
                value={formData.specialRequests}
                onChange={(e) => setFormData({ ...formData, specialRequests: e.target.value })}
                onFocus={handleOtherFieldFocus}
              />
            </div>

            {/* Pre-order */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label>
                  <ShoppingCart className="w-4 h-4 inline mr-2" />
                  Предзаказ
                </Label>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => setShowPreOrderModal(true)}
                >
                  {preOrderItems.length > 0 ? `Изменить (${preOrderItems.length})` : 'Добавить'}
                </Button>
              </div>
              {preOrderItems.length > 0 && (
                <Card className="p-3">
                  <div className="space-y-2">
                    {preOrderItems.map(item => (
                      <div key={item.menuItemId} className="flex justify-between text-sm">
                        <span>{item.name} × {item.quantity}</span>
                        <span>{item.price * item.quantity} ₽</span>
                      </div>
                    ))}
                    <div className="border-t pt-2 mt-2 flex justify-between font-semibold">
                      <span>Итого:</span>
                      <span>{totalPreOrderCost} ₽</span>
                    </div>
                  </div>
                </Card>
              )}
            </div>

            {/* Submit Button */}
            <Button
              type="submit"
              className="w-full h-14"
              disabled={isSubmitting}
              style={{ backgroundColor: restaurant.primaryColor }}
            >
              {isSubmitting ? 'Создание...' : 'Забронировать стол'}
            </Button>
          </form>
        </div>
      </div>

      {/* Pre-order Modal */}
      {showPreOrderModal && (
        <PreOrderModal
          restaurantId={parseInt(id!, 10)}
          onClose={() => setShowPreOrderModal(false)}
          onConfirm={handleAddPreOrder}
          initialItems={preOrderItems}
        />
      )}

      {/* Image Modal */}
      {showImageModal && table.imageUrl && (
        <ImageModal
          imageUrl={table.imageUrl}
          alt={`Стол ${table.tableNumber}`}
          onClose={() => setShowImageModal(false)}
        />
      )}
    </>
  );
}

