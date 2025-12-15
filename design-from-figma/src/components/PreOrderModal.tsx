import { useState } from 'react';
import { Restaurant } from '../data/mockData';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { X, Plus, Minus, Trash2 } from 'lucide-react';
import { ImageWithFallback } from './figma/ImageWithFallback';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { Badge } from './ui/badge';

interface PreOrderItem {
  menuItemId: string;
  name: string;
  quantity: number;
  price: number;
  specialRequests: string | null;
}

interface PreOrderModalProps {
  restaurant: Restaurant;
  preOrderItems: PreOrderItem[];
  onClose: () => void;
  onSave: (items: PreOrderItem[]) => void;
}

export function PreOrderModal({ restaurant, preOrderItems, onClose, onSave }: PreOrderModalProps) {
  const [items, setItems] = useState<PreOrderItem[]>(preOrderItems);

  const addItem = (dishId: string, dishName: string, price: number) => {
    const existingItem = items.find(item => item.menuItemId === dishId);
    if (existingItem) {
      setItems(items.map(item =>
        item.menuItemId === dishId
          ? { ...item, quantity: item.quantity + 1 }
          : item
      ));
    } else {
      setItems([...items, {
        menuItemId: dishId,
        name: dishName,
        quantity: 1,
        price: price,
        specialRequests: null
      }]);
    }
  };

  const updateQuantity = (menuItemId: string, delta: number) => {
    const newQuantity = (items.find(item => item.menuItemId === menuItemId)?.quantity || 1) + delta;
    if (newQuantity <= 0) {
      removeItem(menuItemId);
    } else {
      setItems(items.map(item =>
        item.menuItemId === menuItemId
          ? { ...item, quantity: newQuantity }
          : item
      ));
    }
  };

  const removeItem = (menuItemId: string) => {
    setItems(items.filter(item => item.menuItemId !== menuItemId));
  };

  const updateSpecialRequests = (menuItemId: string, requests: string) => {
    setItems(items.map(item =>
      item.menuItemId === menuItemId
        ? { ...item, specialRequests: requests || null }
        : item
    ));
  };

  const totalCost = items.reduce((sum, item) => sum + item.price * item.quantity, 0);

  return (
    <div className="fixed inset-0 bg-black/50 z-50 flex items-end md:items-center justify-center">
      <div className="bg-white w-full md:max-w-2xl md:rounded-lg max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="p-4 border-b flex items-center justify-between">
          <h2>Предзаказ меню</h2>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="w-5 h-5" />
          </Button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto">
          <Tabs defaultValue="menu" className="w-full">
            <TabsList className="w-full rounded-none border-b">
              <TabsTrigger value="menu" className="flex-1">Меню</TabsTrigger>
              <TabsTrigger value="cart" className="flex-1">
                Корзина {items.length > 0 && `(${items.length})`}
              </TabsTrigger>
            </TabsList>

            {/* Menu Tab */}
            <TabsContent value="menu" className="mt-0">
              <div className="p-4">
                {restaurant.menuCategories.map(category => (
                  <div key={category.id} className="mb-6">
                    <h3 className="mb-3" style={{ color: restaurant.primaryColor }}>
                      {category.name}
                    </h3>
                    <div className="space-y-3">
                      {category.dishes.map(dish => {
                        const itemInCart = items.find(item => item.menuItemId === dish.id);
                        return (
                          <div key={dish.id} className="flex gap-3 p-3 bg-gray-50 rounded-lg">
                            <div className="w-20 h-20 rounded-lg overflow-hidden bg-gray-100 flex-shrink-0 relative">
                              <ImageWithFallback
                                src={dish.imageUrl}
                                alt={dish.name}
                                className="w-full h-full object-cover"
                              />
                              {dish.isNew && (
                                <Badge className="absolute top-1 left-1 bg-green-600 text-white text-xs">
                                  Новинка
                                </Badge>
                              )}
                              {dish.hasPromo && (
                                <Badge className="absolute top-1 left-1 bg-rose-600 text-white text-xs">
                                  Акция
                                </Badge>
                              )}
                            </div>
                            
                            <div className="flex-1 min-w-0">
                              <h4>{dish.name}</h4>
                              <p className="text-gray-600 line-clamp-1">{dish.description}</p>
                              <p style={{ color: restaurant.primaryColor }}>
                                {dish.price} ₸
                              </p>
                            </div>

                            {itemInCart ? (
                              <div className="flex items-center gap-1 flex-shrink-0">
                                <Button
                                  size="icon"
                                  variant="outline"
                                  onClick={() => updateQuantity(dish.id, -1)}
                                  className="w-8 h-8"
                                >
                                  <Minus className="w-4 h-4" />
                                </Button>
                                <span className="w-8 text-center">{itemInCart.quantity}</span>
                                <Button
                                  size="icon"
                                  onClick={() => updateQuantity(dish.id, 1)}
                                  style={{ backgroundColor: restaurant.primaryColor }}
                                  className="w-8 h-8"
                                >
                                  <Plus className="w-4 h-4" />
                                </Button>
                              </div>
                            ) : (
                              <Button
                                size="icon"
                                onClick={() => addItem(dish.id, dish.name, dish.price)}
                                style={{ backgroundColor: restaurant.primaryColor }}
                                className="flex-shrink-0"
                              >
                                <Plus className="w-4 h-4" />
                              </Button>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  </div>
                ))}
              </div>
            </TabsContent>

            {/* Cart Tab */}
            <TabsContent value="cart" className="mt-0">
              <div className="p-4">
                {items.length === 0 ? (
                  <div className="text-center py-12 text-gray-500">
                    <p>Корзина пуста</p>
                    <p className="mt-2">Добавьте блюда из меню</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {items.map((item) => (
                      <div key={item.menuItemId} className="bg-gray-50 rounded-lg p-4">
                        <div className="flex items-start gap-3 mb-3">
                          <div className="flex-1">
                            <h4>{item.name}</h4>
                            <p className="text-gray-600">{item.price} ₸</p>
                          </div>
                          
                          <div className="flex items-center gap-2">
                            <Button
                              size="icon"
                              variant="outline"
                              onClick={() => updateQuantity(item.menuItemId, -1)}
                            >
                              <Minus className="w-4 h-4" />
                            </Button>
                            <span className="w-8 text-center">{item.quantity}</span>
                            <Button
                              size="icon"
                              variant="outline"
                              onClick={() => updateQuantity(item.menuItemId, 1)}
                            >
                              <Plus className="w-4 h-4" />
                            </Button>
                            <Button
                              size="icon"
                              variant="ghost"
                              onClick={() => removeItem(item.menuItemId)}
                            >
                              <Trash2 className="w-4 h-4 text-red-600" />
                            </Button>
                          </div>
                        </div>

                        <Input
                          placeholder="Особые пожелания (необязательно)"
                          value={item.specialRequests || ''}
                          onChange={(e) => updateSpecialRequests(item.menuItemId, e.target.value)}
                        />

                        <p className="text-right mt-2">
                          Итого: {item.price * item.quantity} ₸
                        </p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </TabsContent>
          </Tabs>
        </div>

        {/* Footer */}
        {items.length > 0 && (
          <div className="p-4 border-t">
            <div className="flex justify-between items-center mb-3">
              <span>Общая сумма:</span>
              <span className="text-xl" style={{ color: restaurant.primaryColor }}>
                {totalCost} ₸
              </span>
            </div>
            <Button
              className="w-full"
              onClick={() => onSave(items)}
              style={{ backgroundColor: restaurant.primaryColor }}
            >
              Добавить к бронированию
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}