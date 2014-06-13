/**
 * 
 */
package edu.rpi.tw.sesf.s2s.web.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import net.fortytwo.ripple.query.QueryEngine;

import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.springframework.mock.web.MockHttpServletResponse;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.impl.lod.SearchServiceFromRipple;
import edu.rpi.tw.sesf.s2s.impl.sparql.SearchServiceFromSparql;
import edu.rpi.tw.sesf.s2s.utils.RippleQueryEngineSingleton;
import junit.framework.TestCase;

/**
 * @author rozele
 *
 */
public class OpenSearchServiceEngineTest extends TestCase {

	private static final String TEST_SERVICE_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestSearchService";
	private static final String TEST_SERVICE_QUERY = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInputQuery";
	private static final String TEST_SERVICE_QUERY_ALT = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestResultQuery";
	private static final String TEST_SERVICE_PARAMETER = "http://a9.com/-/spec/opensearch/1.1/searchTerms";
	private static final String TEST_SERVICE_PARAMETER_ALT = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInput";
	private static final String TEST_SERVICE_PARAMETER_VALUE = "test";
	private static final String TEST_SERVICE_QUERY_RESULT = "hello world";

	private OpenSearchServiceEngine _sparqlEngine;
	private OpenSearchServiceEngine _linkedEngine;
	
	/**
	 * @param name
	 */
	public OpenSearchServiceEngineTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		QueryEngine qe = RippleQueryEngineSingleton.getInstance();
		_linkedEngine = new OpenSearchServiceEngine(new SearchServiceFromRipple(TEST_SERVICE_URI, new RippleSource("test",qe)));
		OntModel m = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		m.read(TEST_SERVICE_URI);
		_sparqlEngine = new OpenSearchServiceEngine(new SearchServiceFromSparql(TEST_SERVICE_URI, new JenaPelletSource("test",m)));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		_linkedEngine = null;
		_sparqlEngine = null;
	}
	
	public void testRunQuery() {
		Map<String,String> params = new HashMap<>();
		params.put(TEST_SERVICE_PARAMETER, TEST_SERVICE_PARAMETER_VALUE);
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			_linkedEngine.runQuery(TEST_SERVICE_QUERY_ALT, params, response);
			assertTrue(response.getContentAsString().equals(TEST_SERVICE_QUERY_RESULT));
		} catch (UnsupportedEncodingException e) {
			fail("Could not execute query: " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		response = new MockHttpServletResponse();
		try {
			_sparqlEngine.runQuery(TEST_SERVICE_QUERY_ALT, params, response);
			assertTrue(response.getContentAsString().equals(TEST_SERVICE_QUERY_RESULT));
		} catch (UnsupportedEncodingException e) {
			fail("Could not execute query: " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testGetSearchParameters() {
		assertTrue(_linkedEngine.getInputs().contains(TEST_SERVICE_PARAMETER));
		assertTrue(_linkedEngine.getInputs().contains(TEST_SERVICE_PARAMETER_ALT));
		
		assertTrue(_sparqlEngine.getInputs().contains(TEST_SERVICE_PARAMETER));
		assertTrue(_sparqlEngine.getInputs().contains(TEST_SERVICE_PARAMETER_ALT));
	}

	public void testGetQueryInterfaces() {
		assertTrue(_linkedEngine.getInterfaces().contains(TEST_SERVICE_QUERY));
		assertTrue(_linkedEngine.getInterfaces().contains(TEST_SERVICE_QUERY_ALT));
		
		assertTrue(_sparqlEngine.getInterfaces().contains(TEST_SERVICE_QUERY));
		assertTrue(_sparqlEngine.getInterfaces().contains(TEST_SERVICE_QUERY_ALT));
	}
}
