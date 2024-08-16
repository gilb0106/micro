package org.ac.cst8277.gilbert.joey.ums.DAO;

import org.ac.cst8277.gilbert.joey.ums.DataConverter.BinaryConverter;
import org.ac.cst8277.gilbert.joey.ums.Repo.RoleRepo;
import org.ac.cst8277.gilbert.joey.ums.Bean.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public class RoleDao implements RoleRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Role> getAllRoles() {
        String sql = "SELECT id, name FROM roles";
        List<Role> roles = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Role r = new Role();
                    r.setRoleId(BinaryConverter.convertBytesToUUID(rs.getBytes("id"))); // Convert role ID to UUID from ResultSet
                    r.setRolename(rs.getString("name"));
                    return r;
                });

        return roles;
    }
}
