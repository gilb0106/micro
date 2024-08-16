package org.ac.cst8277.gilbert.joey.messageservice.DAO;

import org.ac.cst8277.gilbert.joey.messageservice.Bean.Message;
import org.ac.cst8277.gilbert.joey.messageservice.DataConverter.BinaryConverter;
import org.ac.cst8277.gilbert.joey.messageservice.Repo.SubscriptionRepo;
import org.ac.cst8277.gilbert.joey.messageservice.Bean.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Repository
public class SubscriptionDao implements SubscriptionRepo {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SubscriptionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public List<Subscription> getAllSubs() {
        try {
            String sql = "SELECT subscribers_id, producers_id FROM subscriptions";
            return jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> {
                        Subscription s = new Subscription();
                        s.setSubscriberId(BinaryConverter.convertBytesToUUID(rs.getBytes("subscribers_id")));
                        s.setProducerId(BinaryConverter.convertBytesToUUID(rs.getBytes("producers_id")));
                        return s;
                    });
        } catch (DataAccessException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public void createsubscription(Subscription subscription) {
        try {
            // Check if the subscriber exists in the database
            String checkSubscriberSql = "SELECT COUNT(*) FROM subscribers WHERE id = ?";
            Integer subscriberCount = jdbcTemplate.queryForObject(checkSubscriberSql, Integer.class,
                    BinaryConverter.convertUUIDToBytes(subscription.getSubscriberId()));

            if (subscriberCount == null || subscriberCount == 0) {
                // If the subscriber does not exist, add them
                String addSubscriberSql = "INSERT INTO subscribers (id) VALUES (?)";
                jdbcTemplate.update(addSubscriberSql,
                        BinaryConverter.convertUUIDToBytes(subscription.getSubscriberId()));
                System.out.println("Subscriber added: " + subscription.getSubscriberId());
            }

            // Now, add the subscription
            String sql = "INSERT INTO subscriptions (producers_id, subscribers_id) VALUES (?, ?)";
            jdbcTemplate.update(sql,
                    BinaryConverter.convertUUIDToBytes(subscription.getProducerId()),
                    BinaryConverter.convertUUIDToBytes(subscription.getSubscriberId()));
            System.out.println("Subscription created between producer " + subscription.getProducerId() +
                    " and subscriber " + subscription.getSubscriberId());

        } catch (DataAccessException e) {
            e.printStackTrace();
            System.out.println("Error creating subscription: " + e.getMessage());
        }
    }

    @Override
    public void deletesubscription(Subscription subscription) {
        try {
            String deleteSubscriptionSql = "DELETE FROM subscriptions WHERE producers_id = ? AND subscribers_id = ?";
            jdbcTemplate.update(deleteSubscriptionSql,
                    BinaryConverter.convertUUIDToBytes(subscription.getProducerId()),
                    BinaryConverter.convertUUIDToBytes(subscription.getSubscriberId()));
            System.out.println("Subscription deleted: " + subscription.getProducerId());

            // Check if the subscriber has zero subscriptions
            String countSubscriptionsSql = "SELECT COUNT(*) FROM subscriptions WHERE subscribers_id = ?";
            Integer subscriptionCount = jdbcTemplate.queryForObject(countSubscriptionsSql, Integer.class,
                    BinaryConverter.convertUUIDToBytes(subscription.getSubscriberId()));

            if (subscriptionCount != null && subscriptionCount == 0) {
                // Remove the subscriber from the subscribers table if they have zero subscriptions
                String deleteSubscriberSql = "DELETE FROM subscribers WHERE id = ?";
                jdbcTemplate.update(deleteSubscriberSql, BinaryConverter.convertUUIDToBytes(subscription.getSubscriberId()));
                System.out.println("Subscriber removed: " + subscription.getSubscriberId());
            }

            System.out.println("Subscription deleted between producer " + subscription.getProducerId() +
                    " and subscriber " + subscription.getSubscriberId());
        } catch (DataAccessException e) {
            System.err.println("Error deleting subscription: " + e.getMessage());
            throw e; // Propagate exception for onErrorResume
        }
    }


    @Override
    public void updatesubscription(Subscription subscription, UUID newProducerId) {
        try {
            String sql = "UPDATE subscriptions SET producers_id = ? WHERE subscribers_id = ? AND producers_id = ?";
            jdbcTemplate.update(sql,
                    BinaryConverter.convertUUIDToBytes(newProducerId),
                    BinaryConverter.convertUUIDToBytes(subscription.getSubscriberId()),
                    BinaryConverter.convertUUIDToBytes(subscription.getProducerId()));
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }
}
