package vdr.jonglisto.lib.impl;

import java.util.List;
import java.util.Set;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.util.ByteSource;
import org.sql2o.Connection;

import vdr.jonglisto.lib.UserService;
import vdr.jonglisto.lib.model.security.Permission;
import vdr.jonglisto.lib.model.security.User;

public class UserServiceImpl extends ServiceBase implements UserService {

    private RandomNumberGenerator randomNumber;
    
    public UserServiceImpl(RandomNumberGenerator randomNumber) {
        this.randomNumber = randomNumber;
    }
    
    @Override
    public Object getPassword(String username) {
        try (Connection con = configuration.getSql2oHsqldb().open()) {
            return con.createQuery("select password from users where username = :user").addParameter("user", username) //
                    .executeScalar(String.class);
        }
    }

    @Override
    public String getPasswordSalt(String username) {
        try (Connection con = configuration.getSql2oHsqldb().open()) {
            return con.createQuery("select salt from users where username = :user").addParameter("user", username) //
                    .executeScalar(String.class);
        }
    }

    @Override
    public boolean existsUser(String username) {
        try (Connection con = configuration.getSql2oHsqldb().open()) {
            return 1 == con.createQuery("select count(id) from users where username = :username") //
                            .addParameter("username", username) //
                            .executeScalar(Integer.class);
        }
    }

    @Override
    public User createUser(User user) {
        ByteSource salt = randomNumber.nextBytes(64);
        String hashedPasswordBase64 = new Sha256Hash(user.getPassword(), salt, 1024).toBase64();

        try (Connection con = configuration.getSql2oHsqldb().beginTransaction()) {
            con.createQuery(
                    "insert into users (id, username, password, salt) values ((next value for seq_users), :username, :password, :salt)") //
                    .addParameter("username", user.getUsername()) //
                    .addParameter("password", hashedPasswordBase64) //
                    .addParameter("salt", salt.toString()) //
                    .executeUpdate();

            user.setId(con.createQuery("select current value for seq_users from users").executeScalar(Integer.class));
            user.setPassword(null);
            user.setPasswordRepeat(null);

            con.commit();

            return user;
        }
    }

    @Override
    public void deleteUser(int id) {
        try (Connection con = configuration.getSql2oHsqldb().beginTransaction()) {
            con.createQuery("delete from users where id = :id") //
                    .addParameter("id", id) //
                    .executeUpdate();

            con.commit();
        }
    }

    @Override
    public void changePassword(User user) {
        ByteSource salt = randomNumber.nextBytes(64);
        String hashedPasswordBase64 = new Sha256Hash(user.getPassword(), salt, 1024).toBase64();

        try (Connection con = configuration.getSql2oHsqldb().beginTransaction()) {
            con.createQuery("update users set salt = :salt, password = :password where id = :id") //
                    .addParameter("salt", salt.toString()) //
                    .addParameter("password", hashedPasswordBase64) //
                    .addParameter("id", user.getId()) //
                    .executeUpdate();

            con.commit();
        }
    }

    @Override
    public List<User> listUsers() {
        try (Connection con = configuration.getSql2oHsqldb().beginTransaction()) {
            return con.createQuery("select id, username from users").executeAndFetch(User.class);
        }
    }

    @Override
    public void deleteRole(String role) {
        try (Connection con = configuration.getSql2oHsqldb().beginTransaction()) {
            con.createQuery("delete from roles where role = :role") //
                .addParameter("role", role) //
                .executeUpdate();
            
            con.commit();        
        }
    }

    @Override
    public void insertRole(String role, Set<String> permissions) {
        try (Connection con = configuration.getSql2oHsqldb().beginTransaction()) {
            con.createQuery("insert into roles (id, role) values ((next value for seq_roles), :role)") //
                    .addParameter("role", role) //
                    .executeUpdate();
            
            Integer roleId = con.createQuery("select current value for seq_roles from roles").executeScalar(Integer.class);
            
            permissions.stream().forEach(s -> {
                con.createQuery("insert into roles_permissions (id, ref_role_id, ref_permission_id) values ((next value for seq_roles_permissions), :roleid, (select id from permissions where permission = :permission))") //
                    .addParameter("roleid", roleId) //
                    .addParameter("permission", s) //
                    .executeUpdate();
            });
            
            con.commit();
        }
    }

    @Override
    public void addPermissionsToRole(String role, Set<String> permissions) {
        try (Connection con = configuration.getSql2oHsqldb().beginTransaction()) {
            Integer roleId = con.createQuery("select id from roles where role = :role").executeScalar(Integer.class);

            permissions.stream().forEach(s -> {
                con.createQuery("insert into roles_permissions (id, ref_role_id, ref_permission_id) values ((next value for seq_roles_permissions), :roleid, (select id from permissions where permission = :permission))") //
                    .addParameter("roleid", roleId) //
                    .addParameter("permission", s) //
                    .executeUpdate();
            });
            
            con.commit();
        }
    }

    @Override
    public void removePermissionsFromRole(String role, Set<String> permissions) {
        try (Connection con = configuration.getSql2oHsqldb().beginTransaction()) {
            Integer roleId = con.createQuery("select id from roles where role = :role").executeScalar(Integer.class);

            permissions.stream().forEach(s -> {
                con.createQuery("delete from roles_permissions where ref_role_id = :roleid and ref_permission_id = (select id from permissions where permission = :permission)") //
                    .addParameter("roleid", roleId) //                    
                    .addParameter("permission", s) //
                    .executeUpdate();
            });
            
            con.commit();
        }
    }

