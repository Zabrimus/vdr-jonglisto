package vdr.jonglisto.web.mixins;

import java.util.List;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.dom.Visitor;

public class GridZoneSortLink {
	
	@InjectContainer
	private Grid grid;

	private Element element;

	void setupRender() {
		if (grid.getDataSource().getAvailableRows() == 0)
			return;

		BeanModel<?> model = grid.getDataModel();
		List<String> propertyNames = model.getPropertyNames();
		for (String propName : propertyNames) {
			PropertyModel propModel = model.get(propName);
			propModel.sortable(false);
		}
	}
	
	void beginRender(MarkupWriter writer) {
		element = writer.getElement();
	}

	void afterRender() {
		if (grid.getDataSource().getAvailableRows() == 0) {
			return;
		}

		element.visit(new Visitor() {
			public void visit(Element element) {
				if ("a".equals(element.getName())) {
					element.attribute("data-update-zone", "^");
				}
			}
		});
	}
}
