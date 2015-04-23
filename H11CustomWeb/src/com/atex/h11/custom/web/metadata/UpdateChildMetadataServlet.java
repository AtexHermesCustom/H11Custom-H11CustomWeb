package com.atex.h11.custom.web.metadata;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.ServletException;
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
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	@Override
    protected void update() {
		NCMObjectBuildProperties spProps = new NCMObjectBuildProperties();
		spProps.setGetByObjId(true);
		spProps.setDoNotChekPermissions(true);
		//objProps.setIncludeMetadataGroups(new Vector<String>());

		NCMObjectPK pk = new NCMObjectPK(objId);
		NCMObjectValueClient sp = (NCMObjectValueClient) ((NCMDataSource)ds).getNode(pk, spProps);
		log("update: Parent package retrieved: name=" + sp.getNCMName() + ", type=" + sp.getType() + ", pk=" + sp.getPK().toString());
    	
		// get child objects
		INodePK[] childPKs = sp.getChildPKs();
		if (childPKs != null) {
			NCMObjectBuildProperties objProps = new NCMObjectBuildProperties();
			objProps.setGetByObjId(true);
			objProps.setDoNotChekPermissions(true);
			objProps.setIncludeMetadataGroups(new Vector<String>());		
			
			for (int i = 0; i < childPKs.length; i++ ) {
				NCMObjectPK childPK = new NCMObjectPK(getObjIdFromPK(childPKs[i]));
				NCMObjectValueClient child = (NCMObjectValueClient) ds.getNode(childPK, objProps);
				log("update: Child object retrieved: name=" + child.getNCMName() + ", type=" + child.getType() + ", pk=" + child.getPK().toString());
		
				if (child.getType() == NCMObjectNodeType.OBJ_TEXT) {
					setMetadata(child, metaSchema, metaField, metaValue);		// update metadata
				}
			}
		}
    }	

}
