package com.lrglobal.portfolio.Service;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.google.gson.Gson;
import com.lrglobal.portfolio.model.PortSummaryTable;
import com.lrglobal.portfolio.model.PortSummaryTableManager;

@Path("/portfolio")
public class PortFolioService {
	
	@GET
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/{port_name}/{ticker}/{desired_date}")
	public String getPortValue(@PathParam("port_name") String port_name, @PathParam("ticker") String ticker,
			@PathParam("desired_date") String desired_date) throws Exception{
		String return_String=null;
		
		try{
			ArrayList<PortSummaryTable> rslt= new ArrayList<PortSummaryTable>();
			PortSummaryTableManager portManager=new PortSummaryTableManager();
			portManager.setup();
			rslt=portManager.getSingledata(port_name, ticker, desired_date);
			portManager.exit();
			Gson gson=new Gson();
			System.out.println("Json output: "+gson.toJson(rslt));
			return_String=gson.toJson(rslt);
			
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception in service providing: "+e);
		}
		return return_String;
		
	}

}
