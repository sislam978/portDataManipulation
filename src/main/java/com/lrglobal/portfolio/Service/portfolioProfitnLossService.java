package com.lrglobal.portfolio.Service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.gson.Gson;
import com.lrglobal.portfolio.model.CorporateDeclaration;
import com.lrglobal.portfolio.model.CorporatedeclarationManager;
import com.lrglobal.portfolio.model.PortFolioManager;
import com.lrglobal.portfolio.model.PortfolioProfitnLoss;
import com.lrglobal.portfolio.model.PortfolioValueManager;

@Path("/profitNLoss")
public class portfolioProfitnLossService {

	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/portfolioProfitGain")
	public String sendProfitnLoss(String json) {
		
		String return_string = null;

		try {
			String[] primaryData = json.split(",|:");
			for (int i = 0; i < primaryData.length; i++) {
				System.out.println(primaryData[i]);
			}
			String portName = primaryData[1].substring(1, primaryData[1].length() - 1);
			String start_date = primaryData[3].substring(1, primaryData[3].length() - 1);
			String end_date = primaryData[5].substring(1, primaryData[5].length() - 2);

			PortFolioManager pfm = new PortFolioManager();
			pfm.setup();
			double t_commission = pfm.calculateTotalCommission(portName, start_date, end_date);
			pfm.exit();

			PortFolioManager pfm1 = new PortFolioManager();
			pfm1.setup();
			double t_cashDividend = pfm1.calculateCashDividend(portName, start_date, end_date);
			pfm1.exit();

			PortfolioValueManager pfvm = new PortfolioValueManager();
			pfvm.setup();
			double total_gain = pfvm.calculateTotalGain(portName, end_date);
			pfvm.exit();
			
			PortfolioValueManager pfvm2=new PortfolioValueManager();
			pfvm2.setup();
			double sum_OfCapitalgain=pfvm2.calculateSummationOfcapitalgain(portName, start_date, end_date);
			pfvm2.exit();
			
			double current_profit=t_cashDividend+total_gain-t_commission+sum_OfCapitalgain;

			PortfolioProfitnLoss pfpnl = new PortfolioProfitnLoss();
			pfpnl.setPortName(portName);
			pfpnl.setTotal_commission(t_commission);
			pfpnl.setTotalCashDividend(t_cashDividend);
			pfpnl.setTotal_gain(total_gain);
			pfpnl.setStart_date(start_date);
			pfpnl.setEnd_date(end_date);
			pfpnl.setCurrent_profit(current_profit);
			pfpnl.setNetCapitalGain(sum_OfCapitalgain);
			
			Gson gson = new Gson();
			return_string = gson.toJson(pfpnl);

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("given exception due to: " + e);
		}
		return return_string;
	}
}
