package edu.rpi.tw.sesf.facetontology;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import edu.rpi.tw.sesf.s2s.InstanceData;

public abstract class Predicate implements Serializable, InstanceData {
	
	private static final long serialVersionUID = 912731690021735383L;

	public abstract Predicate getNextPredicate();
	public abstract String getPredicateValue();
	public abstract String getSparqlBinding();
	public abstract String getSparqlGraph();
	public abstract boolean isReverse();
	public abstract boolean isTransitive();
	public abstract boolean isOptional();
	
	public Collection<Predicate> getList() {
		Vector<Predicate> ret = new Vector<>();
		Predicate p = this;
		while (p != null) {
			ret.add(p);
			p = p.getNextPredicate();
		}
		Collections.reverse(ret);
		return ret;
	}
}
