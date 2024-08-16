package org.ac.cst8277.gilbert.joey.messageservice.Repo;

import org.ac.cst8277.gilbert.joey.messageservice.Bean.Message;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepo {

     List<Message> getAllMsgs();
     List<Message> getProducerMsgs(UUID id);
     List<Message> getSubscriberMsgs(UUID id);
     void createmessage(Message message);
     void deletemessage(UUID id);


}
