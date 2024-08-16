package org.ac.cst8277.gilbert.joey.ums.Service;

import org.ac.cst8277.gilbert.joey.ums.Bean.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.ac.cst8277.gilbert.joey.ums.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private UserRepo userrepo;
    @Autowired
    public UserService(@Qualifier("userDao") UserRepo userrepo) {
        this.userrepo = userrepo;
    }

    public Mono<Map<String, Object>>getAllUsers() {
        List<User> users = userrepo.getAllUsers();
        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        return Mono.just(result);
    }
    public Mono<User> getUserById(UUID id) {

        return Mono.justOrEmpty(userrepo.findById(id).stream().findFirst());

    }
    public Mono<Map<String, Object>>adduser(User user) {
        System.out.println("Creating message with content: " + user.getName());
        return Mono.fromRunnable(() -> {
            try {
                System.out.println("About to call DAO method...");
                userrepo.createUser(user);
                System.out.println("DAO method executed successfully.");
            } catch (Exception e) {
                System.out.println("Exception in DAO method: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    public Mono<Void> deleteUser(UUID id) {
        return Mono.fromRunnable(() -> userrepo.deleteuser(id));
    }
}