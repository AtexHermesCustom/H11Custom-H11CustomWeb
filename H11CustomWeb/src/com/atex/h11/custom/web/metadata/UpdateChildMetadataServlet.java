package com.atex.h11.custom.web.metadata;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.unisys.media.cr.adapter.ncm.common.data.pk.NCMObjectPK;
import com.unisys.media.cr.adapter.ncm.common.data.types.NCMObjectNodeType;
import com.unisys.media.cr.adapter.ncm.common.data.values.NCMObjectBuildProperties;
import com.unisys.media.cr.adapter.ncm.model.data.datasource.NCMDataSource;
import com.unisys.media.cr.adapter.ncm.model.data.values.NCMObjectValueClient;
import com.unisys.media.cr.common.data.interfaces.INodePK;

/**
 * Servlet implementation class UpdateChildMetadataServlet
 */
public class UpdateChildMetadataServlet extends UpdateMetadataServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateChildMetadataServlet() {
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
	
	@Override
    protected void update() {
		// get list of object types for metadata update
		String typesForUpdateProp = props.getProperty("typesForMetadataUpdate");
        if (typesForUpdateProp == null || typesForUpdateProp.isEmpty()) {
        	throw new IllegalStateException("Missing property: typesForMetadataUpdate");
        }
		List<String> typesForUpdate = Arrays.asList(typesForUpdateProp.split(","));
		
		NCMObjectBuildProperties spProps = new NCMObjectBuildProperties();
		spProps.setGetByObjId(true);
		spProps.setDoNotChekPermissions(true);
		//objProps.setIncludeMetadataGroups(new Vector<String>());

		NCMObjectPK pk = new NCMObjectPK(objId);
		NCMObjectValueClient sp = (NCMObjectValueClient) ((NCMDataSource)ds).getNode(pk, spProps);
		log("update: Parent package retrieved: name=" + sp.getNCMName() + ", type=" + sp.getType() + ", pk=" + sp.getPK().toString());
    	
		INodePK[] childPKs = sp.getChildPKs(); 		// get child objects
		if (childPKs != null) {
			NCMObjectBuildProperties objProps = new NCMObjectBuildProperties();
			objProps.setGetByObjId(true);
			objProps.setDoNotChekPermissions(true);
			objProps.setIncludeMetadataGroups(new Vector<String>());		
			
			for (int i = 0; i < childPKs.length; i++ ) {
				NCMObjectPK childPK = new NCMObjectPK(getObjIdFromPK(childPKs[i]));
				NCMObjectValueClient child = (NCMObjectValueClient) ds.getNode(childPK, objProps);
				log("update: Child object retrieved: name=" + child.getNCMName() + ", type=" + child.getType() + ", pk=" + child.getPK().toString());
		
				if (typesForUpdate.contains(Integer.toString(child.getType()))) {
					setMetadata(child, metaSchema, metaField, metaValue);		// update metadata
				}
			}
		}
    }	
	
	@Override
	protected void showOutputMessage(ServletOutputStream out) throws IOException {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Update Child Metadata</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Update Metadata</h1>");
        out.println("<p>Metadata for children of package with id=" + objId + " updated</p>");
        out.println("<p>Set: " + metaSchema + "." + metaField + "=" + metaValue + "</p>");
        out.println("</body>");
        out.println("</html>");   		
	}	

}
