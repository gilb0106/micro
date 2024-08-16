package org.ac.cst8277.gilbert.joey.messageservice.Controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.ac.cst8277.gilbert.joey.messageservice.Bean.Subscription;
import org.ac.cst8277.gilbert.joey.messageservice.Service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/sub")
    public Mono<Map<String, Object>> getAllSubs() {
        return subscriptionService.getAllSubs()
                .map(subsrciption -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("data", subsrciption);
                    return response;
                });
    }

    @Operation(summary = "Create a New Subscription", description = "This creates a new subscription for a subscriber.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Subscribed!"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/sub/create")
    public Mono<Map<String, Object>> createSubscription(@RequestBody Subscription subscription) {
        return subscriptionService.createsubscription(subscription)
                .map(result -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", result);
                    return response;
                })
                .onErrorResume(e -> Mono.fromSupplier(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("error", "Unable to create subscription: " + e.getMessage());
                    return response;
                }));
    }

    @Operation(summary = "Delete a Subscription", description = "This deletes a subscription.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted subscription"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/sub/delete")
    public Mono<Map<String, String>> deleteSubscription(@RequestBody Subscription subscription) {
        return subscriptionService.deletesubscription(subscription)
                .then(Mono.just(Map.of("message", "Subscription deleted successfully")))
                .onErrorResume(e -> Mono.just(Map.of("error", "Error Deleting Subscription: " + e.getMessage())));
    }



    @Operation(summary = "Update a Subscription", description = "This updates an existing subscription.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated subscription"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })

    @PutMapping("/sub/updatesubscription")
    public Mono<String> updateSubscription(@RequestBody Map<String, Object> payload) {
        try {
            // Extract UUIDs from the payload
            UUID producerId = UUID.fromString((String) payload.get("producerId"));
            UUID subscriberId = UUID.fromString((String) payload.get("subscriberId"));
            UUID newProducerId = UUID.fromString((String) payload.get("newProducerId"));

            // Create Subscription object
            Subscription subscription = new Subscription(producerId, subscriberId);

            // Handle subscription update with reactive error handling
            return Mono.fromCallable(() -> {
                subscriptionService.updatesubscription(subscription, newProducerId);
                return "Subscription updated successfully";
            }).onErrorResume(e -> Mono.just("Error updating subscription: " + e.getMessage()));

        } catch (Exception e) {
            // Handle parsing errors
            System.out.println("Error parsing request: " + e);
            return Mono.just("Error parsing request: " + e.getMessage());
        }
    }

}

