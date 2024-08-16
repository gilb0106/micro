package org.ac.cst8277.gilbert.joey.messageservice.Repo;

import org.ac.cst8277.gilbert.joey.messageservice.Bean.Message;
import org.ac.cst8277.gilbert.joey.messageservice.Bean.Subscription;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepo {

    List<Subscription> getAllSubs();
    void createsubscription(Subscription subscription);
    void deletesubscription(Subscription subscription);
    void updatesubscription(Subscription subscription, UUID newProducerId);

}
