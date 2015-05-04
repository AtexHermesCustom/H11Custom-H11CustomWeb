package com.atex.h11.custom.web.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atex.h11.custom.common.DataSource;
import com.atex.h11.custom.web.common.Constants;
import com.unisys.media.cr.adapter.ncm.common.business.interfaces.INCMMetadataNodeManager;
import com.unisys.media.cr.adapter.ncm.common.data.datasource.NCMDataSourceDescriptor;
import com.unisys.media.cr.adapter.ncm.common.data.pk.NCMCustomMetadataPK;
import com.unisys.media.cr.adapter.ncm.common.data.pk.NCMObjectPK;
import com.unisys.media.cr.adapter.ncm.common.data.values.NCMCustomMetadataJournal;
import com.unisys.media.cr.adapter.ncm.common.data.values.NCMMetadataPropertyValue;
import com.unisys.media.cr.adapter.ncm.common.data.values.NCMObjectBuildProperties;
import com.unisys.media.cr.adapter.ncm.model.data.datasource.NCMDataSource;
import com.unisys.media.cr.adapter.ncm.model.data.values.NCMObjectValueClient;
import com.unisys.media.cr.common.data.interfaces.INodePK;
import com.unisys.media.cr.common.data.types.IPropertyDefType;
import com.unisys.media.cr.common.data.values.NodeTypePK;
import com.unisys.media.extension.common.exception.NodeAlreadyLockedException;
import com.unisys.media.ncm.cfg.common.data.values.MetadataSchemaValue;
import com.unisys.media.ncm.cfg.model.values.UserHermesCfgValueClient;

/**
 * Servlet implementation class UpdateMetadataServlet
 */
