package edu.rpi.tw.sesf.s2s.impl.facetontology;

import net.fortytwo.ripple.RippleException;
import edu.rpi.tw.sesf.facetontology.ConnectedFacet;
import edu.rpi.tw.sesf.s2s.Input;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.utils.Configuration;

public class InputFromConnectedFacet extends Input {

	private static final long serialVersionUID = -7158403214675493017L;

	ConnectedFacet facet;
	
	public InputFromConnectedFacet(ConnectedFacet f) {
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
	public String getLabel() {
		return facet.getLabel();
	}

	@Override
	public String getComment() {
		return facet.getComment();
	}

	@Override
	public String getDelimiter() {
		return Configuration.facetInputDelimiter;
	}
}
