import { useState } from 'react'
import { useApp } from '../../context/AppContext'

export default function RestaurantSelector() {
  const { restaurants, currentRestaurant, setCurrentRestaurant } = useApp()
  const [isOpen, setIsOpen] = useState(false)

  // Если ресторанов нет, не показываем селектор
  if (!restaurants || restaurants.length === 0) {
    return null
  }

  // Если ресторан один, показываем его название, но все равно как дропдаун (на случай если ресторанов станет больше)
  const hasMultipleRestaurants = restaurants.length > 1

  return (
    <div style={{ position: 'relative' }}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        style={{
          padding: '0.5rem 1rem',
          backgroundColor: '#f5f5f5',
          border: '1px solid #ddd',
          borderRadius: '4px',
          cursor: hasMultipleRestaurants ? 'pointer' : 'default',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem',
          minWidth: '200px',
        }}
        disabled={!hasMultipleRestaurants}
      >
        <span style={{ flex: 1, textAlign: 'left' }}>
          {currentRestaurant?.name || 'Выберите ресторан'}
        </span>
        {hasMultipleRestaurants && <span>▼</span>}
      </button>

      {isOpen && hasMultipleRestaurants && (
        <>
          <div
            style={{
              position: 'fixed',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              zIndex: 998,
            }}
            onClick={() => setIsOpen(false)}
          />
          <div
            style={{
              position: 'absolute',
              top: '100%',
              right: 0,
              marginTop: '0.5rem',
              backgroundColor: 'white',
              border: '1px solid #ddd',
              borderRadius: '4px',
              boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
              zIndex: 999,
              minWidth: '200px',
              maxHeight: '300px',
              overflowY: 'auto',
            }}
          >
            {restaurants.map((restaurant) => (
              <button
                key={restaurant.id}
                onClick={() => {
                  setCurrentRestaurant(restaurant)
                  setIsOpen(false)
                  // Данные обновятся автоматически через useEffect в страницах, которые зависят от currentRestaurant
                }}
                style={{
                  display: 'block',
                  width: '100%',
                  padding: '0.75rem 1rem',
                  textAlign: 'left',
                  border: 'none',
                  backgroundColor:
                    currentRestaurant?.id === restaurant.id ? '#e3f2fd' : 'white',
                  cursor: 'pointer',
                  transition: 'background-color 0.2s',
                }}
                onMouseEnter={(e) => {
                  if (currentRestaurant?.id !== restaurant.id) {
                    e.currentTarget.style.backgroundColor = '#f5f5f5'
                  }
                }}
                onMouseLeave={(e) => {
                  if (currentRestaurant?.id !== restaurant.id) {
                    e.currentTarget.style.backgroundColor = 'white'
                  }
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span>{restaurant.name}</span>
                  {!restaurant.isActive && (
                    <span
                      style={{
                        fontSize: '0.75rem',
                        color: '#dc3545',
                        marginLeft: '0.5rem',
                        fontWeight: 'normal',
                      }}
                    >
                      (деактивирован)
                    </span>
                  )}
                </div>
              </button>
            ))}
          </div>
        </>
      )}
    </div>
  )
}

