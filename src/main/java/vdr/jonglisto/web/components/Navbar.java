package vdr.jonglisto.web.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ComponentClassResolver;

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
    protected Messages messages;

    @Property
    private NavPage page;

    private List<NavPage> pages;

    public Navbar() {
        pages = new ArrayList<NavPage>();
        pages.add(new NavPage(messages.get("page_index"), "index"));
        pages.add(new NavPage(messages.get("page_program_now"), "programTime"));
        pages.add(new NavPage(messages.get("page_program_day"), "programDay"));
        pages.add(new NavPage(messages.get("page_program_channel"), "programChannel"));
        pages.add(new NavPage(messages.get("page_timer"), "timer"));
        pages.add(new NavPage(messages.get("page_recordings"), "recordings"));
        pages.add(new NavPage(messages.get("page_search_timer"), "searchTimer"));
        pages.add(new NavPage(messages.get("page_svdrp_console"), "svdrpConsole"));
        pages.add(new NavPage(messages.get("page_channelmap"), "channelMap"));
        pages.add(new NavPage(messages.get("page_channelconfig"), "channelConfig"));
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
}
