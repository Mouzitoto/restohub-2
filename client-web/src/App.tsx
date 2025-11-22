import { Routes, Route } from 'react-router-dom'
import HomePage from './pages/HomePage'
import PartnerLandingPage from './pages/PartnerLandingPage'

function App() {
  console.log('App rendered, pathname:', window.location.pathname)
  
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/partner" element={<PartnerLandingPage />} />
      <Route path="/partner/landing" element={<PartnerLandingPage />} />
    </Routes>
  )
}

export default App

