/**
 * 
 */
package edu.rpi.tw.sesf.s2s.web;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Ignore;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

import junit.framework.TestCase;

/**
 * @author rozele
 *
 */

@Ignore
public class SessionServletTest extends TestCase {

	//static test variables
	private static String JSON = "{\"http://example.com/s2s/service\":{\"parameters\":{},\"results\":{}}}";
	private static String JSON_MIME_TYPE = "application/json";
	
	//private variables
	private SessionServlet _servlet;
	
	/**
	 * Constructor for SessionServletTest, calls superclass constructor and sets up servlet
	 * @param name
	 */
	public SessionServletTest(String name) {
		super(name);
		
		_servlet = new SessionServlet();
	    
	    // Create mock objects for servlet context & config
	    MockServletContext context = new MockServletContext("/");
	    MockServletConfig config = new MockServletConfig(context);
	    config.addInitParameter("s2s-session-directory", "/tmp/s2s/sessions");
	    
	    try {
			_servlet.init(config);
		} catch (ServletException e) {
			fail("Could not instantiate SessionServlet");
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testPostSessionForKey() {
		MockHttpServletRequest request = new MockHttpServletRequest("POST",null);
		request.setContent(JSON.getBytes());
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			_servlet.doPost(request, response);
			assertTrue(response.getContentAsString().equals(Integer.toString(JSON.hashCode())));
		} catch (ServletException e) {
			fail("ServletException encountered when trying to POST session.");
		} catch (IOException e) {
			fail("IOException encountered when trying to POST session.");
		}		
	}
	
	public void testGetSessionFromKey() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET",null);
		request.addParameter("key", Integer.toString(JSON.hashCode()));
		request.addHeader("Accept",JSON_MIME_TYPE);
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			_servlet.doGet(request, response);
			assertTrue(response.getContentAsString().equals(JSON));
		} catch (ServletException e) {
			fail("ServletException encountered when trying to POST session.");
		} catch (IOException e) {
			fail("IOException encountered when trying to POST session.");
		}
	}
	
}
