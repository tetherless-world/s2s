/**
 * 
 */
package edu.rpi.tw.sesf.s2s.impl.sparql;

import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;

import junit.framework.TestCase;

/**
 * @author rozele
 *
 */
public class JenaInputFromSparqlTest extends TestCase {

	private static final String TEST_RDF_LOCATION = "http://escience.rpi.edu/s2s/test/4.0/test.owl";
	private static final String TEST_PARAMETER_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInput";
	private static final String TEST_PARAMETER_LABEL = "Test Input";
	private static final String TEST_PARAMETER_COMMENT = "Test Input";
	private static final String TEST_FORMAT = "RDF/XML";

	
	private InputFromSparql _param;
	
	/**
	 * @param name
	 */
	public JenaInputFromSparqlTest(String name) {
		super(name);

	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		_param = new InputFromSparql(TEST_PARAMETER_URI, new JenaPelletSource("test",TEST_RDF_LOCATION, TEST_FORMAT));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		_param = null;
	}
	
	public void testParameter() {
		assertTrue(_param.getURI().equals(TEST_PARAMETER_URI));
		assertTrue(_param.getLabel().equals(TEST_PARAMETER_LABEL));
		assertTrue(_param.getComment().equals(TEST_PARAMETER_COMMENT));
	}
}
