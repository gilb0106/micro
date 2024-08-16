package org.ac.cst8277.gilbert.joey.messageservice.Service;

import org.ac.cst8277.gilbert.joey.messageservice.Bean.Message;
import org.ac.cst8277.gilbert.joey.messageservice.Repo.SubscriptionRepo;
import org.ac.cst8277.gilbert.joey.messageservice.Bean.Subscription;
import org.ac.cst8277.gilbert.joey.messageservice.Bean.User;
import org.ac.cst8277.gilbert.joey.messageservice.Service_Connector.HttpResponseExtractor;
import org.ac.cst8277.gilbert.joey.messageservice.Service_Connector.UMSConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionRepo subscriptionRepo;
    private final UMSConnector umsConnector;

    @Autowired
    public SubscriptionService(SubscriptionRepo subscriptionRepo, UMSConnector umsConnector) {
        this.subscriptionRepo = subscriptionRepo;
        this.umsConnector = umsConnector;
    }


    public Mono<Map<String, Object>> getAllSubs() {
        List<Subscription> subscriptions = subscriptionRepo.getAllSubs();
        Map<String, Object> result = new HashMap<>();
        result.put("subscriptions", subscriptions);
        return Mono.just(result);
    }

    public Mono<String> createsubscription(Subscription subscription) {
        return umsConnector.retrieveUmsData(subscription.getSubscriberId().toString())
                .doOnNext(res -> System.out.println("Raw response for subscriber: " + res))
                .flatMap(res -> {
                    if (res instanceof HashMap) {
                        HashMap<String, Object> responseMap = (HashMap<String, Object>) res;
                        System.out.println("Response map for subscriber: " + responseMap);

                        try {
                            User subscriber = HttpResponseExtractor.extractDataFromHttpClientResponse(responseMap, User.class);
                            System.out.println("Subscriber object: " + subscriber);

                            if (subscriber != null && subscriber.getRoles().stream().anyMatch(role -> role.getRolename().equals("SUBSCRIBER"))) {
                                return umsConnector.retrieveUmsData(subscription.getProducerId().toString())
                                        .doOnNext(res2 -> System.out.println("Raw response for producer: " + res2))
                                        .flatMap(res2 -> {
                                            if (res2 instanceof HashMap) {
                                                HashMap<String, Object> responseMap2 = (HashMap<String, Object>) res2;
                                                System.out.println("Response map for producer: " + responseMap2);

                                                try {
                                                    User producer = HttpResponseExtractor.extractDataFromHttpClientResponse(responseMap2, User.class);
                                                    System.out.println("Producer object: " + producer);

                                                    if (producer != null && producer.getRoles().stream().anyMatch(role -> role.getRolename().equals("PRODUCER"))) {
                                                        return Mono.fromRunnable(() -> {
                                                                    subscriptionRepo.createsubscription(subscription);
                                                                    System.out.println("Subscription created successfully.");
                                                                })
                                                                .then(Mono.just("Subscription created successfully"));
                                                    } else {
                                                        return Mono.error(new RuntimeException("User " + subscription.getProducerId() + " is not a producer or does not exist."));
                                                    }
                                                } catch (Exception e) {
                                                    return Mono.error(new RuntimeException("Failed to map producer data to User object", e));
                                                }
                                            } else {
                                                return Mono.error(new RuntimeException("Unexpected response format for producer"));
                                            }
                                        });
                            } else {
                                return Mono.error(new RuntimeException("User " + subscription.getSubscriberId() + " is not a subscriber or does not exist."));
                            }
                        } catch (Exception e) {
                            return Mono.error(new RuntimeException("Failed to map subscriber data to User object", e));
                        }
                    } else {
                        return Mono.error(new RuntimeException("Unexpected response format for subscriber"));
                    }
                })
                .doOnError(e -> System.err.println("Error during subscription creation: " + e.getMessage()));
    }
    public Mono<Void> deletesubscription(Subscription subscription) {
        return Mono.fromRunnable(() -> subscriptionRepo.deletesubscription(subscription));
    }

    public void updatesubscription(Subscription subscription, UUID newProducerId) {
        subscriptionRepo.updatesubscription(subscription, newProducerId);
    }

}
