package org.ac.cst8277.gilbert.joey.ums.DAO;

import org.ac.cst8277.gilbert.joey.ums.DataConverter.BinaryConverter;
import org.ac.cst8277.gilbert.joey.ums.Repo.UserRepo;
import org.ac.cst8277.gilbert.joey.ums.Bean.Role;
import org.ac.cst8277.gilbert.joey.ums.Bean.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.*;

@Repository
public class UserDao implements UserRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public List<User> findById(UUID id) {
        String sql = "SELECT u.id, u.name, u.password, u.email, u.created, " +
                "GROUP_CONCAT(DISTINCT r.id ORDER BY r.name SEPARATOR ', ') as roleids, " +
                "GROUP_CONCAT(DISTINCT r.name ORDER BY r.name SEPARATOR ', ') as rolenames, " +
                "GROUP_CONCAT(DISTINCT r.description ORDER BY r.name SEPARATOR ', ') as roledescriptions, " +
                "lv.in AS last_session " +
                "FROM users u " +
                "LEFT JOIN users_has_roles uhr ON u.id = uhr.users_id " +
                "LEFT JOIN roles r ON uhr.roles_id = r.id " +
                "LEFT JOIN last_visit lv ON u.last_visit_id = lv.id " +
                "WHERE u.id = ? " +
                "GROUP BY u.id, u.name, u.password, lv.in";
        return jdbcTemplate.query(
                sql,
                new Object[]{BinaryConverter.convertUUIDToBytes(id)},
                (rs, rowNum) -> {
                    User u = new User();
                    u.setId(BinaryConverter.convertBytesToUUID(rs.getBytes("id")));
                    u.setName(rs.getString("name"));
                    u.setPassword(rs.getString("password"));
                    int createdInt = rs.getInt("created");
                    long createdLong = (long) createdInt * 1000; // convert seconds to milliseconds
                    Timestamp createdTimestamp = new Timestamp(createdLong);
                    u.setCreated(createdTimestamp);
                    int lastSessionInt = rs.getInt("last_session");
                    long lastSessionLong = (long) lastSessionInt * 1000; // convert seconds to milliseconds
                    Timestamp lastSessionTimestamp = new Timestamp(lastSessionLong);
                    u.setSessionDate(lastSessionTimestamp);
                    u.setEmail(rs.getString("email"));
                    String roleIdsStr = rs.getString("roleids");
                    String roleNamesStr = rs.getString("rolenames");
                    String roleDescriptionsStr = rs.getString("roledescriptions");

                    List<Role> roles = new ArrayList<>();
                    if (roleIdsStr != null && roleNamesStr != null && roleDescriptionsStr != null) {
                        String[] roleIds = roleIdsStr.split(", ");
                        String[] roleNames = roleNamesStr.split(", ");
                        String[] roleDescriptions = roleDescriptionsStr.split(", ");

                        for (int i = 0; i < roleIds.length; i++) {
                            Role role = new Role();
                            role.setRoleId(BinaryConverter.convertBytesToUUID(roleIds[i].getBytes())); // Convert role ID to UUID
                            role.setRolename(roleNames[i]);
                            role.setDescription(roleDescriptions[i]);
                            roles.add(role);
                        }
                    }
                    u.setRoles(roles);
                    return u;
                });
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT u.id, u.name, u.password, u.created, u.email, " +
                "GROUP_CONCAT(DISTINCT r.id ORDER BY r.name SEPARATOR ', ') as roleids, " +
                "GROUP_CONCAT(DISTINCT r.name ORDER BY r.name SEPARATOR ', ') as rolenames, " +
                "GROUP_CONCAT(DISTINCT r.description ORDER BY r.name SEPARATOR ', ') as roledescriptions, " +
                "lv.in AS last_session " +
                "FROM users u " +
                "LEFT JOIN users_has_roles uhr ON u.id = uhr.users_id " +
                "LEFT JOIN roles r ON uhr.roles_id = r.id " +
                "LEFT JOIN last_visit lv ON u.last_visit_id = lv.id " +
                "GROUP BY u.id, u.name, u.password, lv.in";

