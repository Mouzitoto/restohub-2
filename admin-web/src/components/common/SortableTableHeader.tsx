export type SortDirection = 'asc' | 'desc' | null

export interface SortableTableHeaderProps {
  children: React.ReactNode
  sortKey?: string
  currentSortKey?: string | null
  currentSortDirection?: SortDirection
  onSort?: (sortKey: string, direction: SortDirection) => void
  style?: React.CSSProperties
}

export default function SortableTableHeader({
  children,
  sortKey,
  currentSortKey,
  currentSortDirection,
  onSort,
  style,
}: SortableTableHeaderProps) {
  const isActive = sortKey && currentSortKey === sortKey
  const canSort = sortKey && onSort

  const handleClick = () => {
    if (!canSort) return

    let newDirection: SortDirection = 'asc'
    if (isActive && currentSortDirection === 'asc') {
      newDirection = 'desc'
    } else if (isActive && currentSortDirection === 'desc') {
      newDirection = null
    }

    onSort(sortKey, newDirection)
  }

  const getSortIcon = () => {
    if (!isActive || !currentSortDirection) {
      return <span style={{ opacity: 0.3, marginLeft: '0.25rem' }}>⇅</span>
    }
    if (currentSortDirection === 'asc') {
      return <span style={{ marginLeft: '0.25rem' }}>↑</span>
    }
    return <span style={{ marginLeft: '0.25rem' }}>↓</span>
  }

  return (
    <th
      style={{
        ...style,
        cursor: canSort ? 'pointer' : 'default',
        userSelect: 'none',
        position: 'relative',
      }}
      onClick={handleClick}
    >
      <div style={{ display: 'flex', alignItems: 'center' }}>
        {children}
        {canSort && getSortIcon()}
      </div>
    </th>
  )
}

