package com.greenfieldcommerce.greenerp.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticationLoggingFilter extends OncePerRequestFilter
{

    private static final Logger log = LoggerFactory.getLogger(AuthenticationLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
	{
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            log.info("User: {}, Authorities: {}, Details: {}",
                    auth.getName(),
                    auth.getAuthorities(),
                    auth.getDetails());
        } else {
            log.info("Unauthenticated request to {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
