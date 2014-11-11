package edu.rpi.tw.sesf.facetontology;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import edu.rpi.tw.sesf.facetontology.impl.FacetCollectionImpl;
import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;
import edu.rpi.tw.sesf.s2s.data.SparqlSource;
import org.junit.*;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@Ignore
public class FacetCollectionTest {
	private static final String TEST_FILE_FORMAT = "N3";
	private static final String TEST_URI = "http://aquarius.tw.rpi.edu/s2s/BCO-DMO/1.0/s2s.ttl#FacetCollection";
	private static final String TEST_CONNECTED_FACET = "http://aquarius.tw.rpi.edu/s2s/BCO-DMO/1.0/s2s.ttl#ProgramsFacet";
	private static final String TEST_INPUT_FACET = "http://aquarius.tw.rpi.edu/s2s/BCO-DMO/1.0/s2s.ttl#ProjectsFacet";
	private static final String TEST_INPUT_FACET_VALUE = "http://escience.rpi.edu/ontology/BCO-DMO/bcodmo/3/0/Project_1";
	private static final String TEST_INPUT_FACET_VALUE2 = "http://escience.rpi.edu/ontology/BCO-DMO/bcodmo/3/0/Project_163";
	//private static final String TEST_RESULT = "http://escience.rpi.edu/ontology/BCO-DMO/bcodmo/3/0/Person_1";
	private static final String TEST_RESULT = "http://escience.rpi.edu/ontology/BCO-DMO/bcodmo/3/0/Program_1";

	private static final String SPARQL_ENDPOINT = "http://escience.rpi.edu:8890/sparql";
	private static final String SPARQL_GRAPH = "http://escience.rpi.edu/ontology/BCO-DMO/bcodmo/3/0/";
	
	private FacetCollection _facets;

	private static SparqlSource _sparql;
	private static Model _model;

	@BeforeClass
	public static void initialize() throws Exception {
		URL testFile = FacetCollectionTest .class.getResource("/rdf/BCO-DMO/1.0/s2s.ttl");
		_model = ModelFactory.createDefaultModel();
		_model.read(testFile.openStream(), null, TEST_FILE_FORMAT);
		_sparql = new SparqlSource("test", SPARQL_ENDPOINT, SPARQL_GRAPH);
	}

	@Before
	public void setUp() throws Exception {
		_facets = new FacetCollectionImpl(TEST_URI, new JenaPelletSource("test", _model));
	}

	@After
	public void tearDown() throws Exception {
		_facets = null;
	}

	@Test
	public void testQueryBuilder() {
		Map<String,Collection<String>> map = new HashMap<>();
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
