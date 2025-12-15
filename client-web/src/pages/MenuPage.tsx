import { useState, useRef, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { ArrowLeft, Plus, Minus, Trash2, ShoppingCart, Eye, Calendar, X } from 'lucide-react';
import { ImageWithFallback } from '../components/figma/ImageWithFallback';
import { Badge } from '../components/ui/badge';
import { Card } from '../components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '../components/ui/dialog';
import { restaurantApi } from '../services/api';
import { mapMenuCategory, mapRestaurant } from '../utils/mappers';
import type { MenuCategory, Dish, Restaurant } from '../types/restaurant';
import { toast } from 'sonner';

interface CartItem {
  menuItemId: string;
  name: string;
  quantity: number;
  price: number;
  specialRequests: string | null;
}

type CategoryFilter = 'all' | 'new' | 'promo' | string;

export function MenuPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
  const [menuCategories, setMenuCategories] = useState<MenuCategory[]>([]);
  const [cartItems, setCartItems] = useState<CartItem[]>([]);
  const [showCart, setShowCart] = useState(false);
  const [selectedFilter, setSelectedFilter] = useState<CategoryFilter>('all');
  const [showWaiterModal, setShowWaiterModal] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedImage, setSelectedImage] = useState<{ url: string; alt: string } | null>(null);
  
  const categoryRefs = useRef<{ [key: string]: HTMLDivElement | null }>({});

  useEffect(() => {
    if (id) {
      loadMenu();
    }
  }, [id]);

  const loadMenu = async () => {
    if (!id) return;
    
    setIsLoading(true);
    setError(null);
    
    try {
      const restaurantId = parseInt(id, 10);
      if (isNaN(restaurantId)) {
        setError('Неверный ID ресторана');
        return;
      }

      // Load restaurant info
      const apiRestaurant = await restaurantApi.getRestaurant(restaurantId);
      const mappedRestaurant = mapRestaurant(apiRestaurant, false);
      setRestaurant(mappedRestaurant);

      // Load menu
      const apiMenu = await restaurantApi.getMenu(restaurantId);
      const mappedCategories = apiMenu.map(mapMenuCategory);
      setMenuCategories(mappedCategories);
    } catch (err: any) {
      console.error('Error loading menu:', err);
      if (err.message?.includes('404')) {
        setError('Меню не найдено');
      } else {
        setError('Ошибка при загрузке меню');
      }
      toast.error('Не удалось загрузить меню');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-pulse text-gray-500">Загрузка меню...</div>
        </div>
      </div>
    );
  }

  if (error || !restaurant) {
    return (
      <div className="min-h-screen bg-gray-50 p-4">
        <div className="max-w-md mx-auto mt-8 text-center">
          <p className="text-gray-500 mb-4">{error || 'Меню не найдено'}</p>
          <Button onClick={() => navigate(`/r/${id}`)} className="mt-4">
            Назад
          </Button>
        </div>
      </div>
    );
  }

  // Get all dishes for special categories
  const allDishes: Dish[] = menuCategories.flatMap(cat => cat.dishes);
  const newDishes = allDishes.filter(dish => dish.isNew);
  const promoDishes = allDishes.filter(dish => dish.hasPromo);

  const addItem = (dishId: string, dishName: string, price: number) => {
    const existingItem = cartItems.find(item => item.menuItemId === dishId);
    if (existingItem) {
      setCartItems(cartItems.map(item =>
        item.menuItemId === dishId
          ? { ...item, quantity: item.quantity + 1 }
          : item
      ));
    } else {
      setCartItems([...cartItems, {
        menuItemId: dishId,
        name: dishName,
        quantity: 1,
        price: price,
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
  const totalItems = cartItems.reduce((sum, item) => sum + item.quantity, 0);

  const handleShowToWaiter = () => {
    setShowWaiterModal(true);
  };

  const scrollToCategory = (categoryId: string) => {
    setSelectedFilter(categoryId);
    if (categoryId === 'all' || categoryId === 'new' || categoryId === 'promo') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else {
      categoryRefs.current[categoryId]?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };

  const getItemInCart = (dishId: string) => {
    return cartItems.find(item => item.menuItemId === dishId);
  };

  const renderDishCard = (dish: Dish) => {
    const itemInCart = getItemInCart(dish.id);
    
    return (
      <Card key={dish.id} className="p-3">
        <div className="flex gap-3">
          <div 
            className="w-24 h-24 rounded-lg overflow-hidden bg-gray-100 flex-shrink-0 relative cursor-pointer"
            onClick={(e) => {
              e.stopPropagation();
              if (dish.imageUrl) {
                // Заменяем превью на полное изображение
                const fullImageUrl = dish.imageUrl.replace('?preview=true', '').replace('&preview=true', '');
                setSelectedImage({ url: fullImageUrl, alt: dish.name });
              }
            }}
          >
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
            <h3 className="mb-1">{dish.name}</h3>
            <p className="text-gray-600 line-clamp-2 mb-2">{dish.description}</p>
            {dish.weight && (
              <p className="text-gray-500 mb-2">{dish.weight}</p>
            )}
            <div className="flex items-center justify-between">
              <p style={{ color: restaurant.primaryColor }}>
                {dish.price} ₸
              </p>
            </div>
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
        {itemInCart && (
          <div className="mt-2 pt-2 border-t">
            <p className="text-right" style={{ color: restaurant.primaryColor }}>
              Сумма: {dish.price * itemInCart.quantity} ₸
            </p>
          </div>
        )}
      </Card>
    );
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-32">
      {/* Header */}
      <div className="bg-white border-b sticky top-0 z-20">
        <div className="p-4">
          <div className="flex items-center gap-3 mb-3">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => navigate(`/r/${id}`)}
            >
              <ArrowLeft className="w-5 h-5" />
            </Button>
            <div className="flex-1">
              <h1>Меню</h1>
              <p className="text-gray-600">{restaurant.name}</p>
            </div>
            <Button
              variant="outline"
              size="icon"
              onClick={() => setShowCart(!showCart)}
              className="relative"
            >
              <ShoppingCart className="w-5 h-5" />
              {cartItems.length > 0 && (
                <span className="absolute -top-1 -right-1 w-5 h-5 bg-rose-600 text-white rounded-full text-xs flex items-center justify-center">
                  {cartItems.length}
                </span>
              )}
            </Button>
          </div>

          {/* Category filters with images */}
          {!showCart && (
            <div className="overflow-x-auto -mx-4 px-4">
              <div className="flex gap-3 min-w-max pb-2">
                {/* All Menu */}
                <button
                  onClick={() => scrollToCategory('all')}
                  className={`flex flex-col items-center gap-2 min-w-[90px] transition-all ${
                    selectedFilter === 'all' ? 'opacity-100' : 'opacity-60'
                  }`}
                >
                  <div 
                    className="w-20 h-20 rounded-2xl overflow-hidden border-2 transition-all"
                    style={{ 
                      borderColor: selectedFilter === 'all' ? restaurant.primaryColor : '#e5e7eb'
                    }}
                  >
                    <ImageWithFallback
                      src="https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=200"
                      alt="Всё меню"
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <span 
                    className="text-sm text-center leading-tight"
                    style={{ 
                      color: selectedFilter === 'all' ? restaurant.primaryColor : '#6b7280'
                    }}
                  >
                    Всё меню
                  </span>
                </button>
                
                {/* New Dishes */}
                {newDishes.length > 0 && (
                  <button
                    onClick={() => scrollToCategory('new')}
                    className={`flex flex-col items-center gap-2 min-w-[90px] transition-all ${
                      selectedFilter === 'new' ? 'opacity-100' : 'opacity-60'
                    }`}
                  >
                    <div 
                      className="w-20 h-20 rounded-2xl overflow-hidden border-2 relative transition-all"
                      style={{ 
                        borderColor: selectedFilter === 'new' ? restaurant.primaryColor : '#e5e7eb'
                      }}
                    >
                      <ImageWithFallback
                        src={newDishes[0].imageUrl || 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=200'}
                        alt="Новинки"
                        className="w-full h-full object-cover"
                      />
                      <Badge className="absolute top-1 right-1 bg-green-600 text-white text-xs">
                        New
                      </Badge>
                    </div>
                    <span 
                      className="text-sm text-center leading-tight"
                      style={{ 
                        color: selectedFilter === 'new' ? restaurant.primaryColor : '#6b7280'
                      }}
                    >
                      Новинки
                    </span>
                  </button>
                )}
                
                {/* Promo Dishes */}
                {promoDishes.length > 0 && (
                  <button
                    onClick={() => scrollToCategory('promo')}
                    className={`flex flex-col items-center gap-2 min-w-[90px] transition-all ${
                      selectedFilter === 'promo' ? 'opacity-100' : 'opacity-60'
                    }`}
                  >
                    <div 
                      className="w-20 h-20 rounded-2xl overflow-hidden border-2 relative transition-all"
                      style={{ 
                        borderColor: selectedFilter === 'promo' ? restaurant.primaryColor : '#e5e7eb'
                      }}
                    >
                      <ImageWithFallback
                        src={promoDishes[0].imageUrl || 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=200'}
                        alt="Акции"
                        className="w-full h-full object-cover"
                      />
                      <Badge className="absolute top-1 right-1 bg-rose-600 text-white text-xs">
                        %
                      </Badge>
                    </div>
                    <span 
                      className="text-sm text-center leading-tight"
                      style={{ 
                        color: selectedFilter === 'promo' ? restaurant.primaryColor : '#6b7280'
                      }}
                    >
                      Акции
                    </span>
                  </button>
                )}

                {/* Regular Categories */}
                {menuCategories.map(category => (
                  <button
                    key={category.id}
                    onClick={() => scrollToCategory(category.id)}
                    className={`flex flex-col items-center gap-2 min-w-[90px] transition-all ${
                      selectedFilter === category.id ? 'opacity-100' : 'opacity-60'
                    }`}
                  >
                    <div 
                      className="w-20 h-20 rounded-2xl overflow-hidden border-2 transition-all cursor-pointer"
                      style={{ 
                        borderColor: selectedFilter === category.id ? restaurant.primaryColor : '#e5e7eb'
                      }}
                      onClick={(e) => {
                        e.stopPropagation();
                        const imageUrl = category.imageUrl || category.dishes[0]?.imageUrl;
                        if (imageUrl) {
                          // Заменяем превью на полное изображение
                          const fullImageUrl = imageUrl.replace('?preview=true', '').replace('&preview=true', '');
                          setSelectedImage({ url: fullImageUrl, alt: category.name });
                        }
                      }}
                    >
                      <ImageWithFallback
                        src={category.imageUrl || category.dishes[0]?.imageUrl || 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=200'}
                        alt={category.name}
                        className="w-full h-full object-cover"
                      />
                    </div>
                    <span 
                      className="text-sm text-center leading-tight"
                      style={{ 
                        color: selectedFilter === category.id ? restaurant.primaryColor : '#6b7280'
                      }}
                    >
                      {category.name}
                    </span>
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Content */}
      <div className="p-4">
        {showCart ? (
          /* Cart View */
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h2>Корзина</h2>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setShowCart(false)}
              >
                Продолжить выбор
              </Button>
            </div>

            {cartItems.length === 0 ? (
              <Card className="p-12 text-center">
                <ShoppingCart className="w-12 h-12 mx-auto mb-4 text-gray-400" />
                <p className="text-gray-500">Корзина пуста</p>
                <p className="text-gray-400 mt-2">Добавьте блюда из меню</p>
              </Card>
            ) : (
              <div className="space-y-3">
                {cartItems.map((item) => (
                  <Card key={item.menuItemId} className="p-4">
                    <div className="flex items-start gap-3 mb-3">
                      <div className="flex-1">
                        <h3>{item.name}</h3>
                        <p className="text-gray-600">{item.price} ₸</p>
                      </div>
                      
                      <div className="flex items-center gap-2">
                        <Button
                          size="icon"
                          variant="outline"
                          onClick={() => updateQuantity(item.menuItemId, -1)}
                          className="w-8 h-8"
                        >
                          <Minus className="w-4 h-4" />
                        </Button>
                        <span className="w-8 text-center">{item.quantity}</span>
                        <Button
                          size="icon"
                          variant="outline"
                          onClick={() => updateQuantity(item.menuItemId, 1)}
                          className="w-8 h-8"
                        >
                          <Plus className="w-4 h-4" />
                        </Button>
                        <Button
                          size="icon"
                          variant="ghost"
                          onClick={() => removeItem(item.menuItemId)}
                          className="w-8 h-8"
                        >
                          <Trash2 className="w-4 h-4 text-red-600" />
                        </Button>
                      </div>
                    </div>

                    <Input
                      placeholder="Особые пожелания (необязательно)"
                      value={item.specialRequests || ''}
                      onChange={(e) => updateSpecialRequests(item.menuItemId, e.target.value)}
                      className="mb-2"
                    />

                    <p className="text-right">
                      Итого: <span style={{ color: restaurant.primaryColor }}>{item.price * item.quantity} ₸</span>
                    </p>
                  </Card>
                ))}
              </div>
            )}
          </div>
        ) : (
          /* Menu View */
          <div className="space-y-6">
            {/* Special categories */}
            {selectedFilter === 'new' && newDishes.length > 0 && (
              <div>
                <h2 className="mb-4" style={{ color: restaurant.primaryColor }}>
                  Новинки
                </h2>
                <div className="space-y-3">
                  {newDishes.map(dish => renderDishCard(dish))}
                </div>
              </div>
            )}

            {selectedFilter === 'promo' && promoDishes.length > 0 && (
              <div>
                <h2 className="mb-4" style={{ color: restaurant.primaryColor }}>
                  Акции
                </h2>
                <div className="space-y-3">
                  {promoDishes.map(dish => renderDishCard(dish))}
                </div>
              </div>
            )}

            {/* Regular categories */}
            {(selectedFilter === 'all' || (!['new', 'promo'].includes(selectedFilter))) && (
              <>
                {menuCategories
                  .filter(category => selectedFilter === 'all' || selectedFilter === category.id)
                  .map(category => (
                    <div 
                      key={category.id}
                      ref={(el) => categoryRefs.current[category.id] = el}
                    >
                      <h2 className="mb-4" style={{ color: restaurant.primaryColor }}>
                        {category.name}
                      </h2>
                      <div className="space-y-3">
                        {category.dishes.map(dish => renderDishCard(dish))}
                      </div>
                    </div>
                  ))}
              </>
            )}
          </div>
        )}
      </div>

      {/* Fixed Bottom Actions */}
      {cartItems.length > 0 && (
        <div className="fixed bottom-0 left-0 right-0 bg-white border-t shadow-lg z-10">
          <div className="p-4 space-y-3">
            {/* Total */}
            <div className="flex justify-between items-center">
              <div>
                <p className="text-gray-600">Позиций: {totalItems}</p>
                <p className="text-gray-600">Сумма:</p>
              </div>
              <p className="text-2xl" style={{ color: restaurant.primaryColor }}>
                {totalCost} ₸
              </p>
            </div>

            {/* Action Buttons */}
            <div className="grid grid-cols-2 gap-3">
              <Button
                variant="outline"
                onClick={handleShowToWaiter}
                className="h-12"
              >
                <Eye className="w-5 h-5 mr-2" />
                Показать официанту
              </Button>
              <Button
                style={{ backgroundColor: restaurant.primaryColor }}
                onClick={() => navigate(`/r/${id}/booking/rooms`)}
                className="h-12"
              >
                <Calendar className="w-5 h-5 mr-2" />
                Забронировать
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Waiter Modal */}
      <Dialog open={showWaiterModal} onOpenChange={setShowWaiterModal}>
        <DialogContent className="sm:max-w-[425px] max-h-[85vh] overflow-hidden flex flex-col">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Eye className="w-5 h-5" style={{ color: restaurant.primaryColor }} />
              Показать официанту
            </DialogTitle>
            <DialogDescription>
              Список выбранных позиций для показа официанту
            </DialogDescription>
          </DialogHeader>
          
          <div className="flex-1 overflow-y-auto space-y-3 py-4">
            {cartItems.map((item, index) => (
              <div 
                key={item.menuItemId}
                className="p-3 bg-gray-50 rounded-lg border"
              >
                <div className="flex justify-between items-start mb-2">
                  <div className="flex-1">
                    <h3 className="mb-1">{index + 1}. {item.name}</h3>
                    <p className="text-gray-600">
                      {item.price} ₸ × {item.quantity}
                    </p>
                  </div>
                  <p style={{ color: restaurant.primaryColor }}>
                    {item.price * item.quantity} ₸
                  </p>
                </div>
                
                {item.specialRequests && (
                  <div className="mt-2 pt-2 border-t border-gray-200">
                    <p className="text-gray-500 italic">
                      {item.specialRequests}
                    </p>
                  </div>
                )}
              </div>
            ))}
          </div>

          <div className="border-t pt-4 space-y-3">
            <div className="flex justify-between items-center">
              <div>
                <p className="text-gray-600">Всего позиций: {totalItems}</p>
                <p>Итого:</p>
              </div>
              <p className="text-2xl" style={{ color: restaurant.primaryColor }}>
                {totalCost} ₸
              </p>
            </div>

            <Button
              onClick={() => setShowWaiterModal(false)}
              style={{ backgroundColor: restaurant.primaryColor }}
              className="w-full h-12"
            >
              Закрыть
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Image Preview Modal */}
      <Dialog open={!!selectedImage} onOpenChange={(open) => !open && setSelectedImage(null)}>
        <DialogContent className="sm:max-w-[90vw] max-w-[95vw] p-0 bg-transparent border-0 shadow-none">
          <DialogHeader className="sr-only">
            <DialogTitle>{selectedImage?.alt || 'Просмотр изображения'}</DialogTitle>
            <DialogDescription>Изображение в полном размере</DialogDescription>
          </DialogHeader>
          <div className="relative w-full flex items-center justify-center">
            {selectedImage && (
              <img
                src={selectedImage.url}
                alt={selectedImage.alt}
                className="max-w-full max-h-[85vh] w-auto h-auto"
                onClick={(e) => e.stopPropagation()}
              />
            )}
            <Button
              variant="ghost"
              size="icon"
              className="absolute top-4 right-4 bg-black/50 text-white hover:bg-black/70 z-10"
              onClick={() => setSelectedImage(null)}
              aria-label="Закрыть"
            >
              <X className="w-5 h-5" />
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default MenuPage;

