package vdr.jonglisto.lib.exception;

public class NetworkException extends RuntimeException {
	private static final long serialVersionUID = -1263669743809460484L;

	public NetworkException(Exception e) {
        super(e);
    }

    public NetworkException(String s) {
       super(s);
    }
}
