package vdr.jonglisto.web.mixins;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.dom.Visitor;

@MixinAfter
public class EnableGridZone {

	@InjectContainer
	private Grid grid;

	private Element element;

	void beginRender(MarkupWriter writer) {
		element = writer.getElement();
	}

	void  afterRender() {
		if (grid.getDataSource().getAvailableRows() == 0) {
			return;
		}
		
		element.visit(new Visitor() {
			public void visit(Element element) {
				if ("a".equals(element.getName())) {
					element.attribute("data-update-zone", "recZone");
				}
			}
		});
	}
}
