import '@testing-library/jest-dom'
import { cleanup } from '@testing-library/react'
import { afterEach, beforeAll, afterAll, vi } from 'vitest'
import { server } from './mocks/server'
import axios from 'axios'

// Устанавливаем переменную окружения для тестов
vi.stubEnv('VITE_API_BASE_URL', 'http://localhost:8082')

// Настраиваем axios для использования fetch adapter в тестах
// Это нужно для корректной работы MSW с axios и baseURL
// MSW лучше работает с fetch, чем с XMLHttpRequest
if (typeof globalThis.fetch !== 'undefined') {
  // Используем fetch adapter для axios в тестовом окружении
  // Это позволяет MSW корректно перехватывать запросы с baseURL
  axios.defaults.adapter = 'fetch' as any
}

// Очистка после каждого теста
afterEach(() => {
  cleanup()
})

// Запуск MSW сервера перед всеми тестами
beforeAll(() => {
  server.listen({ 
    onUnhandledRequest: 'error',
  })
})

// Очистка после каждого теста
afterEach(() => {
  server.resetHandlers()
})

// Остановка сервера после всех тестов
afterAll(() => {
  server.close()
})

