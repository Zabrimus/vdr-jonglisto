package vdr.jonglisto.web.pages;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.Response;
import org.tynamo.security.pages.Login;
import org.tynamo.security.services.SecurityService;

public class Signin extends Login {

    @Property
    private String username;

    @Property
    private String password;

    @Inject
    private Response response;

    @Inject
    private RequestGlobals requestGlobals;

    @Inject
    private SecurityService securityService;

    @Inject
    private AlertManager alertManager;

    @OnEvent(EventConstants.SUCCESS)
    public Object submit() {
        Subject currentUser = securityService.getSubject();

        if (currentUser == null) {
            throw new IllegalStateException("Subject can`t be null");
        }

        UsernamePasswordToken token = new UsernamePasswordToken(username, password);

        try {
            currentUser.login(token);
        } catch (Exception e) {
            alertManager.error("Authentication Error");
            return null;
        }

        return "Index";
    }
}
