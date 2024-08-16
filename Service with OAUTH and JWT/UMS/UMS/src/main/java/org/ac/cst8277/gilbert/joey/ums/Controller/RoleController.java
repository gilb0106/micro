package org.ac.cst8277.gilbert.joey.ums.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.Data;
import org.ac.cst8277.gilbert.joey.ums.Service.RoleService;
import org.ac.cst8277.gilbert.joey.ums.Service.JwtService; // Add TokenService to validate tokens
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Data
@RestController
@RequestMapping("/users")
public class RoleController {

    private final RoleService roleService;
    private final JwtService jwtService; // Add TokenService

    @Autowired
    public RoleController(RoleService roleService, JwtService jwtService) {
        this.roleService = roleService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Get all Roles", description = "Fetches all the Roles available in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved roles"),
            @ApiResponse(responseCode = "400", description = "Invalid token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseBody
    @GetMapping("/roles")
    public Mono<Map<String, Object>> getAllRoles(@RequestParam(value = "token", required = false) String token) {
        if (token == null || !jwtService.validateTokenEndpoint(token)) {
            // Token is missing or invalid, return an error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid or missing token");
            return Mono.just(errorResponse);
        }

        // Token is valid, proceed with fetching roles
        return roleService.getAllRoles()
                .map(role -> {
                    Map<String, Object> roleResponse = new HashMap<>();
                    roleResponse.put("data", role);
                    return roleResponse;
                });
    }
}
