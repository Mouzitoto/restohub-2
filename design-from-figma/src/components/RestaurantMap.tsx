import { Restaurant } from '../data/mockData';
import { useNavigate } from 'react-router-dom';
import { MapPin, Navigation } from 'lucide-react';
import { Button } from './ui/button';

interface RestaurantMapProps {
  restaurants: Restaurant[];
}

export function RestaurantMap({ restaurants }: RestaurantMapProps) {
  const navigate = useNavigate();

  return (
    <div className="h-[calc(100vh-240px)] overflow-y-auto">
      <div className="p-4 space-y-3">
        {restaurants.map((restaurant) => (
          <div
            key={restaurant.id}
            className="bg-white rounded-xl p-4 border border-gray-200 shadow-sm"
          >
            <div className="flex items-start gap-3 mb-3">
              <div className="w-10 h-10 rounded-full bg-rose-100 flex items-center justify-center flex-shrink-0">
                <MapPin className="w-5 h-5 text-rose-600" />
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="mb-1">{restaurant.name}</h3>
                <p className="text-gray-600">{restaurant.address}</p>
                <p className="text-gray-500 mt-1">{restaurant.distance} км от вас</p>
              </div>
            </div>

            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                className="flex-1"
                onClick={() => {
                  window.open(
                    `https://maps.google.com/?q=${restaurant.lat},${restaurant.lng}`,
                    '_blank'
                  );
                }}
              >
                <Navigation className="w-4 h-4 mr-2" />
                Маршрут
              </Button>
              <Button
                size="sm"
                className="flex-1"
                onClick={() => navigate(`/r/${restaurant.id}`)}
              >
                Подробнее
              </Button>
            </div>
          </div>
        ))}

        {restaurants.length === 0 && (
          <div className="text-center py-12 text-gray-500">
            <MapPin className="w-12 h-12 mx-auto mb-3 text-gray-300" />
            <p>Рестораны не найдены</p>
          </div>
        )}
      </div>
    </div>
  );
}