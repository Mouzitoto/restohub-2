import { useParams, useNavigate } from 'react-router-dom';
import { mockRestaurants } from '../data/mockData';
import { Button } from './ui/button';
import { CheckCircle2, MessageCircle } from 'lucide-react';
import { Card } from './ui/card';

export function BookingConfirmPage() {
  const { id, bookingId } = useParams<{ id: string; bookingId: string }>();
  const navigate = useNavigate();
  const restaurant = mockRestaurants.find(r => r.id === id);

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

  // In real app, this would come from API
  const bookingData = {
    tableNumber: '5',
    date: new Date().toLocaleDateString('ru-RU'),
    time: '19:00',
    personCount: 4
  };

  const whatsappMessage = encodeURIComponent(
    `Здравствуйте! Я хочу подтвердить бронирование в ресторане ${restaurant.name}.\n\nНомер бронирования: ${bookingId}\nСтол: ${bookingData.tableNumber}\nДата: ${bookingData.date}\nВремя: ${bookingData.time}\nКоличество гостей: ${bookingData.personCount}`
  );
  
  // This would come from API in real app
  const whatsappUrl = `https://wa.me/${restaurant.phone.replace(/[^0-9]/g, '')}?text=${whatsappMessage}`;

  return (
    <div className="min-h-screen bg-gray-50 p-4">
      <div className="max-w-md mx-auto pt-8">
        <div className="text-center mb-6">
          <CheckCircle2 className="w-16 h-16 text-green-600 mx-auto mb-4" />
          <h1 className="text-green-600 mb-2">Бронирование создано!</h1>
          <p className="text-gray-600">{restaurant.name}</p>
        </div>

        <Card className="p-6 mb-6">
          <h2 className="mb-4">Детали бронирования</h2>
          <div className="space-y-3 text-gray-700">
            <div className="flex justify-between">
              <span>Номер бронирования:</span>
              <span>{bookingId}</span>
            </div>
            <div className="flex justify-between">
              <span>Стол:</span>
              <span>№ {bookingData.tableNumber}</span>
            </div>
            <div className="flex justify-between">
              <span>Дата:</span>
              <span>{bookingData.date}</span>
            </div>
            <div className="flex justify-between">
              <span>Время:</span>
              <span>{bookingData.time}</span>
            </div>
            <div className="flex justify-between">
              <span>Количество гостей:</span>
              <span>{bookingData.personCount}</span>
            </div>
          </div>
        </Card>

        <Card className="p-6 mb-6 bg-blue-50 border-blue-200">
          <h3 className="mb-3 text-blue-900">Подтверждение через WhatsApp</h3>
          <div className="space-y-3 text-blue-800">
            <p>Для завершения бронирования отправьте сообщение в WhatsApp.</p>
            <p>Нажмите кнопку ниже, чтобы открыть WhatsApp с предзаполненным сообщением.</p>
            <p>После отправки сообщения менеджер ресторана свяжется с вами для подтверждения.</p>
          </div>
        </Card>

        <Button
          className="w-full h-14 mb-4"
          style={{ backgroundColor: '#25D366' }}
          onClick={() => window.open(whatsappUrl, '_blank')}
        >
          <MessageCircle className="w-5 h-5 mr-2" />
          Открыть WhatsApp
        </Button>

        <Button
          variant="outline"
          className="w-full"
          onClick={() => navigate(`/r/${id}`)}
        >
          Вернуться к ресторану
        </Button>
      </div>
    </div>
  );
}
