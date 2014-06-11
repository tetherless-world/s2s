/**
 * 
 */
package edu.rpi.tw.sesf.s2s.impl.lod;

import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.utils.Ontology;
import edu.rpi.tw.sesf.s2s.utils.RippleQueryEngineSingleton;
import junit.framework.TestCase;
import net.fortytwo.ripple.query.QueryEngine;

/**
 * @author rozele
 *
 */
public class LinkedDataWidgetTest extends TestCase {

	private static final String TEST_WIDGET_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestWidget";
	private static final String TEST_WIDGET_LABEL = "Test Widget";
	private static final String TEST_WIDGET_COMMENT = "Test Widget";
	private static final String TEST_WIDGET_SCRIPT = "http://escience.rpi.edu/s2s/test/TestWidget.js";
	private static final String TEST_WIDGET_STYLESHEET = "http://escience.rpi.edu/s2s/test/TestWidget.css";
	private static final String TEST_WIDGET_PROTOTYPE = "testWidgetPrototype";
	private static final String TEST_WIDGET_OUTPUT = "http://escience.rpi.edu/ontology/sesf/s2s-core/4/0/LabelIdJsonArray";
	private static final String TEST_WIDGET_PARAM = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInput";
	private static final String TEST_WIDGET_TYPE = Ontology.InputWidget;
	
	private WidgetFromRipple _widget;
	
	/**
	 * @param name
	 */
	public LinkedDataWidgetTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		QueryEngine qe = RippleQueryEngineSingleton.getInstance();
		_widget = new WidgetFromRipple(TEST_WIDGET_URI, new RippleSource("test",qe));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		_widget = null;
	}
	
	public void testWidget() {
		assertTrue(_widget.getURI().equals(TEST_WIDGET_URI));
		assertTrue(_widget.getLabel().equals(TEST_WIDGET_LABEL));
		assertTrue(_widget.getComment().equals(TEST_WIDGET_COMMENT));
		assertTrue(_widget.getRequiredScripts().contains(TEST_WIDGET_SCRIPT));
		assertTrue(_widget.getRequiredStylesheets().contains(TEST_WIDGET_STYLESHEET));
		assertTrue(_widget.getPrototype().equals(TEST_WIDGET_PROTOTYPE));
		assertTrue(_widget.getSupportedOutputs().contains(TEST_WIDGET_OUTPUT));
		assertTrue(_widget.getSupportedInputs().contains(TEST_WIDGET_PARAM));
		assertTrue(_widget.getTypes().contains(TEST_WIDGET_TYPE));
	}
}
