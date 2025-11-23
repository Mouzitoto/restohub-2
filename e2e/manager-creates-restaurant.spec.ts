/**
 * E2E тест: Менеджер регистрируется, создает ресторан, ресторан привязан
 * 
 * Примечание: Для запуска этого теста необходимо:
 * 1. Установить Playwright: npm install -D @playwright/test
 * 2. Настроить playwright.config.ts
 * 3. Запустить приложения (admin-api, admin-web)
 * 
 * Этот файл является шаблоном для E2E тестов.
 */

import { test, expect } from '@playwright/test'

test.describe('Manager Restaurant Creation Flow', () => {
  test('manager registers, creates restaurant, restaurant is linked', async ({ page }) => {
    // Шаг 1: Менеджер регистрируется на лендинге /partner
    await page.goto('http://localhost:3000/partner')
    
    // Находим и заполняем форму регистрации
    await page.fill('input[name="email"]', 'manager@test.com')
    await page.fill('input[name="password"]', 'password123')
    await page.fill('input[name="confirmPassword"]', 'password123')
    await page.check('input[name="agreeToTerms"]')
    await page.click('button:has-text("Далее")')
    
    // Шаг 2: Подтверждение email (код 1234)
    await page.fill('input[name="code"]', '1234')
    await page.click('button:has-text("Далее")')
    
    // Шаг 3: Редирект на страницу входа
    await expect(page).toHaveURL(/.*\/admin\/login/)
    
    // Шаг 4: Вход в систему
    await page.fill('input[name="email"]', 'manager@test.com')
    await page.fill('input[name="password"]', 'password123')
    await page.click('button:has-text("Войти")')
    
    // Шаг 5: Переход на страницу ресторанов
    await page.goto('http://localhost:3001/admin/restaurants')
    
    // Шаг 6: Создание ресторана
    await page.click('button:has-text("Создать ресторан")')
    await page.fill('input[name="name"]', 'My Restaurant')
    await page.fill('input[name="address"]', 'Test Address')
    await page.fill('input[name="phone"]', '+79991234567')
    await page.click('button:has-text("Сохранить")')
    
    // Шаг 7: Проверка, что ресторан появился в списке
    await expect(page.locator('text=My Restaurant')).toBeVisible()
    
    // Шаг 8: Проверка, что ресторан привязан к менеджеру
    // (можно проверить через API или через UI)
  })
})

