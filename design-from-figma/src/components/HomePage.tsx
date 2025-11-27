import { useState, useEffect } from 'react';
import { SearchBar } from './SearchBar';
import { Filters } from './Filters';
import { RestaurantCard } from './RestaurantCard';
import { RestaurantMap } from './RestaurantMap';
import { mockRestaurants, Restaurant } from '../data/mockData';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { SlidersHorizontal } from 'lucide-react';
import { Button } from './ui/button';
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger } from './ui/sheet';
import { RestoHubLogo } from './RestoHubLogo';
import { Badge } from './ui/badge';
import { QuickFilters } from './QuickFilters';

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

export function HomePage() {
  const [filters, setFilters] = useState<Filters>({
    searchQuery: '',
    cuisineType: '',
    minRating: 0,
    hasPromotions: false,
    isOutdoor: false,
    isSmoking: false,
    sortBy: 'distance',
    establishmentType: '',
    mealTime: '',
    isNearby: false
  });
  const [filteredRestaurants, setFilteredRestaurants] = useState<Restaurant[]>(mockRestaurants);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    setIsLoading(true);
    // Simulate API call
    setTimeout(() => {
      let filtered = [...mockRestaurants];

      // Apply search filter
      if (filters.searchQuery) {
        filtered = filtered.filter(r =>
          r.name.toLowerCase().includes(filters.searchQuery.toLowerCase()) ||
          r.description.toLowerCase().includes(filters.searchQuery.toLowerCase())
        );
      }

      // Apply cuisine type filter
      if (filters.cuisineType) {
        filtered = filtered.filter(r => r.cuisineType === filters.cuisineType);
      }

      // Apply rating filter
      if (filters.minRating > 0) {
        filtered = filtered.filter(r => r.rating >= filters.minRating);
      }

      // Apply promotions filter
      if (filters.hasPromotions) {
        filtered = filtered.filter(r => r.hasPromotions);
      }

      // Apply outdoor filter
      if (filters.isOutdoor) {
        filtered = filtered.filter(r => r.isOutdoor);
      }

      // Apply smoking filter
      if (filters.isSmoking) {
        filtered = filtered.filter(r => r.isSmoking);
      }

      // Apply establishment type filter
      if (filters.establishmentType) {
        filtered = filtered.filter(r => r.establishmentType === filters.establishmentType);
      }

      // Apply sorting
      filtered.sort((a, b) => {
        switch (filters.sortBy) {
          case 'distance':
            return a.distance - b.distance;
          case 'name':
            return a.name.localeCompare(b.name, 'ru');
          case 'rating':
            return b.rating - a.rating;
          default:
            return 0;
        }
      });

      setFilteredRestaurants(filtered);
      setIsLoading(false);
    }, 300);
  }, [filters]);

  const handleFilterChange = (newFilters: Partial<Filters>) => {
    setFilters(prev => ({ ...prev, ...newFilters }));
  };

  const handleResetFilters = () => {
    setFilters({
      searchQuery: '',
      cuisineType: '',
      minRating: 0,
      hasPromotions: false,
      isOutdoor: false,
      isSmoking: false,
      sortBy: 'distance',
      establishmentType: '',
      mealTime: '',
      isNearby: false
    });
  };

  // Count active filters
  const activeFiltersCount = [
    filters.cuisineType,
    filters.minRating > 0,
    filters.hasPromotions,
    filters.isOutdoor,
    filters.isSmoking,
    filters.sortBy !== 'distance',
    filters.establishmentType
  ].filter(Boolean).length;

  return (
    <div className="min-h-screen pb-24">
      {/* Header */}
      <div className="bg-white border-b sticky top-0 z-10">
        <div className="p-4 space-y-3">
          <div className="flex justify-center">
            <RestoHubLogo className="h-10" />
          </div>
          <SearchBar 
            value={filters.searchQuery}
            onChange={(value) => handleFilterChange({ searchQuery: value })}
          />
          <QuickFilters
            selectedType={filters.establishmentType}
            selectedMealTime={filters.mealTime}
            isNearby={filters.isNearby}
            hasPromotions={filters.hasPromotions}
            onTypeChange={(type) => handleFilterChange({ establishmentType: type })}
            onMealTimeChange={(mealTime) => handleFilterChange({ mealTime })}
            onNearbyToggle={() => handleFilterChange({ isNearby: !filters.isNearby, sortBy: !filters.isNearby ? 'distance' : filters.sortBy })}
            onPromotionsToggle={() => handleFilterChange({ hasPromotions: !filters.hasPromotions })}
          />
        </div>
      </div>

      {/* Results count */}
      <div className="px-4 py-3 bg-gray-50 border-b">
        <p className="text-gray-600">
          {isLoading ? 'Загрузка...' : `Найдено ресторанов: ${filteredRestaurants.length}`}
        </p>
      </div>

      {/* Tabs for List/Map view */}
      <Tabs defaultValue="list" className="w-full">
        <TabsList className="w-full rounded-none">
          <TabsTrigger value="list" className="flex-1">Список</TabsTrigger>
          <TabsTrigger value="map" className="flex-1">Карта</TabsTrigger>
        </TabsList>

        <TabsContent value="list" className="mt-0">
          {isLoading ? (
            <div className="p-4 space-y-4">
              {[1, 2, 3].map(i => (
                <div key={i} className="bg-white rounded-lg p-4 animate-pulse">
                  <div className="h-32 bg-gray-200 rounded-lg mb-3"></div>
                  <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                  <div className="h-4 bg-gray-200 rounded w-1/2"></div>
                </div>
              ))}
            </div>
          ) : filteredRestaurants.length === 0 ? (
            <div className="p-8 text-center">
              <p className="text-gray-500">Рестораны не найдены</p>
              <Button 
                variant="outline" 
                onClick={handleResetFilters}
                className="mt-4"
              >
                Сбросить фильтры
              </Button>
            </div>
          ) : (
            <div className="p-4 space-y-4">
              {filteredRestaurants.map(restaurant => (
                <RestaurantCard key={restaurant.id} restaurant={restaurant} />
              ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="map" className="mt-0">
          <RestaurantMap restaurants={filteredRestaurants} />
        </TabsContent>
      </Tabs>

      {/* Floating Filter Button */}
      <div className="fixed bottom-6 right-6 z-20">
        <Sheet>
          <SheetTrigger asChild>
            <Button 
              size="lg"
              className="rounded-full shadow-lg h-14 px-6 bg-rose-600 hover:bg-rose-700 text-white"
            >
              <SlidersHorizontal className="w-5 h-5 mr-2" />
              Фильтры
              {activeFiltersCount > 0 && (
                <Badge className="ml-2 bg-white text-rose-600 hover:bg-white">
                  {activeFiltersCount}
                </Badge>
              )}
            </Button>
          </SheetTrigger>
          <SheetContent side="bottom" className="h-[85vh] overflow-y-auto">
            <SheetHeader>
              <SheetTitle>Фильтры и сортировка</SheetTitle>
              <SheetDescription>Настройте параметры поиска ресторанов</SheetDescription>
            </SheetHeader>
            <Filters
              filters={filters}
              onFilterChange={handleFilterChange}
              onReset={handleResetFilters}
            />
          </SheetContent>
        </Sheet>
      </div>
    </div>
  );
}