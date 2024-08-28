package com.olivejua.payservice.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olivejua.payservice.error.FailureResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.*;

public class TimeoutFilter implements Filter {
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException {
        Future<?> future = executorService.submit(() -> {
            try {
                filterChain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            future.get(5000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            httpResponse.getWriter().write(new ObjectMapper().writeValueAsString(
                    new FailureResponse("PAYMENT_TIMEOUT", "Payment processing time exceeded the limit of 5 seconds.")));
        } catch (Exception e) {
            e.printStackTrace();
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpResponse.getWriter().write(new ObjectMapper().writeValueAsString(
                    new FailureResponse("UNEXPECTED_ERROR", "internal server error")));
        }
    }

    @Override
    public void destroy() {
        executorService.shutdown();
    }
}
