package vdr.jonglisto.web.services.security.impl;

import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.sql2o.Connection;

import vdr.jonglisto.lib.ConfigurationService;
import vdr.jonglisto.web.model.User;
import vdr.jonglisto.web.services.security.UserService;

public class UserServiceImpl implements UserService {

    @Inject
    private ConfigurationService config;
    
    @Override
    public Object getPassword(String username) {
        try (Connection con = config.getSql2oHsqldb().open()) {
            return con.createQuery("select password from users where username = :user").addParameter("user", username) //
                    .executeScalar(String.class);
        }
    }

    @Override
    public String getPasswordSalt(String username) {
        try (Connection con = config.getSql2oHsqldb().open()) {
            return con.createQuery("select salt from users where username = :user").addParameter("user", username) //
                    .executeScalar(String.class);
        }
    }

    @Override
    public User createUser(User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteUser(int id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateUser(User user) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<User> listUsers() {
        // TODO Auto-generated method stub
        return null;
    }
}
