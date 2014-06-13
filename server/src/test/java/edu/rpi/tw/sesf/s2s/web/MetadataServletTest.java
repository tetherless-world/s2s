package edu.rpi.tw.sesf.s2s.web;

import java.io.IOException;

import javax.servlet.ServletException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

import edu.rpi.tw.sesf.s2s.utils.Ontology;

import junit.framework.TestCase;

/**
 * @author rozele
 * jUnit test case for MetadataServlet
 */

public class MetadataServletTest extends TestCase {
	
	//constants for testing
	private static final String S2S_PROPERTIES = "src/main/resources/s2s.properties";
	private static final String TEST_SOURCES_CONFIG = "s2s.testsources";
	private static final String TEST_CACHING_PROP = "s2s.testcaching";
	private static final String TEST_SERVLET_NO_CACHE = "no-cache";
	
	private static final String TEST_WIDGET_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestWidget";
	private static final String TEST_WIDGET_LABEL = "Test Widget";
	private static final String TEST_WIDGET_COMMENT = "Test Widget";
	private static final String TEST_WIDGET_SCRIPT = "http://escience.rpi.edu/s2s/test/TestWidget.js";
	private static final String TEST_WIDGET_OUTPUT = "http://escience.rpi.edu/ontology/sesf/s2s-core/4/0/LabelIdJsonArray";
	private static final String TEST_WIDGET_PARAMETER = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInput";
	private static final String TEST_WIDGET_TYPE = Ontology.InputWidget;
	
	private static final String TEST_PARAMETER_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInput";
	private static final String TEST_PARAMETER_LABEL = "Test Input";
	private static final String TEST_PARAMETER_COMMENT = "Test Input";
	private static final String TEST_PARAMETER_NOT_CACHED = "false";
	private static final String TEST_PARAMETER_CACHED = "true";
	
	private static final String TEST_QUERY_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInputQuery";
	private static final Object TEST_QUERY_PARAMETER = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInput";
	private static final Object TEST_QUERY_COMMENT = "Test Query Interface";
	private static final Object TEST_QUERY_LABEL = "Test Query Interface";
	private static final Object TEST_QUERY_OUTPUT = "http://escience.rpi.edu/ontology/sesf/s2s-core/4/0/LabelIdJsonArray";
	private static final Object TEST_QUERY_TYPE = Ontology.InputValuesInterface;
	
	private static final String TEST_SERVICE_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestSearchService";
	private static final String TEST_SERVICE_TYPE = Ontology.OpenSearchService;
	private static final String TEST_SERVICE_LABEL = "Test Search Service";
	private static final String TEST_SERVICE_COMMENT = "Test Search Service for S2S Server 3.0";
	private static final String TEST_SERVICE_LINK = "http://tw.rpi.edu/web/project/sesf/workinggroups/s2s";
	
	private static final String JSON_MIME_TYPE = "application/json";
	
	//set up variables
	private MetadataServlet _servlet;
	
