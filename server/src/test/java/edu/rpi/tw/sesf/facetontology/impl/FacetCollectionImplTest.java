package edu.rpi.tw.sesf.facetontology.impl;

import java.util.Arrays;
import java.util.Collection;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.rpi.tw.sesf.facetontology.ConnectedFacet;
import edu.rpi.tw.sesf.facetontology.impl.FacetCollectionImpl;
import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.utils.RippleQueryEngineSingleton;
import junit.framework.TestCase;

public class FacetCollectionImplTest extends TestCase {
	private static final String TEST_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#FacetCollection";
	private static final String TEST_FIRST_ORDER_FACET_LABEL = "SPARQL Test First Order Facet";
	private static final Collection<String> TEST_FACET_LABELS = Arrays.asList("SPARQL Test Facet");

	private FacetCollectionImpl _facets;
	private FacetCollectionImpl _linked;
	
	/**
	 * @param name
	 */
	public FacetCollectionImplTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		OntModel m = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		m.read(TEST_URI);
		_facets = new FacetCollectionImpl(TEST_URI, new JenaPelletSource("test",m));
		_linked = new FacetCollectionImpl(TEST_URI, new RippleSource("test",RippleQueryEngineSingleton.getInstance()));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		_facets = null;
		_linked = null;
	}
	
	public void testSparql() {
		assertTrue(_facets.getFirstOrderFacet().getLabel().equals(TEST_FIRST_ORDER_FACET_LABEL));
		int count = 0;
		for (ConnectedFacet f : _facets.getConnectedFacets()) {
			if (TEST_FACET_LABELS.contains(f.getLabel())) count++;
		}
		assertTrue(count == TEST_FACET_LABELS.size());
	}
	
	public void testLinked() {
		assertTrue(_linked.getFirstOrderFacet().getLabel().equals(TEST_FIRST_ORDER_FACET_LABEL));
		int count = 0;
		for (ConnectedFacet f : _linked.getConnectedFacets()) {
			if (TEST_FACET_LABELS.contains(f.getLabel())) count++;
		}
		assertTrue(count == TEST_FACET_LABELS.size());
	}
}
