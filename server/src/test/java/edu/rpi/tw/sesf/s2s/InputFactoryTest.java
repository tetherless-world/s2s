/**
 * 
 */
package edu.rpi.tw.sesf.s2s;

import java.util.Vector;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
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
public class InputFactoryTest extends TestCase {
	
	private static final String TEST_PARAMETER_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInput";
	private static final String TEST_PARAMETER_LABEL = "Test Input";
	private static final String TEST_PARAMETER_COMMENT = "Test Input";
	
	private InputFactory _linkedInputFactory;
	private InputFactory _cachedLinkedInputFactory;
	private InputFactory _jenaInputFactory;

	/**
	 * @param name
	 */
	public InputFactoryTest(String name) {
		super(name);
		QueryEngine qe = RippleQueryEngineSingleton.getInstance();
		Vector<DataSource> source1 = new Vector<DataSource>();
		Vector<DataSource> source2 = new Vector<DataSource>();
		source1.add(new RippleSource("test",qe));
		OntModel m = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		m.read(TEST_PARAMETER_URI);
		source2.add(new JenaPelletSource("test",m));
		_linkedInputFactory = new InputFactory(source1, false);
		_cachedLinkedInputFactory = new InputFactory(source1, true);
		_jenaInputFactory = new InputFactory(source2, false);
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

	public void testCreateInputFromLinkedData() {
		Input param = _linkedInputFactory.createInput(TEST_PARAMETER_URI);
		assertTrue(param.getLabel().equals(TEST_PARAMETER_LABEL));
		assertTrue(param.getComment().equals(TEST_PARAMETER_COMMENT));
	}
	
	public void testCreateInputFromJena() {
		Input param = _jenaInputFactory.createInput(TEST_PARAMETER_URI);
		assertTrue(param.getLabel().equals(TEST_PARAMETER_LABEL));
		assertTrue(param.getComment().equals(TEST_PARAMETER_COMMENT));
	}
	
	public void testCreateLinkedDataInputFromCache() {
		Input param = _cachedLinkedInputFactory.createInput(TEST_PARAMETER_URI);
		param = _cachedLinkedInputFactory.createInput(TEST_PARAMETER_URI);
		assertTrue(param.isFromCache());
		assertTrue(param.getLabel().equals(TEST_PARAMETER_LABEL));
		assertTrue(param.getComment().equals(TEST_PARAMETER_COMMENT));
	}
}
