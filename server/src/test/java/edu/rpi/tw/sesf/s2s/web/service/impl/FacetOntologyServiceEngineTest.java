/**
 * 
 */
package edu.rpi.tw.sesf.s2s.web.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assume;

import org.springframework.mock.web.MockHttpServletResponse;

import net.fortytwo.ripple.RippleException;
import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.InvalidLinkedDataIdentifierException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.impl.lod.SearchServiceFromRipple;
import edu.rpi.tw.sesf.s2s.impl.sparql.SearchServiceFromSparql;
import edu.rpi.tw.sesf.s2s.utils.RippleQueryEngineSingleton;
import junit.framework.TestCase;

/**
 * @author rozele
 *
 */
public class FacetOntologyServiceEngineTest extends TestCase {

	private static final String TEST_SERVICE_URI = "http://aquarius.tw.rpi.edu/s2s/BCO-DMO/1.0/s2s.ttl#FacetOntologyService";
	private static final String TEST_SERVICE_QUERY = "http://aquarius.tw.rpi.edu/s2s/BCO-DMO/1.0/s2s.ttl#PeopleFacet";
	private static final String TEST_SERVICE_INPUT = "http://aquarius.tw.rpi.edu/s2s/BCO-DMO/1.0/s2s.ttl#ProgramsFacet";
	private static final String TEST_SERVICE_INPUT_VALUE = "http://escience.rpi.edu/ontology/BCO-DMO/bcodmo/3/0/Program_1";
	private static final String TEST_SERVICE_QUERY_RESULT = "http://escience.rpi.edu/ontology/BCO-DMO/bcodmo/3/0/Person_1";
	private static final String TEST_FORMAT = "TURTLE";
	
	private SearchServiceFromSparql _sparqlService;
	private SearchServiceFromRipple _linkedService;
	private FacetOntologyServiceEngine _sparqlEngine;
	private FacetOntologyServiceEngine _linkedEngine;
	
	/**
	 * @param name
	 * @throws UnregisteredInstanceException 
	 * @throws InvalidLinkedDataIdentifierException 
	 * @throws RippleException 
	 * @throws IncompatibleDataSourceException 
	 */
	public FacetOntologyServiceEngineTest(String name) throws UnregisteredInstanceException, InvalidLinkedDataIdentifierException, RippleException, IncompatibleDataSourceException {
		super(name);
		JenaPelletSource source = new JenaPelletSource("test",TEST_SERVICE_URI, TEST_FORMAT);
		RippleSource rsource = new RippleSource("test",RippleQueryEngineSingleton.getInstance());
		_sparqlService = new SearchServiceFromSparql(TEST_SERVICE_URI, source);
		_linkedService = new SearchServiceFromRipple(TEST_SERVICE_URI, rsource);
		_sparqlEngine = new FacetOntologyServiceEngine(_sparqlService);
		_linkedEngine = new FacetOntologyServiceEngine(_linkedService);
		_sparqlService.setWebServiceEngine(_sparqlEngine);
		_linkedService.setWebServiceEngine(_linkedEngine);
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

	public void beforeTest() {
		try {
			Configuration config = new PropertiesConfiguration("src/main/resources/s2s.properties");
			Assume.assumeTrue(config.containsKey("test.rpi") && config.getBoolean("test.rpi"));
		} catch (ConfigurationException e) {
			Assume.assumeTrue(false);
		}
	}
	
	public void testRunQuerySparql() throws IOException, JSONException {
		try {
			Configuration config = new PropertiesConfiguration("src/main/resources/s2s.properties");
			if (!config.containsKey("test.rpi") || !config.getBoolean("test.rpi")) {
				return;
			}
		} catch (ConfigurationException e) {
			return;
		}
		MockHttpServletResponse response = new MockHttpServletResponse();
		Map<String,String> inputs = new HashMap<String,String>();
		inputs.put(TEST_SERVICE_INPUT, TEST_SERVICE_INPUT_VALUE);
		_sparqlService.getWebServiceEngine().runQuery(TEST_SERVICE_QUERY, inputs, response);
		JSONArray arr = new JSONArray(response.getContentAsString());
		boolean check = false;
		for (int i = 0; i < arr.length(); ++i) {
			Object obj = arr.get(i);
			if (JSONObject.class.isAssignableFrom(obj.getClass())) {
				JSONObject jobj = (JSONObject)obj;
				if (jobj.has("id")) {
					check = jobj.get("id").toString().equals(TEST_SERVICE_QUERY_RESULT);
					if (check) break;
				}
			}
		}
		assertTrue(check);
	}
	
	public void testRunQueryLod() throws IOException, JSONException {
		try {
			Configuration config = new PropertiesConfiguration("src/main/resources/s2s.properties");
			if (!config.containsKey("test.rpi") || !config.getBoolean("test.rpi")) {
				return;
			}
		} catch (ConfigurationException e) {
			return;
		}
		MockHttpServletResponse response = new MockHttpServletResponse();
		Map<String,String> inputs = new HashMap<String,String>();
		inputs.put(TEST_SERVICE_INPUT, TEST_SERVICE_INPUT_VALUE);
		_linkedService.getWebServiceEngine().runQuery(TEST_SERVICE_QUERY, inputs, response);
		JSONArray arr = new JSONArray(response.getContentAsString());
		boolean check = false;
		for (int i = 0; i < arr.length(); ++i) {
			Object obj = arr.get(i);
			if (JSONObject.class.isAssignableFrom(obj.getClass())) {
				JSONObject jobj = (JSONObject)obj;
				if (jobj.has("id")) {
					check = jobj.get("id").toString().equals(TEST_SERVICE_QUERY_RESULT);
					if (check) break;
				}
			}
		}
		assertTrue(check);
	}
	
	public void testGetSearchParametersSparql() {
		assertTrue(_sparqlService.getWebServiceEngine().getInputs().contains(TEST_SERVICE_INPUT));
	}

	public void testGetQueryInterfacesSparql() {
		assertTrue(_sparqlService.getWebServiceEngine().getInterfaces().contains(TEST_SERVICE_INPUT));
	}

	public void testGetSearchParametersLod() {
		assertTrue(_linkedService.getWebServiceEngine().getInputs().contains(TEST_SERVICE_INPUT));
	}

	public void testGetQueryInterfacesLod() {
		assertTrue(_linkedService.getWebServiceEngine().getInterfaces().contains(TEST_SERVICE_INPUT));
	}
}