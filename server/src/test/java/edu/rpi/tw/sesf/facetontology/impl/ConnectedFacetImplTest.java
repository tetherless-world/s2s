package edu.rpi.tw.sesf.facetontology.impl;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.rpi.tw.sesf.facetontology.FacetType;
import edu.rpi.tw.sesf.facetontology.Predicate;
import edu.rpi.tw.sesf.facetontology.impl.ConnectedFacetImpl;
import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.utils.RippleQueryEngineSingleton;
import junit.framework.TestCase;

public class ConnectedFacetImplTest extends TestCase {
	
	private static final String TEST_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestConnectedFacet";
	private static final String TEST_LABEL = "SPARQL Test Facet";
	private static final String TEST_PREDICATE = "http://escience.rpi.edu/s2s/test/4.0/test.owl#myPredicate";
	private static final FacetType TEST_FACET_TYPE = FacetType.Literal;

	private ConnectedFacetImpl _facet;
	private ConnectedFacetImpl _linkedFacet;
	
	/**
	 * @param name
	 */
	public ConnectedFacetImplTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		OntModel m = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		m.read(TEST_URI);
		_facet = new ConnectedFacetImpl(TEST_URI, new JenaPelletSource("test",m));
		_linkedFacet = new ConnectedFacetImpl(TEST_URI, new RippleSource("test",RippleQueryEngineSingleton.getInstance()));
		//TODO: linked data facet test
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		_facet = null;
		_linkedFacet = null;
	}
	
	public void testSparql() {
		assertTrue(_facet.getLabel().equals(TEST_LABEL));
		boolean b = false;
		for (Predicate p : _facet.getPredicatePaths()) {
			if (p.getPredicateValue().equals(TEST_PREDICATE)) b = true;
		}
		assertTrue(b);
		assertTrue(_facet.getFacetType().equals(TEST_FACET_TYPE));
	}
	
	public void testLinked() {
		assertTrue(_linkedFacet.getLabel().equals(TEST_LABEL));
		boolean b = false;
		for (Predicate p : _linkedFacet.getPredicatePaths()) {
			if (p.getPredicateValue().equals(TEST_PREDICATE)) b = true;
		}
		assertTrue(b);
		assertTrue(_linkedFacet.getFacetType().equals(TEST_FACET_TYPE));
	}
}
