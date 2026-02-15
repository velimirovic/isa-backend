package com.example.jutjubic.infrastructure.security.jwt;

import com.example.jutjubic.infrastructure.monitoring.ActiveUsersMetrics;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Filter koji validira jwt token pre zahteva
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private TokenUtils tokenUtils;
    private UserDetailsService userDetailsService;
    private ActiveUsersMetrics activeUsersMetrics;

    protected final Log LOGGER = LogFactory.getLog(getClass());

    public TokenAuthenticationFilter(TokenUtils tokenUtils, UserDetailsService userDetailsService, ActiveUsersMetrics activeUsersMetrics) {
        this.tokenUtils = tokenUtils;
        this.userDetailsService = userDetailsService;
        this.activeUsersMetrics = activeUsersMetrics;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String email = null;
        String authToken = tokenUtils.getToken(request);

        try {
            if (authToken != null) {
                email = tokenUtils.getEmailFromToken(authToken);

                if (email != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    if (tokenUtils.validateToken(authToken, userDetails.getUsername())) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        if (activeUsersMetrics != null) {
                            activeUsersMetrics.recordUserActivity(email);
                        }
                    }
                }
            }

        } catch (ExpiredJwtException ex) {
            LOGGER.debug("JWT token je istekao!");
        }

        if (email == null && activeUsersMetrics != null) {
            String sessionId = getAnonymousIdentifier(request);
            activeUsersMetrics.recordAnonymousActivity(sessionId);
        }

        chain.doFilter(request, response);
    }

    private String getAnonymousIdentifier(HttpServletRequest request) {
        String sessionId = request.getHeader("Cookie");
        if (sessionId != null && !sessionId.isEmpty()) {
            return sessionId;
        }
        
        String userAgent = request.getHeader("User-Agent");
        String remoteAddr = request.getRemoteAddr();
        return (userAgent != null ? userAgent : "") + "_" + (remoteAddr != null ? remoteAddr : "unknown");
    }
}