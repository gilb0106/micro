package org.ac.cst8277.gilbert.joey.messageservice.Bean;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Message {
    @Id
    @JsonProperty("msgID")
    private UUID msgID;
    @JsonProperty("content")
    private String content;
    @JsonProperty("created")
    private Timestamp created;
    @JsonProperty("producerID")
    private UUID producerID;
}
