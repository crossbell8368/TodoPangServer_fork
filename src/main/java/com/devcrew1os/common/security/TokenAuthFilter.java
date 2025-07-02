package com.devcrew1os.common.security;

import com.devcrew1os.common.enums.users.UserRole;
import com.devcrew1os.dto.Response;
import com.devcrew1os.repository.users.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenAuthFilter extends OncePerRequestFilter {

    private final UsersRepository userRepo;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthFilter.class);

    // Main
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. extract & check header
        String header = request.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer ")) {
            setResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "[Error] Authorization token is required.");
            logger.warn("[TokenFilter] Authorization header is missing or invalid.");
            return;
        }
        // 2. verified token
        String token = header.substring(7);
        String userId;
        try {
            userId = tokenService.tokenVerifier(token);
            if (userId == null) {
                throw new RuntimeException("Invalid token");
            }
        } catch (Exception err) {
            setResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "[Failed] Invalid or expired token.");
            return;
        }
        // 3. check path
        String path = request.getRequestURI();
        boolean isAdminReq = path.startsWith("/admin/");
        boolean isUserReq = path.startsWith("/main/");
        boolean isPublicAdminPath = path.equals("/admin/auth/signup") || path.equals("/admin/auth/logout");
        boolean isPublicMainPath = path.equals("/main/auth/signup") || path.equals("/main/auth/logout");

        // 4. verified userId
        if(isAdminReq && !isPublicAdminPath) {
            if(!userRepo.existsByUserIdAndRole(userId, UserRole.ADMIN.getValue())){
                setResponse(response, HttpServletResponse.SC_NOT_FOUND, "[Error] Failed to found Admin User with id: " + userId);
                logger.warn("[TokenFilter] Failed to find adminId at server: {}", userId);
                return;
            }
        } else if(isUserReq && !isPublicMainPath) {
            if(!userRepo.existsByUserId(userId)){
                setResponse(response, HttpServletResponse.SC_NOT_FOUND, "[Error] Failed to found User with id: " + userId);
                logger.warn("[TokenFilter] Failed to find user at server: {}", userId);
                return;
            }
        }
        // 4. set role
        List<SimpleGrantedAuthority> authorities;
        if (isAdminReq) {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if (isUserReq) {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        } else {
            setResponse(response, HttpServletResponse.SC_FORBIDDEN, "[Error] Access to this path is denied.");
            return;
        }
        // 5. save context
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId, null, authorities
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    // support
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

