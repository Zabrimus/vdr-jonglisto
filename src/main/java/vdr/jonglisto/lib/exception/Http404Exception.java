package vdr.jonglisto.lib.exception;

public class Http404Exception extends RuntimeException {

    private static final long serialVersionUID = -1263669743809461484L;

    public Http404Exception(Exception e) {
        super(e);
    }

    public Http404Exception(String s) {
        super(s);
    }
}
