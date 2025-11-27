import { Button } from './ui/button';
import { Checkbox } from './ui/checkbox';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Separator } from './ui/separator';

export interface Filters {
  searchQuery: string;
  cuisineType: string;
  minRating: number;
  hasPromotions: boolean;
  isOutdoor: boolean;
  isSmoking: boolean;
  sortBy: 'distance' | 'name' | 'rating';
  establishmentType: string;
  mealTime: string;
  isNearby: boolean;
}

interface FiltersProps {
  filters: Filters;
  onFilterChange: (filters: Partial<Filters>) => void;
  onReset: () => void;
  cuisineTypes?: string[];
}

const defaultCuisineTypes = ['Итальянская', 'Японская', 'Грузинская', 'Европейская', 'Русская', 'Азиатская'];

export function Filters({ filters, onFilterChange, onReset, cuisineTypes = defaultCuisineTypes }: FiltersProps) {
  return (
    <div className="py-4 space-y-6">
      {/* Cuisine Type */}
      <div className="space-y-2">
        <Label>Тип кухни</Label>
        <Select 
          value={filters.cuisineType || 'all'} 
          onValueChange={(value) => onFilterChange({ cuisineType: value === 'all' ? '' : value })}
        >
          <SelectTrigger>
            <SelectValue placeholder="Все типы кухни" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Все типы кухни</SelectItem>
            {cuisineTypes.map(type => (
              <SelectItem key={type} value={type}>{type}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <Separator />

      {/* Rating */}
      <div className="space-y-2">
        <Label>Минимальный рейтинг</Label>
        <Select 
          value={filters.minRating.toString()} 
          onValueChange={(value) => onFilterChange({ minRating: parseFloat(value) })}
        >
          <SelectTrigger>
            <SelectValue placeholder="Любой рейтинг" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="0">Любой рейтинг</SelectItem>
            <SelectItem value="4">4+ звезды</SelectItem>
            <SelectItem value="4.5">4.5+ звезды</SelectItem>
            <SelectItem value="4.8">4.8+ звезды</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <Separator />

      {/* Checkboxes */}
      <div className="space-y-4">
        <div className="flex items-center space-x-2">
          <Checkbox
            id="promotions"
            checked={filters.hasPromotions}
            onCheckedChange={(checked) => 
              onFilterChange({ hasPromotions: checked as boolean })
            }
          />
          <Label htmlFor="promotions" className="cursor-pointer">
            Только с акциями
          </Label>
        </div>

        <div className="flex items-center space-x-2">
          <Checkbox
            id="outdoor"
            checked={filters.isOutdoor}
            onCheckedChange={(checked) => 
              onFilterChange({ isOutdoor: checked as boolean })
            }
          />
          <Label htmlFor="outdoor" className="cursor-pointer">
            Только с залами на открытом воздухе
          </Label>
        </div>

        <div className="flex items-center space-x-2">
          <Checkbox
            id="smoking"
            checked={filters.isSmoking}
            onCheckedChange={(checked) => 
              onFilterChange({ isSmoking: checked as boolean })
            }
          />
          <Label htmlFor="smoking" className="cursor-pointer">
            Только с курящими залами
          </Label>
        </div>
      </div>

      <Separator />

      {/* Sorting */}
      <div className="space-y-2">
        <Label>Сортировка</Label>
        <Select 
          value={filters.sortBy} 
          onValueChange={(value) => 
            onFilterChange({ sortBy: value as 'distance' | 'name' | 'rating' })
          }
        >
          <SelectTrigger>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="distance">По расстоянию</SelectItem>
            <SelectItem value="name">По названию</SelectItem>
            <SelectItem value="rating">По рейтингу</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <Separator />

      {/* Reset Button */}
      <Button 
        variant="outline" 
        onClick={onReset}
        className="w-full"
      >
        Сбросить все фильтры
      </Button>
    </div>
  );
}

