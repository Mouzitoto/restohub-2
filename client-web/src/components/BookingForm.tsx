import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import type { Restaurant, Room, Table } from '../types/restaurant';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Card } from './ui/card';
import { Calendar, Clock, Users, ShoppingCart, ArrowLeft } from 'lucide-react';
import { ImageWithFallback } from './figma/ImageWithFallback';
import { PreOrderModal } from './PreOrderModal';
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

export function BookingForm({ restaurant, table, onClose, onBack }: BookingFormProps) {
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

  return (
    <>
      <div className="flex-1 bg-white overflow-y-auto pb-20">
        <div className="p-4">
          {/* Back Button */}
          <div className="mb-4">
            <Button 
              variant="ghost" 
              size="sm"
              onClick={onBack || onClose}
              className="gap-2"
            >
              <ArrowLeft className="w-4 h-4" />
              Вернуться к карте столов
            </Button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Table Photos */}
            {table.imageUrl && (
              <div className="space-y-2">
                <h3>Фотография стола</h3>
                <div className="aspect-video rounded-lg overflow-hidden bg-gray-100">
                  <ImageWithFallback
                    src={table.imageUrl}
                    alt={`Стол ${table.tableNumber}`}
                    className="w-full h-full object-cover"
                  />
                </div>
              </div>
            )}

            {/* Table Info */}
            <Card className="p-4">
              <h3 className="mb-3">Информация о столе</h3>
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

            {/* Date */}
            <div className="space-y-2">
              <Label htmlFor="date">
                <Calendar className="w-4 h-4 inline mr-2" />
                Дата
              </Label>
              <Input
                id="date"
                type="date"
                min={today}
                value={formData.date}
                onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                required
              />
            </div>

            {/* Time */}
            <div className="space-y-2">
              <Label htmlFor="time">
                <Clock className="w-4 h-4 inline mr-2" />
                Время
              </Label>
              <Input
                id="time"
                type="time"
                value={formData.time}
                onChange={(e) => setFormData({ ...formData, time: e.target.value })}
                required
              />
            </div>

            {/* Person Count */}
            <div className="space-y-2">
              <Label htmlFor="personCount">
                <Users className="w-4 h-4 inline mr-2" />
                Количество гостей
              </Label>
              <Input
                id="personCount"
                type="number"
                min="1"
                max={table.capacity}
                value={formData.personCount}
                onChange={(e) => setFormData({ ...formData, personCount: parseInt(e.target.value) || 1 })}
                required
              />
              <p className="text-sm text-gray-500">
                Максимум: {table.capacity} человек
              </p>
            </div>

            {/* Client Name */}
            <div className="space-y-2">
              <Label htmlFor="clientName">Имя (необязательно)</Label>
              <Input
                id="clientName"
                type="text"
                placeholder="Ваше имя"
                value={formData.clientName}
                onChange={(e) => setFormData({ ...formData, clientName: e.target.value })}
              />
            </div>

            {/* Special Requests */}
            <div className="space-y-2">
              <Label htmlFor="specialRequests">Особые пожелания (необязательно)</Label>
              <Input
                id="specialRequests"
                type="text"
                placeholder="Например, стол у окна"
                value={formData.specialRequests}
                onChange={(e) => setFormData({ ...formData, specialRequests: e.target.value })}
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
    </>
  );
}

