package vdr.jonglisto.web.services.security;

import java.util.List;

import vdr.jonglisto.web.model.User;

public interface UserService {

    public User createUser(User user);
    
    public void deleteUser(int id);
    
    public void updateUser(User user);
    
    public List<User> listUsers();
    
    Object getPassword(String username);

    String getPasswordSalt(String username);

}
