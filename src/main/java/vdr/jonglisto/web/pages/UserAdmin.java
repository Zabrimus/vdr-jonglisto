package vdr.jonglisto.web.pages;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;
import org.tynamo.security.services.SecurityService;

import vdr.jonglisto.lib.UserService;
import vdr.jonglisto.lib.model.security.Permission;
import vdr.jonglisto.lib.model.security.User;

@Import(library = { "webjars:jquery-ui:$version/jquery-ui.js" }, stylesheet = {"webjars:jquery-ui:$version/jquery-ui.css" })
public class UserAdmin {

    @Inject
    private Logger log;

    @Inject
    protected AjaxResponseRenderer ajaxResponseRenderer;

    @Inject
    protected Messages messages;

    @Inject
    private SecurityService securityService;

    @Inject
    protected JavaScriptSupport javaScriptSupport;

    @Inject
    private UserService userService;  

    @InjectComponent
    private Zone passwordChangeZone;

    @InjectComponent
    private Form passwordChangeForm;

    @InjectComponent
    private Zone newUserZone;

    @InjectComponent
    private Form newUserForm;

    @InjectComponent
    private Zone permissionZone;

    @InjectComponent
    private Form permissionForm;

    @Property
    private List<User> users;
    
    @Persist
    @Property
    private User user;
    
    @Property
    private String perm;

    @Property
    private Permission viewPerm;

    public void beginRender() {
        users = userService.getAllUsers();
    }
    
    public void afterRender() {
        javaScriptSupport.require("accordion");
    }
    
    public void onChangePassword(int userid) {
        log.info("CHANGE PASSWORD");
        user = userService.getAllUsers().stream().filter(s -> s.getId() == userid).findFirst().get();

        ajaxResponseRenderer.addCallback(makeScriptToShowModal("changePassword"));
        ajaxResponseRenderer.addRender(passwordChangeZone);
    }
    
    public Object onDeleteUser(int userid) {
        userService.deleteUser(userid);
        
        return this;
    }

    public void onChangePermission(int userid) {
        user = userService.getAllUsers().stream().filter(s -> s.getId() == userid).findFirst().get();

        ajaxResponseRenderer.addCallback(makeScriptToShowModal("editPermissions"));
        ajaxResponseRenderer.addRender(permissionZone);
    }
    
    public void onDeletePermissionPage(String page) {
        log.info("DELETE PAGE PERMISSION: " + page);
    }

    public void onDeletePermissionView(int id) {
        log.info("DELETE VIEW PERMISSION: " + id);
    }
    
    public void onDeletePermissionChannelGroup(int id) {
        log.info("DELETE CHANNEL GROUP PERMISSION: " + id);
    }

    public void onAddUser() {
        user = new User();
        
        ajaxResponseRenderer.addCallback(makeScriptToShowModal("newUser"));
        ajaxResponseRenderer.addRender(newUserZone);
    }

    public void onValidateFromPasswordChangeForm() {
        if ((user.getPassword() == null) || !user.getPassword().equals(user.getPasswordRepeat())) {
            passwordChangeForm.recordError("Passwords do not match!");
            ajaxResponseRenderer.addRender(passwordChangeZone);
            ajaxResponseRenderer.addCallback(makeScriptToShowModal("changePassword"));
        }        
    }
    
    public void onSuccessFromPasswordChangeForm() {
        userService.changePassword(user);
        ajaxResponseRenderer.addCallback(makeScriptToHideModal("changePassword"));
    }

    public void onCancelChangePassword() {
        ajaxResponseRenderer.addCallback(makeScriptToHideModal("changePassword"));
    }

    public void onValidateFromNewUserForm() {
        if (userService.existsUser(user.getUsername())) {
            passwordChangeForm.recordError("User already exists!");
            ajaxResponseRenderer.addRender(newUserZone);
            ajaxResponseRenderer.addCallback(makeScriptToShowModal("newUser"));
        }

        if ((user.getPassword() == null) || !user.getPassword().equals(user.getPasswordRepeat())) {
            passwordChangeForm.recordError("Passwords do not match!");
            ajaxResponseRenderer.addRender(newUserZone);
            ajaxResponseRenderer.addCallback(makeScriptToShowModal("newUser"));
        }           
    }
    
