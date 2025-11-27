import type { Promotion } from '../types/restaurant';
import { Badge } from './ui/badge';
import { Calendar, RefreshCw } from 'lucide-react';
import { ImageWithFallback } from './figma/ImageWithFallback';

interface PromotionCardProps {
  promotion: Promotion;
}

const promotionTypeLabels = {
  PROMOTION: 'Акция',
  THEME_NIGHT: 'Тематический вечер',
  NEW: 'Новинка'
};

const promotionTypeBadgeColors = {
  PROMOTION: 'bg-rose-600',
  THEME_NIGHT: 'bg-purple-600',
  NEW: 'bg-green-600'
};

const dayOfWeekLabels = ['Воскресенье', 'Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота'];

const recurrenceTypeLabels = {
  DAILY: 'Ежедневно',
  WEEKLY: 'Еженедельно',
  MONTHLY: 'Ежемесячно'
};

export function PromotionCard({ promotion }: PromotionCardProps) {
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit', year: 'numeric' });
  };

  const getRecurrenceText = () => {
    if (!promotion.isRecurring) return null;

    if (promotion.recurrenceType === 'WEEKLY' && promotion.recurrenceDayOfWeek !== undefined) {
      return `Каждый ${dayOfWeekLabels[promotion.recurrenceDayOfWeek].toLowerCase()}`;
    }

    if (promotion.recurrenceType) {
      return recurrenceTypeLabels[promotion.recurrenceType];
    }

    return null;
  };

  const getPeriodText = () => {
    const startDate = formatDate(promotion.startDate);
    
    if (promotion.isRecurring && !promotion.endDate) {
      return `с ${startDate} (бесконечно)`;
    }
    
    if (promotion.endDate) {
      const endDate = formatDate(promotion.endDate);
      return `с ${startDate} по ${endDate}`;
    }
    
    return `с ${startDate}`;
  };

  return (
    <div className="bg-white rounded-xl overflow-hidden shadow-sm border border-gray-200">
      {/* Image */}
      <div className="relative h-48 bg-gray-100">
        <ImageWithFallback
          src={promotion.imageUrl}
          alt={promotion.title}
          className="w-full h-full object-cover"
        />
        <Badge className={`absolute top-3 right-3 ${promotionTypeBadgeColors[promotion.type]}`}>
          {promotionTypeLabels[promotion.type]}
        </Badge>
        {promotion.isRecurring && (
          <Badge className="absolute top-3 left-3 bg-blue-600">
            <RefreshCw className="w-3 h-3 mr-1" />
            Повторяется
          </Badge>
        )}
      </div>

      {/* Content */}
      <div className="p-4 space-y-3">
        <h3>{promotion.title}</h3>
        
        {promotion.description && (
          <p className="text-gray-600">{promotion.description}</p>
        )}

        <div className="space-y-2 text-gray-600">
          <div className="flex items-start gap-2">
            <Calendar className="w-4 h-4 mt-0.5 flex-shrink-0" />
            <div>
              <p>{getPeriodText()}</p>
              {getRecurrenceText() && (
                <p className="text-blue-600">{getRecurrenceText()}</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

