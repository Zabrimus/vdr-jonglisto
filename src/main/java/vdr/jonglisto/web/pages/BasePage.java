package vdr.jonglisto.web.pages;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentSource;

public class BasePage {
    @Inject
    private ComponentClassResolver componentClassResolver;
    
    @Inject
    private ComponentSource componentSource;

    protected void discardAllPagePersistent() {
        componentClassResolver.getPageNames().stream() //
            .filter(s -> !s.startsWith("core/")) //
            .forEach(s -> {
                componentSource.getPage(s).getComponentResources().discardPersistentFieldChanges();
            });
    }
}
