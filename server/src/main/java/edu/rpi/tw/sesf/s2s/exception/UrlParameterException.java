package edu.rpi.tw.sesf.s2s.exception;

public class UrlParameterException extends Exception
{

	private static final long serialVersionUID = 6456069700873988879L;

	private int _error;

	public UrlParameterException(String string) {
		super(string);
		_error = 400;
	}
	
	public UrlParameterException(int error, String string) {
		super(string);
		_error = error;
	}
	
	public void setErrorCode(int error) {
		_error = error;
	}
	
	public int getErrorCode() {
		return _error;
	}
}
