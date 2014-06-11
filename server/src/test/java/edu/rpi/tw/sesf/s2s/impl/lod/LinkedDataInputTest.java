/**
 * 
 */
package edu.rpi.tw.sesf.s2s.impl.lod;

import net.fortytwo.ripple.query.QueryEngine;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.impl.lod.InputFromRipple;
import edu.rpi.tw.sesf.s2s.utils.RippleQueryEngineSingleton;
import junit.framework.TestCase;

/**
 * @author rozele
 *
 */
public class LinkedDataInputTest extends TestCase {

	private static final String TEST_PARAMETER_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInput";
	private static final String TEST_PARAMETER_LABEL = "Test Input";
	private static final String TEST_PARAMETER_COMMENT = "Test Input";
	
	private InputFromRipple _param;
	
	/**
	 * @param name
	 */
	public LinkedDataInputTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		QueryEngine qe = RippleQueryEngineSingleton.getInstance();
		_param = new InputFromRipple(TEST_PARAMETER_URI, new RippleSource("test",qe));
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
