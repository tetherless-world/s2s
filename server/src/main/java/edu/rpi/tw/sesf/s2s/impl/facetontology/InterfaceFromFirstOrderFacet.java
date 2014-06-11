package edu.rpi.tw.sesf.s2s.impl.facetontology;

import java.util.Vector;

import net.fortytwo.ripple.RippleException;

import edu.rpi.tw.sesf.facetontology.FirstOrderFacet;
import edu.rpi.tw.sesf.s2s.Interface;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.utils.Ontology;

public class InterfaceFromFirstOrderFacet extends Interface {
	
	private static final long serialVersionUID = -7907106917367560252L;
	
	FirstOrderFacet facet;
	
	public InterfaceFromFirstOrderFacet(FirstOrderFacet f) {
		facet = f;
	}
	
	@Override
	public DataSource getDataSource() {
		return facet.getDataSource();
	}

	@Override
	public void setDataSource(DataSource source)
			throws IncompatibleDataSourceException,
			UnregisteredInstanceException, RippleException {
		facet.setDataSource(source);
	}

	@Override
	public String getURI() {
		return facet.getURI();
	}

	@Override
	public Vector<String> getTypes() {
		Vector<String> types = new Vector<String>();
		types.add(Ontology.facetOntologyResultsJson);
		return types;
	}

	@Override
	public String getOutput() {
		return facet.getFacetClass();
	}

	@Override
	public String getLabel() {
		return facet.getLabel();
	}

	@Override
	public String getComment() {
		return facet.getComment();
	}

	@Override
	public String getInput() {
		return facet.getURI();
	}

	@Override
	public int getLimit() {
		return facet.getDefaultLimit();
	}
}
