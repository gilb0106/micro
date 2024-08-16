package org.ac.cst8277.gilbert.joey.ums.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ac.cst8277.gilbert.joey.ums.Bean.Role;
import org.ac.cst8277.gilbert.joey.ums.Service.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class LoginController {

    private final JwtService jwtService;

    public LoginController(JwtService jwtService) {
        this.jwtService = jwtService;
    }
// model methods to return html templates

    @GetMapping()
    public String index(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authentication: " + authentication);

        if (authentication != null && !Objects.equals(authentication.getName(), "anonymousUser")) {
            String username = authentication.getName(); // Get the username directly
            List<Role> roles = Collections.emptyList(); // Fetch user roles
            String token = jwtService.createToken(username, roles);

            // Debug logs
            System.out.println("Username: " + username);
            System.out.println("Roles: " + roles);
            System.out.println("Token: " + token);

            model.addAttribute("isLoggedIn", true);
            model.addAttribute("githubLogin", username);

            model.addAttribute("token", token);
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        return "index";
    }


    @PostMapping("/logout")
    public RedirectView logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            // Retrieve the token from the request parameter
            String token = request.getParameter("token");
            // Blacklist the token
            if (token != null) {
                jwtService.blacklistToken(token);
            }
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return new RedirectView("/");
    }

    @GetMapping("/demo")
    public String demo(@RequestParam(value = "token", required = false) String token, Model model) {
        if (token != null) {
            String githubLogin = jwtService.getGithubLoginFromToken(token);
            List<String> roles = jwtService.getRolesFromToken(token);
            String expiration = jwtService.getTokenExpiration(token).toString();
            String userId = jwtService.getUserIdFromToken(token).toString();

            model.addAttribute("token", token);
            model.addAttribute("githubLogin", githubLogin);
            model.addAttribute("userId", userId);
            model.addAttribute("roles", String.join(",", roles));
            model.addAttribute("expiration", expiration);
            System.out.println("GitHub Login: " + githubLogin);
            System.out.println("Roles: " + roles);
            System.out.println("Expiration: " + expiration);
        } else {
            model.addAttribute("token", null);
        }
        return "demo"; // Thymeleaf template name
    }
    @PostMapping("/refresh")
    public RedirectView refreshToken(@RequestParam(value = "token") String oldToken) {
        // Print initial parameters
        System.out.println("Received request to refresh token");
        String githubLogin = jwtService.getGithubLoginFromToken(oldToken);
        System.out.println("GitHub Login: " + githubLogin);
        System.out.println("OLD TOKEN: " + oldToken);

        // Blacklist the old token
        System.out.println("Blacklisting the old token...");
        jwtService.blacklistToken(oldToken);
        System.out.println("Old token blacklisted.");

        // Extract roles from the old token
        System.out.println("Extracting roles from the old token...");
        List<String> roles = jwtService.getRolesFromToken(oldToken);
        System.out.println("Roles extracted from token: " + roles);

        // Check if roles are null or empty
        if (roles == null) {
            roles = new ArrayList<>(); // Default to an empty list
            System.out.println("Roles were null, defaulting to an empty list.");
        } else {
            System.out.println("Roles are present.");
        }
        // Convert roles to Role objects
        System.out.println("Converting roles to Role objects");
        List<Role> roleList = new ArrayList<>();
        for (String roleName : roles) {
            System.out.println("Creating Role object for: " + roleName + "  Roles " + roles);
            Role role = new Role(roleName);
            roleList.add(role);
            System.out.println("Created Role: " + role);
        }
        System.out.println("RolesList in Refresh: " + roleList);

        // Create a new token
        System.out.println("Creating a new token");
        String newToken = jwtService.createToken(githubLogin, roleList);
        String newExpiration = jwtService.getTokenExpiration(newToken).toString();
        System.out.println("New Token: " + newToken);
        System.out.println("New Token Expiration: " + newExpiration);

        // Redirect to the demo page with the new token and roles
        System.out.println("Redirecting to /demo with new token and roles");
        return new RedirectView("/demo?token=" + newToken);
    }
}