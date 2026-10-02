package com.example.vietstage_web_be.security;

import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.IUsageSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class UsageSessionFilter extends OncePerRequestFilter {

    private final IUsageSessionService usageSessionService;

    public UsageSessionFilter(IUsageSessionService usageSessionService) {
        this.usageSessionService = usageSessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        if (path.startsWith("/api/") && !path.startsWith("/api/auth/") && !path.startsWith("/api/usage-sessions/")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
                User user = (User) auth.getPrincipal();
                // Record activity asynchronously to not block the main thread
                // Using a simple thread for now, or just sync. Since this is an MVP.
                try {
                    usageSessionService.recordActivity(user);
                } catch (Exception e) {
                    // Ignore exceptions to not break the request
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
