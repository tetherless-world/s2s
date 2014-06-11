package edu.rpi.tw.sesf.facetontology.impl;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import junit.framework.TestCase;
import edu.rpi.tw.sesf.facetontology.impl.FilterImpl;
import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.utils.RippleQueryEngineSingleton;

public class FilterImplTest extends TestCase {
	private static final String TEST_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestFilter";
	private static final String TEST_FILTER_VALUE = "{var} <= 10";
	private static final String TEST_FILTER_PREDICATE = "http://escience.rpi.edu/s2s/test/4.0/test.owl#myFilterPredicate";
	
	private FilterImpl _filter;
	private FilterImpl _linked;
	
	/**
	 * @param name
	 */
	public FilterImplTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		OntModel m = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		m.read(TEST_URI);
		_filter = new FilterImpl(TEST_URI, new JenaPelletSource("test",m));
		_linked = new FilterImpl(TEST_URI, new RippleSource("test",RippleQueryEngineSingleton.getInstance()));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		_filter = null;
		_linked = null;
	}
	
	public void testSparql() {
		assertTrue(_filter.getFilterValue().equals(TEST_FILTER_VALUE));
		assertTrue(_filter.getFirstPredicate().getPredicateValue().equals(TEST_FILTER_PREDICATE));
	}

	public void testLinked() {
		assertTrue(_linked.getFilterValue().equals(TEST_FILTER_VALUE));
		assertTrue(_linked.getFirstPredicate().getPredicateValue().equals(TEST_FILTER_PREDICATE));
	}
}
