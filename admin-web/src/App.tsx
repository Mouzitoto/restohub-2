import { Routes, Route, Navigate } from 'react-router-dom'
import { AppProvider } from './context/AppContext'
import { ToastProvider } from './context/ToastContext'
import AuthGuard from './components/AuthGuard'
import AdminLayout from './components/layout/AdminLayout'
import LoginPage from './pages/LoginPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ResetPasswordPage from './pages/ResetPasswordPage'
import DashboardPage from './pages/DashboardPage'
import RestaurantEditPage from './pages/RestaurantEditPage'
import MenuCategoriesPage from './pages/MenuCategoriesPage'
import MenuItemsPage from './pages/MenuItemsPage'
import FloorsPage from './pages/FloorsPage'
import RoomsPage from './pages/RoomsPage'
import TablesPage from './pages/TablesPage'
import BookingsPage from './pages/BookingsPage'
import SubscriptionPage from './pages/SubscriptionPage'
import PromotionsPage from './pages/PromotionsPage'
import AnalyticsPage from './pages/AnalyticsPage'
import ClientsPage from './pages/ClientsPage'
import AdminRestaurantsPage from './pages/admin/AdminRestaurantsPage'
import AdminDashboardPage from './pages/admin/AdminDashboardPage'
import AdminSettingsPage from './pages/admin/AdminSettingsPage'
import AdminUsersPage from './pages/admin/AdminUsersPage'

function App() {
  return (
    <ToastProvider>
      <AppProvider>
        <Routes>
        {/* Публичные роуты */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />

        {/* Защищенные роуты */}
        <Route
          path="/*"
          element={
            <AuthGuard>
              <AdminLayout>
                <Routes>
                  <Route path="/dashboard" element={<DashboardPage />} />
                  <Route path="/restaurant" element={<RestaurantEditPage />} />
                  <Route path="/menu/categories" element={<MenuCategoriesPage />} />
                  <Route path="/menu/items" element={<MenuItemsPage />} />
                  <Route path="/floors" element={<FloorsPage />} />
                  <Route path="/rooms" element={<RoomsPage />} />
                  <Route path="/tables" element={<TablesPage />} />
                  <Route path="/bookings" element={<BookingsPage />} />
                  <Route path="/subscription" element={<SubscriptionPage />} />
                  <Route path="/promotions" element={<PromotionsPage />} />
                  <Route path="/analytics" element={<AnalyticsPage />} />
                  <Route path="/clients" element={<ClientsPage />} />

                  {/* Административные роуты */}
                  <Route path="/admin/restaurants" element={<AdminRestaurantsPage />} />
                  <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
                  <Route path="/admin/settings" element={<AdminSettingsPage />} />
                  <Route path="/admin/users" element={<AdminUsersPage />} />

                  <Route path="/" element={<Navigate to="/dashboard" replace />} />
                </Routes>
              </AdminLayout>
            </AuthGuard>
          }
        />
      </Routes>
      </AppProvider>
    </ToastProvider>
  )
}

export default App

