/**
 * 
 */
package edu.rpi.tw.sesf.s2s.impl.lod;

import net.fortytwo.ripple.query.QueryEngine;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.impl.lod.InterfaceFromRipple;
import edu.rpi.tw.sesf.s2s.utils.Ontology;
import edu.rpi.tw.sesf.s2s.utils.RippleQueryEngineSingleton;
import junit.framework.TestCase;

/**
 * @author rozele
 *
 */
public class LinkedDataInterfaceTest extends TestCase {

	private static final String TEST_QUERY_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInputQuery";
	private static final Object TEST_QUERY_PARAM = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInput";
	private static final Object TEST_QUERY_COMMENT = "Test Query Interface";
	private static final Object TEST_QUERY_LABEL = "Test Query Interface";
	private static final Object TEST_QUERY_OUTPUT = "http://escience.rpi.edu/ontology/sesf/s2s-core/4/0/LabelIdJsonArray";
	private static final Object TEST_QUERY_TYPE = Ontology.InputValuesInterface;
	
	private InterfaceFromRipple _query;
	
	/**
	 * @param name
	 */
	public LinkedDataInterfaceTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		QueryEngine qe = RippleQueryEngineSingleton.getInstance();
		_query = new InterfaceFromRipple(TEST_QUERY_URI, new RippleSource("test",qe));
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
