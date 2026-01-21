package com.example.load_test_helper

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@Component
class RestLoggingFilter extends OncePerRequestFilter {

    private static final log = LoggerFactory.getLogger(RestLoggingFilter.class)

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        try {
            // Перед началом обработки
            log.info("Request -- {} {}",
                    request.method, request.requestURI)

            // Выполнение основного потока обработки
            chain.doFilter(request, response)

            // После окончания обработки
            log.info("Response -- {} {}; status: {}",
                    request.method, request.requestURI, response.status)
        } catch (Throwable t) {
            log.error("{} {}; message: Ошибка при обработке запроса:",
                    request.method, request.requestURI, t)
        }
    }
}
