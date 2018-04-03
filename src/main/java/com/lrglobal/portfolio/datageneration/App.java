package com.lrglobal.portfolio.datageneration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import com.lrglobal.portfolio.model.PortFolioManager;
import com.lrglobal.portfolio.model.PortSummaryTableManager;
import com.lrglobal.portfolio.model.TestTableManager;

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
    public static void main( String[] args )
    {
    	
    	Scanner input=new Scanner(System.in);
    	System.out.println("Enter the command instruction: ");
    	while(true){
    		String command=input.nextLine();
    		if(command.equalsIgnoreCase("EOF")){
    			break;
    		}
    		else if(command.equalsIgnoreCase("pfi")){
    			String filePath=input.nextLine();
    			PortFolioManager portFolioManager=new PortFolioManager();
    			portFolioManager.setup();
    			portFolioManager.Insert(filePath);
    			portFolioManager.exit();
    		}
    		else if(command.equalsIgnoreCase("pfsi")){
    			PortSummaryTableManager portmanager=new PortSummaryTableManager();
    			portmanager.setup();
    			portmanager.Insert();
    			portmanager.exit();
    		}
    	}
//        TestTableManager testTableManager=new TestTableManager();
//        testTableManager.setup();
//        testTableManager.create();
//        testTableManager.exit();
    }
}
