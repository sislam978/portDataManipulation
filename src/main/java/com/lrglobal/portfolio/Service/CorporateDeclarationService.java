package com.lrglobal.portfolio.Service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.gson.Gson;
import com.lrglobal.portfolio.model.CorporateDeclaration;
import com.lrglobal.portfolio.model.CorporatedeclarationManager;

@Path("/cashDividend")
public class CorporateDeclarationService {

	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/corporatedeclarationInsert")
	public String insertCOrporateTableData(String json){
		String return_string=null;
		
		try{
			String[] primaryData = json.split(",|:");
			for (int i = 0; i < primaryData.length; i++) {
				System.out.println(primaryData[i]);
			}
			String ticker=primaryData[1].substring(1, primaryData[1].length() - 1);
			String cash_dividend=primaryData[3].substring(1,primaryData[3].length() - 1);
			String stocksplit=primaryData[5].substring(1, primaryData[5].length() - 1);
			String rightShareCost=primaryData[7].substring(1,primaryData[7].length() - 1);
			String record_date=primaryData[9].substring(1, primaryData[9].length() - 2);
			
			CorporateDeclaration cdt=new CorporateDeclaration();
			cdt.setTickerName(ticker);
			cdt.setCashDividend(Double.parseDouble(cash_dividend));
			cdt.setStockSplit(Double.parseDouble(stocksplit));
			cdt.setRightShareCost(Double.parseDouble(rightShareCost));
			cdt.setRecord_date(record_date);
			CorporatedeclarationManager cdm=new CorporatedeclarationManager();
			cdm.setup();
			String ss= cdm.insertCDT(cdt);
			cdm.exit();
			Gson gson=new Gson();
			return_string=gson.toJson(ss);
			
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("given exception due to: "+e);
		}
		return return_string;
	}
}
