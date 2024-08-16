package org.ac.cst8277.gilbert.joey.ums.Service;

import org.ac.cst8277.gilbert.joey.ums.Repo.RoleRepo;
import org.ac.cst8277.gilbert.joey.ums.Bean.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoleService {
    @Qualifier("roleDao")
    @Autowired
    private RoleRepo rolerepo;

    public RoleService(@Qualifier("roleDao") RoleRepo rolerepo) {
        this.rolerepo = rolerepo;
    }
    public Mono<Map<String, Object>> getAllRoles() {
        List<Role> roles = rolerepo.getAllRoles();
        Map<String, Object> result = new HashMap<>();
        result.put("roles", roles);
        return Mono.just(result);
    }
}