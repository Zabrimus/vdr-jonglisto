package vdr.jonglisto.web.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.subject.Subject;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.tynamo.security.services.SecurityService;

public class Navbar extends BaseComponent {

    public class NavPage {

        private String name;
        private String page;
        private String shortPage;

        public NavPage(String name, String page) {
            this.name = name;
            this.page = page;

            this.shortPage = getShortName(page);
        }

        public String getName() {
            return name;
        }

        public String getPage() {
            return page;
        }

        public String getShortPage() {
            return shortPage;
        }
    }

    @Inject
    private ComponentClassResolver classResolver;

    @Inject
    private SecurityService securityService;

    @Inject
    protected Messages messages;

    @Property
    private NavPage page;

    private List<NavPage> pages;

    public Navbar() {
        pages = new ArrayList<NavPage>();

        if (configuration.isSuccessfullyInitialized()) {
            // smoke test
            try {
                securityService.isGuest();
            } catch (UnavailableSecurityManagerException e) {
                // do nothing, because this could happen at startup
                return;
            }                
            
            if (securityService.hasPermission("page:index")) {
                pages.add(new NavPage(messages.get("page_index"), "index"));
            }
            
            if (securityService.hasPermission("page:programtime")) {
                pages.add(new NavPage(messages.get("page_program_now"), "programTime"));
            }
            
            if (securityService.hasPermission("page:programday")) {
                pages.add(new NavPage(messages.get("page_program_day"), "programDay"));
            }
            
            if (securityService.hasPermission("page:programchannel")) {
                pages.add(new NavPage(messages.get("page_program_channel"), "programChannel"));
            }
            
            if (securityService.hasPermission("page:timer")) {
                pages.add(new NavPage(messages.get("page_timer"), "timer"));
            }
            
            if (securityService.hasPermission("page:recordings")) {
                pages.add(new NavPage(messages.get("page_recordings"), "recordings"));
            }

            if (securityService.hasPermission("page:searchtimer") && configuration.isUseEpgd()) {
                pages.add(new NavPage(messages.get("page_search_timer"), "searchTimer"));
            }

            if (securityService.hasPermission("page:svdrpconsole")) {
                pages.add(new NavPage(messages.get("page_svdrp_console"), "svdrpConsole"));
            }

            if (securityService.hasPermission("page:channelmap") && configuration.isUseEpgd()) {
                pages.add(new NavPage(messages.get("page_channelmap"), "channelMap"));
            }

            if (securityService.hasPermission("page:channelconfig")) {
                pages.add(new NavPage(messages.get("page_channelconfig"), "channelConfig"));
            }
            
            if (securityService.hasPermission("page:setup")) {
                pages.add(new NavPage(messages.get("page_setup"), "setup"));
            }
            
            if (securityService.hasPermission("page:useradmin")) {
                pages.add(new NavPage(messages.get("page_useradmin"), "userAdmin"));
            }
        }        
    }

    public List<NavPage> getPageNames() {
        return pages;
    }

    public String getClassForPage() {
        return resources.getPageName().equalsIgnoreCase(page.shortPage) ? "active" : null;
    }

    private String getShortName(String pageName) {
        return classResolver.resolvePageClassNameToPageName(classResolver.resolvePageNameToClassName(pageName));
    }

    public boolean isVdrSelected() {
        return currentVdrView != null;
    }

    public boolean isDeveloperMode() {
        return configuration.isDeveloperMode();
    }

    public Object onActionFromLogout() {
        Subject currentUser = securityService.getSubject();

        if (currentUser.isAuthenticated()) {
            currentUser.logout();
        }

        return "Index";
    }
}
