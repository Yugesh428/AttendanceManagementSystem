package com.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Global CORS configuration.
 *
 * Allows the frontend running on a different origin to call the API
 * without being blocked by the browser.
 *
 * Add your deployed frontend URL to allowedOrigins before going to production.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ── Allowed origins ────────────────────────────────────────────────────
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",   // React (CRA / Vite)
                "http://localhost:3001",
                "http://localhost:5173",   // Vite default
                "http://localhost:4200"    // Angular
        ));

        // ── Allowed HTTP methods ───────────────────────────────────────────────
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // ── Allowed request headers ────────────────────────────────────────────
        config.setAllowedHeaders(List.of(
                "Authorization",       // JWT Bearer token
                "Content-Type",
                "Accept",
                "X-Device-Id",         // student device fingerprint for attendance
                "X-Requested-With"
        ));

        // ── Response headers the frontend JS can read ──────────────────────────
        config.setExposedHeaders(List.of(
                "Authorization",
                "Content-Disposition"  // needed for Excel file downloads
        ));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // cache preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
