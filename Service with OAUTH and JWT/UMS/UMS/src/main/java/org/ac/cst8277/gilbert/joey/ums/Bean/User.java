package org.ac.cst8277.gilbert.joey.ums.Bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import jakarta.persistence.Id;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("created")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp created;

    @JsonProperty("roles")
    private List<Role> roles = new ArrayList<>();

    @JsonProperty("sessionDate")
    private Timestamp sessionDate;

    public void addUserRole(Role role) {
        this.roles.add(role);
    }
}
