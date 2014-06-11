/**
 * 
 */
package edu.rpi.tw.sesf.s2s.impl.sparql;

import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;
import edu.rpi.tw.sesf.s2s.utils.Ontology;

import junit.framework.TestCase;

/**
 * @author rozele
 *
 */
public class JenaSearchServiceFromSparqlTest extends TestCase {

	private static final String TEST_RDF_LOCATION = "http://escience.rpi.edu/s2s/test/4.0/test.owl";
	private static final String TEST_SERVICE_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestSearchService";
	private static final String TEST_SERVICE_LABEL = "Test Search Service";
	private static final String TEST_SERVICE_COMMENT = "Test Search Service for S2S Server 3.0";
	private static final String TEST_SERVICE_CONFIG = "http://escience.rpi.edu/s2s/test/default.xml";
	private static final String TEST_SERVICE_TYPE = Ontology.OpenSearchService;
	private static final String TEST_SERVICE_LINK = "http://tw.rpi.edu/web/project/sesf/workinggroups/s2s";
	private static final String TEST_FORMAT = "RDF/XML";

	private SearchServiceFromSparql _service;
	
	/**
	 * @param name
	 */
	public JenaSearchServiceFromSparqlTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		_service = new SearchServiceFromSparql(TEST_SERVICE_URI, new JenaPelletSource("test",TEST_RDF_LOCATION, TEST_FORMAT));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		_service = null;
	}
	
	public void testSearchService() {
		assertTrue(_service.getURI().equals(TEST_SERVICE_URI));
		assertTrue(_service.getLabel().equals(TEST_SERVICE_LABEL));
		assertTrue(_service.getComment().equals(TEST_SERVICE_COMMENT));
		assertTrue(_service.getTypes().contains(TEST_SERVICE_TYPE));
		assertTrue(_service.getRelatedLinks().contains(TEST_SERVICE_LINK));
		assertTrue(_service.getDefaultConfigurationURL().equals(TEST_SERVICE_CONFIG));
	}	
}