    public Object onSuccessFromNewUserForm() {
        userService.createUser(user);
        
        return this;
    }

    public void onCancelNewUser() {
        ajaxResponseRenderer.addCallback(makeScriptToHideModal("newUser"));
    }

    public Object onSuccessFromPermissionForm() {
        System.out.println("SAVE PERMISSION");
        return this;
    }

    public void onCancelPermission() {
        ajaxResponseRenderer.addCallback(makeScriptToHideModal("editPermissions"));
    }

    public Set<String> getAllAvailablePagePermissions() {
        return userService.getAllPagePermissions() // 
                .stream() //
                .map(s -> s.getMessageKey()) //
                .collect(Collectors.toCollection(TreeSet::new));                
    }

    public Set<Permission> getAllAvailableViewPermissions() {
        return userService.getAllViewPermissions() // 
                .stream() //
                .map(s -> {
                    int idx = s.getPermission().indexOf("view:vdr:");
                    if (idx != -1) {
                        s.setPart(s.getPermission().substring("view:vdr:".length()));
                    } else {
                        s.setPart("*");
                    }
                    return s;
                }) 
                .collect(Collectors.toCollection(TreeSet::new));
    }
    
    public Set<Permission> getAllAvailableChannelGroupPermissions() {
        return userService.getAllChannelGroupPermissions() // 
                .stream() //
                .map(s -> {
                    int idx = s.getPermission().indexOf("channel:group:");
                    if (idx != -1) {
                        s.setPart(s.getPermission().substring("channel:group:".length()));
                    } else {
                        s.setPart("*");
                    }
                    return s;
                }) 
                .collect(Collectors.toCollection(TreeSet::new));
    }
    
    public Set<String> getAllPagePermissions() {
        Subject subject = createLocalSubject();
        
        return userService.getAllPagePermissions() // 
                .stream() //
                .filter(s -> subject.isPermitted(s.getPermission())) //
                .map(s -> s.getMessageKey()) //
                .collect(Collectors.toSet());
    }

    public Set<Permission> getAllViewPermissions() {
        Subject subject = createLocalSubject();
        
        return userService.getAllViewPermissions() // 
                .stream() //
                .filter(s -> subject.isPermitted(s.getPermission())) //
                .map(s -> {
                    int idx = s.getPermission().indexOf("view:vdr:");
                    if (idx != -1) {
                        s.setPart(s.getPermission().substring("view:vdr:".length()));
                    } else {
                        s.setPart("*");
                    }
                    return s;
                }) 
                .collect(Collectors.toSet());
    }

    public Set<Permission> getAllChannelGroupPermissions() {
        Subject subject = createLocalSubject();
        
        return userService.getAllChannelGroupPermissions() // 
                .stream() //
                .filter(s -> subject.isPermitted(s.getPermission())) //
                .map(s -> {
                    int idx = s.getPermission().indexOf("channel:group:");
                    if (idx != -1) {
                        s.setPart(s.getPermission().substring("channel:group:".length()));
                    } else {
                        s.setPart("*");
                    }
                    return s;
                }) 
                .collect(Collectors.toSet());
    }
    
    public String getPermMessage() {
        return messages.get(perm);
    }
    
    public String getViewPermMessage() {
        return messages.get(viewPerm.getMessageKey());
    }
    
    public boolean isNotAdmin() {
        return !"admin".equals(user.getUsername());
    }
    
    public JavaScriptCallback makeScriptToShowModal(String name) {
        return new JavaScriptCallback() {
            public void run(JavaScriptSupport javascriptSupport) {
                javaScriptSupport.require("dialogmodal").invoke("activate").with(name, new JSONObject());
            }
        };
    }

    public JavaScriptCallback makeScriptToHideModal(String name) {
        return new JavaScriptCallback() {
            public void run(JavaScriptSupport javascriptSupport) {
                javaScriptSupport.require("dialogmodal").invoke("hide").with(name);
            }
        };
    }
    
    private Subject createLocalSubject() {
        // create a new local Subject for the desired user
        Object userIdentity = user.getUsername(); 
        String realmName = "myRealm";
        PrincipalCollection principals = new SimplePrincipalCollection(userIdentity, realmName);
        Subject subject = new Subject.Builder().principals(principals).buildSubject();
        
        return subject;
    }
}
