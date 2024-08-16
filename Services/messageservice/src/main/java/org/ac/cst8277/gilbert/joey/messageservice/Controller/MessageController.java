package org.ac.cst8277.gilbert.joey.messageservice.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.ac.cst8277.gilbert.joey.messageservice.Bean.Message;
import org.ac.cst8277.gilbert.joey.messageservice.Service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;


@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    Map<String, Object> response = new HashMap<>();

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }
    @Operation(summary = "Get all Messages", description = "Fetches all the messages available in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved messages"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public Mono<Map<String, Object>> getAllMessages() {
        return messageService.getAllMsgs()
                .map(message -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("data", message);
                    return response;
                });
    }

    @Operation(summary = "Get Messages by Producer", description = "Fetches all the messages by who produced them")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved messages"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })

    @GetMapping("/pro/{id}")
    public Mono<Map<String, Object>> getProducerMessages(@PathVariable String id) {
        return messageService.getProducerMsgs(UUID.fromString(id))
                .map(messages -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("data", messages);
                    return response;
                })
                .defaultIfEmpty(Map.of("data","No Messages by Producers found for " + id)) // Return an empty list if no messages are found
                .onErrorResume(e ->
                        Mono.just(Map.of("data", "No Messages by Producers found for " + id))); // Return an empty list on error
    }
    @Operation(summary = "Get Messages by Subscriber", description = "Fetches all the messages for subscribers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved messages"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sub/{id}")
    public Mono<Map<String, Object>> getSubscriberMessages(@PathVariable String id) {
        return messageService.getSubscriberMsgs(UUID.fromString(id))
                .map(messages -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("data", messages);
                    return response;
                })
                .defaultIfEmpty(Map.of("message", "No messages found for subscriber " + id))
                .onErrorResume(e -> Mono.just(Map.of("error", "An error occurred while retrieving messages for subscriber " + id)));
    }

    @Operation(summary = "Create a New Message", description = "This creates a new message.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created message"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/pro/create")
    public Mono<Map<String, Object>> createMessage(@RequestBody Message message) {
        return messageService.createmessage(message)
                .then(Mono.fromSupplier(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("data", "Message created successfully!");
                    return response;
                }))
                .onErrorResume(e -> Mono.fromSupplier(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("error", "Unable to create message: " + e.getMessage());
                    return response;
                }));
    }

    @DeleteMapping("/delete/{id}")
    public Mono<Map<String, Object>> deleteMessage(@PathVariable String id) {
        return messageService.deletemessage(UUID.fromString(id))
                .then(Mono.fromSupplier(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("data", "Message deleted successfully with ID: " + id);
                    return response;
                }))
                .onErrorResume(e -> Mono.fromSupplier(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("error", "Error deleting message: " + e.getMessage());
                    return response;
                }));
    }



}
