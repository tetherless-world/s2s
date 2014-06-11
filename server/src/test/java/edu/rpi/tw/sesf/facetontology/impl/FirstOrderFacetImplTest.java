package edu.rpi.tw.sesf.facetontology.impl;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.rpi.tw.sesf.facetontology.FacetType;
import edu.rpi.tw.sesf.facetontology.Filter;
import edu.rpi.tw.sesf.facetontology.impl.FirstOrderFacetImpl;
import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.utils.Namespace;
import edu.rpi.tw.sesf.s2s.utils.RippleQueryEngineSingleton;
import junit.framework.TestCase;

public class FirstOrderFacetImplTest extends TestCase {

	private static final String TEST_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestFirstOrderFacet";
	private static final String TEST_LABEL = "SPARQL Test First Order Facet";
	private static final String TEST_CLASS = "http://escience.rpi.edu/s2s/test/4.0/test.owl#MyFacetClass";
	private static final FacetType TEST_FACET_TYPE = FacetType.Object;
	private static final String TEST_ITEM_LABEL = Namespace.rdfs.getURI() + "label";
	private static final String TEST_FILTER = "{var} <= 10";
	
	private FirstOrderFacetImpl _facet;
	private FirstOrderFacetImpl _linked;
	
	/**
	 * @param name
	 */
	public FirstOrderFacetImplTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		OntModel m = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		m.read(TEST_URI);
		_facet = new FirstOrderFacetImpl(TEST_URI, new JenaPelletSource("test",m));
		_linked = new FirstOrderFacetImpl(TEST_URI, new RippleSource("test",RippleQueryEngineSingleton.getInstance()));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		_facet = null;
		_linked = null;
	}
	
	public void testSparql() {
		assertTrue(_facet.getFacetClass().equals(TEST_CLASS));
		assertTrue(_facet.getLabel().equals(TEST_LABEL));
		assertTrue(_facet.getItemLabelPredicate().getPredicateValue().equals(TEST_ITEM_LABEL));
		assertTrue(_facet.getFacetType().equals(TEST_FACET_TYPE));
		boolean exists = false;
		for (Filter f : _facet.getFilters()) {
			if (f.getFilterValue().equals(TEST_FILTER)) exists = true;
		}
		assertTrue(exists);
	}
	
	public void testLinked() {
		assertTrue(_linked.getFacetClass().equals(TEST_CLASS));
		assertTrue(_linked.getLabel().equals(TEST_LABEL));
		assertTrue(_linked.getItemLabelPredicate().getPredicateValue().equals(TEST_ITEM_LABEL));
		assertTrue(_linked.getFacetType().equals(TEST_FACET_TYPE));
		boolean exists = false;
		for (Filter f : _linked.getFilters()) {
			if (f.getFilterValue().equals(TEST_FILTER)) exists = true;
		}
		assertTrue(exists);
	}
}
