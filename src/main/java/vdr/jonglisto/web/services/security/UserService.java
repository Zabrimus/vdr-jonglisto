package vdr.jonglisto.web.services.security;

import java.util.List;
import java.util.Set;

import vdr.jonglisto.web.model.User;

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
    
    public void addIndividualPermission(int id, String permission, String permissionAdd);
    
    public void removeIndividualPersmission(int id, String permission);
}
