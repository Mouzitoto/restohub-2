import type { ReactNode } from 'react'
import Sidebar from './Sidebar'
import Header from './Header'
import SubscriptionWarningBanner from './SubscriptionWarningBanner'

interface AdminLayoutProps {
  children: ReactNode
}

export default function AdminLayout({ children }: AdminLayoutProps) {
  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <Header />
        <SubscriptionWarningBanner />
        <main style={{ flex: 1, padding: '2rem', backgroundColor: '#f5f5f5' }}>
          {children}
        </main>
      </div>
    </div>
  )
}

