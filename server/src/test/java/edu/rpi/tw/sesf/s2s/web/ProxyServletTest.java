package edu.rpi.tw.sesf.s2s.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

import junit.framework.TestCase;

public class ProxyServletTest extends TestCase {
	//set up variables
	//constants for testing
	private static final String S2S_PROPERTIES = "src/main/resources/s2s.properties";
	private static final String TEST_CACHING_PROP = "s2s.testcaching";
	private static final String TEST_SOURCES_CONFIG = "s2s.testsources";
	private static final String TEST_SERVLET_SERVICE = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestSearchService";
	private static final String TEST_SERVLET_QUERY = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestResultQuery";
	private static final String TEST_SERVLET_PARAMETER = "http://a9.com/-/spec/opensearch/1.1/searchTerms";
	private static final String TEST_SERVLET_PARAMETER_VALUE = "test";
	private static final String TEST_SERVLET_RESPONSE = "hello world";
		
	private ProxyServlet _servlet;
	
	/**
	 * Constructor for ProxyServletTest, calls superclass constructor
	 * @param name
	 */
	public ProxyServletTest(String name) {
		super(name);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		_servlet = new ProxyServlet();
	    
	    // Create mock objects for servlet context & config
	    MockServletContext context = new MockServletContext("/");
	    MockServletConfig config = new MockServletConfig(context);
	    
	    config.addInitParameter("s2s-properties",S2S_PROPERTIES);
	    config.addInitParameter("s2s-sources-property",TEST_SOURCES_CONFIG);
	    config.addInitParameter("s2s-caching-property",TEST_CACHING_PROP);
	    
	    _servlet.init(config);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		_servlet = null;
	}
	
	public void testRunQuery() {
		//set up HttpServletRequest
		MockHttpServletRequest request = new MockHttpServletRequest("POST",null);
		JSONObject json = new JSONObject();
		try {
			json.put("service", TEST_SERVLET_SERVICE);
			json.put("interface", TEST_SERVLET_QUERY);
			Map<String,String> parameters = new HashMap<>();
			parameters.put(TEST_SERVLET_PARAMETER, TEST_SERVLET_PARAMETER_VALUE);
			json.put("inputs", parameters);
		} catch (JSONException e) {
			fail("JSON Exception: " + e);
		}

		request.setContent(json.toString().getBytes());

		//set up HttpServletResponse
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		//perform servlet request
		try {
			_servlet.doPost(request, response);
			assertTrue(response.getContentAsString().equals(TEST_SERVLET_RESPONSE));
		} catch (ServletException e) {
			fail("Servlet Exception: " + e);
		} catch (IOException e) {
			fail("IO Exception: " + e);
		}
	}
}
