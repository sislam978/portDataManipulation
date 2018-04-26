package com.lrglobal.portfolio.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import com.google.gson.JsonObject;
import com.lrglobal.portfolio.model.PortFolio;
import com.lrglobal.portfolio.model.PortFolioManager;
import com.lrglobal.portfolio.model.PortSummaryTable;
import com.lrglobal.portfolio.model.PortSummaryTableManager;
import com.lrglobal.portfolio.model.PortfolioNameTable;
import com.lrglobal.portfolio.model.PortfolioNameTableManager;
import com.lrglobal.portfolio.model.PortfolioValue;
import com.lrglobal.portfolio.model.PortfolioValueManager;

@Path("/portfolio")
public class PortFolioService {

	@GET
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/{port_name}/{ticker}/{desired_date}")
	public String getPortValue(@PathParam("port_name") String port_name, @PathParam("ticker") String ticker,
			@PathParam("desired_date") String desired_date) throws Exception {
		String return_String = null;

		try {
			ArrayList<PortSummaryTable> rslt = new ArrayList<PortSummaryTable>();
			PortSummaryTableManager portManager = new PortSummaryTableManager();
			portManager.setup();
			rslt = portManager.getSingledata(port_name, ticker, desired_date);
			portManager.exit();
			Gson gson = new Gson();
			System.out.println("Json output: " + gson.toJson(rslt));
			return_String = gson.toJson(rslt);

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception in service providing: " + e);
		}
		return return_String;

	}

	@GET
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/PortWiseEntries/{port_name}/{d_date}")
	public String getALlPortDataFromSummary(@PathParam("port_name") String port_name,
			@PathParam("d_date") String d_date) {
		// this.input = input;
		String return_String = null;
		try {
			ArrayList<PortSummaryTable> rslt = new ArrayList<PortSummaryTable>();
			PortSummaryTableManager portManager = new PortSummaryTableManager();
			portManager.setup();
			rslt = portManager.getEachPortData(port_name, d_date);
			portManager.exit();
			Gson gson = new Gson();
			System.out.println("Json output: " + gson.toJson(rslt));
			return_String = gson.toJson(rslt);

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception in service providing: " + e);
		}

		return return_String;
	}

	@GET
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/PortValue/{p_portname}/{pd_date}")
	public String getSinglePortValue(@PathParam("p_portname") String port_name, @PathParam("pd_date") String d_date) {
		// this.input = input;
		String return_String = null;
		String text=null;
		try {
			ArrayList<PortfolioValue> rslt = new ArrayList<PortfolioValue>();
			PortfolioValueManager portValueManager = new PortfolioValueManager();
			
			portValueManager.setup();
			rslt = portValueManager.getSinglePortValueData(port_name, d_date);
			portValueManager.exit();
			
			PortSummaryTableManager pstm=new PortSummaryTableManager();
			pstm.setup();
			ArrayList<PortSummaryTable> pst=pstm.getSingledata(rslt.get(0).getPortName(),"CASH", d_date);
			pstm.exit();
			//char ch='"';
			Map<String,String> sendString=new HashMap<String,String>();
			
			
			sendString.put("portName", rslt.get(0).getPortName());
			sendString.put("portfolio_value", rslt.get(0).getPortfolio_value().toString());
			sendString.put("cash", pst.get(0).getShare_quantity().toString());
			sendString.put("source_date", rslt.get(0).getSource_date());
			
			Gson gson = new Gson();
			
			return_String = gson.toJson(sendString);
			System.out.println("json string: "+return_String);

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception in service providing: " + e);
		}

		return return_String;
	}

