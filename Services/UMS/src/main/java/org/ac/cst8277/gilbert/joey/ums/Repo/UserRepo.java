package org.ac.cst8277.gilbert.joey.ums.Repo;
import org.ac.cst8277.gilbert.joey.ums.Bean.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepo {

    List<User> findById(UUID id);
    List<User> getAllUsers();
    void createUser(User user);
    void deleteuser(UUID id);
}
