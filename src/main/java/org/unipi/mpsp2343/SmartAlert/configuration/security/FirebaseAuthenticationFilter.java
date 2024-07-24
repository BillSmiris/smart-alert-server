package org.unipi.mpsp2343.SmartAlert.configuration.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.unipi.mpsp2343.SmartAlert.utils.AuthUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FirebaseAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);
    private final FirebaseAuth firebaseAuth;
    private final AuthUtils authUtils;

    public FirebaseAuthenticationFilter(FirebaseAuth firebaseAuth, AuthUtils authUtils) {
        this.firebaseAuth = firebaseAuth;
        this.authUtils = authUtils;
    }

    //Security filter for firebase authentication
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        //lets requests to public endpoints pass without authorization
        if (requestURI.startsWith("/api/v1/public/")) {
            filterChain.doFilter(request, response);
            return;
        }

        //Retrieves the authentication token from the request.
        //If not token is included, an error is returned
        String idToken = request.getHeader("Authorization");

        if (idToken == null || !idToken.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "AUTH_ERROR_NO_TOKEN");
            return;
        }

        //Strips the token of the Bearer string and verifies it with firebase for validity
        try {
            FirebaseToken firebaseToken =
                    firebaseAuth.verifyIdToken(idToken.replace("Bearer ", ""));

            //Retrieves the user's role
            String role = "";
            try {
                role = authUtils.getUserRole(firebaseToken.getUid());
            }
            catch (Exception e){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "AUTH_ERROR_UNEXPECTED_ERROR");
            }

            //Adds the user's role to the authorities so the user can access role protected endpoints.
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(role));


            SecurityContextHolder.getContext()
                    .setAuthentication(
                            new FirebaseAuthenticationToken(idToken, firebaseToken, authorities));

            SecurityContextHolder.getContext().getAuthentication().setAuthenticated(true);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "AUTH_ERROR_INVALID_TOKEN");
        }
    }
}
