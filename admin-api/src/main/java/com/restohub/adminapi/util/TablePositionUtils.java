package com.restohub.adminapi.util;

import com.restohub.adminapi.dto.UpdateTablePositionRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class TablePositionUtils {
    
    /**
     * Проверяет пересечения прямоугольников в списке позиций столов
     * @param positions список позиций столов для проверки
     * @throws RuntimeException если найдены пересечения
     */
    public void checkRectangleIntersections(List<UpdateTablePositionRequest> positions) {
        List<UpdateTablePositionRequest> validPositions = new ArrayList<>();
        
        for (UpdateTablePositionRequest pos : positions) {
            if (pos.getPositionX1() == null || pos.getPositionY1() == null || 
                pos.getPositionX2() == null || pos.getPositionY2() == null) {
                continue; // Пропускаем позиции без координат
            }
            
            // Проверяем пересечение с уже проверенными позициями
            for (UpdateTablePositionRequest existing : validPositions) {
                if (rectanglesIntersect(
                    pos.getPositionX1(), pos.getPositionY1(), pos.getPositionX2(), pos.getPositionY2(),
                    existing.getPositionX1(), existing.getPositionY1(), 
                    existing.getPositionX2(), existing.getPositionY2()
                )) {
                    throw new RuntimeException("TABLE_POSITIONS_INTERSECT");
                }
            }
            
            validPositions.add(pos);
        }
    }
    
    /**
     * Проверяет пересечение двух прямоугольников
     * @param x1 первая точка X первого прямоугольника
     * @param y1 первая точка Y первого прямоугольника
     * @param x2 вторая точка X первого прямоугольника
     * @param y2 вторая точка Y первого прямоугольника
     * @param x3 первая точка X второго прямоугольника
     * @param y3 первая точка Y второго прямоугольника
     * @param x4 вторая точка X второго прямоугольника
     * @param y4 вторая точка Y второго прямоугольника
     * @return true если прямоугольники пересекаются
     */
    public boolean rectanglesIntersect(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2,
                                       BigDecimal x3, BigDecimal y3, BigDecimal x4, BigDecimal y4) {
        // Нормализуем координаты (находим min/max для каждого прямоугольника)
        BigDecimal minX1 = x1.min(x2);
        BigDecimal maxX1 = x1.max(x2);
        BigDecimal minY1 = y1.min(y2);
        BigDecimal maxY1 = y1.max(y2);
        
        BigDecimal minX2 = x3.min(x4);
        BigDecimal maxX2 = x3.max(x4);
        BigDecimal minY2 = y3.min(y4);
        BigDecimal maxY2 = y3.max(y4);
        
        // Проверяем перекрытие: прямоугольники НЕ пересекаются, если один полностью левее/правее или выше/ниже другого
        // Используем <= для того, чтобы касание не считалось пересечением
        boolean noOverlap = maxX1.compareTo(minX2) <= 0 || maxX2.compareTo(minX1) <= 0 ||
                           maxY1.compareTo(minY2) <= 0 || maxY2.compareTo(minY1) <= 0;
        
        return !noOverlap;
    }
}

