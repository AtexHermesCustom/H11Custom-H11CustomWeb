package com.atex.h11.custom.web.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atex.h11.custom.common.DataSource;
import com.atex.h11.custom.common.Edition;
import com.atex.h11.custom.common.HermesObject;
import com.atex.h11.custom.common.LogPage;
import com.atex.h11.custom.common.Newspaper;
import com.atex.h11.custom.common.PhysPage;
import com.atex.h11.custom.web.common.Constants;
import com.unisys.media.cr.adapter.ncm.common.data.pk.NCMObjectPK;
import com.unisys.media.cr.adapter.ncm.model.data.datasource.NCMDataSource;


/**
 * Servlet implementation class ExportServlet
 */
public class ExportXMLServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private static final String CONFIG_FILENAME = "h11-custom-web-export.properties";	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ExportXMLServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        NCMDataSource ds = null;

        String user = request.getParameter("user");
        String password = request.getParameter("password");
        String sessionId = request.getParameter("sessionid");
        String nodeType = request.getParameter("nodetype");

        log(request.getParameterMap().toString());  // For debugging only       
        
        Properties props = getProperties();
        
        String convertFormat = props.getProperty("convert.format", "Neutral");
        String outDir = props.getProperty("output.dir");
        if (outDir == null || outDir.isEmpty()) {
        	throw new IllegalArgumentException("Output directory not defined");
        }
        
        // default credentials
        if (user == null && password == null) {
        	user = props.getProperty("user");
        	password = props.getProperty("password");
        }
                       
        if (sessionId != null) {
            ds = (NCMDataSource) DataSource.newInstance(sessionId);
        } else {
            ds = (NCMDataSource) DataSource.newInstance(user, password);
        }
               
        try {       	
            if (nodeType.equalsIgnoreCase(Constants.NODETYPE_OBJECT) || nodeType.equalsIgnoreCase(Constants.NODETYPE_SP)) {
                String id = request.getParameter("id");
            	
            	HermesObject obj = new HermesObject(ds);
            	obj.setConvertFormat(convertFormat);
            	
            	String outFilename;
            	if (nodeType.equalsIgnoreCase(Constants.NODETYPE_SP)) {
            		outFilename = "sp_" + id + ".xml";
            	} else {
            		outFilename = "obj_" + id + ".xml";
            	}
            	File outFile = new File(outDir, outFilename);
            	
            	NCMObjectPK pk = new NCMObjectPK(Integer.parseInt(id));
            	obj.export(pk, outFile);
            	
            	showOutputMessage(out, outFile.getPath(), request);
            	
            } else if (nodeType.equalsIgnoreCase(Constants.NODETYPE_LOGPAGE)) {
            	String pubDate = request.getParameter("pubdate");
            	String edition = request.getParameter("edition");
            	String level = request.getParameter("level");
            	String name = request.getParameter("name");
            	
                LogPage lp = new LogPage(ds);
                lp.setConvertFormat(convertFormat);
                
            	File outFile = new File(outDir, "logpage_" + name + ".xml");
            	
            	lp.write(level, edition, Integer.parseInt(pubDate), name, new FileOutputStream(outFile));
            	
            	showOutputMessage(out, outFile.getPath(), request);
            	
            } else if (nodeType.equalsIgnoreCase(Constants.NODETYPE_PHYSPAGE)) {
            	String pubDate = request.getParameter("pubdate");
            	String level = request.getParameter("level");
            	String edition = request.getParameter("edition");
            	String sequenceNum = request.getParameter("number");
            	
            	PhysPage pp = new PhysPage(ds);
            	pp.setConvertFormat(convertFormat);
            	
            	File outFile = new File(outDir, "physpage_" 
            		+ pubDate + "_" + level + "_" + edition + "_" + sequenceNum + ".xml");
            	
            	pp.write(level, edition, Integer.parseInt(pubDate), Short.parseShort(sequenceNum), new FileOutputStream(outFile));
            	
            	showOutputMessage(out, outFile.getPath(), request);
            	
            } else if (nodeType.equalsIgnoreCase(Constants.NODETYPE_EDITION)) {
            	String pubDate = request.getParameter("pubdate");
            	String level = request.getParameter("level");
            	String edition = request.getParameter("edition");
            	
            	Edition ed = new Edition(ds);
            	ed.setConvertFormat(convertFormat);
            	
            	File outFile = new File(outDir, "edition_" + pubDate + "_" + level + "_" + edition + ".xml");
            	
            	ed.write(level, edition, Integer.parseInt(pubDate), new FileOutputStream(outFile));
            	
            	showOutputMessage(out, outFile.getPath(), request);
            	
            } else if (nodeType.equalsIgnoreCase(Constants.NODETYPE_NEWSPAPER)) {
            	String pubDate = request.getParameter("pubdate");
            	String level = request.getParameter("level");
            	
            	Newspaper np = new Newspaper(ds);
            	np.setConvertFormat(convertFormat);
            	
            	File outFile = new File(outDir, "newspaper_" + pubDate + "_" + level + ".xml");
            	
            	np.write(level, Integer.parseInt(pubDate), new FileOutputStream(outFile));
            	
            	showOutputMessage(out, outFile.getPath(), request);
            	
            } else {
            	throw new IllegalArgumentException("Usupported node type");
            }        	
            
            log("Export completed");
            
        } catch (Exception e) {
            log("Exception encountered", e);
            response.sendError(HttpServletResponse.SC_CONFLICT, e.toString());
            
        } finally {
            out.close();
            if (ds != null) {
            	ds.logout();        	
            }
        }
	}
	
    protected Properties getProperties () throws IOException {
        Properties props = new Properties();

        String jbossHomeDir = System.getProperty(Constants.JBOSS_HOMEDIR_PROPERTY);
        File propsFile = new File(jbossHomeDir + File.separator + Constants.CONFIG_DIR 
        		+ File.separator + CONFIG_FILENAME);
                
        try {
            props.load(new FileInputStream(propsFile));
        } catch (FileNotFoundException fnf) {
            log("Properties file " + propsFile.getPath() + " not found", fnf);
            throw fnf;
        }

        return props;
    }			
    
    protected void showOutputMessage(PrintWriter out, String outFile, HttpServletRequest request) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Export XML</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Parameters:</h1>");
        
        Enumeration parameterList = request.getParameterNames();
        while( parameterList.hasMoreElements() )
        {
        	String name = parameterList.nextElement().toString();
        	String[] values = request.getParameterValues(name);
            if(values.length > 1) {
            	for (int i = 0; i < values.length; i++) {
            		out.println("<p>" + name + "[" + i + "] = " + values[i] + "</p>" );
            	}
        	} else {
        		out.println("<p>" + name + " = " + request.getParameter(name) + "</p>");
        	}
        }

        out.println("<h1>Output:</h1>");
        out.println("<p>Exported to " + outFile + "</p>");
        out.println("</body>");
        out.println("</html>");    	
    }

}
