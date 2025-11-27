import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Utensils, Wine, Coffee, MapPin, Sunrise, Sun, Moon, Percent } from 'lucide-react';

interface QuickFiltersProps {
  selectedType: string;
  selectedMealTime: string;
  isNearby: boolean;
  hasPromotions: boolean;
  onTypeChange: (type: string) => void;
  onMealTimeChange: (mealTime: string) => void;
  onNearbyToggle: () => void;
  onPromotionsToggle: () => void;
}

const establishmentTypes = [
  { id: '', label: 'Все', icon: null },
  { id: 'restaurant', label: 'Рестораны', icon: Utensils },
  { id: 'bar', label: 'Бары', icon: Wine },
  { id: 'cafe', label: 'Кафе', icon: Coffee },
];

const mealTimes = [
  { id: '', label: 'Все', icon: null },
  { id: 'breakfast', label: 'Завтрак', icon: Sunrise },
  { id: 'lunch', label: 'Обед', icon: Sun },
  { id: 'dinner', label: 'Ужин', icon: Moon },
];

export function QuickFilters({ 
  selectedType, 
  selectedMealTime,
  isNearby,
  hasPromotions,
  onTypeChange, 
  onMealTimeChange,
  onNearbyToggle,
  onPromotionsToggle
}: QuickFiltersProps) {
  return (
    <div>
      {/* Single row with all filters */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-hide">
        {/* Nearby */}
        <Button
          variant={isNearby ? 'default' : 'outline'}
          size="sm"
          onClick={onNearbyToggle}
          className={`flex-shrink-0 ${
            isNearby 
              ? 'bg-rose-600 hover:bg-rose-700 text-white' 
              : 'hover:border-rose-300'
          }`}
        >
          <MapPin className="w-4 h-4 mr-1.5" />
          Рядом
        </Button>

        {/* Promotions */}
        <Button
          variant={hasPromotions ? 'default' : 'outline'}
          size="sm"
          onClick={onPromotionsToggle}
          className={`flex-shrink-0 ${
            hasPromotions 
              ? 'bg-rose-600 hover:bg-rose-700 text-white' 
              : 'hover:border-rose-300'
          }`}
        >
          <Percent className="w-4 h-4 mr-1.5" />
          Акции
        </Button>

        {/* Meal Times */}
        {mealTimes.slice(1).map(mealTime => {
          const Icon = mealTime.icon;
          const isSelected = selectedMealTime === mealTime.id;
          
          return (
            <Button
              key={mealTime.id}
              variant={isSelected ? 'default' : 'outline'}
              size="sm"
              onClick={() => onMealTimeChange(isSelected ? '' : mealTime.id)}
              className={`flex-shrink-0 ${
                isSelected 
                  ? 'bg-rose-600 hover:bg-rose-700 text-white' 
                  : 'hover:border-rose-300'
              }`}
            >
              {Icon && <Icon className="w-4 h-4 mr-1.5" />}
              {mealTime.label}
            </Button>
          );
        })}
      </div>
    </div>
  );
}

