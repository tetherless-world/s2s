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
public class InterfaceFactoryTest extends TestCase {

	private static final String TEST_QUERY_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInputQuery";
	private static final Object TEST_QUERY_COMMENT = "Test Query Interface";
	private static final Object TEST_QUERY_LABEL = "Test Query Interface";
	
	private InterfaceFactory _linkedInterfaceFactory;
	private InterfaceFactory _cachedLinkedInterfaceFactory;
	private InterfaceFactory _jenaInterfaceFactory;

	/**
	 * @param name
	 */
	public InterfaceFactoryTest(String name) {
		super(name);
		QueryEngine qe = RippleQueryEngineSingleton.getInstance();
		Vector<DataSource> source1 = new Vector<DataSource>();
		Vector<DataSource> source2 = new Vector<DataSource>();
		source1.add(new RippleSource("test",qe));
		OntModel m = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		m.read(TEST_QUERY_URI);
		source2.add(new JenaPelletSource("test",m));
		_linkedInterfaceFactory = new InterfaceFactory(source1, false);
		_cachedLinkedInterfaceFactory = new InterfaceFactory(source1, true);
		_jenaInterfaceFactory = new InterfaceFactory(source2, false);
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
	
	public void testCreateQueryInterfaceFromLinkedData() {
		Interface query = _linkedInterfaceFactory.createInterface(TEST_QUERY_URI);
		assertTrue(query.getLabel().equals(TEST_QUERY_LABEL));
		assertTrue(query.getComment().equals(TEST_QUERY_COMMENT));
	}
	
	public void testCreateQueryInterfaceFromJena() {
		Interface query = _jenaInterfaceFactory.createInterface(TEST_QUERY_URI);
		assertTrue(query.getLabel().equals(TEST_QUERY_LABEL));
		assertTrue(query.getComment().equals(TEST_QUERY_COMMENT));
	}
	
	public void testCreateLinkedDataQueryInterfaceFromCache() {
		Interface query = _cachedLinkedInterfaceFactory.createInterface(TEST_QUERY_URI);
		query = _cachedLinkedInterfaceFactory.createInterface(TEST_QUERY_URI);
		assertTrue(query.isFromCache());
		assertTrue(query.getLabel().equals(TEST_QUERY_LABEL));
		assertTrue(query.getComment().equals(TEST_QUERY_COMMENT));
	}
	
}
