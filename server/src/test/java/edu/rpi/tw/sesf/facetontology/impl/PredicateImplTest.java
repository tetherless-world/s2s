package edu.rpi.tw.sesf.facetontology.impl;

import com.hp.hpl.jena.ontology.OntModelSpec;
////import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.rpi.tw.sesf.facetontology.impl.PredicateImpl;
import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.utils.RippleQueryEngineSingleton;
import junit.framework.TestCase;

public class PredicateImplTest extends TestCase {	

	private static final String TEST_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestPredicate";
	private static final String TEST_FIRST_PREDICATE = "http://escience.rpi.edu/s2s/test/4.0/test.owl#myFirstFilterPredicate";
	private static final String TEST_SECOND_PREDICATE = "http://escience.rpi.edu/s2s/test/4.0/test.owl#mySecondFilterPredicate";

	private PredicateImpl _pred;
	private PredicateImpl _linked;
	
	/**
	 * @param name
	 */
	public PredicateImplTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);

		m.read(TEST_URI);
		_pred = new PredicateImpl(TEST_URI, new JenaPelletSource("test",m));
		_linked = new PredicateImpl(TEST_URI, new RippleSource("test",RippleQueryEngineSingleton.getInstance()));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		_pred = null;
		_linked = null;
	}
	
	public void testSparql() {
		assertTrue(_pred.getPredicateValue().equals(TEST_FIRST_PREDICATE));
		assertTrue(_pred.getNextPredicate().getPredicateValue().equals(TEST_SECOND_PREDICATE));
		assertTrue(_pred.isReverse());
	}

	public void testLinked() {
		assertTrue(_linked.getPredicateValue().equals(TEST_FIRST_PREDICATE));
		assertTrue(_linked.getNextPredicate().getPredicateValue().equals(TEST_SECOND_PREDICATE));
		assertTrue(_linked.isReverse());
	}
}
