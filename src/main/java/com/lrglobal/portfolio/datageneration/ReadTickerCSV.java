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


import com.lrglobal.portfolio.model.TickerTable;

public class ReadTickerCSV {
	public ArrayList<TickerTable> getAllData(String fileNameDefined) {

		//declaring a list to store all the records from csv file
		ArrayList<TickerTable> result = new ArrayList<TickerTable>();
		String thisLine = null;
		try {
			//open a reader to read the csv file  
			Reader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileNameDefined), "utf-8"));
			CSVParser csvParser = new CSVParser(br, CSVFormat.DEFAULT);
			//Now data is seprated as a set of records 
			Iterable<CSVRecord> csvRecords = csvParser.getRecords();
			int i = 0;
			//reading data AS records
			for (CSVRecord csvRecord : csvRecords) {
				TickerTable tickerTable=new TickerTable();
				if(i==0) {
					i++;
					continue;
				}
				if(!csvRecord.get(0).toString().equals("")){
					tickerTable.setTickerName(csvRecord.get(0).toString());
				}
				if(!csvRecord.get(1).toString().equals("")){
					tickerTable.setSector(csvRecord.get(1).toString());
				}
				
				result.add(tickerTable);
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
