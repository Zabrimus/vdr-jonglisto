package vdr.jonglisto.web.conduit;

import java.lang.annotation.Annotation;

import org.apache.tapestry5.PropertyConduit;

import vdr.jonglisto.lib.model.SearchTimer;

@SuppressWarnings({ "rawtypes" })
public class SearchTimerConduit implements PropertyConduit {
	private final String key;
	private final Class<?> type;

	public SearchTimerConduit(String key, Class<?> type) {
		super();
		this.key = key;
		this.type = type;
	}

	public Object get(Object instance) {
		return ((SearchTimer) instance).get(key);
	}

	public void set(Object instance, Object value) {
		((SearchTimer) instance).set(key, value);
	}

	public Class getPropertyType() {
		return type;
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return null;
	}
}