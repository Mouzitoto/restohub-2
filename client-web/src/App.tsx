import { Routes, Route } from 'react-router-dom'
import HomePage from './pages/HomePage'
import PartnerLandingPage from './pages/PartnerLandingPage'
import RestaurantPage from './pages/RestaurantPage'
import MenuPage from './pages/MenuPage'
import RoomSelectionPage from './pages/RoomSelectionPage'
import TableSelectionPage from './pages/TableSelectionPage'
import BookingConfirmPage from './pages/BookingConfirmPage'
import { Toaster } from './components/ui/sonner'

function App() {
  console.log('App rendered, pathname:', window.location.pathname)

  return (
    <>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/partner" element={<PartnerLandingPage />} />
        <Route path="/partner/landing" element={<PartnerLandingPage />} />
        <Route path="/r/:id" element={<RestaurantPage />} />
        <Route path="/r/:id/menu" element={<MenuPage />} />
        <Route path="/r/:id/booking/rooms" element={<RoomSelectionPage />} />
        <Route path="/r/:id/booking/tables/:roomId" element={<TableSelectionPage />} />
        <Route path="/r/:id/booking/confirm/:bookingId" element={<BookingConfirmPage />} />
      </Routes>
      <Toaster />
    </>
  )
}

export default App
