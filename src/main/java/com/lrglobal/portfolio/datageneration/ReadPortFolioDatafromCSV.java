package com.lrglobal.portfolio.datageneration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.lrglobal.portfolio.model.PortFolio;



public class ReadPortFolioDatafromCSV {
	
	public ArrayList<PortFolio> getAllData(String fileNameDefined) {

		//declaring a list to store all the records from csv file
		ArrayList<PortFolio> result = new ArrayList<PortFolio>();
		String thisLine = null;
		try {
			//open a reader to read the csv file  
			Reader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileNameDefined), "utf-8"));
			// Files.newBufferedReader(Paths.get(fileNameDefined));
			//csv parser take all the records by parsing from the reader
			CSVParser csvParser = new CSVParser(br, CSVFormat.DEFAULT);
			//Now data is seprated as a set of records 
			Iterable<CSVRecord> csvRecords = csvParser.getRecords();
			int i = 0;
			//reading data AS records
			for (CSVRecord csvRecord : csvRecords) {
				if (i == 0) {
					//first row is the tickers thats why we skip it
					i++;
					continue;
				}
				PortFolio portFolio=new PortFolio();
				/*
				 * 0,1,2,3 .... are column numbers of CSV which are st as object attributes
				 */
				if(csvRecord.get(0).toString().equals("")){
					portFolio.setTicker(null);
				}
				else{
					portFolio.setTicker(csvRecord.get(0).toString());
				}
//				if(csvRecord.get(1).toString().equals("")){
//					portFolio.setSector(null);
//				}
//				else{
//					portFolio.setSector(csvRecord.get(1).toString());
//				}
				if(csvRecord.get(1).toString().equals("")){
					portFolio.setNumber_of_share(null);
				}
				else{
					portFolio.setNumber_of_share(Double.parseDouble(csvRecord.get(1).toString()));
				}
				if(csvRecord.get(2).toString().equals("")){
					portFolio.setCost_price(null);
				}
				else{
					portFolio.setCost_price(Double.parseDouble(csvRecord.get(2).toString()));
				}
				if(csvRecord.get(3).toString().equals("")){
					portFolio.setSign(null);
				}
				else{
					portFolio.setSign(csvRecord.get(3).toString());
				}
				
				if(csvRecord.get(4).toString().equals("")){
					portFolio.setSource_date(null);
				}
				else{
					String dd=csvRecord.get(4).toString();
					App aa= new App();
					String f_date=aa.dateFormation(dd);
					portFolio.setSource_date(f_date);
				}
				if(csvRecord.get(5).toString().equals("")){
					portFolio.setPortfoli_name(null);
				}
				else{
					
					portFolio.setPortfoli_name(csvRecord.get(5).toString());
				}
				result.add(portFolio);
			}
			// after loop, close scanner
			br.close();

		} catch (IOException e) {

			System.out.println("exception at reading: " + e);
		}
		//return the result
		return result;

	}

}
