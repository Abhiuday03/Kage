package com.contentos.security.filter;

import com.contentos.security.JwtTokenProvider;
import com.contentos.security.TenantContext;
import com.contentos.security.UserPrincipal;
import com.contentos.modules.identity.repository.UserRepository;
import com.contentos.modules.identity.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * WHAT IT DOES:
 * Runs ONCE per HTTP request. Extracts the JWT token from the Authorization header,
 * validates it, loads the user, and sets up Spring Security context.
 *
 * WHY IT EXISTS:
 * This is how Spring Boot knows "who is making this request". Without it, every
 * controller would be unauthenticated. This filter runs before controllers.
 *
 * HOW IT CONNECTS:
 * Registered in SecurityConfig.
 * Runs for every request EXCEPT /auth/register and /auth/login (which are permit-all).
 * Calls JwtTokenProvider.extractUserId() to get the user from the token.
 * Sets TenantContext.setCurrentUserId() so the rest of the application can access it.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final TenantContext tenantContext;

    public JwtAuthenticationFilter(
        JwtTokenProvider jwtTokenProvider,
        UserRepository userRepository,
        TenantContext tenantContext
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.tenantContext = tenantContext;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // Extract the JWT token from the Authorization header
            String authHeader = request.getHeader("Authorization");
            String token = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7); // Remove "Bearer " prefix
            }

            // If there's a token, try to validate it
            if (token != null && jwtTokenProvider.validateToken(token)) {
                UUID userId = jwtTokenProvider.extractUserId(token);

                if (userId != null) {
                    // Load the user from database
                    User user = userRepository.findById(userId).orElse(null);

                    if (user != null) {
                        // Create a UserPrincipal and set it in Spring Security context
                        UserPrincipal principal = new UserPrincipal(
                            user.getId(),
                            user.getEmail(),
                            user.getPasswordHash(),
                            user.getFullName()
                        );

                        UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities()
                            );

                        SecurityContextHolder.getContext().setAuthentication(auth);

                        // Set the user ID in TenantContext for later access
                        tenantContext.setCurrentUserId(userId);

                        log.debug("Authentication set for user: {}", user.getEmail());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not set user authentication in security context", e);
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
