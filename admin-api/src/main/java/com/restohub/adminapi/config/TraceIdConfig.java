package com.restohub.adminapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter для установки X-Trace-Id заголовка из Micrometer Tracing (Spring Sleuth).
 * Логирование HTTP запросов/ответов выполняется через Logbook.
 * 
 * Micrometer Tracing автоматически добавляет traceId в MDC с ключом "traceId".
 */
@Component
@Order(1)
public class TraceIdConfig extends OncePerRequestFilter {
    
    private static final String TRACE_ID_KEY = "traceId";
    private static final String X_TRACE_ID_HEADER = "X-Trace-Id";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Micrometer Tracing автоматически добавляет traceId в MDC
        // Устанавливаем заголовок X-Trace-Id в ответе для клиентов
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId != null && !traceId.isEmpty()) {
            response.setHeader(X_TRACE_ID_HEADER, traceId);
        } else {
            // Если traceId еще не установлен Sleuth, проверяем входящий заголовок
            String incomingTraceId = request.getHeader(X_TRACE_ID_HEADER);
            if (incomingTraceId != null && !incomingTraceId.isEmpty()) {
                // Устанавливаем в MDC для использования в логах
                MDC.put(TRACE_ID_KEY, incomingTraceId);
                response.setHeader(X_TRACE_ID_HEADER, incomingTraceId);
            }
        }
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // После обработки запроса, убеждаемся, что traceId установлен в заголовке ответа
            String finalTraceId = MDC.get(TRACE_ID_KEY);
            if (finalTraceId != null && !finalTraceId.isEmpty()) {
                response.setHeader(X_TRACE_ID_HEADER, finalTraceId);
            }
        }
    }
}

