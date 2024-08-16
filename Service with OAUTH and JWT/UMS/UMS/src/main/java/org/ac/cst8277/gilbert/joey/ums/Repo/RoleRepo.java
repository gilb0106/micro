package org.ac.cst8277.gilbert.joey.ums.Repo;

import org.ac.cst8277.gilbert.joey.ums.Bean.Role;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface RoleRepo{

    List<Role> getAllRoles();
}
