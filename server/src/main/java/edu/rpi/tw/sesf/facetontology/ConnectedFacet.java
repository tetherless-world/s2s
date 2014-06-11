package edu.rpi.tw.sesf.facetontology;

import java.util.Collection;

public interface ConnectedFacet extends Facet {
	public abstract Collection<Predicate> getPredicatePaths();
	public abstract Collection<String> getCoDependentFacets();
}
