package com.example.jutjubic.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Ako neautentifikovani korisnik proba da pristupi zasticenom endpointu
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {


    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // 401
        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Morate se prijaviti kako biste pristupili ovoj funkcionalnosti."
        );
    }
}