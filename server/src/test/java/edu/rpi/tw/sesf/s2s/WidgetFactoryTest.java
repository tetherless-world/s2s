/**
 * 
 */
package edu.rpi.tw.sesf.s2s;

import java.util.Vector;

//import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import net.fortytwo.ripple.query.QueryEngine;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.utils.RippleQueryEngineSingleton;
import junit.framework.TestCase;

/**
 * @author rozele
 *
 */
public class WidgetFactoryTest extends TestCase {
	
	private static final String TEST_WIDGET_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestWidget";
	private static final String TEST_WIDGET_LABEL = "Test Widget";
	private static final String TEST_WIDGET_COMMENT = "Test Widget";
	
	private WidgetFactory _linkedWidgetFactory;
	private WidgetFactory _cachedLinkedWidgetFactory;
	private WidgetFactory _jenaWidgetFactory;

	/**
	 * @param name
	 */
	public WidgetFactoryTest(String name) {
		super(name);
		QueryEngine qe = RippleQueryEngineSingleton.getInstance();
		Vector<DataSource> source1 = new Vector<>();
		Vector<DataSource> source2 = new Vector<>();
		OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
		m.read(TEST_WIDGET_URI);
		source2.add(new JenaPelletSource("test",m));
		source1.add(new RippleSource("test",qe));
		_linkedWidgetFactory = new WidgetFactory(source1, false);
		_cachedLinkedWidgetFactory = new WidgetFactory(source1, true);
		_jenaWidgetFactory = new WidgetFactory(source2, false);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testCreateWidgetFromLinkedData() {
		Widget widget = _linkedWidgetFactory.createWidget(TEST_WIDGET_URI);
		assertTrue(widget.getLabel().equals(TEST_WIDGET_LABEL));
		assertTrue(widget.getComment().equals(TEST_WIDGET_COMMENT));
	}
	
	public void testCreateWidgetFromJena() {
		Widget widget = _jenaWidgetFactory.createWidget(TEST_WIDGET_URI);
		assertTrue(widget.getLabel().equals(TEST_WIDGET_LABEL));
		assertTrue(widget.getComment().equals(TEST_WIDGET_COMMENT));
	}
	
	public void testCreateLinkedDateWidgetFromCache() {
		_cachedLinkedWidgetFactory.createWidget(TEST_WIDGET_URI);
        Widget widget = _cachedLinkedWidgetFactory.createWidget(TEST_WIDGET_URI);
		assertTrue(widget.isFromCache());
		assertTrue(widget.getLabel().equals(TEST_WIDGET_LABEL));
		assertTrue(widget.getComment().equals(TEST_WIDGET_COMMENT));
	}

}
