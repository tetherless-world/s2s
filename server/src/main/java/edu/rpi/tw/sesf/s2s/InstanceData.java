package edu.rpi.tw.sesf.s2s;

import java.io.Serializable;

import net.fortytwo.ripple.RippleException;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;

public interface InstanceData extends Serializable {
	public DataSource getDataSource();
	public void setDataSource(DataSource source) throws IncompatibleDataSourceException, UnregisteredInstanceException, RippleException;
	public boolean isFromCache();
	public void setFromCache();
}
