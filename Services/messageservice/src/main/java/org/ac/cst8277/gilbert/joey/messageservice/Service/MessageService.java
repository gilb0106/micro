package org.ac.cst8277.gilbert.joey.messageservice.Service;

import org.ac.cst8277.gilbert.joey.messageservice.Repo.MessageRepo;
import org.ac.cst8277.gilbert.joey.messageservice.Bean.Message;
import org.ac.cst8277.gilbert.joey.messageservice.Bean.User;
import org.ac.cst8277.gilbert.joey.messageservice.Service_Connector.HttpResponseExtractor;
import org.ac.cst8277.gilbert.joey.messageservice.Service_Connector.UMSConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageService {

    @Autowired
    private final MessageRepo messageRepo;

    private final UMSConnector umsConnector;

    @Autowired
    public MessageService(@Qualifier("messageDao") MessageRepo messageRepo, UMSConnector umsConnector) {
        this.messageRepo = messageRepo;
        this.umsConnector = umsConnector;
    }


    public Mono<Map<String, Object>> getAllMsgs() {
        List<Message> messages = messageRepo.getAllMsgs();
        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);
        return Mono.just(result);
    }


    public Mono<Map<String, Object>> getProducerMsgs(UUID id) {
        System.out.println("ID: " + id);
        return umsConnector.retrieveUmsData(id.toString())
                .flatMap(res -> {
                    System.out.println("Response from UMS: " + res);
                    Map<String, Object> responseMap = (Map<String, Object>) res;
                    User user = HttpResponseExtractor.extractDataFromHttpClientResponse(responseMap, User.class);
                    System.out.println("Extracted user: " + user);

                    if (user == null) {
                        System.out.println("User is null after extraction");
                        return Mono.error(new RuntimeException("User not found"));
                    }

                    System.out.println("User name: " + user.getName());
                    boolean isProducer = user.getRoles().stream()
                            .anyMatch(role -> "PRODUCER".equals(role.getRolename()));
                    System.out.println("Is producer: " + isProducer);

                    if (isProducer) {
                        List<Message> messages = messageRepo.getProducerMsgs(id);
                        System.out.println("Messages: " + messages);

                        Map<String, Object> result = new HashMap<>();
                        result.put("messages", messages);
                        return Mono.just(result);
                    } else {
                        System.out.println("User is not a producer");
                        return Mono.error(new RuntimeException("User is not a producer"));
                    }
                })
                .switchIfEmpty(Mono.error(new RuntimeException("User data is empty")));
    }


    public Mono<List<Message>> getSubscriberMsgs(UUID id) {
        return umsConnector.retrieveUmsData(id.toString())
                .flatMap(res -> {
                    System.out.println("Response from UMS: " + res);
                    Map<String, Object> responseMap = (Map<String, Object>) res;
                    User user = HttpResponseExtractor.extractDataFromHttpClientResponse(responseMap, User.class);
                    System.out.println("Extracted user: " + user);

                    if (user == null) {
                        System.out.println("User is null after extraction");
                        return Mono.error(new RuntimeException("User not found"));
                    }

                    boolean isSubscriber = user.getRoles().stream()
                            .anyMatch(role -> "SUBSCRIBER".equals(role.getRolename()));
                    System.out.println("Is subscriber: " + isSubscriber);

                    if (isSubscriber) {
                        List<Message> messages = messageRepo.getSubscriberMsgs(id);
                        System.out.println("Messages: " + messages);
                        return Mono.just(messages);
                    } else {
                        System.out.println("User is not a subscriber");
                        return Mono.error(new RuntimeException("User is not a subscriber"));
                    }
                })
                .switchIfEmpty(Mono.error(new RuntimeException("User data is empty")));
    }

    public Mono<Object> createmessage(Message message) {
        System.out.println("Checking UMS : " + message.getProducerID());
        return umsConnector.retrieveUmsData(message.getProducerID().toString())
                   .flatMap(res -> {
            System.out.println("Response from UMS: " + res);
            Map<String, Object> responseMap = (Map<String, Object>) res;
            User user = HttpResponseExtractor.extractDataFromHttpClientResponse(responseMap, User.class);
            System.out.println("Extracted user: " + user);

            if (user == null) {
                System.out.println("User is null after extraction");
                return Mono.error(new RuntimeException("User not found"));
            }

            boolean isProducer = user.getRoles().stream()
                    .anyMatch(role -> "PRODUCER".equals(role.getRolename()));
            System.out.println("Is producer: " + isProducer);

            if (isProducer) {
                return Mono.fromRunnable(() ->
                    messageRepo.createmessage(message));

            } else {
                System.out.println("User is not a producer");
                return Mono.error(new RuntimeException("User is not a producer"));
            }
        });
    }
    public Mono<Void> deletemessage(UUID id) {
        System.out.println("Attempting to delete message with ID: " + id);
        return Mono.fromRunnable(() -> {
            try {
                messageRepo.deletemessage(id);
                System.out.println("Message deleted successfully with ID: " + id);
            } catch (Exception e) {
                System.out.println("Exception in DAO method: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}


