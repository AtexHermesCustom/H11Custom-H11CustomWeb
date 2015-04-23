package com.atex.h11.custom.web.metadata;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.unisys.media.cr.adapter.ncm.common.data.pk.NCMObjectPK;
import com.unisys.media.cr.adapter.ncm.common.data.values.NCMObjectBuildProperties;
import com.unisys.media.cr.adapter.ncm.model.data.datasource.NCMDataSource;
import com.unisys.media.cr.adapter.ncm.model.data.values.NCMObjectValueClient;

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
		NCMObjectBuildProperties objProps = new NCMObjectBuildProperties();
		objProps.setGetByObjId(true);
		objProps.setDoNotChekPermissions(true);
		//objProps.setIncludeMetadataGroups(new Vector<String>());

		NCMObjectPK pk = new NCMObjectPK(objId);
		NCMObjectValueClient objVC = (NCMObjectValueClient) ((NCMDataSource)ds).getNode(pk, objProps);
		log("update: Parent package retrieved: name=" + objVC.getNCMName() + ", type=" + objVC.getType() + ", pk=" + objVC.getPK().toString());
    	
		setMetadata(objVC, metaSchema, metaField, metaValue);    	
    }	

}
