package org.ac.cst8277.gilbert.joey.ums.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.ac.cst8277.gilbert.joey.ums.Bean.Role;
import org.ac.cst8277.gilbert.joey.ums.Bean.User;
import org.ac.cst8277.gilbert.joey.ums.DAO.UserDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Service
public class JwtService {
// set values directly in Token Service
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

private UserDao userRepo;
  public JwtService(UserDao userRepo) {
      this.userRepo = userRepo;
  }

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet(); // For token invalidation

    public String createToken(String githubLogin, List<Role> roles) {
        System.out.println("Creating token for GitHub Login: " + githubLogin);

        List<String> roleNames = roles.stream()
                .map(Role::getRolename)
                .collect(Collectors.toList());
        System.out.println("Roles in TokenService: " + roleNames);
        Optional<User> user = userRepo.findByEmail(githubLogin);
        String userId = String.valueOf(user.get().getId());
        Claims claims = Jwts.claims().setSubject(githubLogin);
        // load up jwt with roles and userid
        claims.put("roles", roleNames);
        claims.put("userId", userId);
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtExpirationMs);
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, String.valueOf(secretKey))
                .compact();

        System.out.println("Generated Token: " + token);
        return token;
    }

    public boolean validateTokenEndpoint(String token) {
        System.out.println("Validating token: " + token);
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            boolean isValid = !isTokenExpired(token);
            System.out.println("Token validation result: " + isValid);
            return isValid;
        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        System.out.println("Checking if token is expired: " + token);

        final Date expiration = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        System.out.println("Token expiration: " + expiration);
        boolean expired = expiration.before(new Date());
        System.out.println("Token expired: " + expired);
        return expired;
    }
    public String getGithubLoginFromToken(String token) {
        System.out.println("Extracting GitHub login from token: " + token);

        String githubLogin = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();// Subject = login name

        System.out.println("GitHub Login: " + githubLogin);
        return githubLogin;
    }
    public String getUserIdFromToken(String token) {
        System.out.println("Extracting UserID from token: " + token);

        String userId = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .get("userId", String.class);

        System.out.println("USERID : " + userId);
        return userId;
    }
    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        // Extract roles from the token
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        List<String> roles = claims.get("roles", List.class);
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public List<String> getRolesFromToken(String token) {
        System.out.println("Extracting roles from token: " + token);

        List<String> roles = (List<String>) Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .get("roles");

        System.out.println("Roles Token SERVICE: " + roles);
        return roles;
    }
    public Date getTokenExpiration(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
    public void blacklistToken(String token) {
        System.out.println("Blacklisting token: " + token);
        blacklistedTokens.add(token);
    }
}