public class UpdateMetadataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected static final String CONFIG_FILENAME = "h11-custom-web-metadata.properties";
	protected Properties props = null;
    protected NCMDataSource ds = null;
    
    protected String user;
    protected String password;
    protected String sessionId;
    protected int objId;
    protected String metaSchema;
    protected String metaField;
    protected String metaValue;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateMetadataServlet() {
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

        user = request.getParameter("user");
        password = request.getParameter("password");
        sessionId = request.getParameter("sessionid");
        String objIdString = request.getParameter("id");
        metaSchema = request.getParameter("schema");
        metaField = request.getParameter("field");
        metaValue = request.getParameter("value");

        log("processRequest: params=" + request.getParameterMap().toString());  // For debugging only

        // check parameters
        if (objIdString == null || objIdString.isEmpty()) {
        	throw new IllegalArgumentException("Missing parameter: id");
        } else {
        	try {
        		objId = Integer.parseInt(objIdString);
        	} catch (NumberFormatException e) {
        		throw new IllegalArgumentException("Illegal value for parameter: id");
        	}
        }
        if (metaSchema == null || metaSchema.isEmpty()) {
        	throw new IllegalArgumentException("Missing parameter: schema");
        }
        if (metaField == null || metaField.isEmpty()) {
        	throw new IllegalArgumentException("Missing parameter: field");
        }
        if (metaValue == null || metaValue.isEmpty()) {
        	throw new IllegalArgumentException("Missing parameter: value");
        }        
        
        props = getProperties();
        
        if (sessionId != null) {
            ds = (NCMDataSource) DataSource.newInstance(sessionId);
        } else {
            if (user == null && password == null) {            // default credentials
            	user = props.getProperty("user");
            	password = props.getProperty("password");
            }            
            ds = (NCMDataSource) DataSource.newInstance(user, password);
        
        }
        
        // perform update
        update();
		
        // output message
        showOutputMessage(out);
	}
    
    protected void update() {
		NCMObjectBuildProperties objProps = new NCMObjectBuildProperties();
		objProps.setGetByObjId(true);
		objProps.setDoNotChekPermissions(true);
		//objProps.setIncludeMetadataGroups(new Vector<String>());

		NCMObjectPK pk = new NCMObjectPK(objId);
		NCMObjectValueClient objVC = (NCMObjectValueClient) ((NCMDataSource)ds).getNode(pk, objProps);
		log("update: Object retrieved: name=" + objVC.getNCMName() + ", type=" + objVC.getType() + ", pk=" + objVC.getPK().toString());
    	
		setMetadata(objVC, metaSchema, metaField, metaValue);    	
    }
    
	protected void setMetadata(NCMObjectValueClient objVC, String metaSchema, String metaField, String metaValue) {
		String objName = objVC.getNCMName();
		Integer objId = getObjIdFromPK(objVC.getPK());
		log("setMetadata: object [" + objId.toString() + "," + objName + "," + Integer.toString(objVC.getType()) + "]" +
			", meta=" + metaSchema + "." + metaField + ", value=" + metaValue);
		
		UserHermesCfgValueClient cfg = ds.getUserHermesCfg();
		
		// Get from configuration the schemaId using schemaName for metadata
		MetadataSchemaValue schema = cfg.getMetadataSchemaByName(metaSchema);
		int schemaId = schema.getId();
		
		// Get metadata property
		IPropertyDefType metaGroupDefType = ds.getPropertyDefType(metaSchema);
		//IPropertyValueClient metaGroupPK = objVC.getProperty(metaGroupDefType.getPK());		
		
		// Get metadata manager
		INCMMetadataNodeManager metaMgr = null;
		NodeTypePK PK = new NodeTypePK(NCMDataSourceDescriptor.NODETYPE_NCMMETADATA);
		metaMgr = (INCMMetadataNodeManager) ds.getNodeManager(PK);		
		
		NCMMetadataPropertyValue pValue = new NCMMetadataPropertyValue(
				metaGroupDefType.getPK(), null, schema);
		pValue.setMetadataValue(metaField, metaValue);
		NCMCustomMetadataPK cmPk = new NCMCustomMetadataPK(
				objId, (short) objVC.getType(), schemaId);
		schemaId = schema.getId();
		NCMCustomMetadataPK[] nodePKs = new NCMCustomMetadataPK[] { cmPk };
		
		try {
			try {
				metaMgr.lockMetadataGroup(schemaId, nodePKs);
			} catch (NodeAlreadyLockedException e) {
			}
			NCMCustomMetadataJournal j = new NCMCustomMetadataJournal();
			j.setCreateDuringUpdate(true);
			metaMgr.updateMetadataGroup(schemaId, nodePKs, pValue, j);
			log("setMetadata: Update metadata successful for [" + objId.toString() + "," + objName + "," + Integer.toString(objVC.getType()) + "]");
		} catch (Exception e) {
			log("setMetadata: Update metadata failed for [" + objId.toString() + "," + objName + "," + Integer.toString(objVC.getType()) + "]: ", 
				e);
		} finally {
			try {
				metaMgr.unlockMetadataGroup(schemaId, nodePKs);
			} catch (Exception e) {
			}
		}			
	}	
	
	protected int getObjIdFromPK(INodePK pk) {
		String s = pk.toString();
		int delimIdx = s.indexOf(":");
		if (delimIdx >= 0)
			s = s.substring(0, delimIdx);
		return Integer.parseInt(s);
	}	        
	
    protected Properties getProperties () throws IOException {
        Properties props = new Properties();

        String jbossHomeDir = System.getProperty(Constants.JBOSS_HOMEDIR_PROPERTY);
        File propsFile = new File(jbossHomeDir + File.separator + Constants.CONFIG_DIR 
        		+ File.separator + CONFIG_FILENAME);
                
        try {
            props.load(new FileInputStream(propsFile));
        } catch (FileNotFoundException fnf) {
            log("getProperties: Properties file " + propsFile.getPath() + " not found", fnf);
            throw fnf;
        }

        return props;
    }	
	
	protected void showOutputMessage(ServletOutputStream out) throws IOException {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Update Metadata</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Update Metadata</h1>");
        out.println("<p>Metadata for object with id=" + objId + " updated</p>");
        out.println("<p>Set: " + metaSchema + "." + metaField + "=" + metaValue + "</p>");
        out.println("</body>");
        out.println("</html>");   		
	}

}
