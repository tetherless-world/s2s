package edu.rpi.tw.sesf.facetontology;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import junit.framework.TestCase;

import edu.rpi.tw.sesf.facetontology.impl.FacetCollectionImpl;
import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;
import edu.rpi.tw.sesf.s2s.data.SparqlSource;

public class FacetCollectionTest extends TestCase {
	private static String TEST_FILE_FORMAT = "N3";
	private static String TEST_FILE = "http://aquarius.tw.rpi.edu/s2s/BCO-DMO/1.0/s2s.ttl";
	private static final String TEST_URI = "http://aquarius.tw.rpi.edu/s2s/BCO-DMO/1.0/s2s.ttl#FacetCollection";
	private static final String TEST_CONNECTED_FACET = "http://aquarius.tw.rpi.edu/s2s/BCO-DMO/1.0/s2s.ttl#ProgramsFacet";
	private static final String TEST_INPUT_FACET = "http://aquarius.tw.rpi.edu/s2s/BCO-DMO/1.0/s2s.ttl#ProjectsFacet";
	private static final String TEST_INPUT_FACET_VALUE = "http://escience.rpi.edu/ontology/BCO-DMO/bcodmo/3/0/Project_1";
	private static final String TEST_INPUT_FACET_VALUE2 = "http://escience.rpi.edu/ontology/BCO-DMO/bcodmo/3/0/Project_163";
	//private static final String TEST_RESULT = "http://escience.rpi.edu/ontology/BCO-DMO/bcodmo/3/0/Person_1";
	private static final String TEST_RESULT = "http://escience.rpi.edu/ontology/BCO-DMO/bcodmo/3/0/Program_1";
	
	private FacetCollection _facets;
	private SparqlSource _sparql;
	/**
	 * @param name
	 */
	public FacetCollectionTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Model m = ModelFactory.createDefaultModel();
		m.read(TEST_FILE, TEST_FILE_FORMAT);
		_sparql = new SparqlSource("test","http://escience.rpi.edu:8890/sparql","http://escience.rpi.edu/ontology/BCO-DMO/bcodmo/3/0/");
		_facets = new FacetCollectionImpl(TEST_URI, new JenaPelletSource("test",m));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		_facets = null;
	}
	
	public void testQueryBuilder() {
		try {
			Configuration config = new PropertiesConfiguration("src/main/resources/s2s.properties");
			if (!config.containsKey("test.rpi") || !config.getBoolean("test.rpi")) {
				return;
			}
		} catch (ConfigurationException e) {
			return;
		}
		Map<String,Collection<String>> map = new HashMap<String,Collection<String>>();
		map.put(TEST_INPUT_FACET, Arrays.asList(TEST_INPUT_FACET_VALUE, TEST_INPUT_FACET_VALUE2));
		String query = _facets.buildConnectedFacetQuery(TEST_CONNECTED_FACET, map);
		ResultSet rs = _sparql.sparqlSelect(query);
		boolean check = false;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("id") && qs.get("id").toString().equals(TEST_RESULT)) check = true;
		}
		assertTrue(check);
	}
}
