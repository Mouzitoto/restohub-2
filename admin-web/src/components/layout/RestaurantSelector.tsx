import { useState } from 'react'
import { useApp } from '../../context/AppContext'

export default function RestaurantSelector() {
  const { restaurants, currentRestaurant, setCurrentRestaurant } = useApp()
  const [isOpen, setIsOpen] = useState(false)

  // Показываем только если ресторанов больше одного
  if (!restaurants || restaurants.length <= 1) {
    return currentRestaurant ? (
      <div style={{ padding: '0.5rem 1rem', backgroundColor: '#f5f5f5', borderRadius: '4px' }}>
        {currentRestaurant.name}
      </div>
    ) : null
  }

  return (
    <div style={{ position: 'relative' }}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        style={{
          padding: '0.5rem 1rem',
          backgroundColor: '#f5f5f5',
          border: '1px solid #ddd',
          borderRadius: '4px',
          cursor: 'pointer',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem',
        }}
      >
        {currentRestaurant?.name || 'Выберите ресторан'}
        <span>▼</span>
      </button>

      {isOpen && (
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
            }}
          >
            {restaurants.map((restaurant) => (
              <button
                key={restaurant.id}
                onClick={() => {
                  setCurrentRestaurant(restaurant)
                  setIsOpen(false)
                  // Перезагружаем страницу для обновления данных
                  window.location.reload()
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
                }}
              >
                {restaurant.name}
              </button>
            ))}
          </div>
        </>
      )}
    </div>
  )
}

