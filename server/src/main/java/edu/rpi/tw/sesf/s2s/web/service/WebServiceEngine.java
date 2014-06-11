package edu.rpi.tw.sesf.s2s.web.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public interface WebServiceEngine {
	public Collection<String> getInputs();
	public Collection<String> getInterfaces();
	public void runQuery(String query, Map<String,String> inputs, HttpServletResponse response) throws IOException;
}
