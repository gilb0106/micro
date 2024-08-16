package org.ac.cst8277.gilbert.joey.messageservice.Bean;
import lombok.*;

import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Subscription {
    private UUID producerId;
    private UUID subscriberId;

}
