import type { ReactNode } from 'react'

interface StatCardProps {
  title: string
  value: string | number
  icon?: ReactNode
  trend?: {
    value: number
    isPositive: boolean
  }
  onClick?: () => void
}

export default function StatCard({ title, value, icon, trend, onClick }: StatCardProps) {
  return (
    <div
      onClick={onClick}
      style={{
        backgroundColor: 'white',
        padding: '1.5rem',
        borderRadius: '8px',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
        cursor: onClick ? 'pointer' : 'default',
        transition: 'transform 0.2s',
      }}
      onMouseEnter={(e) => {
        if (onClick) {
          e.currentTarget.style.transform = 'translateY(-2px)'
        }
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = 'translateY(0)'
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <div style={{ fontSize: '0.875rem', color: '#666', marginBottom: '0.5rem' }}>
            {title}
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 'bold' }}>{value}</div>
          {trend && (
            <div
              style={{
                fontSize: '0.875rem',
                color: trend.isPositive ? '#4caf50' : '#f44336',
                marginTop: '0.5rem',
              }}
            >
              {trend.isPositive ? '↑' : '↓'} {Math.abs(trend.value)}%
            </div>
          )}
        </div>
        {icon && <div style={{ fontSize: '2rem' }}>{icon}</div>}
      </div>
    </div>
  )
}

