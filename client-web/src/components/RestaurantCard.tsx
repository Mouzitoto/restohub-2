import { useNavigate } from 'react-router-dom';
import type { Restaurant } from '../types/restaurant';
import { MapPin, Star, Tag } from 'lucide-react';
import { Badge } from './ui/badge';
import { ImageWithFallback } from './figma/ImageWithFallback';

interface RestaurantCardProps {
  restaurant: Restaurant;
}

export function RestaurantCard({ restaurant }: RestaurantCardProps) {
  const navigate = useNavigate();

  return (
    <div
      onClick={() => navigate(`/r/${restaurant.id}`)}
      className="bg-white rounded-xl overflow-hidden shadow-sm border border-gray-200 cursor-pointer transition-all active:scale-[0.98]"
    >
      {/* Image */}
      <div className="relative h-48 bg-gray-100">
        <ImageWithFallback
          src={restaurant.backgroundUrl}
          alt={restaurant.name}
          className="w-full h-full object-cover"
        />
        {restaurant.hasPromotions && (
          <Badge className="absolute top-3 right-3 bg-rose-600">
            <Tag className="w-3 h-3 mr-1" />
            Акции
          </Badge>
        )}
      </div>

      {/* Content */}
      <div className="p-4">
        <div className="flex items-start justify-between mb-2">
          <h3 className="flex-1">{restaurant.name}</h3>
          {restaurant.rating && (
            <div className="flex items-center gap-1 ml-2 bg-amber-50 px-2 py-1 rounded-lg">
              <Star className="w-4 h-4 fill-amber-400 text-amber-400" />
              <span className="text-amber-700">{restaurant.rating}</span>
            </div>
          )}
        </div>

        {restaurant.description && (
          <p className="text-gray-600 mb-3 line-clamp-2">
            {restaurant.description}
          </p>
        )}

        <div className="flex items-center gap-4 text-gray-500">
          {restaurant.distance !== undefined && (
            <div className="flex items-center gap-1">
              <MapPin className="w-4 h-4" />
              <span>{restaurant.distance} км</span>
            </div>
          )}
          {restaurant.cuisineType && (
            <Badge variant="secondary">{restaurant.cuisineType}</Badge>
          )}
        </div>

        {restaurant.address && (
          <div className="flex items-center gap-2 mt-3 text-gray-600">
            <MapPin className="w-4 h-4" />
            <span>{restaurant.address}</span>
          </div>
        )}

        {/* Feature badges */}
        <div className="flex gap-2 mt-3">
          {restaurant.isOutdoor && (
            <Badge variant="outline" className="text-green-700 border-green-300">
              Открытая веранда
            </Badge>
          )}
          {restaurant.isSmoking && (
            <Badge variant="outline" className="text-gray-700 border-gray-300">
              Курящий зал
            </Badge>
          )}
        </div>
      </div>
    </div>
  );
}

