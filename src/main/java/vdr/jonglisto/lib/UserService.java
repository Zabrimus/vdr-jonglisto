package vdr.jonglisto.lib;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import vdr.jonglisto.lib.model.security.Permission;
import vdr.jonglisto.lib.model.security.User;

public interface UserService {

    public boolean existsUser(String username);
    
    public User createUser(User user);
    
    public void deleteUser(int id);
    
    public void changePassword(User user);
    
    public List<User> listUsers();
    
    public Object getPassword(String username);

    public String getPasswordSalt(String username);

    public void deleteRole(String role);
    
    public void insertRole(String role, Set<String> permissions);
    
    public void addPermissionsToRole(String role, Set<String> permissions);
    
    public void removePermissionsFromRole(String role, Set<String> permissions);
    
    public void addUserToRoles(int id, Set<String> roles);
    
    public void removeUserFromRoles(int id, Set<String> roles);

    public void addIndividualPermission(String username, String permission, String messageKey);
    
    public void addIndividualPermission(int id, String permission, String messageKey);
    
    public void removeIndividualPersmission(int id, String permission);

    public List<User> getAllUsers();
    
    public List<Permission> getAllPagePermissions();

    public List<Permission> getAllViewPermissions();
    
    public List<Permission> getAllChannelGroupPermissions();
    
    void addPermission(String permission, String messageKey);
}
