import { useState, useRef, useEffect, useCallback } from 'react'
import { apiClient } from '../services/apiClient'
import { useToast } from '../context/ToastContext'
import type { RoomLayout, Table, UpdateTablePositionRequest } from '../types'
import Modal from './common/Modal'

// Получаем базовый URL API для формирования полного URL изображения
const getImageUrl = (imageUrl: string | undefined | null): string | undefined => {
  if (!imageUrl) return undefined
  // Если URL уже полный (начинается с http:// или https://), возвращаем как есть
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl
  }
  // Иначе добавляем базовый URL API
  const baseURL = apiClient.instance.defaults.baseURL || 'http://localhost:8082'
  return `${baseURL}${imageUrl.startsWith('/') ? imageUrl : '/' + imageUrl}`
}

interface RoomLayoutEditorProps {
  restaurantId: number
  roomId: number
  onClose: () => void
  onTableClick?: (table: Table) => void
}

interface TableRectangle {
  table: Table
  x1: number
  y1: number
  x2: number
  y2: number
}

export default function RoomLayoutEditor({ 
  restaurantId, 
  roomId, 
  onClose,
  onTableClick 
}: RoomLayoutEditorProps) {
  const [roomLayout, setRoomLayout] = useState<RoomLayout | null>(null)
  const [visualTables, setVisualTables] = useState<Table[]>([]) // Локальное состояние для визуализации
  const [isLoading, setIsLoading] = useState(true)
  const [imageError, setImageError] = useState(false)
  const [imageBlobUrl, setImageBlobUrl] = useState<string | null>(null)
  const [selectedTable, setSelectedTable] = useState<Table | null>(null)
  const [isEditingTable, setIsEditingTable] = useState(false)
  const [editingTablePosition, setEditingTablePosition] = useState<{ x: number; y: number } | null>(null)
  const [showTableDropdown, setShowTableDropdown] = useState(false)
  const [dropdownPosition, setDropdownPosition] = useState({ x: 0, y: 0 })
  const [editFormData, setEditFormData] = useState({
    width: 0,
    height: 0,
    centerX: 0,
    centerY: 0,
  })
  const [draggingTable, setDraggingTable] = useState<number | null>(null)
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 })
  const [hasMoved, setHasMoved] = useState(false)
  const hasMovedRef = useRef(false)
  const wasDraggingRef = useRef(false) // Флаг для отслеживания, что было перетаскивание
  const dragEndTimeRef = useRef<number>(0) // Время окончания перетаскивания
  const [resizingTable, setResizingTable] = useState<{ id: number; corner: 'nw' | 'ne' | 'sw' | 'se' } | null>(null)
  
  const imageRef = useRef<HTMLImageElement>(null)
  const containerRef = useRef<HTMLDivElement>(null)
  const toast = useToast()

  useEffect(() => {
    loadRoomLayout()
  }, [restaurantId, roomId])

  // Обновляем позицию блока редактирования при изменении выбранного стола или размеров изображения
  useEffect(() => {
    if (isEditingTable && selectedTable && selectedTable.positionX1 != null && imageRef.current) {
      // Используем визуальное состояние, если оно есть
      const tableToUse = visualTables.find(t => t.id === selectedTable.id) || selectedTable
      const rect = imageRef.current.getBoundingClientRect()
      const minX = Math.min(tableToUse.positionX1!, tableToUse.positionX2!)
      const maxX = Math.max(tableToUse.positionX1!, tableToUse.positionX2!)
      const minY = Math.min(tableToUse.positionY1!, tableToUse.positionY2!)
      const maxY = Math.max(tableToUse.positionY1!, tableToUse.positionY2!)
      
      const leftPx = (minX / 100) * rect.width
      const topPx = (maxY / 100) * rect.height + 10
      
      const width = maxX - minX
      const height = maxY - minY
      const centerX = (minX + maxX) / 2
      const centerY = (minY + maxY) / 2
      
      setEditFormData({ width, height, centerX, centerY })
      setEditingTablePosition({ x: leftPx, y: topPx })
    }
  }, [isEditingTable, selectedTable, imageBlobUrl, visualTables])

  const loadRoomLayout = async () => {
    setIsLoading(true)
    setImageError(false)
    // Очищаем предыдущий blob URL
    if (imageBlobUrl) {
      URL.revokeObjectURL(imageBlobUrl)
      setImageBlobUrl(null)
    }
    try {
      const response = await apiClient.instance.get<RoomLayout>(
        `/admin-api/r/${restaurantId}/room/${roomId}/layout`
      )
      setRoomLayout(response.data)
      // Инициализируем визуальное состояние
      setVisualTables([...response.data.tables])
      console.log('Room layout loaded:', response.data)
      
      // Загружаем изображение через API с авторизацией
      if (response.data.imageUrl) {
        await loadImageWithAuth(response.data.imageUrl)
      }
    } catch (error: any) {
      toast.error('Не удалось загрузить схему зала')
      console.error('Error loading room layout:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const loadImageWithAuth = async (imageUrl: string) => {
    try {
      if (!imageUrl) {
        setImageError(true)
        return
      }
      
      // Используем относительный путь для API клиента
      // imageUrl уже содержит путь вида "/admin-api/image?id=10&isPreview=false"
      console.log('Loading image with auth:', imageUrl)
      
      // Загружаем изображение через API клиент с авторизацией
      const response = await apiClient.instance.get(imageUrl, {
        responseType: 'blob'
      })
      
      // Создаем blob URL для отображения
      const blob = new Blob([response.data])
      const blobUrl = URL.createObjectURL(blob)
      setImageBlobUrl(blobUrl)
      setImageError(false)
      console.log('Image loaded successfully')
    } catch (error: any) {
      console.error('Error loading image:', error)
      setImageError(true)
    }
  }

  const handleImageError = () => {
    setImageError(true)
    console.error('Failed to load image:', roomLayout?.imageUrl)
  }

  // Очищаем blob URL при размонтировании компонента
  useEffect(() => {
    return () => {
      if (imageBlobUrl) {
        URL.revokeObjectURL(imageBlobUrl)
      }
    }
  }, [imageBlobUrl])

  const handleImageClick = (e: React.MouseEvent<HTMLDivElement>) => {
    // Не открываем dropdown если идет перетаскивание/изменение размера
    if (!imageRef.current || draggingTable || resizingTable) {
      return
    }
    
    // Проверяем, не было ли только что перетаскивания (в течение 300ms)
    const timeSinceDragEnd = Date.now() - dragEndTimeRef.current
    if (wasDraggingRef.current && timeSinceDragEnd < 300) {
      // Сбрасываем флаг и игнорируем клик
      wasDraggingRef.current = false
      return
    }
    
    // Проверяем, что клик был именно на контейнере, а не на прямоугольнике стола
    const target = e.target as HTMLElement
    if (target.closest('[data-table-rectangle]')) {
      // Клик был на прямоугольнике стола, не открываем dropdown
      return
    }
    
    setDropdownPosition({ x: e.clientX, y: e.clientY })
    setShowTableDropdown(true)
  }

  // Функция для обновления визуального отображения стола
  const updateVisualTable = (formData: { width: number; height: number; centerX: number; centerY: number }) => {
    if (!selectedTable) return
    
    // Преобразуем ширину/высоту/центр в координаты двух точек
    const x1 = Math.max(0, Math.min(100, formData.centerX - formData.width / 2))
    const y1 = Math.max(0, Math.min(100, formData.centerY - formData.height / 2))
    const x2 = Math.max(0, Math.min(100, formData.centerX + formData.width / 2))
    const y2 = Math.max(0, Math.min(100, formData.centerY + formData.height / 2))
    
    // Обновляем визуальное состояние
    setVisualTables(prev => {
      const updated = prev.map(table => 
        table.id === selectedTable.id 
          ? { ...table, positionX1: x1, positionY1: y1, positionX2: x2, positionY2: y2 }
          : table
      )
      // Обновляем selectedTable, чтобы он использовал актуальные данные
      const updatedTable = updated.find(t => t.id === selectedTable.id)
      if (updatedTable) {
        setSelectedTable(updatedTable)
      }
      return updated
    })
    
    // Обновляем позицию блока редактирования
    if (imageRef.current) {
      const rect = imageRef.current.getBoundingClientRect()
      const minX = Math.min(x1, x2)
      const maxY = Math.max(y1, y2)
      const leftPx = (minX / 100) * rect.width
      const topPx = (maxY / 100) * rect.height + 10
      setEditingTablePosition({ x: leftPx, y: topPx })
    }
  }

  const handleTableClick = (e: React.MouseEvent, table: Table) => {
    e.stopPropagation()
    if (draggingTable || resizingTable) return
    
    // Используем визуальное состояние, если оно есть
    const tableToUse = visualTables.find(t => t.id === table.id) || table
    
    if (!imageRef.current || tableToUse.positionX1 == null || tableToUse.positionY1 == null ||
        tableToUse.positionX2 == null || tableToUse.positionY2 == null) return
    
    // Вычисляем позицию для блока редактирования (под прямоугольником)
    const rect = imageRef.current.getBoundingClientRect()
    const minX = Math.min(tableToUse.positionX1, tableToUse.positionX2)
    const maxX = Math.max(tableToUse.positionX1, tableToUse.positionX2)
    const minY = Math.min(tableToUse.positionY1, tableToUse.positionY2)
    const maxY = Math.max(tableToUse.positionY1, tableToUse.positionY2)
    
    // Позиция в пикселях относительно изображения
    const leftPx = (minX / 100) * rect.width
    const topPx = (maxY / 100) * rect.height + 10 // 10px отступ снизу
    
    // Вычисляем ширину, высоту и центр
    const width = maxX - minX
    const height = maxY - minY
    const centerX = (minX + maxX) / 2
    const centerY = (minY + maxY) / 2
    
    setEditFormData({ width, height, centerX, centerY })
    setEditingTablePosition({ x: leftPx, y: topPx })
    setSelectedTable(tableToUse)
    setIsEditingTable(true)
    if (onTableClick) {
      onTableClick(tableToUse)
    }
  }

  const handleTableSelect = async (table: Table) => {
    setShowTableDropdown(false)
    if (!imageRef.current) return
    
    const rect = imageRef.current.getBoundingClientRect()
    const x = ((dropdownPosition.x - rect.left) / rect.width) * 100
    const y = ((dropdownPosition.y - rect.top) / rect.height) * 100
    
    // Если у стола уже есть координаты, показываем предупреждение
    if (table.positionX1 != null && table.positionY1 != null) {
      if (!confirm('У этого стола уже есть расположение. Переместить его?')) {
        return
      }
    }
    
    // Размещаем стол по клику (создаем небольшой прямоугольник)
    const size = 5 // 5% от размера изображения
    await updateTablePosition(table.id, x, y, x + size, y + size)
  }

  const updateTablePosition = async (
    tableId: number, 
    x1: number, 
    y1: number, 
    x2: number, 
    y2: number
  ) => {
    try {
      const request: UpdateTablePositionRequest = {
        tableId,
        positionX1: Math.max(0, Math.min(100, x1)),
        positionY1: Math.max(0, Math.min(100, y1)),
        positionX2: Math.max(0, Math.min(100, x2)),
        positionY2: Math.max(0, Math.min(100, y2)),
      }
      
      await apiClient.instance.put(
        `/admin-api/r/${restaurantId}/room/${roomId}/tables/positions`,
        [request]
      )
      
      toast.success('Позиция стола обновлена')
      await loadRoomLayout() // Это обновит и visualTables через setVisualTables
    } catch (error: any) {
      if (error.response?.data?.exceptionName === 'TABLE_POSITIONS_INTERSECT') {
        toast.error('Позиции столов пересекаются')
        // Откатываем визуальные изменения при ошибке
        if (roomLayout) {
          setVisualTables([...roomLayout.tables])
        }
      } else {
        toast.error('Не удалось обновить позицию стола')
        // Откатываем визуальные изменения при ошибке
        if (roomLayout) {
          setVisualTables([...roomLayout.tables])
        }
      }
    }
  }

  const handleDeletePosition = async (tableId: number) => {
    try {
      await apiClient.instance.delete(
        `/admin-api/r/${restaurantId}/table/${tableId}/position`
      )
      toast.success('Расположение стола удалено')
      setIsEditingTable(false)
      setSelectedTable(null)
      await loadRoomLayout()
    } catch (error: any) {
      toast.error('Не удалось удалить расположение')
    }
  }

  const handleDrag = useCallback((e: MouseEvent) => {
    if (!draggingTable || !imageRef.current) return
    
    // Отмечаем, что было движение
    if (!hasMovedRef.current) {
      hasMovedRef.current = true
      wasDraggingRef.current = true
      setHasMoved(true)
    }
    
    const rect = imageRef.current.getBoundingClientRect()
    const tablesToUse = visualTables.length > 0 ? visualTables : (roomLayout?.tables || [])
    const table = tablesToUse.find(t => t.id === draggingTable)
    
    if (!table || table.positionX1 == null || table.positionY1 == null || 
        table.positionX2 == null || table.positionY2 == null) return
    
    // Вычисляем новую позицию относительно изображения
    const mouseX = e.clientX - rect.left
    const mouseY = e.clientY - rect.top
    
    // Новая позиция левого верхнего угла с учетом смещения
    const newX = ((mouseX - dragOffset.x) / rect.width) * 100
    const newY = ((mouseY - dragOffset.y) / rect.height) * 100
    
    // Вычисляем размеры стола
    const width = Math.abs(table.positionX2 - table.positionX1)
    const height = Math.abs(table.positionY2 - table.positionY1)
    
    // Ограничиваем позицию границами изображения
    const clampedX = Math.max(0, Math.min(100 - width, newX))
    const clampedY = Math.max(0, Math.min(100 - height, newY))
    
    // Обновляем визуальное состояние
    setVisualTables(prev => prev.map(t => 
      t.id === draggingTable
        ? { ...t, 
            positionX1: clampedX,
            positionY1: clampedY,
            positionX2: clampedX + width,
            positionY2: clampedY + height
          }
        : t
    ))
  }, [draggingTable, dragOffset, visualTables, roomLayout])

  const handleDragEnd = useCallback(async () => {
    const currentDraggingTable = draggingTable
    const currentHasMoved = hasMoved
    
    if (!currentDraggingTable || !imageRef.current) {
      setDraggingTable(null)
      setHasMoved(false)
      hasMovedRef.current = false
      return
    }
    
    setDraggingTable(null)
    setHasMoved(false)
    hasMovedRef.current = false
    
    // Если было движение - сохраняем позицию, иначе это был клик
    if (currentHasMoved) {
      const tablesToUse = visualTables.length > 0 ? visualTables : (roomLayout?.tables || [])
      const table = tablesToUse.find(t => t.id === currentDraggingTable)
      
      if (table && table.positionX1 != null && table.positionY1 != null && 
          table.positionX2 != null && table.positionY2 != null) {
        // Сохраняем на сервер
        await updateTablePosition(
          table.id,
          table.positionX1,
          table.positionY1,
          table.positionX2,
          table.positionY2
        )
      }
      
      // Запоминаем время окончания перетаскивания
      dragEndTimeRef.current = Date.now()
      // Флаг останется true, чтобы предотвратить открытие dropdown при следующем onClick
    } else {
      // Если не было движения, сразу сбрасываем флаг
      wasDraggingRef.current = false
      dragEndTimeRef.current = 0
    }
  }, [draggingTable, hasMoved, visualTables, roomLayout])

  const handleDragStart = (e: React.MouseEvent, table: Table) => {
    e.stopPropagation()
    if (!imageRef.current || table.positionX1 == null || table.positionY1 == null || 
        table.positionX2 == null || table.positionY2 == null) return
    
    // Начинаем перетаскивание сразу
    setDraggingTable(table.id)
    setHasMoved(false)
    hasMovedRef.current = false
    wasDraggingRef.current = false
    
    const rect = imageRef.current.getBoundingClientRect()
    const minX = Math.min(table.positionX1, table.positionX2)
    const minY = Math.min(table.positionY1, table.positionY2)
    
    // Вычисляем смещение от точки клика до левого верхнего угла прямоугольника
    const tableLeftPx = (minX / 100) * rect.width
    const tableTopPx = (minY / 100) * rect.height
    
    setDragOffset({
      x: e.clientX - rect.left - tableLeftPx,
      y: e.clientY - rect.top - tableTopPx
    })
    
    // Предотвращаем выделение текста при перетаскивании
    e.preventDefault()
  }

  // Обработчики для drag and drop на уровне документа
  useEffect(() => {
    if (draggingTable) {
      document.addEventListener('mousemove', handleDrag)
      document.addEventListener('mouseup', handleDragEnd)
      
      return () => {
        document.removeEventListener('mousemove', handleDrag)
        document.removeEventListener('mouseup', handleDragEnd)
      }
    }
  }, [draggingTable, handleDrag, handleDragEnd])

  const handleResizeStart = (e: React.MouseEvent, table: Table, corner: 'nw' | 'ne' | 'sw' | 'se') => {
    e.stopPropagation()
    setResizingTable({ id: table.id, corner })
  }

  const getTableRectangles = (): TableRectangle[] => {
    // Используем visualTables для визуализации (включая несохраненные изменения)
    const tablesToUse = visualTables.length > 0 ? visualTables : (roomLayout?.tables || [])
    
    return tablesToUse
      .filter(table => 
        table.positionX1 != null && 
        table.positionY1 != null && 
        table.positionX2 != null && 
        table.positionY2 != null
      )
      .map(table => ({
        table,
        x1: table.positionX1!,
        y1: table.positionY1!,
        x2: table.positionX2!,
        y2: table.positionY2!,
      }))
  }

  const checkIntersections = (rectangles: TableRectangle[]): boolean => {
    for (let i = 0; i < rectangles.length; i++) {
      for (let j = i + 1; j < rectangles.length; j++) {
        const r1 = rectangles[i]
        const r2 = rectangles[j]
        
        const minX1 = Math.min(r1.x1, r1.x2)
        const maxX1 = Math.max(r1.x1, r1.x2)
        const minY1 = Math.min(r1.y1, r1.y2)
        const maxY1 = Math.max(r1.y1, r1.y2)
        
        const minX2 = Math.min(r2.x1, r2.x2)
        const maxX2 = Math.max(r2.x1, r2.x2)
        const minY2 = Math.min(r2.y1, r2.y2)
        const maxY2 = Math.max(r2.y1, r2.y2)
        
        const intersects = !(maxX1 < minX2 || maxX2 < minX1 || maxY1 < minY2 || maxY2 < minY1)
        if (intersects) {
          return true
        }
      }
    }
    return false
  }

  if (isLoading) {
    return (
      <Modal isOpen={true} onClose={onClose} title="Загрузка схемы зала">
        <div>Загрузка...</div>
      </Modal>
    )
  }

  if (!roomLayout || !roomLayout.imageUrl) {
    return (
      <Modal isOpen={true} onClose={onClose} title="Схема зала">
        <div>У зала нет схемы. Загрузите изображение схемы в настройках зала.</div>
      </Modal>
    )
  }

  const rectangles = getTableRectangles()
  const hasIntersections = checkIntersections(rectangles)

  return (
    <Modal isOpen={true} onClose={onClose} title="Редактирование схемы зала" size="large">
      <div style={{ position: 'relative' }}>
        {hasIntersections && (
          <div style={{ 
            padding: '10px', 
            backgroundColor: '#ffebee', 
            color: '#c62828', 
            marginBottom: '10px',
            borderRadius: '4px'
          }}>
            Внимание: Обнаружены пересечения прямоугольников столов!
          </div>
        )}
        
        <div
          ref={containerRef}
          style={{ position: 'relative', display: 'inline-block', width: '100%' }}
          onClick={handleImageClick}
        >
          {imageError ? (
            <div style={{ 
              padding: '40px', 
              textAlign: 'center', 
              backgroundColor: '#f5f5f5',
              color: '#666',
              border: '2px dashed #ccc',
              borderRadius: '4px'
            }}>
              <div style={{ fontSize: '48px', marginBottom: '10px' }}>🖼️</div>
              <div>Не удалось загрузить изображение схемы зала</div>
              <div style={{ fontSize: '12px', marginTop: '10px', color: '#999' }}>
                URL: {getImageUrl(roomLayout.imageUrl)}
              </div>
            </div>
          ) : imageBlobUrl ? (
            <img
              ref={imageRef}
              src={imageBlobUrl}
              alt="Схема зала"
              style={{ width: '100%', height: 'auto', display: 'block' }}
              draggable={false}
              onError={handleImageError}
              onLoad={() => setImageError(false)}
            />
          ) : (
            <div style={{ 
              padding: '40px', 
              textAlign: 'center', 
              backgroundColor: '#f5f5f5',
              color: '#666'
            }}>
              Загрузка изображения...
            </div>
          )}
          
          {rectangles.map((rect) => {
            const minX = Math.min(rect.x1, rect.x2)
            const maxX = Math.max(rect.x1, rect.x2)
            const minY = Math.min(rect.y1, rect.y2)
            const maxY = Math.max(rect.y1, rect.y2)
            const width = maxX - minX
            const height = maxY - minY
            const isSelected = selectedTable?.id === rect.table.id
            const isDragging = draggingTable === rect.table.id
            
            return (
              <div
                key={rect.table.id}
                data-table-rectangle
                style={{
                  position: 'absolute',
                  left: `${minX}%`,
                  top: `${minY}%`,
                  width: `${width}%`,
                  height: `${height}%`,
                  border: `2px solid ${isSelected ? '#1976d2' : isDragging ? '#ff9800' : '#f44336'}`,
                  backgroundColor: isSelected ? 'rgba(25, 118, 210, 0.2)' : 'rgba(244, 67, 54, 0.3)',
                  cursor: 'move',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '14px',
                  fontWeight: 'bold',
                  color: '#fff',
                  textShadow: '1px 1px 2px rgba(0,0,0,0.7)',
                  pointerEvents: 'auto',
                }}
                onMouseDown={(e) => {
                  // Предотвращаем открытие панели редактирования при начале перетаскивания
                  e.stopPropagation()
                  handleDragStart(e, rect.table)
                }}
                onClick={(e) => {
                  // Открываем панель редактирования только если не было перетаскивания
                  if (!wasDraggingRef.current && !draggingTable && !resizingTable) {
                    e.stopPropagation()
                    handleTableClick(e, rect.table)
                  } else {
                    // Если было перетаскивание, сбрасываем флаг
                    wasDraggingRef.current = false
                  }
                }}
                title={`Стол ${rect.table.tableNumber} (${rect.table.capacity} мест)`}
              >
                {rect.table.tableNumber}
                
                {/* Resize handles */}
                {isSelected && (
                  <>
                    <div
                      style={{
                        position: 'absolute',
                        top: '-4px',
                        left: '-4px',
                        width: '8px',
                        height: '8px',
                        backgroundColor: '#1976d2',
                        border: '1px solid #fff',
                        cursor: 'nw-resize',
                      }}
                      onMouseDown={(e) => {
                        e.stopPropagation()
                        handleResizeStart(e, rect.table, 'nw')
                      }}
                    />
                    <div
                      style={{
                        position: 'absolute',
                        top: '-4px',
                        right: '-4px',
                        width: '8px',
                        height: '8px',
                        backgroundColor: '#1976d2',
                        border: '1px solid #fff',
                        cursor: 'ne-resize',
                      }}
                      onMouseDown={(e) => {
                        e.stopPropagation()
                        handleResizeStart(e, rect.table, 'ne')
                      }}
                    />
                    <div
                      style={{
                        position: 'absolute',
                        bottom: '-4px',
                        left: '-4px',
                        width: '8px',
                        height: '8px',
                        backgroundColor: '#1976d2',
                        border: '1px solid #fff',
                        cursor: 'sw-resize',
                      }}
                      onMouseDown={(e) => {
                        e.stopPropagation()
                        handleResizeStart(e, rect.table, 'sw')
                      }}
                    />
                    <div
                      style={{
                        position: 'absolute',
                        bottom: '-4px',
                        right: '-4px',
                        width: '8px',
                        height: '8px',
                        backgroundColor: '#1976d2',
                        border: '1px solid #fff',
                        cursor: 'se-resize',
                      }}
                      onMouseDown={(e) => {
                        e.stopPropagation()
                        handleResizeStart(e, rect.table, 'se')
                      }}
                    />
                  </>
                )}
              </div>
            )
          })}
          
          {/* Блок редактирования позиционируется внутри контейнера с изображением */}
          {isEditingTable && selectedTable && selectedTable.positionX1 != null && editingTablePosition && imageRef.current && (
            <div 
            data-edit-panel
            style={{ 
              position: 'absolute',
              left: `${editingTablePosition.x}px`,
              top: `${editingTablePosition.y}px`,
              padding: '20px', 
              backgroundColor: '#fff',
              border: '2px solid #1976d2',
              borderRadius: '8px',
              boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
              zIndex: 1000,
              minWidth: '300px',
              maxWidth: '400px',
            }}
            onClick={(e) => e.stopPropagation()}
            >
              <h3 style={{ marginTop: 0, marginBottom: '15px', color: '#1976d2' }}>
                Редактирование стола {selectedTable.tableNumber}
              </h3>
              <div style={{ marginBottom: '15px' }}>
                <p style={{ margin: '5px 0' }}><strong>Вместимость:</strong> {selectedTable.capacity} мест</p>
                {selectedTable.description && (
                  <p style={{ margin: '5px 0' }}><strong>Описание:</strong> {selectedTable.description}</p>
                )}
              </div>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '15px', marginBottom: '15px' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', fontSize: '14px' }}>
                    Ширина (%):
                  </label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.1"
                  value={editFormData.width.toFixed(1)}
                  onChange={(e) => {
                    const newWidth = parseFloat(e.target.value) || 0
                    const clampedWidth = Math.max(0, Math.min(100, newWidth))
                    const newFormData = { ...editFormData, width: clampedWidth }
                    setEditFormData(newFormData)
                    
                    // Обновляем визуальное отображение сразу
                    if (selectedTable) {
                      updateVisualTable(newFormData)
                    }
                  }}
                    style={{
                      width: '100%',
                      padding: '8px',
                      border: '1px solid #ccc',
                      borderRadius: '4px',
                      fontSize: '14px',
                    }}
                  />
                </div>
                
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', fontSize: '14px' }}>
                    Высота (%):
                  </label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.1"
                  value={editFormData.height.toFixed(1)}
                  onChange={(e) => {
                    const newHeight = parseFloat(e.target.value) || 0
                    const clampedHeight = Math.max(0, Math.min(100, newHeight))
                    const newFormData = { ...editFormData, height: clampedHeight }
                    setEditFormData(newFormData)
                    
                    // Обновляем визуальное отображение сразу
                    if (selectedTable) {
                      updateVisualTable(newFormData)
                    }
                  }}
                    style={{
                      width: '100%',
                      padding: '8px',
                      border: '1px solid #ccc',
                      borderRadius: '4px',
                      fontSize: '14px',
                    }}
                  />
                </div>
                
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', fontSize: '14px' }}>
                    Центр X (%):
                  </label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.1"
                  value={editFormData.centerX.toFixed(1)}
                  onChange={(e) => {
                    const newCenterX = parseFloat(e.target.value) || 0
                    const clampedCenterX = Math.max(0, Math.min(100, newCenterX))
                    const newFormData = { ...editFormData, centerX: clampedCenterX }
                    setEditFormData(newFormData)
                    
                    // Обновляем визуальное отображение сразу
                    if (selectedTable) {
                      updateVisualTable(newFormData)
                    }
                  }}
                    style={{
                      width: '100%',
                      padding: '8px',
                      border: '1px solid #ccc',
                      borderRadius: '4px',
                      fontSize: '14px',
                    }}
                  />
                </div>
                
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', fontSize: '14px' }}>
                    Центр Y (%):
                  </label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.1"
                  value={editFormData.centerY.toFixed(1)}
                  onChange={(e) => {
                    const newCenterY = parseFloat(e.target.value) || 0
                    const clampedCenterY = Math.max(0, Math.min(100, newCenterY))
                    const newFormData = { ...editFormData, centerY: clampedCenterY }
                    setEditFormData(newFormData)
                    
                    // Обновляем визуальное отображение сразу
                    if (selectedTable) {
                      updateVisualTable(newFormData)
                    }
                  }}
                    style={{
                      width: '100%',
                      padding: '8px',
                      border: '1px solid #ccc',
                      borderRadius: '4px',
                      fontSize: '14px',
                    }}
                  />
                </div>
              </div>
              
              <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                <button
                onClick={async () => {
                  // Преобразуем ширину/высоту/центр обратно в координаты двух точек
                  const x1 = Math.max(0, Math.min(100, editFormData.centerX - editFormData.width / 2))
                  const y1 = Math.max(0, Math.min(100, editFormData.centerY - editFormData.height / 2))
                  const x2 = Math.max(0, Math.min(100, editFormData.centerX + editFormData.width / 2))
                  const y2 = Math.max(0, Math.min(100, editFormData.centerY + editFormData.height / 2))
                  
                  await updateTablePosition(selectedTable.id, x1, y1, x2, y2)
                  // После сохранения обновляем данные с сервера, что также обновит visualTables
                  setIsEditingTable(false)
                  setSelectedTable(null)
                  setEditingTablePosition(null)
                }}
                  style={{
                    padding: '10px 20px',
                    backgroundColor: '#1976d2',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontWeight: 'bold',
                  }}
                >
                  Сохранить
                </button>
                <button
                  onClick={() => handleDeletePosition(selectedTable.id)}
                  style={{
                    padding: '10px 20px',
                    backgroundColor: '#f44336',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontWeight: 'bold',
                  }}
                >
                  Удалить расположение
                </button>
              <button
                onClick={() => {
                  // При закрытии без сохранения возвращаем визуальное состояние к исходному
                  if (roomLayout) {
                    setVisualTables([...roomLayout.tables])
                  }
                  setIsEditingTable(false)
                  setSelectedTable(null)
                  setEditingTablePosition(null)
                }}
                  style={{
                    padding: '10px 20px',
                    backgroundColor: '#757575',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                  }}
                >
                  Закрыть
                </button>
              </div>
            </div>
          )}
        </div>
        
        {showTableDropdown && roomLayout && (
          <div
            style={{
              position: 'fixed',
              left: `${dropdownPosition.x}px`,
              top: `${dropdownPosition.y}px`,
              backgroundColor: 'white',
              border: '1px solid #ccc',
              borderRadius: '4px',
              boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
              zIndex: 1000,
              minWidth: '200px',
            }}
            onClick={(e) => e.stopPropagation()}
          >
            {roomLayout.tables.map((table) => (
              <div
                key={table.id}
                style={{
                  padding: '8px 12px',
                  cursor: 'pointer',
                  borderBottom: '1px solid #eee',
                }}
                onClick={() => handleTableSelect(table)}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = '#f5f5f5'
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = 'white'
                }}
              >
                Стол {table.tableNumber} ({table.capacity} мест)
                {table.positionX1 != null && ' ✓'}
              </div>
            ))}
            <div
              style={{
                padding: '8px 12px',
                cursor: 'pointer',
                borderTop: '1px solid #eee',
                backgroundColor: '#f5f5f5',
                fontWeight: 'bold',
              }}
              onClick={() => setShowTableDropdown(false)}
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = '#e0e0e0'
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = '#f5f5f5'
              }}
            >
              Закрыть
            </div>
          </div>
        )}
        
      </div>
    </Modal>
  )
}

