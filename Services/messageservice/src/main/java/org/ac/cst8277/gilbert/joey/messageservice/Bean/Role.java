package org.ac.cst8277.gilbert.joey.messageservice.Bean;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data

public class Role {

    @Id
    @JsonProperty("roleId")
    private UUID roleid;

    @JsonProperty("rolename")
    private String rolename;

    @JsonProperty("description")
    private String description;
}