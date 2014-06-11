package edu.rpi.tw.sesf.s2s.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import edu.rpi.tw.sesf.s2s.exception.UrlParameterException;

public class SessionServlet extends HttpServlet {

	private static final long serialVersionUID = 8807688926959544012L;

	private Log log = LogFactory.getLog(SessionServlet.class);
	
	private String _directory;
	
	public SessionServlet() {}
	
	@Override
	public void init(ServletConfig config) throws ServletException{
		super.init(config);
		
		//set up servlet based on configuration
		_directory = config.getInitParameter("s2s-session-directory");
		
		//initialize log4j
		String log4jLocation = config.getInitParameter("log4j-properties-location");
		ServletContext sc = config.getServletContext();
		if (log4jLocation == null) {
			System.err.println("*** No log4j-properties-location init param, so initializing log4j with BasicConfigurator");
			BasicConfigurator.configure();
		} else {
			String webAppPath = sc.getRealPath("/");
			String log4jProp = webAppPath + log4jLocation;
			File f = new File(log4jProp);
			if (f.exists()) {
				PropertyConfigurator.configure(log4jProp);
			} else {
				System.err.println("*** " + log4jProp + " file not found, so initializing log4j with BasicConfigurator");
				BasicConfigurator.configure();
			}
		}
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		try {
			String[] key = request.getParameterValues("key");
			if (key == null || key.length != 1)
			{
				throw new UrlParameterException("Exactly one \"key\" parameter should be provided");
			}

			File f = new File(_directory + "/" + key[0]);
			if (!f.canRead()) {
				throw new UrlParameterException(422, "Session does not exist for \"key\" = " + key[0]);
			}
			BufferedReader reader = new BufferedReader(new FileReader(f));
			char[] buffer = new char[65536];
			int n;
			StringWriter sw = new StringWriter();
			while ((n = reader.read(buffer)) > 0) sw.write(buffer, 0, n);
			response.setHeader("Content-Type", "application/json");
			Writer writer = response.getWriter();
			writer.write(sw.toString());
		} catch (UrlParameterException e) {
    		log.error(e.getMessage(), e);
    		response.sendError(e.getErrorCode(), e.getMessage());
    	}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		try {
			
			BufferedReader reader = request.getReader();
			StringWriter sw = new StringWriter();
			char[] buffer = new char[65536];
			int n;
			while ((n = reader.read(buffer)) > 0) sw.write(buffer, 0, n);
			
			String key = Integer.toString(sw.toString().hashCode());
			key = key.replace('-', 'n');
			
			File f;
			if (_directory != null && _directory != "") f = new File(_directory + "/" + key);
			else f = new File(key);
			f.createNewFile();
			if (!f.canWrite()) {
				throw new UrlParameterException(422, "Cannot create session file for \"key\" = " + key);
			}
			
			BufferedWriter fw = new BufferedWriter(new FileWriter(f));
			fw.write(sw.toString());
			fw.flush();
			
			Writer rw = response.getWriter();
			rw.write(key);
		} catch (UrlParameterException e) {
    		log.error(e.getMessage(), e);
    		response.sendError(e.getErrorCode(), e.getMessage());
    	}
	}
}
