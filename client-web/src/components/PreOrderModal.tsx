import { useState, useEffect } from 'react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Card } from './ui/card';
import { Plus, Minus, Trash2 } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from './ui/dialog';
import { restaurantApi } from '../services/api';
import { mapMenuCategory } from '../utils/mappers';
import type { MenuCategory, Dish } from '../types/restaurant';
import { toast } from 'sonner';

interface PreOrderModalProps {
  restaurantId: number;
  onClose: () => void;
  onConfirm: (items: PreOrderItem[]) => void;
  initialItems?: PreOrderItem[];
}

export interface PreOrderItem {
  menuItemId: string;
  name: string;
  quantity: number;
  price: number;
  specialRequests: string | null;
}

export function PreOrderModal({ restaurantId, onClose, onConfirm, initialItems = [] }: PreOrderModalProps) {
  const [menuCategories, setMenuCategories] = useState<MenuCategory[]>([]);
  const [cartItems, setCartItems] = useState<PreOrderItem[]>(initialItems);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadMenu();
  }, [restaurantId]);

  const loadMenu = async () => {
    setIsLoading(true);
    try {
      const apiMenu = await restaurantApi.getMenu(restaurantId);
      const mappedCategories = apiMenu.map(mapMenuCategory);
      setMenuCategories(mappedCategories);
    } catch (err) {
      console.error('Error loading menu:', err);
      toast.error('Не удалось загрузить меню');
    } finally {
      setIsLoading(false);
    }
  };

  const addItem = (dish: Dish) => {
    const existingItem = cartItems.find(item => item.menuItemId === dish.id);
    if (existingItem) {
      setCartItems(cartItems.map(item =>
        item.menuItemId === dish.id
          ? { ...item, quantity: item.quantity + 1 }
          : item
      ));
    } else {
      setCartItems([...cartItems, {
        menuItemId: dish.id,
        name: dish.name,
        quantity: 1,
        price: dish.price,
        specialRequests: null
      }]);
    }
  };

  const updateQuantity = (menuItemId: string, delta: number) => {
    const newQuantity = (cartItems.find(item => item.menuItemId === menuItemId)?.quantity || 1) + delta;
    if (newQuantity <= 0) {
      removeItem(menuItemId);
    } else {
      setCartItems(cartItems.map(item =>
        item.menuItemId === menuItemId
          ? { ...item, quantity: newQuantity }
          : item
      ));
    }
  };

  const removeItem = (menuItemId: string) => {
    setCartItems(cartItems.filter(item => item.menuItemId !== menuItemId));
  };

  const updateSpecialRequests = (menuItemId: string, requests: string) => {
    setCartItems(cartItems.map(item =>
      item.menuItemId === menuItemId
        ? { ...item, specialRequests: requests || null }
        : item
    ));
  };

  const totalCost = cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);

  return (
    <Dialog open={true} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[600px] max-h-[85vh] overflow-hidden flex flex-col">
        <DialogHeader>
          <DialogTitle>Предзаказ блюд</DialogTitle>
          <DialogDescription>
            Выберите блюда для предзаказа
          </DialogDescription>
        </DialogHeader>
        
        <div className="flex-1 overflow-y-auto space-y-4 py-4">
          {isLoading ? (
            <div className="text-center py-8 text-gray-500">Загрузка меню...</div>
          ) : (
            <>
              {/* Menu Categories */}
              {menuCategories.map(category => (
                <div key={category.id} className="space-y-2">
                  <h3 className="font-semibold">{category.name}</h3>
                  <div className="space-y-2">
                    {category.dishes.map(dish => {
                      const itemInCart = cartItems.find(item => item.menuItemId === dish.id);
                      return (
                        <Card key={dish.id} className="p-3">
                          <div className="flex items-center justify-between gap-3">
                            <div className="flex-1 min-w-0">
                              <h4 className="mb-1">{dish.name}</h4>
                              <p className="text-gray-600 text-sm line-clamp-1">{dish.description}</p>
                              <p className="text-gray-700 font-semibold mt-1">{dish.price} ₸</p>
                            </div>
                            {itemInCart ? (
                              <div className="flex items-center gap-2">
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
                                  className="w-8 h-8"
                                >
                                  <Plus className="w-4 h-4" />
                                </Button>
                              </div>
                            ) : (
                              <Button
                                size="icon"
                                onClick={() => addItem(dish)}
                                className="w-8 h-8"
                              >
                                <Plus className="w-4 h-4" />
                              </Button>
                            )}
                          </div>
                          {itemInCart && (
                            <div className="mt-2 pt-2 border-t">
                              <Input
                                placeholder="Особые пожелания"
                                value={itemInCart.specialRequests || ''}
                                onChange={(e) => updateSpecialRequests(dish.id, e.target.value)}
                                className="text-sm"
                              />
                            </div>
                          )}
                        </Card>
                      );
                    })}
                  </div>
                </div>
              ))}
            </>
          )}
        </div>

        {/* Cart Summary */}
        {cartItems.length > 0 && (
          <div className="border-t pt-4 space-y-3">
            <div className="space-y-2 max-h-32 overflow-y-auto">
              {cartItems.map(item => (
                <div key={item.menuItemId} className="flex items-center justify-between text-sm">
                  <span className="flex-1">{item.name} × {item.quantity}</span>
                  <span className="mr-2">{item.price * item.quantity} ₸</span>
                  <Button
                    size="icon"
                    variant="ghost"
                    onClick={() => removeItem(item.menuItemId)}
                    className="w-6 h-6"
                  >
                    <Trash2 className="w-3 h-3 text-red-600" />
                  </Button>
                </div>
              ))}
            </div>
            <div className="flex justify-between items-center font-semibold pt-2 border-t">
              <span>Итого:</span>
              <span>{totalCost} ₸</span>
            </div>
          </div>
        )}

        {/* Actions */}
        <div className="flex gap-2 pt-4 border-t">
          <Button
            variant="outline"
            onClick={onClose}
            className="flex-1"
          >
            Отмена
          </Button>
          <Button
            onClick={() => onConfirm(cartItems)}
            className="flex-1"
          >
            Сохранить ({cartItems.length})
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}

