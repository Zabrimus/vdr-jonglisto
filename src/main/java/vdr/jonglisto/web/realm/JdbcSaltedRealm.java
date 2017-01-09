package vdr.jonglisto.web.realm;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SaltedAuthenticationInfo;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.SimpleByteSource;
import org.sql2o.Connection;

import vdr.jonglisto.lib.ConfigurationService;
import vdr.jonglisto.web.services.security.UserService;

public class JdbcSaltedRealm extends AuthorizingRealm {

    private ConfigurationService configService;
    
    private UserService userService;

    public JdbcSaltedRealm(UserService userService, ConfigurationService configService) {
        super(new org.apache.shiro.cache.MemoryConstrainedCacheManager());
        this.userService = userService;
        this.configService = configService;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        
        try (Connection con = configService.getSql2oHsqldb().open()) {
            // read individual and role permissions
            String sql = "SELECT CONCAT(p.PERMISSION, casewhen( up.PERMISSION_ADD IS NOT NULL, CONCAT( ':', up.PERMISSION_ADD ), '' )) " + //
                          "FROM users u, user_permissions up, permissions p " + //
                          "WHERE  u.USERNAME = :username " + //
                          "AND   up.REF_USER_ID = u.ID " + //
                          "AND   up.REF_PERMISSION_ID = p.ID " + //
                          "UNION " + //
                          "SELECT p.PERMISSION " + //
                          "FROM users u, ROLES r, USER_ROLES ur, ROLES_PERMISSIONS rp, PERMISSIONS p " + //
                          "WHERE u.username = :username " + //
                          "AND   ur.REF_ROLE_ID = r.ID " + //
                          "AND   ur.REF_USER_ID = u.id " + //
                          "AND   rp.REF_PERMISSION_ID = p.ID " + //
                          "AND   rp.REF_ROLE_ID = r.ID";
            
            Set<String> permissions = con.createQuery(sql) //
                                        .addParameter("username", principals.getPrimaryPrincipal()) //
                                        .executeAndFetch(String.class) //
                                        .stream() //
                                        .collect(Collectors.toSet());

            info.setStringPermissions(permissions);
        }
        
        return info;
    }

    @Override
    protected SaltedAuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String principal = (String) token.getPrincipal();

        return new SimpleAuthenticationInfo(principal, userService.getPassword(principal),
                new SimpleByteSource(Base64.decode(userService.getPasswordSalt(principal))), "JdbcSaltedRealm");
    }
}