	@POST
	// @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/PortfolioInsert")
	@Produces("application/json")
	@Consumes("application/json")
	public String savePortFolioRecords(String data) {
		String return_string = null;
		System.out.println("string data: " + data);
		// ArrayList<PortFolio> rslt= new ArrayList<PortFolio>();
		try {
			PortFolio port = new PortFolio();
			String[] primaryData = data.split(",|:");
			for (int i = 0; i < primaryData.length; i++) {
				System.out.println(primaryData[i]);
			}
			port.setPortfoli_name(primaryData[1].substring(1, primaryData[1].length() - 1));
			port.setTicker(primaryData[3].substring(1, primaryData[3].length() - 1));

			String numshare = primaryData[5].substring(1, primaryData[5].length() - 1);
			port.setNumber_of_share(Double.parseDouble(numshare));

			String cost = primaryData[7].substring(1, primaryData[7].length() - 1);
			port.setCost_price(Double.parseDouble(cost));

			port.setSource_date(primaryData[9].substring(1, primaryData[9].length() - 1));

			port.setSign(primaryData[11].substring(1, primaryData[11].length() - 1));
			
			String commision=primaryData[13].substring(1, primaryData[13].length()-2);
			port.setCommission(Double.parseDouble(commision));

			String ticker = primaryData[3].substring(1, primaryData[3].length() - 1);
			String portName = primaryData[1].substring(1, primaryData[1].length() - 1);
			String d_date = primaryData[9].substring(1, primaryData[9].length() - 1);
			String ss = null;
			
			PortFolioManager portManager = new PortFolioManager();
			portManager.setup();
			ss=portManager.insertRecordsApi(port);
			portManager.exit();

			/*
			 * Cash ticker record insert in portfolio Table
			 */
			PortFolioManager portManager1 = new PortFolioManager();
			portManager1.setup();
			portManager1.cashrow_insert(portName, d_date);
			portManager1.exit();

			Gson gson = new Gson();
			if (ss.equals("sucessfull")) {
				return_string = gson.toJson("successfully insert the data");
			} else {
				return_string = gson.toJson("please put valid data");
			}

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Giving Exception due to: " + e);
		}
		return return_string;
	}

