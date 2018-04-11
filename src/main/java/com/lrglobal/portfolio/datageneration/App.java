package com.lrglobal.portfolio.datageneration;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import com.lrglobal.portfolio.model.PortFolioManager;
import com.lrglobal.portfolio.model.PortSummaryTableManager;
import com.lrglobal.portfolio.model.PortfolioValueManager;
import com.lrglobal.portfolio.model.PriceTableManager;
import com.lrglobal.portfolio.model.TestTableManager;
import com.lrglobal.portfolio.model.TickerTableManager;

/**
 * Hello world!
 *
 */
public class App 
{
	
	public String dateFormation(String toformat){
		
		SimpleDateFormat input_format= new SimpleDateFormat("MM/dd/yyyy");
		//Date date= new SimpleDateFormat(dateToformat);
		String formatedDate=null;
		//String toformat="12/31/2017";
		
		Date date;
		try {
			date = input_format.parse(toformat);
			SimpleDateFormat output_format= new SimpleDateFormat("yyyy-MM-dd");
			formatedDate=output_format.format(date);
			//System.out.println("printed date: "+formatedDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return formatedDate;
	}
    public static void main( String[] args ) throws SQLException, ParseException
    {
//    	App a=new App();
//    	a.dateFormation("1/10/2018");
    	
    	Scanner input=new Scanner(System.in);
    	System.out.println("Enter the command instruction: ");
    	while(true){
    		String command=input.nextLine();
    		if(command.equalsIgnoreCase("EOF")){
    			break;
    		}
    		//portfolio table data insert
    		else if(command.equalsIgnoreCase("pfi")){
    			System.out.println("Enter the portfolio CSV file path: ");
    			String filePath=input.nextLine();
    			PortFolioManager portFolioManager=new PortFolioManager();
    			portFolioManager.setup();
    			portFolioManager.Insert(filePath);
    			portFolioManager.exit();
    		}//portfolio summary table data insert
    		else if(command.equalsIgnoreCase("pfsi")){
    			PortSummaryTableManager portmanager=new PortSummaryTableManager();
    			portmanager.setup();
    			portmanager.Insert();
    			portmanager.exit();
    		}
    		// text export from summary table data //need to change the method for all possible outputs
    		else if(command.equalsIgnoreCase("export")){
    			PortSummaryTableManager portmanager=new PortSummaryTableManager();
    			portmanager.setup();
    			portmanager.exportData();
    			portmanager.exit();
    		}
    		//portfolio Value table data insert
    		else if(command.equalsIgnoreCase("pfvi")){
    			PortfolioValueManager portfolioValueManager=new PortfolioValueManager();
    			portfolioValueManager.setup();
    			portfolioValueManager.Insert();
    			portfolioValueManager.exit();
    		}
    		// portfolio summary table row update by update weight in portfolio column
    		else if(command.equalsIgnoreCase("pstru")){
    			PortSummaryTableManager portmanager=new PortSummaryTableManager();
    			portmanager.setup();
    			portmanager.rowUpdateWeightInPortfolio();
    			portmanager.exit();
    		}
    		//price table data insert
    		else if(command.equals("ptdi")){
    			System.out.println("Enter the price CSV file path: ");
    			String f_path=input.nextLine();
    			PriceTableManager priceTableManager=new PriceTableManager();
    			priceTableManager.setup();
    			priceTableManager.Insert(f_path);
    			priceTableManager.exit();
    		}
    		//ticker table data insert
    		else if(command.equalsIgnoreCase("ttdi")){
    			System.out.println("Enter the ticker CSV file path: ");
    			String f_path=input.nextLine();
    			TickerTableManager tickerManager=new TickerTableManager();
    			tickerManager.setup();
    			tickerManager.Insert(f_path);
    			tickerManager.exit();
    		}
    		
    		//price table row update 
    		else if(command.equals("ptru")){
    			PriceTableManager priceTableManager=new PriceTableManager();
    			priceTableManager.setup();
    			priceTableManager.updateRowPriceChange();
    			priceTableManager.exit();
    		}
    	}
//        TestTableManager testTableManager=new TestTableManager();
//        testTableManager.setup();
//        testTableManager.create();
//        testTableManager.exit();
    }
}
