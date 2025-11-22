import { Link } from 'react-router-dom'
import './HomePage.css'

export default function HomePage() {
  return (
    <div className="home-page">
      <header className="home-header">
        <div className="container">
          <Link to="/" className="logo">Resto-Hub</Link>
          <nav>
            <Link to="/partner" className="partner-link">Стать нашим партнером</Link>
          </nav>
        </div>
      </header>
      
      <main className="home-main">
        <div className="container">
          <h1>Добро пожаловать в Resto-Hub</h1>
          <p>Система управления ресторанами нового поколения</p>
          <div className="cta-section">
            <Link to="/partner" className="cta-button">Стать нашим партнером</Link>
          </div>
        </div>
      </main>
      
      <footer className="home-footer">
        <div className="container">
          <p>&copy; 2024 Resto-Hub. Все права защищены.</p>
        </div>
      </footer>
    </div>
  )
}

