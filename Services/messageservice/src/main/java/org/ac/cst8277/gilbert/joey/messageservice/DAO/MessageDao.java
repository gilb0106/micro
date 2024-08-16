package org.ac.cst8277.gilbert.joey.messageservice.DAO;

import org.ac.cst8277.gilbert.joey.messageservice.DataConverter.BinaryConverter;
import org.ac.cst8277.gilbert.joey.messageservice.Repo.MessageRepo;
import org.ac.cst8277.gilbert.joey.messageservice.Bean.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Repository
public class MessageDao implements MessageRepo {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MessageDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Message> getAllMsgs() {
        try {
            String sql = "SELECT id, content, created, producer_id FROM messages";
            return jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> {
                        Message m = new Message();
                        m.setMsgID(BinaryConverter.convertBytesToUUID(rs.getBytes("id")));
                        m.setContent(rs.getString("content"));
                        m.setProducerID(BinaryConverter.convertBytesToUUID(rs.getBytes("producer_id")));

                        int createdInt = rs.getInt("created");
                        long createdLong = (long) createdInt * 1000; // convert seconds to milliseconds
                        Timestamp createdTimestamp = new Timestamp(createdLong);
                        m.setCreated(createdTimestamp);

                        return m;
                    });
        } catch (DataAccessException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Message> getProducerMsgs(UUID id) {
        try {
            String sql = "SELECT id, content, created, producer_id FROM messages WHERE producer_id = ?";
            return jdbcTemplate.query(
                    sql,
                    new Object[]{BinaryConverter.convertUUIDToBytes(id)},
                    (rs, rowNum) -> {
                        Message m = new Message();
                        m.setMsgID(BinaryConverter.convertBytesToUUID(rs.getBytes("id")));
                        m.setContent(rs.getString("content"));
                        m.setProducerID(BinaryConverter.convertBytesToUUID(rs.getBytes("producer_id")));

                        int createdInt = rs.getInt("created");
                        long createdLong = (long) createdInt * 1000; // convert seconds to milliseconds
                        Timestamp createdTimestamp = new Timestamp(createdLong);
                        m.setCreated(createdTimestamp);

                        return m;
                    });
        } catch (DataAccessException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Message> getSubscriberMsgs(UUID id) {
        try {
            String sql = "SELECT m.id, m.content, m.created, m.producer_id " +
                    "FROM messages m " +
                    "JOIN subscriptions s ON m.producer_id = s.producers_id " +
                    "WHERE s.subscribers_id = ?";
            return jdbcTemplate.query(
                    sql,
                    new Object[]{BinaryConverter.convertUUIDToBytes(id)},
                    (rs, rowNum) -> {
                        Message m = new Message();
                        m.setMsgID(BinaryConverter.convertBytesToUUID(rs.getBytes("id")));
                        m.setContent(rs.getString("content"));
                        m.setProducerID(BinaryConverter.convertBytesToUUID(rs.getBytes("producer_id")));

                        int createdInt = rs.getInt("created");
                        long createdLong = (long) createdInt * 1000; // convert seconds to milliseconds
                        Timestamp createdTimestamp = new Timestamp(createdLong);
                        m.setCreated(createdTimestamp);

                        return m;
                    });
        } catch (DataAccessException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    @Override
    public void createmessage(Message message) {
        try {
            String sql = "INSERT INTO messages (id, content, created, producer_id) VALUES (?, ?, ?, ?)";
            UUID randomUUID = UUID.randomUUID();
            byte[] msgIdBytes = BinaryConverter.convertUUIDToBytes(randomUUID);
            byte[] producerIdBytes = BinaryConverter.convertUUIDToBytes(message.getProducerID());
            long createdMillis = System.currentTimeMillis();

            System.out.println("Content: " + message.getContent());
            System.out.println("Producer ID: " + message.getProducerID());

            jdbcTemplate.update(sql, msgIdBytes,
                    message.getContent(),
                    createdMillis / 1000,
                    producerIdBytes);

            System.out.println("Message inserted successfully.");
        } catch (DataAccessException e) {
            e.printStackTrace();
            System.out.println("Error inserting message: " + e.getMessage());
        }
    }

    @Override
    public void deletemessage(UUID id) {
        try {
            String sql = "DELETE FROM messages WHERE id = ?";
            jdbcTemplate.update(sql, BinaryConverter.convertUUIDToBytes(id));
        } catch (DataAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting message", e);
        }
    }
}