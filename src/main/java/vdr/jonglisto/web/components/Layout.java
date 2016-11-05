package vdr.jonglisto.web.components;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Meta;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.tynamo.conversations.services.ConversationManager;

import vdr.jonglisto.lib.model.VDRView;
import vdr.jonglisto.lib.model.VDRView.Type;
import vdr.jonglisto.web.pages.Index;

@Import(module = "bootstrap/collapse", stylesheet = "META-INF/assets/css/jonglisto.less")
@Meta("tapestry.persistence-strategy=conversation")
public class Layout extends BaseComponent {

	enum Mode {
		VIEW, VDR;
	}

	@Inject
	private ConversationManager conversationManager;

	@Inject
	protected ComponentResources componentResources;

	@Persist("session")
	private String conversationId;
	
	@Property
	@Parameter(required = true, defaultPrefix = BindingConstants.LITERAL)
	private String title;

	@Property
	private String pageName;

	@Property
	private VDRView view;

	@Property
	@Inject
	@Symbol(SymbolConstants.APPLICATION_VERSION)
	private String appVersion;

	@SessionAttribute
	@Property
	private Type type;

	public void setupRender() {
		if (!conversationManager.isActiveConversation(conversationId)) {
			conversationId = conversationManager.createConversation(componentResources.getPageName(), 60, true);
		}
	}

	public String getClassForView() {
		return currentVdrView.getDisplayName().equals(view.getDisplayName()) ? "active" : null;
	}

	public List<VDRView> getViews() {
		if (type == null) {
			// set default type and view
			type = Type.View;
			currentVdrView = configuration.getConfiguredViews().values().stream().filter(s -> s.getType() == type)
					.sorted().findFirst().get();
		}

		return configuration.getConfiguredViews().values().stream().filter(s -> s.getType() == type).sorted()
				.collect(Collectors.toList());
	}

	public void onSelectView(String displayName) {
		currentVdrView = configuration.getConfiguredViews().values().stream()
				.filter(s -> s.getDisplayName().equals(displayName)).findFirst().get();
		type = currentVdrView.getType();
	}

	public Object onToggleViewType() {
		if (type == Type.VDR) {
			type = Type.View;
		} else {
			type = Type.VDR;
		}

		currentVdrView = configuration.getConfiguredViews().values().stream().filter(s -> s.getType() == type)
				.findFirst().get();
		return Index.class;
	}

	public boolean showVdrList() {
		return type == Type.VDR;
	}
}
