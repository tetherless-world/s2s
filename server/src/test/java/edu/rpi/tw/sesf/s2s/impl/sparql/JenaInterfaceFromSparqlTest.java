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
public class JenaInterfaceFromSparqlTest extends TestCase {

	private static final String TEST_RDF_LOCATION = "http://escience.rpi.edu/s2s/test/4.0/test.owl";
	private static final String TEST_QUERY_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInputQuery";
	private static final Object TEST_QUERY_PARAM = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInput";
	private static final Object TEST_QUERY_COMMENT = "Test Query Interface";
	private static final Object TEST_QUERY_LABEL = "Test Query Interface";
	private static final Object TEST_QUERY_OUTPUT = "http://escience.rpi.edu/ontology/sesf/s2s-core/4/0/LabelIdJsonArray";
	private static final Object TEST_QUERY_TYPE = Ontology.InputValuesInterface;
	private static final String TEST_FORMAT = "RDF/XML";
	
	private InterfaceFromSparql _query;
	
	/**
	 * @param name
	 */
	public JenaInterfaceFromSparqlTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		_query = new InterfaceFromSparql(TEST_QUERY_URI, new JenaPelletSource("test",TEST_RDF_LOCATION, TEST_FORMAT));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		_query = null;
	}
	
	public void testQueryInterface() {
		assertTrue(_query.getURI().equals(TEST_QUERY_URI));
		assertTrue(_query.getTypes().contains(TEST_QUERY_TYPE));
		assertTrue(_query.getOutput().equals(TEST_QUERY_OUTPUT));
		assertTrue(_query.getLabel().equals(TEST_QUERY_LABEL));
		assertTrue(_query.getComment().equals(TEST_QUERY_COMMENT));
		assertTrue(_query.getInput().equals(TEST_QUERY_PARAM));
	}
}
