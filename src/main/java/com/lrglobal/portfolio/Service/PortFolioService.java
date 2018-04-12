package com.lrglobal.portfolio.Service;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.lrglobal.portfolio.model.PortFolio;
import com.lrglobal.portfolio.model.PortFolioManager;
import com.lrglobal.portfolio.model.PortSummaryTable;
import com.lrglobal.portfolio.model.PortSummaryTableManager;
import com.lrglobal.portfolio.model.PortfolioValue;
import com.lrglobal.portfolio.model.PortfolioValueManager;

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
	
	@GET
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/PortWiseEntries/{port_name}/{d_date}")
	public String getALlPortDataFromSummary(@PathParam("port_name") String port_name,@PathParam("d_date") String d_date){
		//this.input = input;
		String return_String=null;
		try{
			ArrayList<PortSummaryTable> rslt= new ArrayList<PortSummaryTable>();
			PortSummaryTableManager portManager=new PortSummaryTableManager();
			portManager.setup();
			rslt=portManager.getEachPortData(port_name, d_date);
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
	
	@GET
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/PortValue/{p_portname}/{pd_date}")
	public String getSinglePortValue(@PathParam("p_portname") String port_name,@PathParam("pd_date") String d_date){
		//this.input = input;
		String return_String=null;
		try{
			ArrayList<PortfolioValue> rslt= new ArrayList<PortfolioValue>();
			PortfolioValueManager portValueManager=new PortfolioValueManager();
			portValueManager.setup();
			rslt=portValueManager.getSinglePortValueData(port_name, d_date);
			portValueManager.exit();
			Gson gson=new Gson();
			System.out.println("Json output: "+gson.toJson(rslt));
			return_String=gson.toJson(rslt);
			
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception in service providing: "+e);
		}
		
		return return_String;
	}
	
	@POST
//	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/PortfolioInsert")
	@Produces("application/json")
	@Consumes("application/json")
	public String savePortFolioRecords(PortFolio list_portfolio){
		String return_string=null;
		//ArrayList<PortFolio> rslt= new ArrayList<PortFolio>();
		try{
			PortFolioManager portManager=new PortFolioManager();
			portManager.setup();
			portManager.insertRecordsApi(list_portfolio);
			portManager.exit();
			
			return_string="successfully insert the data";
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("Giving Exception due to: "+e);
		}
		return return_string;
	}

}
