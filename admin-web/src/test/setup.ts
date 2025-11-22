import '@testing-library/jest-dom'
import { cleanup } from '@testing-library/react'
import { afterEach, beforeAll, afterAll } from 'vitest'
import { server } from './mocks/server'

// Очистка после каждого теста
afterEach(() => {
  cleanup()
})

// Запуск MSW сервера перед всеми тестами
beforeAll(() => {
  server.listen({ onUnhandledRequest: 'error' })
})

// Очистка после каждого теста
afterEach(() => {
  server.resetHandlers()
})

// Остановка сервера после всех тестов
afterAll(() => {
  server.close()
})

