package com.eduScale.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.eduScale.controller.ApiError;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.eduScale.domain.User;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (isPublic(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        try {
            var claims = jwtService.parseAccessToken(token);
            String userId = claims.getSubject();
            String roleClaim = claims.get("role", String.class);
            var role = User.Role.fromTokenClaim(roleClaim);
            if (role.isEmpty()) {
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN", "Invalid access token.");
                return;
            }
            var authorities = List.of(new SimpleGrantedAuthority(role.get().toSpringAuthority()));
            var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (ExpiredJwtException e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_EXPIRED", "Access token expired.");
            return;
        } catch (JwtException e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN", "Invalid access token.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublic(String path, String method) {
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/actuator/health")) {
            return true;
        }
        if ("POST".equalsIgnoreCase(method)) {
            return path.equals("/api/v1/auth/login")
                    || path.equals("/api/v1/auth/refresh")
                    || path.equals("/api/v1/auth/logout")
                    || path.equals("/api/v1/users/parents");
        }
        return false;
    }

    private void writeError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(new ApiError(code, message)));
    }
}
