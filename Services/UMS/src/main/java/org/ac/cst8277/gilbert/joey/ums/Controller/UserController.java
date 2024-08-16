
package org.ac.cst8277.gilbert.joey.ums.Controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.ac.cst8277.gilbert.joey.ums.Bean.User;
import org.ac.cst8277.gilbert.joey.ums.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userservice;
    Map<String, Object> response = new HashMap<>();

    @GetMapping(path = "/main")
    public String getMainPage() {
        return "Hello, World!";
    }

    @Operation(summary = "Get all Users", description = "Fetches all the users available in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public Mono<Map<String, Object>> getAllUsers() {
        return userservice.getAllUsers()
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("data", user);
                    return response;
                });
    }


    @Operation(summary = "Fetch User by ID", description = "Fetches a user by their id from the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The user is returned"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public Mono<Map<String, Object>> getUserById(@PathVariable String id) {
        return userservice.getUserById(UUID.fromString(id))
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("data", user);
                    return response;
                })
                .defaultIfEmpty(
                        Map.of("message", "User not found for id: " + id)
                )
                .onErrorResume(e ->
                        Mono.just(
                                Map.of("message", "Error fetching user: " + e.getMessage())
                        )
                );
    }

    @Operation(summary = "Create a New User", description = "Creates a new user in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/create")
    public Mono<Map<String, Object>> addUser(@RequestBody User user) {
        System.out.println("Received User object: " + user.getName());
        return userservice.adduser(user)
                .map(user1 -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("data", user1);
                    return response;
                })
                .defaultIfEmpty(Map.of("message", "User Created Successfully!")) // Return an empty list if no messages are found
                .onErrorResume(e -> Mono.just(Map.of("message","Unable to create User"))); // Return an empty list on error

    }

        @Operation(summary = "Delete a User", description = "Deletes a user in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User Deleted Successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server user")
    })
    @DeleteMapping("/delete/{id}")
    public Mono<String> deleteUser(@PathVariable UUID id) {
        return userservice.deleteUser(id)
                .then(Mono.just(("User deleted successfully with ID: " + id)))
                .onErrorResume(e ->
                        Mono.just(
                                ("Error deleting user: " + e.getMessage())
                        )
                );
    }



    }
