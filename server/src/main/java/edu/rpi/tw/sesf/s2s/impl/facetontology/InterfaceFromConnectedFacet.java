package edu.rpi.tw.sesf.s2s.impl.facetontology;

import java.util.Vector;

import net.fortytwo.ripple.RippleException;

import edu.rpi.tw.sesf.facetontology.ConnectedFacet;
import edu.rpi.tw.sesf.s2s.Interface;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.utils.Ontology;

public class InterfaceFromConnectedFacet extends Interface {

	private static final long serialVersionUID = 48594098483027902L;

	ConnectedFacet facet;
	
	public InterfaceFromConnectedFacet(ConnectedFacet f) {
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
		types.add(Ontology.InputValuesInterface);
		return types;
	}

	@Override
	public String getOutput() {
		if (ConnectedFacet.class.isAssignableFrom(facet.getClass())) {
			return Ontology.LabelIdCountJsonArray;
		} else {
			return facet.getFacetClass();
		}
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
		return -1;
	}
}
