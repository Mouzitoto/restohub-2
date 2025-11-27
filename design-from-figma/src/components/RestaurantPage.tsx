import { useParams, useNavigate } from 'react-router-dom';
import { mockRestaurants, mockPromotions } from '../data/mockData';
import { Button } from './ui/button';
import { ArrowLeft, MapPin, Phone, Mail, Calendar, MessageCircle, Instagram, Globe, UtensilsCrossed } from 'lucide-react';
import { PromotionCard } from './PromotionCard';
import { Badge } from './ui/badge';
import { ImageWithFallback } from './figma/ImageWithFallback';
import { useEffect, useRef } from 'react';
import { RestoHubLogo } from './RestoHubLogo';

export function RestaurantPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const menuRef = useRef<HTMLDivElement>(null);
  const promotionsRef = useRef<HTMLDivElement>(null);

  const restaurant = mockRestaurants.find(r => r.id === id);
  const promotions = mockPromotions[id || ''] || [];

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [id]);

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

  const scrollToSection = (ref: React.RefObject<HTMLDivElement>) => {
    ref.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header with background image */}
      <div className="relative h-64 bg-gray-900">
        <ImageWithFallback
          src={restaurant.backgroundUrl}
          alt={restaurant.name}
          className="w-full h-full object-cover opacity-70"
        />
        
        {/* Back button */}
        <Button
          variant="secondary"
          size="icon"
          className="absolute top-4 left-4 rounded-full"
          onClick={() => navigate('/')}
        >
          <ArrowLeft className="w-5 h-5" />
        </Button>

        {/* Restaurant logo and name */}
        <div className="absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-black/80 to-transparent">
          <div className="flex items-end gap-4">
            <div className="w-20 h-20 rounded-xl overflow-hidden border-2 border-white bg-white">
              <ImageWithFallback
                src={restaurant.logoUrl}
                alt={restaurant.name}
                className="w-full h-full object-cover"
              />
            </div>
            <div className="flex-1 pb-2">
              <h1 className="text-white mb-1">{restaurant.name}</h1>
              <Badge 
                style={{ backgroundColor: restaurant.primaryColor }}
                className="text-white"
              >
                {restaurant.cuisineType}
              </Badge>
            </div>
          </div>
        </div>
      </div>

      {/* Quick navigation */}
      <div className="bg-white border-b sticky top-0 z-10 overflow-x-auto">
        <div className="flex gap-2 p-3 min-w-max">
          <Button 
            variant="outline" 
            size="sm"
            onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
          >
            О ресторане
          </Button>
          {promotions.length > 0 && (
            <Button 
              variant="outline" 
              size="sm"
              onClick={() => scrollToSection(promotionsRef)}
            >
              Акции
            </Button>
          )}
        </div>
      </div>

      {/* Main content */}
      <div className="p-4 space-y-6">
        {/* Action buttons */}
        <div className="space-y-3">
          <Button 
            className="w-full h-14"
            style={{ backgroundColor: restaurant.primaryColor }}
            onClick={() => navigate(`/r/${id}/booking/rooms`)}
          >
            <Calendar className="w-5 h-5 mr-2" />
            Забронировать стол
          </Button>

          {restaurant.menuCategories.length > 0 && (
            <Button 
              variant="outline"
              className="w-full h-14 border-2"
              style={{ borderColor: restaurant.primaryColor, color: restaurant.primaryColor }}
              onClick={() => navigate(`/r/${id}/menu`)}
            >
              <UtensilsCrossed className="w-5 h-5 mr-2" />
              Меню ресторана
            </Button>
          )}
        </div>

        {/* Restaurant info */}
        <div className="bg-white rounded-xl p-5 space-y-4">
          {/* Description */}
          {restaurant.description && (
            <div>
              <p className="text-gray-700 leading-relaxed">{restaurant.description}</p>
            </div>
          )}

          {/* Address - Full Display */}
          <a
            href={`https://maps.google.com/?q=${restaurant.lat},${restaurant.lng}`}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-start gap-3 p-3 rounded-lg hover:bg-gray-50 transition-all group"
          >
            <MapPin className="w-5 h-5 flex-shrink-0 mt-0.5" style={{ color: restaurant.primaryColor }} />
            <div className="flex-1 min-w-0">
              <p className="text-gray-900">{restaurant.address}</p>
              <p className="text-gray-500 group-hover:text-gray-700 transition-colors">Открыть на карте</p>
            </div>
          </a>

          {/* Social Icons Row */}
          <div className="flex items-center justify-center gap-3 pt-2">
            {/* Phone */}
            <a
              href={`tel:${restaurant.phone}`}
              className="w-12 h-12 rounded-full flex items-center justify-center border-2 border-gray-200 hover:border-gray-300 hover:bg-gray-50 transition-all"
              aria-label="Позвонить"
            >
              <Phone className="w-5 h-5" style={{ color: restaurant.primaryColor }} />
            </a>

            {/* Instagram */}
            {restaurant.instagram && (
              <a
                href={`https://instagram.com/${restaurant.instagram}`}
                target="_blank"
                rel="noopener noreferrer"
                className="w-12 h-12 rounded-full flex items-center justify-center border-2 border-gray-200 hover:border-gray-300 hover:bg-gray-50 transition-all"
                aria-label="Instagram"
              >
                <Instagram className="w-5 h-5" style={{ color: restaurant.primaryColor }} />
              </a>
            )}

            {/* WhatsApp */}
            {restaurant.whatsapp && (
              <a
                href={`https://wa.me/${restaurant.whatsapp}`}
                target="_blank"
                rel="noopener noreferrer"
                className="w-12 h-12 rounded-full flex items-center justify-center border-2 border-gray-200 hover:border-gray-300 hover:bg-gray-50 transition-all"
                aria-label="WhatsApp"
              >
                <MessageCircle className="w-5 h-5" style={{ color: restaurant.primaryColor }} />
              </a>
            )}

            {/* Email */}
            <a
              href={`mailto:${restaurant.email}`}
              className="w-12 h-12 rounded-full flex items-center justify-center border-2 border-gray-200 hover:border-gray-300 hover:bg-gray-50 transition-all"
              aria-label="Email"
            >
              <Mail className="w-5 h-5" style={{ color: restaurant.primaryColor }} />
            </a>

            {/* Website */}
            {restaurant.website && (
              <a
                href={restaurant.website}
                target="_blank"
                rel="noopener noreferrer"
                className="w-12 h-12 rounded-full flex items-center justify-center border-2 border-gray-200 hover:border-gray-300 hover:bg-gray-50 transition-all"
                aria-label="Сайт"
              >
                <Globe className="w-5 h-5" style={{ color: restaurant.primaryColor }} />
              </a>
            )}
          </div>

          {/* Features */}
          {(restaurant.isOutdoor || restaurant.isSmoking) && (
            <div className="flex gap-2 pt-2">
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
          )}
        </div>

        {/* Promotions */}
        {promotions.length > 0 && (
          <div ref={promotionsRef} className="space-y-3">
            <h2>Акции и события</h2>
            {promotions.map(promotion => (
              <PromotionCard key={promotion.id} promotion={promotion} />
            ))}
          </div>
        )}

        {/* Footer */}
        <div className="mt-12 pt-8 border-t border-gray-200">
          <div className="flex flex-col items-center gap-4 text-center">
            <RestoHubLogo className="h-8" />
            <p className="text-gray-500">
              Забронировано через RestoHub
            </p>
            <div className="flex gap-4 text-gray-400">
              <a href="#" className="hover:text-rose-600 transition-colors">
                О сервисе
              </a>
              <span>•</span>
              <a href="#" className="hover:text-rose-600 transition-colors">
                Поддержка
              </a>
              <span>•</span>
              <a href="#" className="hover:text-rose-600 transition-colors">
                Условия
              </a>
            </div>
            <p className="text-gray-400">
              © 2024 RestoHub. Все права защищены.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}