package edu.rpi.tw.sesf.s2s.web.service.impl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.impl.lod.SearchServiceFromRipple;
import edu.rpi.tw.sesf.s2s.impl.sparql.SearchServiceFromSparql;
import edu.rpi.tw.sesf.s2s.utils.RippleQueryEngineSingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class FacetOntologyServiceEngineTest {

	private static final String TEST_SERVICE_URI = "http://aquarius.tw.rpi.edu/s2s/BCO-DMO/1.0/s2s.ttl#FacetOntologyService";
	private static final String TEST_SERVICE_QUERY = "http://aquarius.tw.rpi.edu/s2s/BCO-DMO/1.0/s2s.ttl#PeopleFacet";
	private static final String TEST_SERVICE_INPUT = "http://aquarius.tw.rpi.edu/s2s/BCO-DMO/1.0/s2s.ttl#ProgramsFacet";
	private static final String TEST_SERVICE_INPUT_VALUE = "http://escience.rpi.edu/ontology/BCO-DMO/bcodmo/3/0/Program_1";
	private static final String TEST_SERVICE_QUERY_RESULT = "http://escience.rpi.edu/ontology/BCO-DMO/bcodmo/3/0/Person_1";
	private static final String TEST_FORMAT = "TURTLE";
	
	private static SearchServiceFromSparql _sparqlService;
	private static SearchServiceFromRipple _linkedService;

	@BeforeClass
	public static void setUp() throws Exception {

		Model m = ModelFactory.createDefaultModel();
		URL testFile = FacetOntologyServiceEngineTest.class.getResource("/rdf/BCO-DMO/1.0/s2s.ttl");
		m.read(testFile.openStream(), null, TEST_FORMAT);

		JenaPelletSource source = new JenaPelletSource("test", m);
		_sparqlService = new SearchServiceFromSparql(TEST_SERVICE_URI, source);
		FacetOntologyServiceEngine _sparqlEngine = new FacetOntologyServiceEngine(_sparqlService);
		_sparqlService.setWebServiceEngine(_sparqlEngine);

		// TODO mock the RippleSource?
		//RippleSource rsource = new RippleSource("test",RippleQueryEngineSingleton.getInstance());
		//_linkedService = new SearchServiceFromRipple(TEST_SERVICE_URI, rsource);
		//FacetOntologyServiceEngine _linkedEngine = new FacetOntologyServiceEngine(_linkedService);
		//_linkedService.setWebServiceEngine(_linkedEngine);
	}

	@Test
	public void testRunQuerySparql() throws IOException, JSONException {

		MockHttpServletResponse response = new MockHttpServletResponse();
		Map<String,String> inputs = new HashMap<>();
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

	@Ignore
	@Test
	public void testRunQueryLod() throws IOException, JSONException {
		MockHttpServletResponse response = new MockHttpServletResponse();
		Map<String,String> inputs = new HashMap<>();
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

	@Test
	public void testGetSearchParametersSparql() {
		assertTrue(_sparqlService.getWebServiceEngine().getInputs().contains(TEST_SERVICE_INPUT));
	}

	@Test
	public void testGetQueryInterfacesSparql() {
		assertTrue(_sparqlService.getWebServiceEngine().getInterfaces().contains(TEST_SERVICE_INPUT));
	}

	@Ignore
	@Test
	public void testGetSearchParametersLod() {
		assertTrue(_linkedService.getWebServiceEngine().getInputs().contains(TEST_SERVICE_INPUT));
	}

    @Ignore
	@Test
	public void testGetQueryInterfacesLod() {
		assertTrue(_linkedService.getWebServiceEngine().getInterfaces().contains(TEST_SERVICE_INPUT));
	}
}