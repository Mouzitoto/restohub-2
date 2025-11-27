import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { CheckCircle2, MessageCircle } from 'lucide-react';
import { Card } from '../components/ui/card';
import { BookingResponse } from '../types/restaurant';

export function BookingConfirmPage() {
  const { id, bookingId } = useParams<{ id: string; bookingId: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  
  // Get booking data from navigation state or use defaults
  const bookingResponse = location.state?.bookingResponse as BookingResponse | undefined;

  if (!bookingResponse) {
    return (
      <div className="min-h-screen bg-gray-50 p-4">
        <div className="max-w-md mx-auto mt-8 text-center">
          <p className="text-gray-500 mb-4">Данные бронирования не найдены</p>
          <Button onClick={() => navigate(`/r/${id}`)} className="mt-4">
            Вернуться к ресторану
          </Button>
        </div>
      </div>
    );
  }

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit', year: 'numeric' });
  };

  const formatTime = (timeString: string) => {
    // Time is in HH:mm:ss format, extract HH:mm
    return timeString.substring(0, 5);
  };

  return (
    <div className="min-h-screen bg-gray-50 p-4">
      <div className="max-w-md mx-auto pt-8">
        <div className="text-center mb-6">
          <CheckCircle2 className="w-16 h-16 text-green-600 mx-auto mb-4" />
          <h1 className="text-green-600 mb-2">Бронирование создано!</h1>
          <p className="text-gray-600">Номер бронирования: {bookingResponse.id}</p>
        </div>

        <Card className="p-6 mb-6">
          <h2 className="mb-4">Детали бронирования</h2>
          <div className="space-y-3 text-gray-700">
            <div className="flex justify-between">
              <span>Номер бронирования:</span>
              <span className="font-semibold">{bookingResponse.id}</span>
            </div>
            <div className="flex justify-between">
              <span>Дата:</span>
              <span>{formatDate(bookingResponse.date)}</span>
            </div>
            <div className="flex justify-between">
              <span>Время:</span>
              <span>{formatTime(bookingResponse.time)}</span>
            </div>
            <div className="flex justify-between">
              <span>Количество гостей:</span>
              <span>{bookingResponse.personCount}</span>
            </div>
            {bookingResponse.clientName && (
              <div className="flex justify-between">
                <span>Имя:</span>
                <span>{bookingResponse.clientName}</span>
              </div>
            )}
            {bookingResponse.specialRequests && (
              <div className="pt-3 border-t">
                <p className="text-gray-600">{bookingResponse.specialRequests}</p>
              </div>
            )}
          </div>
        </Card>

        <Card className="p-6 mb-6 bg-blue-50 border-blue-200">
          <h3 className="mb-3 text-blue-900">Подтверждение через WhatsApp</h3>
          <div className="space-y-3 text-blue-800">
            <p>{bookingResponse.message || 'Для завершения бронирования отправьте сообщение в WhatsApp.'}</p>
            <p>Нажмите кнопку ниже, чтобы открыть WhatsApp с предзаполненным сообщением.</p>
            <p>После отправки сообщения менеджер ресторана свяжется с вами для подтверждения.</p>
          </div>
        </Card>

        <Button
          className="w-full h-14 mb-4"
          style={{ backgroundColor: '#25D366' }}
          onClick={() => window.open(bookingResponse.whatsappUrl, '_blank')}
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

export default BookingConfirmPage;

