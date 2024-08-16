package org.ac.cst8277.gilbert.joey.ums.Bean;

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
    private UUID roleId;
    private String rolename;
    private String description;


}
