import { HashRouter as Router, Routes, Route } from 'react-router-dom';
import { HomePage } from './components/HomePage';
import { RestaurantPage } from './components/RestaurantPage';
import { MenuPage } from './components/MenuPage';
import { RoomSelectionPage } from './components/RoomSelectionPage';
import { TableSelectionPage } from './components/TableSelectionPage';
import { BookingConfirmPage } from './components/BookingConfirmPage';
import { Toaster } from './components/ui/sonner';

export default function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/r/:id" element={<RestaurantPage />} />
          <Route path="/r/:id/menu" element={<MenuPage />} />
          <Route path="/r/:id/booking/rooms" element={<RoomSelectionPage />} />
          <Route path="/r/:id/booking/tables/:roomId" element={<TableSelectionPage />} />
          <Route path="/r/:id/booking/confirm/:bookingId" element={<BookingConfirmPage />} />
        </Routes>
        <Toaster />
      </div>
    </Router>
  );
}