	@GET
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/PortValueChart/{port_name}/{start_date}/{end_date}")
	public String getALLPortValueChart(@PathParam("port_name") String portName,
			@PathParam("start_date") String startDate, @PathParam("end_date") String endDate) {
		String return_string = null;

		try {
			Map<String, Double> rslt = new HashMap<String, Double>();
			PortfolioValueManager portValueManager = new PortfolioValueManager();

			portValueManager.setup();
			rslt = portValueManager.getALlValueFOrChart(portName, startDate, endDate);
			portValueManager.exit();
			Gson gson = new Gson();
			System.out.println("Json output: " + gson.toJson(rslt));
			return_string = gson.toJson(rslt);

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception in service providing: " + e);
		}

		return return_string;
	}
	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/newPortInsert")
	public String InsertNewPortfolioName(PortfolioNameTable newPort){
		String return_string=null;
		try{
			PortfolioNameTableManager newportMan=new PortfolioNameTableManager();
			newportMan.setup();
			return_string=newportMan.Insert(newPort);
			newportMan.exit();
			Gson gson = new Gson();
			if (return_string.equals("Successfull")) {
				return_string = gson.toJson("successfully insert the data");
			} else {
				return_string = gson.toJson("please put valid data");
			}
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception in service providing during inserting newportfolio name: " + e);
		}
		return return_string;
	}

	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/portValuesChart")
	public String sendPortValuesDateRange(String json){
		String return_string = null;

		try {
			
			String[] primaryData = json.split(",|:");
			for (int i = 0; i < primaryData.length; i++) {
				System.out.println(primaryData[i]);
			}
			String portName=primaryData[1].substring(1, primaryData[1].length() - 1);
			String Start_date=primaryData[3].substring(1,primaryData[3].length() - 1);
			String end_date=primaryData[5].substring(1, primaryData[5].length() - 2);
			
			Map<String, Double> rslt = new HashMap<String, Double>();
			PortfolioValueManager portValueManager = new PortfolioValueManager();

			portValueManager.setup();
			rslt = portValueManager.portValuesForChart(portName, Start_date, end_date);
			portValueManager.exit();
			Gson gson = new Gson();
			System.out.println("Json output: " + gson.toJson(rslt));
			return_string = gson.toJson(rslt);

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception in service providing: " + e);
		}

		return return_string;
	}
	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/updateSummaryPortValue")
	public String updaterowsSummaryAndPortvalue(String json){
		String return_string = null;

		try {
			
			String[] primaryData = json.split(",|:");
			for (int i = 0; i < primaryData.length; i++) {
				System.out.println(primaryData[i]);
			}
			String portName=primaryData[1].substring(1, primaryData[1].length() - 1);
			String Start_date=primaryData[3].substring(1,primaryData[3].length() - 1);
			String end_date1=primaryData[5].substring(1, primaryData[5].length() - 2);
			
			SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
			Date eee = input_format.parse(end_date1);
			Calendar end = Calendar.getInstance();
			end.setTime(eee);
			end.add(Calendar.DATE, 1); // number of days to add
			String end_date = input_format.format(end.getTime());
			
			/*
			 * Drop Summar Table data if exist on the date by enabling flag in
			 * delete_flag
			 */
			PortSummaryTableManager pstm = new PortSummaryTableManager();
			pstm.setup();
			pstm.summarytableDataDropAndInsert(portName, Start_date,end_date);
			pstm.exit();

			/*
			 * after make delete flag on re insert the data for that date
			 */
			//SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
			
//			Calendar end = Calendar.getInstance();
//			end.setTime(dateStart);
//			end.add(Calendar.DATE, 1); // number of days to add
//			String end_date = input_format.format(end.getTime());
//
			PortSummaryTableManager pstm1 = new PortSummaryTableManager();
			pstm1.setup();
			pstm1.Insert(portName,Start_date, end_date);
			pstm1.exit();

			/*
			 * portfolio value table select data to on delete flag and call
			 * insertion method of portfolio value table
			 */
			PortfolioValueManager pfvm = new PortfolioValueManager();
			pfvm.setup();
			pfvm.deleteandReInsertonDate(portName, Start_date, end_date);
			pfvm.exit();

			/*
			 * re insert new records in portfolio value table on the date
			 */
			PortfolioValueManager pfvm1 = new PortfolioValueManager();
			pfvm1.setup();
			pfvm1.Insert(portName, Start_date, end_date);
			pfvm1.exit();

			/*
			 * Summary table row update after inserting the new rows in
			 * portfolio value table
			 */
			PortSummaryTableManager pstm2 = new PortSummaryTableManager();
			pstm2.setup();
			pstm2.BulkUpdateSummaryRecords(portName, Start_date,end_date);
			pstm2.exit();

			/*
			 * Change in index calculation and update each row on the date in
			 * portfolio value table
			 */
			PortfolioValueManager pfvm2 = new PortfolioValueManager();
			pfvm2.setup();
			pfvm2.insertIndexinEachRow(portName,Start_date,end_date);
			pfvm2.exit();

			/*
			 * the whole cycle will finish here from portfolio table to summary
			 * table to portfolio value table data insert and updates
			 */
			
			Gson gson = new Gson();
			System.out.println("Json output: " + gson.toJson("Successfull"));
			return_string = gson.toJson("Successfull");

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception in Updating: " + e);
		}

		return return_string;
	}
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/portfolioTableData")
	public String portFolioValueDataSend(String json){
		String return_String=null;
		try{
			String[] primaryData = json.split(",|:");
			for (int i = 0; i < primaryData.length; i++) {
				System.out.println(primaryData[i]);
			}
			String portName=primaryData[1].substring(1, primaryData[1].length() - 1);
			String desired_date=primaryData[3].substring(1, primaryData[3].length() - 2);
			
			PortFolioManager pfm=new PortFolioManager();
			pfm.setup();
			ArrayList<PortFolio>rslt=pfm.sendDesiredDateData(portName, desired_date);
			pfm.exit();
			if(rslt.isEmpty()){
				return_String="No data found on that date.";
			}
			else{
				Gson gson=new Gson();
				return_String= gson.toJson(rslt);
			}
			System.out.println(return_String);
		}catch(Exception e){
			System.out.println("Showing exception due to: "+e);
		}
		return return_String;
	}
	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/test")
	public String Testdata(String data) {
		String retrun_data = null;
		retrun_data = data;
		System.out.println(data);
		return "sucessfully recieved.";
	}

}
