package org.ac.cst8277.gilbert.joey.ums.Config;

import io.jsonwebtoken.ExpiredJwtException;
import org.ac.cst8277.gilbert.joey.ums.Service.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class JwtFilterConfig extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFilterConfig(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("Filtering request: " + request.getRequestURI());
        //get query parms
        String jwt = getJwtFromRequest(request);

        if (StringUtils.hasText(jwt)) {
            System.out.println("JWT found: " + jwt);
            try {
                String githubLogin = jwtService.getGithubLoginFromToken(jwt);
                System.out.println("GitHub Login from token: " + githubLogin);

                if (jwtService.validateTokenEndpoint(jwt)) {
                    System.out.println("Token is valid");

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            githubLogin, null, jwtService.getAuthoritiesFromToken(jwt));

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
             //Handle expirations gracefully, send back to login
                } else {
                    System.out.println("Token is expired");
                    handleTokenExpiration(response);
                    return;
                }
            } catch (ExpiredJwtException e) {
                System.out.println("Token is expired");
                handleTokenExpiration(response);
                return;
            }
        } else {
            System.out.println("No JWT found");
        }

        filterChain.doFilter(request, response);
    }
    private void handleTokenExpiration(HttpServletResponse response) throws IOException {
        // Redirect to login page
        response.sendRedirect("/");
    }
    private String getJwtFromRequest(HttpServletRequest request) {
        // extract token from url ? parm
        String jwtFromParam = request.getParameter("token");
        if (StringUtils.hasText(jwtFromParam)) {
            return jwtFromParam;
        }
        return null;
    }
}