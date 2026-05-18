package com.fitlife.platform.config;

import com.fitlife.platform.store.VisualStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MysqlPersistenceFilter extends OncePerRequestFilter {
    @Autowired
    private VisualStore store;

    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "fitlife-mysql-save");
        t.setDaemon(true);
        return t;
    });

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/api/") && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
            // Login requests do not change business data. Persisting the full state after every
            // login can make the login button appear stuck if MySQL is busy, so skip auth endpoints.
            if (uri.endsWith("/login") || uri.contains("/auth/login")) return;
            // Do not block the HTTP response while MySQL writes the readable tables/state snapshot.
            // Several user interactions (like, reply, join team) should return immediately; otherwise
            // the Vite client may hit its timeout while waiting for a full snapshot sync.
            saveExecutor.submit(() -> {
                try { store.saveToMysqlQuietly(); }
                catch (Exception ex) { System.err.println("[FitLife] async MySQL save failed: " + ex.getMessage()); }
            });
        }
    }
}
