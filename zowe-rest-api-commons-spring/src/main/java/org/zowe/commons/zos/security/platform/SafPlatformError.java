package org.zowe.commons.zos.security.platform;

public class SafPlatformError extends RuntimeException {
    private static final long serialVersionUID = 1920433542069453114L;

    public SafPlatformError(Throwable e) {
        super(e);
	}

	public SafPlatformError(String message, Exception e) {
        super(message, e);
	}

	public SafPlatformError(String message) {
        super(message);
	}
}
