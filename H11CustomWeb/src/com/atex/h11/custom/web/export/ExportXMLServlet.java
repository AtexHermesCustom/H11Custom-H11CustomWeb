package com.atex.h11.custom.web.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atex.h11.custom.common.DataSource;
import com.atex.h11.custom.web.common.Constants;
import com.unisys.media.cr.adapter.ncm.common.data.pk.NCMObjectPK;
import com.unisys.media.cr.adapter.ncm.common.data.values.NCMObjectBuildProperties;
import com.unisys.media.cr.adapter.ncm.model.data.datasource.NCMDataSource;
import com.unisys.media.cr.adapter.ncm.model.data.values.NCMObjectValueClient;
import com.unisys.media.extension.common.serialize.xml.XMLSerializeWriter;

/**
 * Servlet implementation class ExportServlet
 */
public class ExportXMLServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	public static final String CONFIG_FILENAME = "h11-custom-web-export.properties";	
	
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
        ServletOutputStream out = response.getOutputStream();
        NCMDataSource ds = null;

        String user = request.getParameter("user");
        String password = request.getParameter("password");
        String sessionId = request.getParameter("sessionid");
        String nodeType = request.getParameter("nodetype");
        String id = request.getParameter("id");
        String metadataSchema = request.getParameter("schema");
        String metadataField = request.getParameter("field");
        String metadataValue = request.getParameter("value");

        Properties props = getProperties();
        
        // default credentials
        if (user == null && password == null) {
        	user = props.getProperty("user");
        	password = props.getProperty("password");
        }
               
        log(request.getParameterMap().toString());  // For debugging only
        
        if (sessionId != null) {
            ds = (NCMDataSource) DataSource.newInstance(sessionId);
        } else {
            ds = (NCMDataSource) DataSource.newInstance(user, password);
        }
               
        if (nodeType.equalsIgnoreCase(Constants.NODETYPE_OBJECT) || nodeType.equalsIgnoreCase(Constants.NODETYPE_SP)) {
        	NCMObjectPK objPK = new NCMObjectPK(Integer.parseInt(id));
            NCMObjectBuildProperties objProps = new NCMObjectBuildProperties();
            objProps.setIncludeObjContent(true);
            objProps.setIncludeObjContent(true);
            objProps.setIncludeLay(true);
            objProps.setIncludeLayContent(true);
            objProps.setIncludeLayObjContent(true);
            objProps.setIncludeAttachments(true);
            objProps.setIncludeCaption(true);
            objProps.setIncludeCreditBox(true);
            objProps.setIncludeIPTC(true);
            objProps.setIncludeTextPreview(true);
            objProps.setIncludeLinkedObject(true);
            objProps.setIncludeVariants(true);
            objProps.setIncludeSpChild(true);
            objProps.setXhtmlNestedAsXml(true);
            objProps.setNeutralNestedAsXml(true);
            objProps.setIncludeMetadataChild(true);
            objProps.setIncludeMetadataGroups(new Vector());
            objProps.setIncludeConvertTo("Neutral");            
            NCMObjectValueClient objVC = (NCMObjectValueClient) ds.getNode(objPK, objProps);
            try {
	            XMLSerializeWriter w = new XMLSerializeWriter(out);
	            w.writeObject(objVC, objProps);
	            w.close();            	
            } catch (Exception e) {
            	e.printStackTrace();
            	log(e.toString());
            }
        	
        } else if (nodeType.equalsIgnoreCase(Constants.NODETYPE_PAGE)) {
        	
        } else {
        	throw new IllegalArgumentException("Usupported node type");
        }
	}
	
    private Properties getProperties () throws IOException {
        Properties props = new Properties();

        String jbossHomeDir = System.getProperty(Constants.JBOSS_HOMEDIR_PROPERTY);
        File propsFile = new File(jbossHomeDir + File.separator + Constants.CONFIG_DIR 
        		+ File.separator + CONFIG_FILENAME);
                
        try {
            props.load(new FileInputStream(propsFile));
        } catch (FileNotFoundException fnf) {
            log("Properties file " + propsFile.getPath() + " not found.", fnf);
            throw fnf;
        }

        return props;
    }			

}
