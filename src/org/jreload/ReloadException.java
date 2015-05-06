package org.jreload;

/**
 * Exception thrown during compilation or reloading of class.
 * 
 * @author Steve Cook
 * 
 */
public class ReloadException extends Exception {

	private static final long serialVersionUID = 1L;

	public ReloadException(String message) {
		super(message);
	}

	public ReloadException(String message, Throwable cause) {
		super(message, cause);
	}
}