	/**
	 * Constructor for MetadataServletTest, calls superclass constructor
	 * @param name
	 * @throws ServletException 
	 */
	public MetadataServletTest(String name) throws ServletException {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		_servlet = new MetadataServlet();
	    
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

	public void testGetSearchService() {
		//set up HttpServletRequest
		MockHttpServletRequest request = new MockHttpServletRequest("GET",null);
		request.addParameter("type", "services");
		request.addParameter("instance", TEST_SERVICE_URI);
		request.addHeader("Accept",JSON_MIME_TYPE);
		
		//set up HttpServletResponse
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		//perform servlet request
		try {
			_servlet.doGet(request, response);
			assertTrue(response.getContentType().equals(JSON_MIME_TYPE));
			String json = response.getContentAsString();
			try {
				JSONObject obj = new JSONObject(json);
				
				//check top level contents of JSON
				assertTrue(obj.has(TEST_SERVICE_URI));
				JSONObject serviceObj = obj.getJSONObject(TEST_SERVICE_URI);
				assertTrue(serviceObj.has("service"));
				JSONObject serviceInfo = serviceObj.getJSONObject("service");
				
				//check if rdf:type is correct
				JSONArray types = serviceInfo.getJSONArray("types");
				assertTrue(types.toString().contains(TEST_SERVICE_TYPE));
				
				//check if rdfs:seeAlso is correct
				JSONArray links = serviceInfo.getJSONArray("links");
				assertTrue(links.toString().contains(TEST_SERVICE_LINK));
				
				//check if rdfs:label is correct
				assertTrue(serviceInfo.getString("label").equals(TEST_SERVICE_LABEL));
				
				//check if rdfs:comment is correct
				assertTrue(serviceInfo.getString("comment").equals(TEST_SERVICE_COMMENT));
				
				//check if number of parameters is correct
				assertTrue(serviceObj.has("inputs"));
				assertTrue(serviceObj.getJSONObject("inputs").length() == 2);
				
				//check if number of query interfaces is correct
				assertTrue(serviceObj.has("interfaces"));
				assertTrue(serviceObj.getJSONObject("interfaces").length() == 2);
			} catch (JSONException e) {
				fail("Invalid JSON response: " + e);
			}
		} catch (ServletException e) {
			fail("Servlet Exception: " + e);
		} catch (IOException e) {
			fail("IO Exception: " + e);
		}
	}
	
	public void testGetWidget() {
		//set up HttpServletRequest
		MockHttpServletRequest request = new MockHttpServletRequest("GET",null);
		request.addParameter("type", "widgets");
		request.addParameter("instance", TEST_WIDGET_URI);
		request.addHeader("Accept",JSON_MIME_TYPE);
		
		//set up HttpServletResponse
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		//perform servlet request
		try {
			_servlet.doGet(request, response);
			assertTrue(response.getContentType().equals(JSON_MIME_TYPE));
			String json = response.getContentAsString();
			try {
				JSONObject obj = new JSONObject(json);
				assertTrue(obj.has(TEST_WIDGET_URI));
				JSONObject widgetObj = obj.getJSONObject(TEST_WIDGET_URI);
				
				//check if rdfs:seeAlso link exists
				JSONArray types = widgetObj.getJSONArray("types");
				assertTrue(types.toString().contains(TEST_WIDGET_TYPE));
				
				//check if rdfs:label is correct
				assertTrue(widgetObj.getString("label").equals(TEST_WIDGET_LABEL));
				
				//check if rdfs:comment is correct
				assertTrue(widgetObj.getString("comment").equals(TEST_WIDGET_COMMENT));
				
				//check if contains correct script is correct
				assertTrue(widgetObj.getJSONArray("scripts").toString().contains(TEST_WIDGET_SCRIPT));
				
				//check if contains correct script is correct
				assertTrue(widgetObj.getJSONArray("inputs").toString().contains(TEST_WIDGET_PARAMETER));
				
			} catch (JSONException e) {
				fail("Invalid JSON response: " + e);
			}
		} catch (ServletException e) {
			fail("Servlet Exception: " + e);
		} catch (IOException e) {
			fail("IO Exception: " + e);
		}
	}
	
	//TODO: Decide the intent of a "parameter" only or "output" only request is and rewrite test
	/*public void testGetWidgetsFromOutput() {
		//set up HttpServletRequest
		MockHttpServletRequest request = new MockHttpServletRequest("GET",null);
		request.addParameter("type", "widgets");
		request.addParameter("output", TEST_WIDGET_OUTPUT);
		request.addHeader("Accept",JSON_MIME_TYPE);
		
		//set up HttpServletResponse
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		//perform servlet request
		try {
			_servlet.doGet(request, response);
			assertTrue(response.getContentType().equals(JSON_MIME_TYPE));
			String json = response.getContentAsString();
			try {
				JSONObject obj = new JSONObject(json);
				assertTrue(obj.has(TEST_WIDGET_URI));
				JSONObject widgetObj = obj.getJSONObject(TEST_WIDGET_URI);
				
				//check if rdfs:seeAlso link exists
				JSONArray types = widgetObj.getJSONArray("types");
				assertTrue(types.toString().contains(TEST_WIDGET_TYPE));
				
				//check if rdfs:label is correct
				assertTrue(widgetObj.getString("label").equals(TEST_WIDGET_LABEL));
				
				//check if rdfs:comment is correct
				assertTrue(widgetObj.getString("comment").equals(TEST_WIDGET_COMMENT));
				
				//check if contains correct script is correct
				assertTrue(widgetObj.getJSONArray("scripts").toString().contains(TEST_WIDGET_SCRIPT));
				
				//check if contains correct script is correct
				assertTrue(widgetObj.getJSONArray("parameters").toString().contains(TEST_WIDGET_PARAMETER));
				
			} catch (JSONException e) {
				fail("Invalid JSON response: " + e);
			}
		} catch (ServletException e) {
			fail("Servlet Exception: " + e);
		} catch (IOException e) {
			fail("IO Exception: " + e);
		}
	}
	
	public void testGetWidgetsFromParameter() {
		//set up HttpServletRequest
		MockHttpServletRequest request = new MockHttpServletRequest("GET",null);
		request.addParameter("type", "widgets");
		request.addParameter("parameter", TEST_WIDGET_PARAMETER);
		request.addHeader("Accept",JSON_MIME_TYPE);
		
		//set up HttpServletResponse
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		//perform servlet request
		try {
			_servlet.doGet(request, response);
			assertTrue(response.getContentType().equals(JSON_MIME_TYPE));
			String json = response.getContentAsString();
			try {
				JSONObject obj = new JSONObject(json);
				assertTrue(obj.has(TEST_WIDGET_URI));
				JSONObject widgetObj = obj.getJSONObject(TEST_WIDGET_URI);
				
				//check if rdfs:seeAlso link exists
				JSONArray types = widgetObj.getJSONArray("types");
				assertTrue(types.toString().contains(TEST_WIDGET_TYPE));
				
				//check if rdfs:label is correct
				assertTrue(widgetObj.getString("label").equals(TEST_WIDGET_LABEL));
				
				//check if rdfs:comment is correct
				assertTrue(widgetObj.getString("comment").equals(TEST_WIDGET_COMMENT));
				
				//check if contains correct script is correct
				assertTrue(widgetObj.getJSONArray("scripts").toString().contains(TEST_WIDGET_SCRIPT));
				
				//check if contains correct script is correct
				assertTrue(widgetObj.getJSONArray("parameters").toString().contains(TEST_WIDGET_PARAMETER));
				
			} catch (JSONException e) {
				fail("Invalid JSON response: " + e);
			}
		} catch (ServletException e) {
			fail("Servlet Exception: " + e);
		} catch (IOException e) {
			fail("IO Exception: " + e);
		}
	}*/
	
	public void testGetWidgetsFromOutputAndInput() {
		//set up HttpServletRequest
		MockHttpServletRequest request = new MockHttpServletRequest("GET",null);
		request.addParameter("type", "widgets");
		request.addParameter("output", TEST_WIDGET_OUTPUT);
		request.addParameter("input", TEST_WIDGET_PARAMETER);
		request.addHeader("Accept",JSON_MIME_TYPE);
		
		//set up HttpServletResponse
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		//perform servlet request
		try {
			_servlet.doGet(request, response);
			assertTrue(response.getContentType().equals(JSON_MIME_TYPE));
			String json = response.getContentAsString();
			try {
				JSONObject obj = new JSONObject(json);
				assertTrue(obj.has(TEST_WIDGET_URI));
				JSONObject widgetObj = obj.getJSONObject(TEST_WIDGET_URI);
				
				//check if rdfs:seeAlso link exists
				JSONArray types = widgetObj.getJSONArray("types");
				assertTrue(types.toString().contains(TEST_WIDGET_TYPE));
				
				//check if rdfs:label is correct
				assertTrue(widgetObj.getString("label").equals(TEST_WIDGET_LABEL));
				
				//check if rdfs:comment is correct
				assertTrue(widgetObj.getString("comment").equals(TEST_WIDGET_COMMENT));
				
				//check if contains correct script is correct
				assertTrue(widgetObj.getJSONArray("scripts").toString().contains(TEST_WIDGET_SCRIPT));
				
				//check if contains correct script is correct
				assertTrue(widgetObj.getJSONArray("inputs").toString().contains(TEST_WIDGET_PARAMETER));
				
			} catch (JSONException e) {
				fail("Invalid JSON response: " + e);
			}
		} catch (ServletException e) {
			fail("Servlet Exception: " + e);
		} catch (IOException e) {
			fail("IO Exception: " + e);
		}
	}
	
	public void testGetInput() {
		//set up HttpServletRequest
		MockHttpServletRequest request = new MockHttpServletRequest("GET",null);
		request.addParameter("type", "inputs");
		request.addParameter("instance", TEST_PARAMETER_URI);
		request.addHeader("Accept",JSON_MIME_TYPE);
		
		//set up HttpServletResponse
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		//perform servlet request
		try {
			_servlet.doGet(request, response);
			assertTrue(response.getContentType().equals(JSON_MIME_TYPE));
			String json = response.getContentAsString();
			try {
				JSONObject obj = new JSONObject(json);
				assertTrue(obj.has(TEST_PARAMETER_URI));
				JSONObject paramObj = obj.getJSONObject(TEST_PARAMETER_URI);
				
				//check if rdfs:label is correct
				assertTrue(paramObj.getString("label").equals(TEST_PARAMETER_LABEL));
				
				//check if rdfs:comment is correct
				assertTrue(paramObj.getString("comment").equals(TEST_PARAMETER_COMMENT));
				
			} catch (JSONException e) {
				fail("Invalid JSON response: " + e);
			}
		} catch (ServletException e) {
			fail("Servlet Exception: " + e);
		} catch (IOException e) {
			fail("IO Exception: " + e);
		}
	}
	
	public void testGetInputsFromService() {
		//set up HttpServletRequest
		MockHttpServletRequest request = new MockHttpServletRequest("GET",null);
		request.addParameter("type", "inputs");
		request.addParameter("service", TEST_SERVICE_URI);
		request.addHeader("Accept",JSON_MIME_TYPE);
		
		//set up HttpServletResponse
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		//perform servlet request
		try {
			_servlet.doGet(request, response);
			assertTrue(response.getContentType().equals(JSON_MIME_TYPE));
			String json = response.getContentAsString();
			try {
				JSONObject obj = new JSONObject(json);
				assertTrue(obj.has(TEST_PARAMETER_URI));
				JSONObject paramObj = obj.getJSONObject(TEST_PARAMETER_URI);
				
				//check if rdfs:label is correct
				assertTrue(paramObj.getString("label").equals(TEST_PARAMETER_LABEL));
				
				//check if rdfs:comment is correct
				assertTrue(paramObj.getString("comment").equals(TEST_PARAMETER_COMMENT));
				
			} catch (JSONException e) {
				fail("Invalid JSON response: " + e);
			}
		} catch (ServletException e) {
			fail("Servlet Exception: " + e);
		} catch (IOException e) {
			fail("IO Exception: " + e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testGetInterface() {
		//set up HttpServletRequest
		MockHttpServletRequest request = new MockHttpServletRequest("GET",null);
		request.addParameter("type", "interfaces");
		request.addParameter("instance", TEST_QUERY_URI);
		request.addHeader("Accept",JSON_MIME_TYPE);
		
		//set up HttpServletResponse
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		//perform servlet request
		try {
			_servlet.doGet(request, response);
			assertTrue(response.getContentType().equals(JSON_MIME_TYPE));
			String json = response.getContentAsString();
			try {
				JSONObject obj = new JSONObject(json);
				assertTrue(obj.has(TEST_QUERY_URI));
				JSONObject queryObj = obj.getJSONObject(TEST_QUERY_URI);
				
				//check if rdfs:seeAlso link exists
				JSONArray types = queryObj.getJSONArray("types");
				assertTrue(types.toString().contains((CharSequence) TEST_QUERY_TYPE));
				
				//check if rdfs:label is correct
				assertTrue(queryObj.getString("label").equals(TEST_QUERY_LABEL));
				
				//check if rdfs:comment is correct
				assertTrue(queryObj.getString("comment").equals(TEST_QUERY_COMMENT));
				
				//check if output is correct
				assertTrue(queryObj.getString("output").equals(TEST_QUERY_OUTPUT));
				
				//check if parameter query is correct
				assertTrue(queryObj.getString("input").equals(TEST_QUERY_PARAMETER));
				
			} catch (JSONException e) {
				fail("Invalid JSON response: " + e);
			}
		} catch (ServletException e) {
			fail("Servlet Exception: " + e);
		} catch (IOException e) {
			fail("IO Exception: " + e);
		}
	}
	
	public void testGetInterfacesFromService() {
		//set up HttpServletRequest
		MockHttpServletRequest request = new MockHttpServletRequest("GET",null);
		request.addParameter("type", "interfaces");
		request.addParameter("service", TEST_SERVICE_URI);
		request.addHeader("Accept",JSON_MIME_TYPE);
		
		//set up HttpServletResponse
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		//perform servlet request
		try {
			_servlet.doGet(request, response);
			assertTrue(response.getContentType().equals(JSON_MIME_TYPE));
			String json = response.getContentAsString();
			try {
				JSONObject obj = new JSONObject(json);
				assertTrue(obj.has(TEST_QUERY_URI));
				JSONObject queryObj = obj.getJSONObject(TEST_QUERY_URI);
				
				//check if rdfs:seeAlso link exists
				JSONArray types = queryObj.getJSONArray("types");
				assertTrue(types.toString().contains((CharSequence) TEST_QUERY_TYPE));
				
				//check if rdfs:label is correct
				assertTrue(queryObj.getString("label").equals(TEST_QUERY_LABEL));
				
				//check if rdfs:comment is correct
				assertTrue(queryObj.getString("comment").equals(TEST_QUERY_COMMENT));
				
				//check if output is correct
				assertTrue(queryObj.getString("output").equals(TEST_QUERY_OUTPUT));
				
				//check if parameter query is correct
				assertTrue(queryObj.getString("input").equals(TEST_QUERY_PARAMETER));
				
			} catch (JSONException e) {
				fail("Invalid JSON response: " + e);
			}
		} catch (ServletException e) {
			fail("Servlet Exception: " + e);
		} catch (IOException e) {
			fail("IO Exception: " + e);
		}
	}
	
	public void testGetDefaultConfiguration() {
		//set up HttpServletRequest
		MockHttpServletRequest request = new MockHttpServletRequest("GET",null);
		request.addParameter("type", "defaults");
		request.addParameter("service", TEST_SERVICE_URI);
		
		//set up HttpServletResponse
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		//perform servlet request
		try {
			_servlet.doGet(request, response);
			assertTrue(response.getContentAsString().contains("<?xml"));
		} catch (ServletException e) {
			fail("Servlet Exception: " + e);
		} catch (IOException e) {
			fail("IO Exception: " + e);
		}
	}
	
	public void testCaching() {
		//set up first request / response
		MockHttpServletRequest request = new MockHttpServletRequest("GET",null);
		request.addParameter("type", "inputs");
		request.addParameter("instance", TEST_PARAMETER_URI);
		request.addHeader("Accept", JSON_MIME_TYPE);

		MockHttpServletResponse response = new MockHttpServletResponse();
		
		//perform first servlet request
		try {
			_servlet.doGet(request, response);
		} catch (ServletException e) {
			fail("Servlet Exception: " + e);
		} catch (IOException e) {
			fail("IO Exception: " + e);
		}
		
		//set up second request
		request = new MockHttpServletRequest("GET",null);
		request.addParameter("type", "inputs");
		request.addParameter("instance", TEST_PARAMETER_URI);
		request.addHeader("Accept", JSON_MIME_TYPE);

		response = new MockHttpServletResponse();
		
		//perform second servlet request
		try {
			_servlet.doGet(request, response);
			assertTrue(response.getContentType().equals(JSON_MIME_TYPE));
			String json = response.getContentAsString();
			try {
				JSONObject obj = new JSONObject(json);
				assertTrue(obj.has(TEST_PARAMETER_URI));
				JSONObject paramObj = obj.getJSONObject(TEST_PARAMETER_URI);
				
				//check if rdfs:label is correct
				assertTrue(paramObj.getString("cached").equals(TEST_PARAMETER_CACHED));
				
			} catch (JSONException e) {
				fail("Invalid JSON response: " + e);
			}
		} catch (ServletException e) {
			fail("Servlet Exception: " + e);
		} catch (IOException e) {
			fail("IO Exception: " + e);
		}
	}
	
	public void testNoCache() {
		//set up first request / response
		MockHttpServletRequest request = new MockHttpServletRequest("GET",null);
		request.addParameter("type", "inputs");
		request.addParameter("instance", TEST_PARAMETER_URI);
		request.addHeader("Accept", JSON_MIME_TYPE);

		MockHttpServletResponse response = new MockHttpServletResponse();
		
		//perform first servlet request
		try {
			_servlet.doGet(request, response);
		} catch (ServletException e) {
			fail("Servlet Exception: " + e);
		} catch (IOException e) {
			fail("IO Exception: " + e);
		}
		
		//set up second request with no-cache
		request = new MockHttpServletRequest("GET",null);
		request.addParameter("type", "inputs");
		request.addParameter("instance", TEST_PARAMETER_URI);
		request.addHeader("Accept", JSON_MIME_TYPE);
		request.addHeader("Pragma", TEST_SERVLET_NO_CACHE);

		response = new MockHttpServletResponse();
		
		//perform second servlet request
		try {
			_servlet.doGet(request, response);
			assertTrue(response.getContentType().equals(JSON_MIME_TYPE));
			String json = response.getContentAsString();
			try {
				JSONObject obj = new JSONObject(json);
				assertTrue(obj.has(TEST_PARAMETER_URI));
				JSONObject paramObj = obj.getJSONObject(TEST_PARAMETER_URI);
				
				//check if rdfs:label is correct
				assertTrue(paramObj.getString("cached").equals(TEST_PARAMETER_NOT_CACHED));
				
			} catch (JSONException e) {
				fail("Invalid JSON response: " + e);
			}
		} catch (ServletException e) {
			fail("Servlet Exception: " + e);
		} catch (IOException e) {
			fail("IO Exception: " + e);
		}
	}
}
