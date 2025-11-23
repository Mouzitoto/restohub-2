/**
 * E2E тест: Ресторан без подписки недоступен клиентам, после активации подписки доступен
 * 
 * Примечание: Для запуска этого теста необходимо:
 * 1. Установить Playwright: npm install -D @playwright/test
 * 2. Настроить playwright.config.ts
 * 3. Запустить приложения (admin-api, client-api, client-web)
 * 
 * Этот файл является шаблоном для E2E тестов.
 */

import { test, expect } from '@playwright/test'

test.describe('Restaurant Subscription Visibility', () => {
  test('restaurant without subscription is hidden from clients, after activation becomes visible', async ({ page }) => {
    // Шаг 1: Менеджер создает ресторан (через admin-web)
    // (предполагается, что менеджер уже залогинен)
    await page.goto('http://localhost:3001/admin/restaurants')
    await page.click('button:has-text("Создать ресторан")')
    await page.fill('input[name="name"]', 'Test Restaurant')
    await page.fill('input[name="address"]', 'Test Address')
    await page.fill('input[name="phone"]', '+79991234567')
    await page.click('button:has-text("Сохранить")')
    
    // Получаем ID созданного ресторана (из UI или API)
    const restaurantId = 1 // В реальном тесте получаем из ответа API
    
    // Шаг 2: Проверяем, что ресторан не виден клиентам (нет подписки)
    await page.goto('http://localhost:3000/restaurants')
    await expect(page.locator('text=Test Restaurant')).not.toBeVisible()
    
    // Шаг 3: Прямой переход на страницу ресторана возвращает 404
    await page.goto(`http://localhost:3000/restaurants/${restaurantId}`)
    await expect(page.locator('text=Ресторан не найден')).toBeVisible()
    
    // Шаг 4: ADMIN активирует подписку (через admin-web)
    // (предполагается, что admin уже залогинен)
    await page.goto(`http://localhost:3001/admin/restaurants/${restaurantId}/subscription`)
    // Активируем подписку через UI (зависит от реализации)
    
    // Шаг 5: Проверяем, что ресторан теперь виден клиентам
    await page.goto('http://localhost:3000/restaurants')
    await expect(page.locator('text=Test Restaurant')).toBeVisible()
    
    // Шаг 6: Прямой переход на страницу ресторана теперь работает
    await page.goto(`http://localhost:3000/restaurants/${restaurantId}`)
    await expect(page.locator('text=Test Restaurant')).toBeVisible()
  })
})

