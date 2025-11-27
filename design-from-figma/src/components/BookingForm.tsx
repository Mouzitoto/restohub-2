import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Restaurant } from '../data/mockData';
import { Room, Table } from '../data/bookingMockData';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Card } from './ui/card';
import { Calendar, Clock, Users, ShoppingCart, ArrowLeft, Info } from 'lucide-react';
import { ImageWithFallback } from './figma/ImageWithFallback';
import { PreOrderModal } from './PreOrderModal';
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

export function BookingForm({ restaurant, room, table, onClose, onBack }: BookingFormProps) {
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

    setIsSubmitting(true);
    
    // Simulate API call
    setTimeout(() => {
      const bookingId = Math.random().toString(36).substring(7);
      toast.success('Бронирование создано!');
      navigate(`/r/${id}/booking/confirm/${bookingId}`);
      setIsSubmitting(false);
    }, 1000);
  };

  const handleAddPreOrder = (items: PreOrderItem[]) => {
    setPreOrderItems(items);
    setShowPreOrderModal(false);
  };

  const totalPreOrderCost = preOrderItems.reduce((sum, item) => sum + item.price * item.quantity, 0);

  return (
    <>
      <div className="flex-1 bg-white overflow-y-auto pb-20">
        <div className="p-4">
          {/* Back Button */}
          <div className="mb-4">
            <Button 
              variant="ghost" 
              size="sm"
              onClick={onBack}
              className="gap-2"
            >
              <ArrowLeft className="w-4 h-4" />
              Вернуться к карте столов
            </Button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Table Photos */}
            {table.images && table.images.length > 0 && (
              <div className="space-y-2">
                <h3>Фотографии стола</h3>
                <div className="grid grid-cols-2 gap-2">
                  {table.images.map((image, index) => (
                    <div key={index} className="aspect-video rounded-lg overflow-hidden bg-gray-100">
                      <ImageWithFallback
                        src={image}
                        alt={`Стол ${table.tableNumber} - фото ${index + 1}`}
                        className="w-full h-full object-cover"
                      />
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Table Info */}
            <Card className="p-4">
              <h3 className="mb-2">Стол {table.tableNumber}</h3>
              <p className="text-gray-600 mb-1">
                <Users className="w-4 h-4 inline mr-2" />
                Вместимость: {table.capacity} {table.capacity === 1 ? 'человек' : table.capacity <= 4 ? 'человека' : 'человек'}
              </p>
              {table.description && (
                <p className="text-gray-500 mt-2">{table.description}</p>
              )}
            </Card>

            {/* Booking Conditions */}
            {table.deposit && (
              <Card className="p-4 bg-blue-50 border-blue-200">
                <div className="flex gap-2">
                  <Info className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" />
                  <div>
                    <h3 className="text-blue-900 mb-1">Условия бронирования</h3>
                    <p className="text-blue-800">
                      Для бронирования этого стола требуется депозит: <span className="font-semibold">{table.deposit} ₽</span>
                    </p>
                    <p className="text-blue-700 mt-1">
                      Депозит будет зачтен в счет заказа при посещении ресторана.
                    </p>
                  </div>
                </div>
              </Card>
            )}

            {/* Date and Time */}
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label htmlFor="date">
                  <Calendar className="w-4 h-4 inline mr-2" />
                  Дата
                </Label>
                <Input
                  id="date"
                  type="date"
                  required
                  value={formData.date}
                  onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                  min={new Date().toISOString().split('T')[0]}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="time">
                  <Clock className="w-4 h-4 inline mr-2" />
                  Время
                </Label>
                <Input
                  id="time"
                  type="time"
                  required
                  value={formData.time}
                  onChange={(e) => setFormData({ ...formData, time: e.target.value })}
                />
              </div>
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
                required
                min={1}
                max={table.capacity}
                value={formData.personCount}
                onChange={(e) => setFormData({ ...formData, personCount: parseInt(e.target.value) })}
              />
              <p className="text-gray-500">Максимум: {table.capacity} человек</p>
            </div>

            {/* Contact Info */}
            <div className="space-y-3">
              <div className="space-y-2">
                <Label htmlFor="clientFirstName">Ваше имя (необязательно)</Label>
                <Input
                  id="clientFirstName"
                  type="text"
                  placeholder="Иван"
                  maxLength={255}
                  value={formData.clientFirstName}
                  onChange={(e) => setFormData({ ...formData, clientFirstName: e.target.value })}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="clientName">На чье имя бронировать (необязательно)</Label>
                <Input
                  id="clientName"
                  type="text"
                  placeholder="Иван Иванов"
                  maxLength={255}
                  value={formData.clientName}
                  onChange={(e) => setFormData({ ...formData, clientName: e.target.value })}
                />
              </div>
            </div>

            {/* Special Requests */}
            <div className="space-y-2">
              <Label htmlFor="specialRequests">Особые пожелания (необязательно)</Label>
              <Input
                id="specialRequests"
                type="text"
                placeholder="Например: окно, тихое место..."
                maxLength={500}
                value={formData.specialRequests}
                onChange={(e) => setFormData({ ...formData, specialRequests: e.target.value })}
              />
            </div>

            {/* Pre-order - Made more prominent */}
            <Card className="p-4 bg-gradient-to-r from-amber-50 to-orange-50 border-amber-200">
              <div className="space-y-3">
                <div className="flex items-center gap-2 mb-2">
                  <ShoppingCart className="w-5 h-5 text-amber-700" />
                  <h3 className="text-amber-900">Хотите сделать предзаказ?</h3>
                </div>
                <p className="text-amber-800">
                  Закажите блюда заранее, и они будут готовы к вашему приходу
                </p>
                <Button
                  type="button"
                  variant="default"
                  className="w-full bg-amber-600 hover:bg-amber-700 text-white"
                  onClick={() => setShowPreOrderModal(true)}
                >
                  <ShoppingCart className="w-4 h-4 mr-2" />
                  Выбрать блюда
                  {preOrderItems.length > 0 && (
                    <span className="ml-2 bg-white text-amber-700 px-2 py-0.5 rounded-full">
                      {preOrderItems.length}
                    </span>
                  )}
                </Button>

                {preOrderItems.length > 0 && (
                  <Card className="p-3 bg-white">
                    <p className="mb-2">Выбранные блюда:</p>
                    {preOrderItems.map((item, index) => (
                      <div key={index} className="flex justify-between text-gray-700 mb-1">
                        <span>{item.name} x{item.quantity}</span>
                        <span>{item.price * item.quantity} ₽</span>
                      </div>
                    ))}
                    <div className="border-t pt-2 mt-2 flex justify-between">
                      <span>Итого:</span>
                      <span>{totalPreOrderCost} ₽</span>
                    </div>
                  </Card>
                )}
              </div>
            </Card>
          </form>
        </div>
      </div>

      {/* Fixed Submit Button */}
      <div className="fixed bottom-0 left-0 right-0 p-4 bg-white border-t shadow-lg z-10">
        <Button
          type="submit"
          onClick={handleSubmit}
          className="w-full h-12"
          style={{ backgroundColor: restaurant.primaryColor }}
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Отправка...' : 'Забронировать'}
        </Button>
      </div>

      {/* Pre-order Modal */}
      {showPreOrderModal && (
        <PreOrderModal
          restaurant={restaurant}
          preOrderItems={preOrderItems}
          onClose={() => setShowPreOrderModal(false)}
          onSave={handleAddPreOrder}
        />
      )}
    </>
  );
}