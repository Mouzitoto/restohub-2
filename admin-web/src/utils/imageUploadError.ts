/**
 * Обрабатывает ошибки при загрузке изображений и возвращает понятное сообщение для пользователя
 */
export function getImageUploadErrorMessage(error: any): string {
  // Проверяем статус код ошибки
  const status = error?.response?.status
  
  if (status === 413) {
    return 'Файл слишком большой. Пожалуйста, выберите изображение меньшего размера (максимум 10 МБ)'
  }
  
  // Проверяем сообщение от сервера
  if (error?.response?.data?.message) {
    return error.response.data.message
  }
  
  // Проверяем exceptionName от сервера
  if (error?.response?.data?.exceptionName) {
    const exceptionName = error.response.data.exceptionName
    
    switch (exceptionName) {
      case 'FILE_TOO_LARGE':
        return 'Файл слишком большой. Пожалуйста, выберите изображение меньшего размера (максимум 10 МБ)'
      case 'INVALID_FILE_TYPE':
        return 'Недопустимый тип файла. Разрешены только изображения (JPG, PNG, WebP)'
      case 'INVALID_IMAGE':
        return 'Не удалось обработать изображение. Убедитесь, что файл является корректным изображением'
      case 'FILE_REQUIRED':
        return 'Необходимо выбрать файл для загрузки'
      default:
        return error.response.data.message || 'Ошибка загрузки изображения'
    }
  }
  
  // Общее сообщение по умолчанию
  return 'Ошибка загрузки изображения. Попробуйте позже'
}

