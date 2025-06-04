package com.hexplatoon.syncrift_backend.security;

import com.hexplatoon.syncrift_backend.exception.InvalidJwtAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter responsible for JWT token validation and authentication.
 * <p>
 * This filter intercepts each HTTP request, extracts the JWT token from the
 * Authorization header (if present), validates it, and sets up the authentication
 * context if the token is valid.
 * </p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Constructs a new JwtAuthenticationFilter.
     *
     * @param jwtTokenProvider  The provider for JWT token operations
     * @param userDetailsService The service to load user details
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Process an incoming HTTP request, extract and validate the JWT token,
     * and set up the authentication context if the token is valid.
     *
     * @param request The HTTP request
     * @param response The HTTP response
     * @param filterChain The filter chain
     * @throws ServletException If a servlet exception occurs
     * @throws IOException If an I/O exception occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = extractTokenFromRequest(request);
            
            // Only proceed with authentication if token exists and no authentication is already set
            if (token != null && !token.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    // Get username from token using the updated method name
                    String username = jwtTokenProvider.extractUsername(token);
                    
                    // Validate both username and token
                    if (username != null && !username.isEmpty() && jwtTokenProvider.validateToken(token)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        
                        // Ensure user details are valid
                        if (userDetails != null && userDetails.isEnabled()) {
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, 
                                    null, 
                                    userDetails.getAuthorities()
                            );
                            
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                } catch (InvalidJwtAuthenticationException ex) {
                    // Handle token validation errors within the token processing block
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                                      "Invalid JWT token: " + ex.getMessage());
                    return;
                }
            }
            
            filterChain.doFilter(request, response);
        } catch (InvalidJwtAuthenticationException ex) {
            logger.error("JWT Authentication failed", ex);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                             "Invalid JWT token: " + ex.getMessage());
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                             "Authentication error: " + ex.getMessage());
        }
    }

    /**
     * Extract JWT token from the HTTP request Authorization header.
     *
     * @param request The HTTP request
     * @return The JWT token or null if not found
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }

    /**
     * Sends an error response with proper content type header.
     *
     * @param response The HTTP response
     * @param statusCode The HTTP status code
     * @param message The error message
     * @throws IOException If an I/O exception occurs
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) 
            throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"" + message.replace("\"", "\\\"") + "\"}");
    }
}