    @Override
    public void addUserToRoles(int id, Set<String> roles) {
        try (Connection con = configuration.getSql2oHsqldb().beginTransaction()) {
            roles.stream().forEach(s -> {
                con.createQuery("insert into user_roles (id, ref_role_id, ref_user_id) values ((next value for seq_user_roles), (select id from roles where role = :role), :userid)")
                    .addParameter("userid", id) //
                    .addParameter("role", s) //
                    .executeUpdate();            
            });
            
            con.commit();
        }
    }

    @Override
    public void removeUserFromRoles(int id, Set<String> roles) {
        try (Connection con = configuration.getSql2oHsqldb().beginTransaction()) {
            roles.stream().forEach(s -> {
                con.createQuery("delete from user_roles where ref_user_ud = :userid and ref_role_id = (select id from roles where role = :role)")
                    .addParameter("userid", id) //
                    .addParameter("role", s) //
                    .executeUpdate();            
            });
            
            con.commit();
        }
    }

    @Override
    public void addIndividualPermission(String username, String permission, String messageKey) {
        try (Connection con = configuration.getSql2oHsqldb().beginTransaction()) {
            // add permission, if not exists
            addOrUpdatePermission(permission, messageKey, con);
            
            if (!existsUserPermission(username, permission, con)) {
                con.createQuery("insert into user_permissions (id, ref_user_id, ref_permission_id) values ((next value for seq_user_roles), (select id from users where username = :username), (select id from permissions where permission = :permission))") //
                    .addParameter("username", username) //
                    .addParameter("permission", permission) //
                    .executeUpdate();
            }
            
            con.commit();
        }
    }
    
    @Override
    public void addIndividualPermission(int id, String permission, String messageKey) {
        try (Connection con = configuration.getSql2oHsqldb().beginTransaction()) {
            addOrUpdatePermission(permission, messageKey, con);
            
            if (!existsUserPermission(id, permission, con)) {
                con.createQuery("insert into user_permissions (id, ref_user_id, ref_permission_id) values ((next value for seq_user_roles), :userid, (select id from permissions where permission = :permission))") //
                    .addParameter("userid", id) //
                    .addParameter("permission", permission) //
                    .executeUpdate();
            }
            
            con.commit();
        }
    }

    @Override
    public void removeIndividualPersmission(int id, String permission) {
        try (Connection con = configuration.getSql2oHsqldb().beginTransaction()) {
            con.createQuery("delete from user_permissions where ref_user_id = :userid and ref_permission_id = (select id from permissions where permission = :permission)") //
                .addParameter("userid", id) //
                .addParameter("permissision", permission) //
                .executeUpdate();
           
            con.commit();
        }
    }

    @Override
    public List<User> getAllUsers() {
        try (Connection con = configuration.getSql2oHsqldb().open()) {
            return con.createQuery("select id, username from users").executeAndFetch(User.class);
        }
    }

    @Override
    public List<Permission> getAllPagePermissions() {
        try (Connection con = configuration.getSql2oHsqldb().open()) {
            return con.createQuery("select id, permission, message_key as messageKey from permissions where permission like 'page:%' or permission = 'page'").executeAndFetch(Permission.class);
        }
    }

    @Override
    public List<Permission> getAllViewPermissions() {
        try (Connection con = configuration.getSql2oHsqldb().open()) {
            return con.createQuery("select id, permission, message_key as messageKey from permissions where permission like 'view:%' or permission='view'").executeAndFetch(Permission.class);
        }
    }
    
    @Override
    public List<Permission> getAllChannelGroupPermissions() {
        try (Connection con = configuration.getSql2oHsqldb().open()) {
            return con.createQuery("select id, permission, message_key as messageKey from permissions where permission like 'channel:group:%' or permission = 'channel:group'").executeAndFetch(Permission.class);
        }
    }
    
    @Override
    public void addPermission(String permission, String messageKey) {
        try (Connection con = configuration.getSql2oHsqldb().beginTransaction()) {
            addOrUpdatePermission(permission, messageKey, con);
            con.commit();
        }
    }
    
    private void addOrUpdatePermission(String permission, String messageKey, Connection con) {
        con.createQuery("merge into permissions as c " + // 
                        "using(values(:permission, :messagekey)) " + // 
                        "as vals(permission, message_key) on c.permission = vals.permission " + //
                        "when not matched then insert values (next value for seq_permissions), vals.permission, vals.message_key")
            .addParameter("permission", permission) //
            .addParameter("messagekey", messageKey) //
            .executeUpdate();
    }
        
    private boolean existsUserPermission(int userid, String permission, Connection con) {
        return con.createQuery("select count(*) from user_permissions where ref_user_id = :userid and ref_permission_id = (select id from permissions where permission = :permission)") //
                .addParameter("userid", userid) //
                .addParameter("permission", permission) //
                .executeScalar(Integer.class) > 0;
    }
    
    private boolean existsUserPermission(String username, String permission, Connection con) {
        return con.createQuery("select count(*) from user_permissions where ref_user_id = (select id from users where username = :username) and ref_permission_id = (select id from permissions where permission = :permission)") //
                .addParameter("username", username) //
                .addParameter("permission", permission) //
                .executeScalar(Integer.class) > 0;
    }
}
