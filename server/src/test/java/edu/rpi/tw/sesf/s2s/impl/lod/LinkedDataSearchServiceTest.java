/**
 * 
 */
package edu.rpi.tw.sesf.s2s.impl.lod;

import net.fortytwo.ripple.query.QueryEngine;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.impl.lod.SearchServiceFromRipple;
import edu.rpi.tw.sesf.s2s.utils.Ontology;
import edu.rpi.tw.sesf.s2s.utils.RippleQueryEngineSingleton;
import junit.framework.TestCase;

/**
 * @author rozele
 *
 */
public class LinkedDataSearchServiceTest extends TestCase {

	private static final String TEST_SERVICE_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestSearchService";
	private static final String TEST_SERVICE_LABEL = "Test Search Service";
	private static final String TEST_SERVICE_COMMENT = "Test Search Service for S2S Server 3.0";
	private static final String TEST_SERVICE_CONFIG = "http://escience.rpi.edu/s2s/test/default.xml";
	private static final String TEST_SERVICE_TYPE = Ontology.OpenSearchService;
	private static final String TEST_SERVICE_LINK = "http://tw.rpi.edu/web/project/sesf/workinggroups/s2s";

	
	private SearchServiceFromRipple _service;
	
	/**
	 * Constructor for LinkedDataSearchServiceTest, calls superclass constructor
	 * @param name
	 */
	public LinkedDataSearchServiceTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		QueryEngine qe = RippleQueryEngineSingleton.getInstance();
		_service = new SearchServiceFromRipple(TEST_SERVICE_URI, new RippleSource("test",qe));
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
