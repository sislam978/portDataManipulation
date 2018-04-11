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
import com.lrglobal.portfolio.model.PriceTable;

public class ReadPriceCSV {
	public ArrayList<PriceTable> getAllData(String fileNameDefined) {

		//declaring a list to store all the records from csv file
		ArrayList<PriceTable> result = new ArrayList<PriceTable>();
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
				PriceTable priceTable=new PriceTable();
				if(i==0) {
					i++;
					continue;
				}
				if(!csvRecord.get(0).toString().equals("")){
					priceTable.setTicker(csvRecord.get(0).toString());
				}
				if(!csvRecord.get(1).toString().equals("")){
					priceTable.setPrice(Double.parseDouble(csvRecord.get(1).toString()));
				}
				if(!csvRecord.get(2).toString().equals("")){
					App app=new App();
					String s_date=app.dateFormation(csvRecord.get(2).toString());
					priceTable.setPrice_date(s_date);
				}
				result.add(priceTable);
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
