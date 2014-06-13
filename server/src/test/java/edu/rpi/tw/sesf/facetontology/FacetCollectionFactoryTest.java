package edu.rpi.tw.sesf.facetontology;

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.rpi.tw.sesf.facetontology.ConnectedFacet;
import edu.rpi.tw.sesf.facetontology.FacetCollection;
import edu.rpi.tw.sesf.facetontology.FacetCollectionFactory;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;
import junit.framework.TestCase;

public class FacetCollectionFactoryTest extends TestCase {

	private static final String TEST_FACETCOLLECTION_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#FacetCollection";
	private static final Collection<String> TEST_FACET_LABELS = Arrays.asList("SPARQL Test Facet");
	
	private FacetCollectionFactory _jenaFactory;
	private FacetCollectionFactory _cachedFactory;

	/**
	 * @param name
	 */
	public FacetCollectionFactoryTest(String name) {
		super(name);
		
		Vector<DataSource> source = new Vector<>();
		OntModel m = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		source.add(new JenaPelletSource("test",m));
		m.read(TEST_FACETCOLLECTION_URI);
		_jenaFactory = new FacetCollectionFactory(source, false);
		_cachedFactory = new FacetCollectionFactory(source, true);
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
	
	public void testCreateCollectionFromJena() {
		FacetCollection facets = _jenaFactory.createFacetCollection(TEST_FACETCOLLECTION_URI);
		int count = 0;
		for (ConnectedFacet f : facets.getConnectedFacets()) {
			if (TEST_FACET_LABELS.contains(f.getLabel())) count++;
		}
		assertTrue(count == TEST_FACET_LABELS.size());
	}

	public void testCreateCollectionFromCache() {
        _cachedFactory.createFacetCollection(TEST_FACETCOLLECTION_URI);
		FacetCollection facets = _cachedFactory.createFacetCollection(TEST_FACETCOLLECTION_URI);
		int count = 0;
		for (ConnectedFacet f : facets.getConnectedFacets()) {
			if (TEST_FACET_LABELS.contains(f.getLabel())) count++;
		}
		assertTrue(count == TEST_FACET_LABELS.size());
		assertTrue(facets.isFromCache());
	}
}