        List<User> users = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    User u = new User();
                    u.setId(BinaryConverter.convertBytesToUUID(rs.getBytes("id")));
                    u.setName(rs.getString("name"));
                    u.setPassword(rs.getString("password"));
                    int createdInt = rs.getInt("created");
                    long createdLong = (long) createdInt * 1000; // convert seconds to milliseconds
                    Timestamp createdTimestamp = new Timestamp(createdLong);
                    u.setCreated(createdTimestamp);
                    int lastSessionInt = rs.getInt("last_session");
                    long lastSessionLong = (long) lastSessionInt * 1000; // convert seconds to milliseconds
                    Timestamp lastSessionTimestamp = new Timestamp(lastSessionLong);
                    u.setSessionDate(lastSessionTimestamp);
                    u.setEmail(rs.getString("email"));

                    // Debugging logs
                    String roleIdsStr = rs.getString("roleids");
                    String roleNamesStr = rs.getString("rolenames");
                    String roleDescriptionsStr = rs.getString("roledescriptions");


                    // Split the strings
                    String[] roleIds = roleIdsStr.split(", ");
                    String[] roleNames = roleNamesStr.split(", ");
                    String[] roleDescriptions = roleDescriptionsStr.split(", ");

                    List<Role> roles = new ArrayList<>();
                    for (int i = 0; i < roleIds.length; i++) {
                        Role role = new Role();
                        role.setRoleId(BinaryConverter.convertBytesToUUID(roleIds[i].getBytes())); // Convert role ID to UUID
                        role.setRolename(roleNames[i]);
                        role.setDescription(roleDescriptions[i]);
                        roles.add(role);
                    }

                    u.setRoles(roles);
                    return u;
                });

        return users;
    }


    public void createUser(User user) {
        String sql = "INSERT INTO users (id, name, email, password, created) VALUES (?, ?, ?, ?, ?)";
        Timestamp created = user.getCreated();
        long millis = created.getTime();
        int intMillis = (int) millis;
        System.out.println(user.getId());
        UUID randomUUID = UUID.randomUUID();
        jdbcTemplate.update(sql, BinaryConverter.convertUUIDToBytes(randomUUID), user.getName(),
                user.getEmail(), user.getPassword(), intMillis);

        for (Role role : user.getRoles()) {
            String roleName = role.getRolename();
            System.out.println("Role Name: " + roleName);
            String roleId = getRoleIdByRoleName(roleName);
            System.out.println("Role ID: " + roleId);
            if (roleId == null) {
                throw new IllegalArgumentException("Role not found for roleName: " + roleName);
            }

            String sqlAssignRole = "INSERT INTO users_has_roles (users_id, roles_id) VALUES (?, ?)";
            jdbcTemplate.update(sqlAssignRole, BinaryConverter.convertUUIDToBytes(randomUUID),
                    BinaryConverter.convertUUIDToBytes(UUID.fromString(roleId)));
        }
    }
    public void deleteuser(UUID id) {
        // Delete from users_has_roles first
        String sqlDeleteRoles = "DELETE FROM users_has_roles WHERE users_id = ?";
        jdbcTemplate.update(sqlDeleteRoles, BinaryConverter.convertUUIDToBytes(id));
        // Then delete from users
        String sqlDeleteUser = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sqlDeleteUser, BinaryConverter.convertUUIDToBytes(id));
    }
    private String getRoleIdByRoleName(String roleName) {
        String sql = "SELECT id FROM roles WHERE name = ?";
        try {
            System.out.println("Querying role ID for roleName: " + roleName);
            byte[] roleIdBytes = jdbcTemplate.queryForObject(sql, new Object[]{roleName}, byte[].class);
            return BinaryConverter.convertBytesToUUID(roleIdBytes).toString();
        } catch (Exception e) {
            System.err.println("Error retrieving role ID for roleName: " + roleName);
            e.printStackTrace();
            return null;
        }
    }
}
