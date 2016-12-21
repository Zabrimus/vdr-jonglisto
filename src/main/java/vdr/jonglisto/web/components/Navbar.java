package vdr.jonglisto.web.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.annotations.Property;
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

    @Property
    private NavPage page;

    private List<NavPage> pages;

    public Navbar() {
        pages = new ArrayList<NavPage>();
        pages.add(new NavPage("Startseite", "index"));
        pages.add(new NavPage("Programm (jetzt)", "programTime"));
        pages.add(new NavPage("Programm (Tag)", "programDay"));
        pages.add(new NavPage("Programm (Kanal)", "programChannel"));
        pages.add(new NavPage("Timer", "timer"));
        pages.add(new NavPage("Aufnahmen", "recordings"));
        pages.add(new NavPage("Suchtimer", "searchTimer"));
        pages.add(new NavPage("SVDRP Konsole", "svdrpConsole"));
        pages.add(new NavPage("Channelmap", "channelMap"));
        pages.add(new NavPage("Kanalkonfiguration", "channelConfig"));
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
