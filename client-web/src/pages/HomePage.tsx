import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { SearchBar } from '../components/SearchBar';
import { Filters, Filters as FiltersType } from '../components/Filters';
import { RestaurantCard } from '../components/RestaurantCard';
import { RestaurantMap } from '../components/RestaurantMap';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../components/ui/tabs';
import { SlidersHorizontal } from 'lucide-react';
import { Button } from '../components/ui/button';
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger } from '../components/ui/sheet';
import { RestoHubLogo } from '../components/RestoHubLogo';
import { Badge } from '../components/ui/badge';
import { QuickFilters } from '../components/QuickFilters';
import { restaurantApi } from '../services/api';
import { mapRestaurant, checkRestaurantFeatures } from '../utils/mappers';
import { Restaurant } from '../types/restaurant';
import { toast } from 'sonner';

export function HomePage() {
  const navigate = useNavigate();
  const [filters, setFilters] = useState<FiltersType>({
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
  const [filteredRestaurants, setFilteredRestaurants] = useState<Restaurant[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadRestaurants();
  }, [filters]);

  const loadRestaurants = async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      let restaurants: Restaurant[] = [];

      // If search query or filters are active, use search API
      if (filters.searchQuery || filters.hasPromotions || filters.isOutdoor || filters.isSmoking || filters.cuisineType) {
        const searchParams: any = {};
        
        if (filters.searchQuery) {
          searchParams.q = filters.searchQuery;
        }
        
        if (filters.hasPromotions) {
          searchParams.promotion = 'true';
        }
        
        if (filters.isOutdoor) {
          searchParams.isOutdoor = true;
        }
        
        if (filters.isSmoking) {
          searchParams.isSmoking = true;
        }
        
        if (filters.cuisineType) {
          // Note: API might not support cuisineType directly in search
          // This might need to be filtered on frontend
        }

        const apiRestaurants = await restaurantApi.searchRestaurants(searchParams);
        
        // Get promotions for each restaurant to check hasPromotions
        const restaurantsWithPromotions = await Promise.all(
          apiRestaurants.map(async (apiRest) => {
            try {
              const promotions = await restaurantApi.getPromotions(apiRest.id, { isCurrent: true, limit: 1 });
              const hasPromotions = promotions.length > 0;
              
              // Get rooms to check isOutdoor and isSmoking
              const rooms = await restaurantApi.getRooms(apiRest.id);
              const features = checkRestaurantFeatures(rooms);
              
              const mapped = mapRestaurant(apiRest, hasPromotions);
              return {
                ...mapped,
                isOutdoor: features.isOutdoor,
                isSmoking: features.isSmoking,
              };
            } catch (err) {
              // If error getting promotions/rooms, use defaults
              return mapRestaurant(apiRest, false);
            }
          })
        );
        
        restaurants = restaurantsWithPromotions;
      } else {
        // Use regular list API
        const apiRestaurants = await restaurantApi.getRestaurants({ limit: 50 });
        
        // Get promotions and rooms for each restaurant
        const restaurantsWithData = await Promise.all(
          apiRestaurants.map(async (apiRest) => {
            try {
              const [promotions, rooms] = await Promise.all([
                restaurantApi.getPromotions(apiRest.id, { isCurrent: true, limit: 1 }).catch(() => []),
                restaurantApi.getRooms(apiRest.id).catch(() => [])
              ]);
              
              const hasPromotions = promotions.length > 0;
              const features = rooms.length > 0 ? checkRestaurantFeatures(rooms) : { isOutdoor: false, isSmoking: false };
              
              const mapped = mapRestaurant(apiRest, hasPromotions);
              return {
                ...mapped,
                isOutdoor: features.isOutdoor,
                isSmoking: features.isSmoking,
              };
            } catch (err) {
              return mapRestaurant(apiRest, false);
            }
          })
        );
        
        restaurants = restaurantsWithData;
      }

      // Apply frontend filters
      let filtered = [...restaurants];

      // Filter by cuisine type (if not already filtered by API)
      if (filters.cuisineType && !filters.searchQuery) {
        filtered = filtered.filter(r => r.cuisineType === filters.cuisineType);
      }

      // Filter by rating
      if (filters.minRating > 0) {
        filtered = filtered.filter(r => (r.rating || 0) >= filters.minRating);
      }

      // Apply sorting
      filtered.sort((a, b) => {
        switch (filters.sortBy) {
          case 'distance':
            return (a.distance || 0) - (b.distance || 0);
          case 'name':
            return a.name.localeCompare(b.name, 'ru');
          case 'rating':
            return (b.rating || 0) - (a.rating || 0);
          default:
            return 0;
        }
      });

      setFilteredRestaurants(filtered);
    } catch (err) {
      console.error('Error loading restaurants:', err);
      setError('Ошибка при загрузке ресторанов');
      toast.error('Не удалось загрузить рестораны');
    } finally {
      setIsLoading(false);
    }
  };

  const handleFilterChange = (newFilters: Partial<FiltersType>) => {
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
          {isLoading ? 'Загрузка...' : error ? error : `Найдено ресторанов: ${filteredRestaurants.length}`}
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

export default HomePage;
