import { MenuCategory } from '../data/mockData';
import { ImageWithFallback } from './figma/ImageWithFallback';

interface MenuSectionProps {
  category: MenuCategory;
  restaurantColor: string;
}

export function MenuSection({ category, restaurantColor }: MenuSectionProps) {
  return (
    <div className="bg-white rounded-xl overflow-hidden">
      <div 
        className="p-4"
        style={{ backgroundColor: `${restaurantColor}15` }}
      >
        <h3 style={{ color: restaurantColor }}>{category.name}</h3>
      </div>
      
      <div className="divide-y">
        {category.dishes.map(dish => (
          <div key={dish.id} className="p-4 flex gap-4">
            <div className="w-24 h-24 rounded-lg overflow-hidden bg-gray-100 flex-shrink-0">
              <ImageWithFallback
                src={dish.imageUrl}
                alt={dish.name}
                className="w-full h-full object-cover"
              />
            </div>
            
            <div className="flex-1 min-w-0">
              <div className="flex items-start justify-between gap-2 mb-1">
                <h4 className="flex-1">{dish.name}</h4>
                <p 
                  className="flex-shrink-0"
                  style={{ color: restaurantColor }}
                >
                  {dish.price} ₸
                </p>
              </div>
              
              <p className="text-gray-600 mb-2 line-clamp-2">
                {dish.description}
              </p>
              
              {dish.weight && (
                <p className="text-gray-500">
                  {dish.weight}
                </p>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
