package com.atex.h11.custom.web.metadata;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.atex.h11.custom.web.common.Constants;
//import com.atex.h11.custom.web.common.CustomUtils;
import com.unisys.media.cr.adapter.ncm.common.data.pk.NCMObjectPK;
import com.unisys.media.cr.adapter.ncm.common.data.values.NCMObjectBuildProperties;
import com.unisys.media.cr.adapter.ncm.model.data.datasource.NCMDataSource;
import com.unisys.media.cr.adapter.ncm.model.data.values.NCMObjectValueClient;

/**
 * Servlet implementation class UpdateMultiMetadataServlet
 */
public class UpdateMultiMetadataServlet extends UpdateMetadataServlet {
	private static final long serialVersionUID = 1L;
       
	protected String jsonParams = "";
	protected JSONArray jsonMetadata = null;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateMultiMetadataServlet() {
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
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logParameters(request.getParameterMap());
		
        response.setContentType("text/html;charset=" + Constants.DEFAULT_ENCODING);
        ServletOutputStream out = response.getOutputStream();

        props = getProperties();	// custom properties
        
        encoding = Constants.DEFAULT_ENCODING;
        if (props.containsKey("paramEncoding")) {
        	encoding = props.getProperty("paramEncoding").trim();
        }
                
        user = request.getParameter("user");
        password = request.getParameter("password");
        sessionId = request.getParameter("sessionid");
        String objIdString = request.getParameter("id");
        
        jsonParams = URLDecoder.decode(request.getParameter("metadata"), encoding);
        //log("jsonParams=" + jsonParams);
        //log("jsonParams bytes=" + CustomUtils.getHexBytes(jsonParams, encoding));

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
        
        if (jsonParams == null || jsonParams.isEmpty()) {
        	throw new IllegalArgumentException("Missing JSON parameter: metadata");
        }
        
        JSONParser parser=new JSONParser();
        try {
			jsonMetadata = (JSONArray) parser.parse(jsonParams);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid metadata JSON parameter. Error while parsing JSON: " + e.toString());
		}
        
        // get the datasource
        ds = getDatasource();
        
        // perform update
        update();
		
        // output message
        showOutputMessage(out);
	}	
	
	@Override
    protected void update() {
		NCMObjectBuildProperties objProps = new NCMObjectBuildProperties();
		objProps.setGetByObjId(true);
		objProps.setDoNotChekPermissions(true);
		objProps.setIncludeMetadataGroups(new Vector<String>());
    	
		NCMObjectPK pk = new NCMObjectPK(objId);

		Iterator <JSONObject> iter = jsonMetadata.iterator();
	    while (iter.hasNext()) {	    	
	    	JSONObject obj = iter.next();
	    	
	    	// need to be within the loop, always get the latest copy
			NCMObjectValueClient objVC = (NCMObjectValueClient) ((NCMDataSource)ds).getNode(pk, objProps);
			log("update: Object retrieved: name=" + objVC.getNCMName() + ", type=" + objVC.getType() + ", pk=" + objVC.getPK().toString());
	    	
	    	setMetadata(objVC, (String) obj.get("schema"), (String) obj.get("field"), (String) obj.get("value"));	// update metadata
	    }		    	
    }	
	
	@Override
	protected void showOutputMessage(ServletOutputStream out) throws IOException {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Update Metadata</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Update Metadata</h1>");
        out.println("<p>Metadata for object with id=" + objId + " updated</p>");
        out.println("<p>Parameters: " + jsonMetadata.toString() + "</p>");
        out.println("</body>");
        out.println("</html>");   		
	}	
	
}
