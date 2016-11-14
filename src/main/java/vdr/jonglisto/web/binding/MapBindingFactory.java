package vdr.jonglisto.web.binding;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.internal.services.StringInterner;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.services.BindingFactory;
import org.apache.tapestry5.services.PropertyConduitSource;

/**
 * A two way binding backed by a property of type java.util.Map. The binding
 * will get() and put() into the map Expressions are of the form
 * map:mapexpression[keyExpression] where mapexpression and keyexpression are
 * normal tapestry property expressions
 * 
 * From Tapestry Stitch: https://github.com/uklance/tapestry-stitch
 * 
 * MIT License
 */
public class MapBindingFactory implements BindingFactory {

    private final PropertyConduitSource source;
    private final StringInterner interner;
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("^(.*)\\[(.*?)\\]$");

    public MapBindingFactory(PropertyConduitSource source, StringInterner interner) {
        super();
        this.source = source;
        this.interner = interner;
    }

    public Binding newBinding(String description, ComponentResources container, ComponentResources component,
            String expression, Location location) {
        Object target = container.getComponent();
        Class<?> targetClass = target.getClass();

        Matcher matcher = EXPRESSION_PATTERN.matcher(expression);
        if (!matcher.matches()) {
            String msg = String.format("Expression %s does not match %s", expression, EXPRESSION_PATTERN.pattern());
            throw new TapestryException(msg, location, null);
        }

        PropertyConduit mapConduit = source.create(targetClass, matcher.group(1));
        PropertyConduit keyConduit = source.create(targetClass, matcher.group(2));

        String toString = interner.format("MapPropBinding[%s %s(%s)]", description, container.getCompleteId(),
                expression);

        return new MapBinding(location, target, mapConduit, keyConduit, toString);
    }
}