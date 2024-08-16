package org.ac.cst8277.gilbert.joey.messageservice.Bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Id;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    @Setter
    @Getter
    @Id
    @JsonProperty("id")
    private UUID  id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("password")
    private String password;
    @JsonProperty("created")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp created;
    @JsonProperty("roles")
    List<Role> roles = new ArrayList<>();
    @JsonProperty("sessionDate")
    private Timestamp sessionDate;

    public void addUserRole(Role role)
    {
        this.roles.add(role);
    }

}

