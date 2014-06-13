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
public class SearchServiceFactoryTest extends TestCase {

	private static final String TEST_SERVICE_PARAMETER = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestInput";
	private static final String TEST_SERVICE_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#TestSearchService";
	private static final String TEST_SERVICE_LABEL = "Test Search Service";
	private static final String TEST_SERVICE_COMMENT = "Test Search Service for S2S Server 3.0";
	
	private SearchServiceFactory _linkedSearchServiceFactory;
	private SearchServiceFactory _cachedLinkedSearchServiceFactory;
	private SearchServiceFactory _jenaSearchServiceFactory;

	/**
	 * @param name
	 */
	public SearchServiceFactoryTest(String name) {
		super(name);
		QueryEngine qe = RippleQueryEngineSingleton.getInstance();
		Vector<DataSource> source1 = new Vector<>();
		Vector<DataSource> source2 = new Vector<>();
		OntModel m = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		m.read(TEST_SERVICE_URI);
		source2.add(new JenaPelletSource("test",m));
		source1.add(new RippleSource("test",qe));
		_linkedSearchServiceFactory = new SearchServiceFactory(source1, false);
		_cachedLinkedSearchServiceFactory = new SearchServiceFactory(source1, true);
		_jenaSearchServiceFactory = new SearchServiceFactory(source2, false);
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

	public void testCreateSearchServiceFromLinkedData() {
		SearchService service = _linkedSearchServiceFactory.createSearchService(TEST_SERVICE_URI);
		assertTrue(service.getComment().equals(TEST_SERVICE_COMMENT));
		assertTrue(service.getLabel().equals(TEST_SERVICE_LABEL));
		assertTrue(service.getWebServiceEngine().getInputs().size() == 2);
		assertTrue(service.getWebServiceEngine().getInputs().contains(TEST_SERVICE_PARAMETER));
	}

	public void testCreateSearchServiceFromJena() {
		SearchService service = _jenaSearchServiceFactory.createSearchService(TEST_SERVICE_URI);
		assertTrue(service.getComment().equals(TEST_SERVICE_COMMENT));
		assertTrue(service.getLabel().equals(TEST_SERVICE_LABEL));
		assertTrue(service.getWebServiceEngine().getInputs().size() == 2);
		assertTrue(service.getWebServiceEngine().getInputs().contains(TEST_SERVICE_PARAMETER));
	}
	
	public void testCreateLinkedDataSearchServiceFromCache() {
        _cachedLinkedSearchServiceFactory.createSearchService(TEST_SERVICE_URI);
		SearchService service = _cachedLinkedSearchServiceFactory.createSearchService(TEST_SERVICE_URI);
		assertTrue(service.isFromCache());
		assertTrue(service.getComment().equals(TEST_SERVICE_COMMENT));
		assertTrue(service.getLabel().equals(TEST_SERVICE_LABEL));
		assertTrue(service.getWebServiceEngine().getInputs().size() == 2);
		assertTrue(service.getWebServiceEngine().getInputs().contains(TEST_SERVICE_PARAMETER));
	}

}
