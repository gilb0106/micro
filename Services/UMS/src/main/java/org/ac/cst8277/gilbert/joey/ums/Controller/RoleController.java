package org.ac.cst8277.gilbert.joey.ums.Controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.Data;
import org.ac.cst8277.gilbert.joey.ums.Service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Data
@RestController
@RequestMapping("/users")
public class RoleController {
    @Autowired
    private RoleService roleservice;


    public RoleController(RoleService roleservice){
        this.roleservice = roleservice;
    }

    @Operation(summary = "Get all Roles", description = "Fetches all the Roles available in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved roles"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })

    @ResponseBody
    @GetMapping("/roles")
    public Mono<Map<String, Object>> getAllRoles() {
        return roleservice.getAllRoles()
                .map(role -> {
                    Map<String, Object> roleResponse = new HashMap<>();
                    roleResponse.put("data", role);
                    return roleResponse;
                });
    }
}
