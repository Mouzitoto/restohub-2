import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import RegistrationForm from '../components/RegistrationForm'
import './PartnerLandingPage.css'

export default function PartnerLandingPage() {
  const [showRegistrationForm, setShowRegistrationForm] = useState(false)

  useEffect(() => {
    console.log('PartnerLandingPage mounted')
    console.log('Current path:', window.location.pathname)
  }, [])

  const handleGoToLogin = () => {
    const partnerDomain = import.meta.env.VITE_PARTNER_DOMAIN || 'http://partner.restohub.local'
    window.location.href = `${partnerDomain}/login`
  }

  return (
    <div className="partner-landing">
      <header className="landing-header">
        <div className="container">
          <Link to="/" className="logo">Resto-Hub</Link>
          <nav className="header-nav">
            <button className="nav-button" onClick={handleGoToLogin}>
              Вход
            </button>
            <button
              className="nav-button primary"
              onClick={() => setShowRegistrationForm(true)}
            >
              Регистрация
            </button>
          </nav>
        </div>
      </header>

      {showRegistrationForm ? (
        <div className="registration-overlay">
          <div className="registration-container">
            <RegistrationForm
              onSuccess={() => setShowRegistrationForm(false)}
              onCancel={() => setShowRegistrationForm(false)}
            />
          </div>
        </div>
      ) : (
        <>
          <section className="hero-section">
            <div className="container">
              <h1>Присоединяйтесь к Resto-Hub</h1>
              <p className="hero-subtitle">
                Современная система управления рестораном в одном месте
              </p>
              <button
                className="cta-button-large"
                onClick={() => setShowRegistrationForm(true)}
              >
                Начать регистрацию
              </button>
            </div>
          </section>

          <section className="benefits-section">
            <div className="container">
              <h2>Преимущества Resto-Hub</h2>
              <div className="benefits-grid">
                <div className="benefit-card">
                  <div className="benefit-icon">📅</div>
                  <h3>Управление бронированиями</h3>
                  <p>
                    Все бронирования в одном месте. Удобный календарь и автоматические
                    напоминания для ваших клиентов.
                  </p>
                </div>
                <div className="benefit-card">
                  <div className="benefit-icon">💬</div>
                  <h3>Интеграция с WhatsApp</h3>
                  <p>
                    Автоматические подтверждения бронирований через WhatsApp. Экономьте
                    время на общении с клиентами.
                  </p>
                </div>
                <div className="benefit-card">
                  <div className="benefit-icon">🏢</div>
                  <h3>Персональная страница</h3>
                  <p>
                    Создайте уникальную страницу вашего ресторана с меню, фотографиями и
                    информацией о заведении.
                  </p>
                </div>
                <div className="benefit-card">
                  <div className="benefit-icon">📊</div>
                  <h3>Аналитика и статистика</h3>
                  <p>
                    Отслеживайте загрузку столов, популярные блюда и получайте детальную
                    аналитику по вашему бизнесу.
                  </p>
                </div>
                <div className="benefit-card">
                  <div className="benefit-icon">🍽️</div>
                  <h3>Управление меню</h3>
                  <p>
                    Легко управляйте меню, добавляйте акции и специальные предложения.
                    Все изменения мгновенно отображаются на сайте.
                  </p>
                </div>
                <div className="benefit-card">
                  <div className="benefit-icon">⚡</div>
                  <h3>Простота использования</h3>
                  <p>
                    Интуитивно понятный интерфейс. Начните работу за несколько минут без
                    специальной подготовки.
                  </p>
                </div>
              </div>
            </div>
          </section>

          <section className="how-it-works-section">
            <div className="container">
              <h2>Как это работает</h2>
              <div className="steps">
                <div className="step">
                  <div className="step-number">1</div>
                  <h3>Регистрация</h3>
                  <p>Зарегистрируйтесь и подтвердите ваш email</p>
                </div>
                <div className="step-arrow">→</div>
                <div className="step">
                  <div className="step-number">2</div>
                  <h3>Настройка</h3>
                  <p>Добавьте информацию о вашем ресторане</p>
                </div>
                <div className="step-arrow">→</div>
                <div className="step">
                  <div className="step-number">3</div>
                  <h3>Запуск</h3>
                  <p>Начните принимать бронирования от клиентов</p>
                </div>
              </div>
            </div>
          </section>

          <section className="cta-section">
            <div className="container">
              <h2>Готовы начать?</h2>
              <p>Присоединяйтесь к сотням ресторанов, которые уже используют Resto-Hub</p>
              <button
                className="cta-button-large"
                onClick={() => setShowRegistrationForm(true)}
              >
                Зарегистрироваться
              </button>
            </div>
          </section>

          <footer className="landing-footer">
            <div className="container">
              <div className="footer-content">
                <div className="footer-section">
                  <h4>Resto-Hub</h4>
                  <p>Система управления ресторанами</p>
                </div>
                <div className="footer-section">
                  <h4>Контакты</h4>
                  <p>Email: info@restohub.com</p>
                  <p>Телефон: +7 (999) 123-45-67</p>
                </div>
                <div className="footer-section">
                  <h4>Навигация</h4>
                  <Link to="/">Главная</Link>
                  <button className="link-button" onClick={handleGoToLogin}>
                    Вход для партнеров
                  </button>
                </div>
              </div>
              <div className="footer-bottom">
                <p>&copy; 2024 Resto-Hub. Все права защищены.</p>
              </div>
            </div>
          </footer>
        </>
      )}
    </div>
  )
}

