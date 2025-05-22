package com.devcrew1os.security;

import com.devcrew1os.dto.Response;
import com.devcrew1os.dto.main.auth.LoginDTO;
import com.devcrew1os.repository.main.users.UsersRepository;
import com.devcrew1os.common.util.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenAuthFilter extends OncePerRequestFilter {

    private final UsersRepository userRepo;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. check header
        String header = request.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer ")) {
            setResponse(response, HttpServletResponse.SC_BAD_REQUEST, "[Failed]Authorization header missing");
            return;
        }

        // 1. check URL
        String path = request.getRequestURI();
        boolean isAdmin = path.startsWith("/admin");
        boolean isSignup = path.equals("/main/auth/signup");

        // 2. check token
        String token = header.substring(7);
        LoginDTO dto;

        // 3-1. Normal User
        try {
            dto = tokenService.tokenVerifier(token, isAdmin);
            if(dto == null) {
                throw new RuntimeException("Invalid token");
            }
        } catch (Exception err) {
            setResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "[Failed]Invalid or expired token");
            return;
        }
        if(!isAdmin && !isSignup) {
            if(!userRepo.existsUsersByUserId(dto.getUid())) {
                setResponse(response, HttpServletResponse.SC_NOT_FOUND, "[Failed]Invalid userId");
                return;
            }
        }

        // 4-1. save context
        String role = isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                dto.getUid(), null, List.of(new SimpleGrantedAuthority(role))
        );
        if(isAdmin) auth.setDetails(dto);
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    private void setResponse(HttpServletResponse res, int status, String message) throws IOException {
        // struct body
        Response.Body<Object> body = Response.Body.builder()
                .status(status)
                .result(Response.ResponseResult.FAIL)
                .message(message)
                .data(Boolean.FALSE)
                .build();
        // set response
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

