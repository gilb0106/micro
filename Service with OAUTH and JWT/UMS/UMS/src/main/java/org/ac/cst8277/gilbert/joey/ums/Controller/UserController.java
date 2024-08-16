package org.ac.cst8277.gilbert.joey.ums.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.ac.cst8277.gilbert.joey.ums.Bean.User;
import org.ac.cst8277.gilbert.joey.ums.Service.UserService;
import org.ac.cst8277.gilbert.joey.ums.Service.JwtService; // Import TokenService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService; // Inject TokenService

    @GetMapping("/main")
    public Mono<String> getMainPage(@RequestParam(value = "token", required = false) String token) {
        if (token == null || !jwtService.validateTokenEndpoint(token)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Missing token");
            return Mono.just(errorResponse.toString()); // Convert to String for simplicity
        }
        return Mono.just("Hello, World!");
    }

    @Operation(summary = "Get all Users", description = "Fetches all the users available in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> getAllUsers(@RequestParam(value = "token", required = false) String token) {
        if (token == null || !jwtService.validateTokenEndpoint(token)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Missing token");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
        }

        return userService.getAllUsers()
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("data", user);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred"))));
    }

    @Operation(summary = "Fetch User by ID", description = "Fetches a user by their id from the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The user is returned"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public Mono<Map<String, Object>> getUserById(@PathVariable String id, @RequestParam(value = "token", required = false) String token) {
        if (token == null || !jwtService.validateTokenEndpoint(token)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Missing token");
            return Mono.just(errorResponse);
        }

        return userService.getUserById(UUID.fromString(id))
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("data", user);
                    return response;
                })
                .defaultIfEmpty(Map.of("message", "User not found for id: " + id))
                .onErrorResume(e -> Mono.just(Map.of("message", "Error fetching user: " + e.getMessage())));
    }

    @Operation(summary = "Create a New User", description = "Creates a new user in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/create")
    public Mono<Map<String, Object>> addUser(@RequestBody User user, @RequestParam(value = "token", required = false) String token) {
        if (token == null || !jwtService.validateTokenEndpoint(token)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Missing token");
            return Mono.just(errorResponse);
        }

        return userService.adduser(user)
                .map(user1 -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("data", user1);
                    return response;
                })
                .defaultIfEmpty(Map.of("message", "User Created Successfully!"))
                .onErrorResume(e -> Mono.just(Map.of("message", "Unable to create User")));
    }

    @Operation(summary = "Delete a User", description = "Deletes a user in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User Deleted Successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/delete/{id}")
    public Mono<String> deleteUser(@PathVariable UUID id, @RequestParam(value = "token", required = false) String token) {
        if (token == null || !jwtService.validateTokenEndpoint(token)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Missing token");
            return Mono.just(errorResponse.toString()); // Convert to String for simplicity
        }

        return userService.deleteUser(id)
                .then(Mono.just("User deleted successfully with ID: " + id))
                .onErrorResume(e -> Mono.just("Error deleting user: " + e.getMessage()));
    }
}
