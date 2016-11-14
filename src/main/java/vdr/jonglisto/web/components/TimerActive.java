package vdr.jonglisto.web.components;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;

public class TimerActive {

    @Parameter(required = true)
    @Property
    private Boolean value;

    @Parameter
    @Property
    private Boolean recording;

    @Parameter
    @Property
    private Boolean text;
}
