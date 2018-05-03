package com.lrglobal.portfolio.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.tomcat.jni.Mmap;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;
import org.hibernate.tool.schema.internal.exec.GenerationTargetToStdout;

import com.lrglobal.portfolio.Service.PortFolioService;
import com.lrglobal.portfolio.datageneration.ReadPortFolioDatafromCSV;

public class PortSummaryTableManager {

	public SessionFactory sessionFactory;

	// connection initialization. dont bother much. this connection process will
	// need in every database transaction
	// no need to change it either
	public void setup() {
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml") // configures
																														// settings
																														// from
																														// hibernate.cfg.xml
				.build();
		try {
			sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
		} catch (Exception ex) {
			StandardServiceRegistryBuilder.destroy(registry);
			throw new RuntimeException(ex);
		}
	}

	public void exit() {
		// code to close Hibernate Session factory
		sessionFactory.close();

	}

	/*
	 * Quantity calculation for portfolio summary. 
	 * The quantity calculation is depended on buy sell sign. after getting the list of portfolio for certain date range
	 * loop through each record and cechk whether the sign is buy or sell. if buy map will add the quantity with previous 
	 * quantity and put it to the map for the certain ticker.
	 * Initially map will add the ticker with quantity either positive or negative according to the sign is buy or sell.
	 * ticker map generation completed. 
	 */
	public Map<String, Double> gettickerValuesForSinglePort(Map<String, Double> tickermap, ArrayList<PortFolio> rslt) {
		for (int j = 0; j < rslt.size(); j++) {
			if (tickermap.containsKey(rslt.get(j).getTicker())) {
				if (rslt.get(j).getSign().equals("BUY")) {
					Double vv = tickermap.get(rslt.get(j).getTicker());
					vv += rslt.get(j).getNumber_of_share();
					tickermap.put(rslt.get(j).getTicker(), vv);
				} else {
					Double vv = tickermap.get(rslt.get(j).getTicker());
					vv -= rslt.get(j).getNumber_of_share();
					tickermap.put(rslt.get(j).getTicker(), vv);
				}
			} else {
				if (rslt.get(j).getSign().equals("BUY")) {
					tickermap.put(rslt.get(j).getTicker(), rslt.get(j).getNumber_of_share());
				} else {
					tickermap.put(rslt.get(j).getTicker(), 0 - rslt.get(j).getNumber_of_share());
				}
			}
		}
		return tickermap;
	}

	/*
	 * The function is calculating cost price for certain portfolio and ticker within certain date range. 
	 * 1.The algorithmic concept is that If the ticker has a record in portfolio with buy sign 
	 * the weighted average wil calculate like the equation (A1W1(+-)A2W2(+-)A3W3/(W1(+-)W2(+-)W3))
	 * 2. The plus and minus sign will be decided by buy or sell sign. BUY=positive, sell = negative
	 * 3. Here is a critical case where if I sell any row and then buy only the quantity will be minus 
	 * but the previous calculated price will have no impact on this and then again we buy any share 
	 * for that ticker then the weighted average will be the (prev_cost_price * prev_calculated_quantity 
	 * + current_cost_price * quantity)/(prev_calculated_quantity+quantity)
	 */
	public double getCostpriceSingleTicker(ArrayList<PortFolio> rslt) {
		double result = 0.0;
		double last_costprice = 0;
		double sumQuantity = 0;
		int flag = 0;
		for (int j = 0; j < rslt.size(); j++) {
			if (rslt.get(j).getSign().equals("BUY")) {

				double qq = rslt.get(j).getNumber_of_share();
				if (flag == 1) {

					result = last_costprice * sumQuantity + qq * rslt.get(j).getCost_price();
					last_costprice = result / (sumQuantity + qq);
					sumQuantity = sumQuantity + qq;
					flag = 0;
				} else {
					sumQuantity += qq;
					result += qq * rslt.get(j).getCost_price();
					last_costprice = result / sumQuantity;
				}
			} else {
				sumQuantity -= rslt.get(j).getNumber_of_share();
				flag = 1;
			}
		}
		return last_costprice;
	}

	/*
	 * method for Inserting new data into the summary table. Before the first loop in the method the dates are convert 
	 * into calendar instants for iterating from the start to till end. inside the loop 
	 * 1. Take always the previous date from the current date of the loop to calculate the summary table record elements 
	 * 2.The query took all the records of portfolio in consideration within the startdate to till current date.
	 * 3. create a map called tickerMap which map the ticker value and quantity from the considered records of portfolio table.
	 * 4.From ticker Map according to the positive and negative sign the buy and sell sign set in the signmap Map<ticker,sign>  
	 * 5.similarly calculate and generate the cost price map Map<ticker, costprice>
	 * 6.Current price Map from price table Map<ticker, current price>
	 * 7.Calculate portfolio value and create map for all ticker Map<ticker, portfolio_value>
	 * Each Map will contain unique ticker and their corresponding quantity, cost price, sign, current price.
	 * 8. now take all the previous day records from summary table for certain portfolio.
	 * 9. now create a loop for ticker map and nested loop for previous record list. if the ticker map contains the ticker in the 
	 * previous day records re calculate the cost price, quantity, portfolio value. the cost price will update according to buy 
	 * sell sign. If not found in the previous day record, create a new record for that ticker and insert into the summary table.
	 * After that the remaining records from previous day will be insert as current date records in the summary table.
	 */
	public void Insert(String portName, String startdate, String enddate) throws SQLException, ParseException {
		Session session = sessionFactory.openSession();
		

		// format the dates for calendar doing for loop
		SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
		Date dateStart = input_format.parse(startdate);
		Date EndDate = input_format.parse(enddate);

		Calendar start = Calendar.getInstance();
		start.setTime(dateStart);
		Calendar end = Calendar.getInstance();
		end.setTime(EndDate);

		/*
		 * prev date for considering the summary records of previous date
		 */
		for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			// Do your job here with `date`.
			String UptoendDate = input_format.format(date);
			
			Calendar prev = Calendar.getInstance();
			prev.setTime(dateStart);
			prev.add(Calendar.DATE, -1); // number of days to add
			String prev_date = input_format.format(prev.getTime());
			
			session.beginTransaction();

			Query queryPort = session.getNamedQuery("getAllFromProvidedRange").setParameter("q_portName", portName)
					.setParameter("q_startdate", startdate).setParameter("q_enddate", UptoendDate);
			ArrayList<PortFolio> rslt = (ArrayList<PortFolio>) queryPort.getResultList();

			System.out.println("size array of query for certain port: " + rslt.size());
			Map<String, Double> tickermap = new HashMap<String, Double>();
			Map<String, String> signMap = new HashMap<String, String>();
			/*
			 * calculating weighted sum of quantity an mapping with ticker
			 */
			tickermap = gettickerValuesForSinglePort(tickermap, rslt);
			//Map<String,String> signmap=new HashMap<String,String>();
			for (Map.Entry<String, Double> entry : tickermap.entrySet()) {
				if(entry.getValue()<0){
					signMap.put(entry.getKey(),"SELL");
				}
				else{
					signMap.put(entry.getKey(), "BUY");
				}
			}

			System.out.println("size tickerMap : " + tickermap.size());
			/*
			 * create a map for portfolio value with ticker
			 */

			Map<String, Double> MapportfolioValue = new HashMap<String, Double>();
			Map<String, Double> MapCurrentPrice = new HashMap<String, Double>();

			/*
			 * calculate the cost price
			 */
			Map<String, Double> MapCostPrice = new HashMap<String, Double>();

			MapCostPrice = calculateCostprice(tickermap, startdate, UptoendDate, portName);

			/*
			 * get current price and calculate portfolio_value
			 */
			MapCurrentPrice = calculateCurrentprice(tickermap, UptoendDate);
			MapportfolioValue = calculatePorfolioValue(MapCurrentPrice, MapCostPrice, tickermap);

			/*
			 * This is the insertion code for port summary table;
			 */

			Query prev_records = session.getNamedQuery("getprevRecordsSummary").setParameter("q_portName", portName)
					.setParameter("q_date", prev_date);

			ArrayList<PortSummaryTable> prev_daySummary = (ArrayList<PortSummaryTable>) prev_records.getResultList();
			Map<Integer,Integer> indexList=new HashMap<Integer,Integer>();
			int flag = 0;
			
			for (Map.Entry<String, Double> entry : tickermap.entrySet()) {
				flag = 0;
				for (int k = 0; k < prev_daySummary.size(); k++) {

					if (entry.getKey().equals((prev_daySummary.get(k).getTicker()))) {
						indexList.put(k,k);
						flag = 1;
						String ticker = prev_daySummary.get(k).getTicker();
						double quantity = tickermap.get(prev_daySummary.get(k).getTicker());
						PortSummaryTable portSummary = new PortSummaryTable();
						portSummary.setPort_name(portName);
						portSummary.setTicker(ticker);
						double CostPrice =0;
						double t_quantity = prev_daySummary.get(k).getShare_quantity() + quantity;
						if(signMap.get(ticker).equals("SELL")){
							CostPrice=prev_daySummary.get(k).getCost_price();
						
						}
						else{
							CostPrice = (quantity * MapCostPrice.get(ticker)+ 
									prev_daySummary.get(k).getShare_quantity() * prev_daySummary.get(k).getCost_price())/ t_quantity;
						}
								
						double PorfolioValue = MapCurrentPrice.get(ticker) * t_quantity;

						portSummary.setShare_quantity(t_quantity);
						portSummary.setSource_date(UptoendDate);
						portSummary.setCost_price(CostPrice);
						portSummary.setCurrent_price(MapCurrentPrice.get(ticker));
						portSummary.setPortfoli_value(PorfolioValue);
						portSummary.setDelete_flag(0);
						// save method of hibernate
						session.save(portSummary);
					} 
				}
				if (flag == 0) {
					
					PortSummaryTable portSummary = new PortSummaryTable();
					portSummary.setPort_name(portName);
					portSummary.setTicker(entry.getKey());
					portSummary.setShare_quantity(entry.getValue());
					portSummary.setSource_date(UptoendDate);
					portSummary.setCost_price(MapCostPrice.get(entry.getKey()));
					portSummary.setCurrent_price(MapCurrentPrice.get(entry.getKey()));
					portSummary.setPortfoli_value(MapportfolioValue.get(entry.getKey()));
					portSummary.setDelete_flag(0);
					// save method of hibernate
					session.save(portSummary);
				}
			}
			for(int m=0;m<prev_daySummary.size();m++){
				if(!indexList.containsKey(m)){
					PortSummaryTable portSummary = new PortSummaryTable();
					portSummary.setPort_name(portName);
					portSummary.setTicker(prev_daySummary.get(m).getTicker());
					portSummary.setShare_quantity(prev_daySummary.get(m).getShare_quantity());
					portSummary.setSource_date(UptoendDate);
					portSummary.setCost_price(prev_daySummary.get(m).getCost_price());
					portSummary.setCurrent_price(prev_daySummary.get(m).getCurrent_price());
					portSummary.setPortfoli_value(prev_daySummary.get(m).getPortfoli_value());
					portSummary.setDelete_flag(0);
					// save method of hibernate
					session.save(portSummary);
				}
			}
			session.getTransaction().commit();
			
		}

		session.close();
	}

	/*
	 * creating map for portfolio value and calculation.
	 * Here we consider the portfolio Value calculation in such a way that the ticker map, current price and cost price map 
	 * with ticker will pass as a parameter in the function. Now if current price = 0 then multiply cost price and quantity
	 * to calculate portfolio value for that ticker 
	 * else multiply quantity and current price to calcualte portfolio value
	 * then put it on a portfolio value map
	 */
	private Map<String, Double> calculatePorfolioValue(Map<String, Double> mapCurrentPrice,
			Map<String, Double> MapCostPrice, Map<String, Double> tickerMap) {
		// TODO Auto-generated method stub
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		Map<String, Double> MapportfolioValue = new HashMap<String, Double>();
		for (Map.Entry<String, Double> entry : mapCurrentPrice.entrySet()) {
			System.out.println(entry.getKey() + "/" + entry.getValue());

			if (entry.getValue() == 0) {
				double port_value = MapCostPrice.get(entry.getKey()) * tickerMap.get(entry.getKey());
				if (!MapportfolioValue.containsKey(entry.getKey())) {
					MapportfolioValue.put(entry.getKey(), port_value);
				}
			} else {
				double portvalue = entry.getValue() * tickerMap.get(entry.getKey());
				if (!MapportfolioValue.containsKey(entry.getKey())) {
					MapportfolioValue.put(entry.getKey(), portvalue);
				}
			}

		}
		session.getTransaction().commit();
		session.close();
		return MapportfolioValue;
	}

	/*
	 * current price calculation and maping according to ticker
	 * 1. For each ticker execute a query on the desired date to take in consideration the value of current date
	 * 2. put it on the price map structure 
	 */
	private Map<String, Double> calculateCurrentprice(Map<String, Double> tickermap, String uptoendDate) {
		// TODO Auto-generated method stub

		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Map<String, Double> MapCurrentPrice = new HashMap<String, Double>();
		for (Map.Entry<String, Double> entry : tickermap.entrySet()) {
			System.out.println(entry.getKey() + "/" + entry.getValue());

			Query currentpriceQuery = session.getNamedQuery("getCurrentpriceFromPriceTable")
					.setParameter("q_tickerName", entry.getKey()).setParameter("q_date", uptoendDate);
			// current price row
			ArrayList<PriceTable> singleResult = (ArrayList<PriceTable>) currentpriceQuery.getResultList();
			double current_price = 0.0;
			if (singleResult.size() > 0) {
				current_price = singleResult.get(0).getPrice();
			}

			// seting current price column data in port summary table
			if (!MapCurrentPrice.containsKey(entry.getKey())) {
				MapCurrentPrice.put(entry.getKey(), current_price);
			}
		}
		session.getTransaction().commit();
		session.close();
		return MapCurrentPrice;
	}

	/*
	 * calculating cost price for certain date range
	 * 1. to calculate the cost price for a ticker of a certain portname on a certain date range, execute a query 
	 * in portfolio table and take all the records within the range of parameters 
	 * 2. going to call the function for calculating cost price for each ticker. 
	 * 3. for ticker CASH the cost price is intend to set as 1.00  
	 */
	private Map<String, Double> calculateCostprice(Map<String, Double> tickermap, String startdate, String uptoendDate,
			String portName) {
		// TODO Auto-generated method stub
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Map<String, Double> MapCostPrice = new HashMap<String, Double>();
		for (Map.Entry<String, Double> entry : tickermap.entrySet()) {

			String ticker = entry.getKey();
			Query queryCostPrice = session.getNamedQuery("getAllFromProvidedRangeForCostPrice")
					.setParameter("q_tickerName", ticker).setParameter("q_startdate", startdate)
					.setParameter("q_enddate", uptoendDate).setParameter("q_portName", portName);
			ArrayList<PortFolio> rsltCost = (ArrayList<PortFolio>) queryCostPrice.getResultList();
			System.out.println("array size for cost price data from port table: " + rsltCost.size());

			double cost_price = getCostpriceSingleTicker(rsltCost);
			if (!MapCostPrice.containsKey(entry.getKey())) {
				if (entry.getKey().equals("CASH")) {
					MapCostPrice.put(entry.getKey(), 1.0);
				} else {
					MapCostPrice.put(entry.getKey(), cost_price);
				}

			}

		}
		session.getTransaction().commit();
		session.close();
		return MapCostPrice;
	}

	/*
	 * This method is basically for requesting api method which request with parameters , portName, ticker, and 
	 * desired date
	 * it will return a single day record from portfolio summary table for that ticker with corresponding ticker name.
	 * The query is return a lone record.
	 */
	public ArrayList<PortSummaryTable> getSingledata(String portName, String ticker, String d_date) {
		ArrayList<PortSummaryTable> rslt = new ArrayList<PortSummaryTable>();
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		Query query = session.getNamedQuery("getDesiredDateData").setParameter("q_portName", portName)
				.setParameter("q_ticker", ticker).setParameter("q_date", d_date);

		rslt = (ArrayList<PortSummaryTable>) query.getResultList();
		System.out.println(rslt.size());
		session.getTransaction().commit();
		session.close();
		return rslt;
	}

	/*
	 * weight in portfolio calculation and update each row in port summary table.
	 * 1. for certain portname and date we execute a query to take the data from summary table into the rslt array 
	 * 2 The we take the same parameters into consideration in portfolio value table to execute a query and take 
	 * the portfolio value on that date
	 * 3.equation weight in portfolio = rslt list portfolio value for certain ticker/ portfolio_value table  portfolio value on that date
	 * 4. set the weight for the record and update the record by calling hibernate update method. 
	 */
	public void rowUpdateWeightInPortfolio(String portName, String src_date) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		Query query = session.getNamedQuery("PortsummaryDataforCertainDate").setParameter("q_portName", portName)
				.setParameter("q_date", src_date);
		ArrayList<PortSummaryTable> rslt = (ArrayList<PortSummaryTable>) query.getResultList();

		String SQL_QUERY = "select u from PortfolioValue u where u.portName='" + portName + "' and u.source_date='"
				+ src_date + "' and u.delete_flag<>1";
		Query queryPortValue = session.createQuery(SQL_QUERY);
		ArrayList<PortfolioValue> portRslt = (ArrayList<PortfolioValue>) queryPortValue.getResultList();
		double portValueSum = portRslt.get(0).getPortfolio_value();
		for (int i = 0; i < rslt.size(); i++) {
			double weightinChange = rslt.get(i).getPortfoli_value() / portValueSum;
			rslt.get(i).setWeightInPortfolio(weightinChange);
			session.update(rslt.get(i));
		}

		session.getTransaction().commit();
		session.close();
	}

	/*
	 * Bulk calculation for certain date range an update portfolio records with
	 * inserting weight in portfolio column value
	 * The process is noramlly if we want to update the portfolio summary table records to insert weight in portfolio then 
	 * the provided date range in parameteres  will help to loop through the dates and the port name will help to update the 
	 * value  for a certain date by calling the rowUpdateWeightinPortfolio
	 */
	public void BulkUpdateSummaryRecords(String portName, String from_date, String to_date) throws ParseException {
		SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
		Date dateStart = input_format.parse(from_date);
		Date EndDate = input_format.parse(to_date);

		Calendar start = Calendar.getInstance();
		start.setTime(dateStart);
		Calendar end = Calendar.getInstance();
		end.setTime(EndDate);
		for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			String src_date = input_format.format(date);
			rowUpdateWeightInPortfolio(portName, src_date);
		}
	}

	/*
	 * get portfolio summary table data under each port name and certain date
	 * Again it is a method for requesting api. the api will request with a 
	 * portname and date it will execute the query with those parameters in portfolio summary table and 
	 * the return list of records will return to the api calling functio.
	 */
	public ArrayList<PortSummaryTable> getEachPortData(String port_name, String d_date) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		Query query = session.getNamedQuery("getEachPortInfo").setParameter("q_portName", port_name)
				.setParameter("q_date", d_date);

		ArrayList<PortSummaryTable> rslt = (ArrayList<PortSummaryTable>) query.getResultList();
		System.out.println(rslt.size());

		session.getTransaction().commit();
		session.close();
		return rslt;
	}

	public void exportData() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		String SQL_QUERY = "select u from PortSummaryTable u where u.port_name='" + "DBH" + "' and u.source_date='"
				+ "2010-06-12" + "' and u.share_quantity<>0 and u.delete_flag<>1";
		Query query = session.createQuery(SQL_QUERY);
		List<PortSummaryTable> rsult = query.getResultList();
		File fileName = new File("generate_files\\values.txt");
		try {
			FileWriter fw = new FileWriter(fileName);
			BufferedWriter output = new BufferedWriter(fw);

			for (int i = 0; i < rsult.size(); i++) {
				// line by line write
				output.write(rsult.get(i).toString());
				output.newLine();
				output.newLine();
			}
			output.close();
		} catch (Exception e) {
			System.out.println("The Given Exception is: " + e);
		}

		session.getTransaction().commit();
		session.close();
	}

	/*
	 * Inserting data into summary table for certain ticker  of a port name within date range. 
	 * This whole process as similar as the insertion process described earlier.
	 * 1. point to be noted here every calculation of quantity, costprice, portfolio value, and other thing is map with date
	 * exacly <date,costprice>, <date, portfolio_value>, <date,current_price> <date,quantity> this format
	 * 3. +datewise map quantity map
	 * 4.Tree map used to sort the map
	 * 5.similarly price map, cost price map, portfolio value map
	 * 6. after calcualting all insert new data into summary table. 
	 */
	public void inserRowInSummaryTickerWise(String ticker, String portName, String from_date, String to_date)
			throws ParseException {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		String SQL_QUERY = "select u from PortFolio u where u.ticker='" + ticker + "' " + "and u.portfoli_name='"
				+ portName + "' and u.delete_flag<>1 order by u.source_date";
		Query query = session.createQuery(SQL_QUERY);
		ArrayList<PortFolio> rslt = (ArrayList<PortFolio>) query.getResultList();
		Map<String, Double> datewiseMap = new HashMap<String, Double>();

		SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
		Date dateStart = input_format.parse(from_date);
		Date EndDate = input_format.parse(to_date);

		Calendar start = Calendar.getInstance();
		start.setTime(dateStart);
		Calendar end = Calendar.getInstance();
		end.setTime(EndDate);

		for (int i = 0; i < rslt.size(); i++) {

			// double curr_price=rslt.get(i).getCurrent_price();
			// double quantity=rslt.get(i).getNumber_of_share();
			// double portfolioValue=
			String sign = rslt.get(i).getSign();

			if (datewiseMap.containsKey(rslt.get(i).getSource_date())) {
				double quantitySum = datewiseMap.get(rslt.get(i).getSource_date());
				if (sign.equals("BUY")) {
					quantitySum += rslt.get(i).getNumber_of_share();
				} else {
					quantitySum -= rslt.get(i).getNumber_of_share();

				}
				datewiseMap.put(rslt.get(i).getSource_date(), quantitySum);
			} else {
				double quantity = 0.0;
				if (sign.equals("BUY")) {
					quantity = rslt.get(i).getNumber_of_share();
					datewiseMap.put(rslt.get(i).getSource_date(), quantity);
				} else {
					quantity = 0 - rslt.get(i).getNumber_of_share();
					datewiseMap.put(rslt.get(i).getSource_date(), quantity);
				}
			}

		}
		double initialvalue = datewiseMap.get(rslt.get(0).getSource_date());
		int k = 0;
		double val = 0;
		/*
		 * calculating weight so that the summation will work from the beging of
		 * the date for a ticker
		 */
		TreeMap<String, Double> sorted = new TreeMap<>();

		// Copy all data from hashMap into TreeMap
		sorted.putAll(datewiseMap);

		for (Map.Entry<String, Double> entry : sorted.entrySet()) {
			if (k == 0) {
				k++;
				continue;
			}
			initialvalue += entry.getValue();
			sorted.put(entry.getKey(), initialvalue);
		}

		for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			String src_date = input_format.format(date);
			if (!sorted.containsKey(src_date)) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.add(Calendar.DATE, -1);
				date = cal.getTime();
				String prev_date = input_format.format(date);
				sorted.put(src_date, sorted.get(prev_date));
			}
		}
		Map<String, Double> costpriceMap = new HashMap<String, Double>();
		double currentprice = 0;
		// double costPriceT = 0;
		Map<String, Double> currentpriceMap = new HashMap<String, Double>();
		// for calculating cost price selecting date wise values of port from
		// rslt array

		for (Map.Entry<String, Double> entry : sorted.entrySet()) {
			ArrayList<PortFolio> costcalArray = new ArrayList<PortFolio>();
			for (int i = 0; i < rslt.size(); i++) {
				if (rslt.get(i).getSource_date().equals(entry.getKey())) {
					costcalArray.add(rslt.get(i));
				}
			}
			double costprice = getCostpriceSingleTicker(costcalArray);
			if (costprice <= 0) {
				costprice = 1;
			}
			if (!costpriceMap.containsKey(entry.getKey())) {
				costpriceMap.put(entry.getKey(), costprice);
			}
			if (ticker.equals("CASH")) {
				currentprice = 1.0;
				if (!currentpriceMap.containsKey(entry.getKey())) {
					currentpriceMap.put(entry.getKey(), 1.0);
				}
			} else {
				String SQL_price = "select u from PriceTable u where u.ticker='" + ticker + "' and u.price_date='"
						+ entry.getKey() + "'";
				Query priceQuery = session.createQuery(SQL_price);
				ArrayList<PriceTable> curPrice = (ArrayList<PriceTable>) priceQuery.getResultList();
				if (!currentpriceMap.containsKey(entry.getKey())) {
					currentpriceMap.put(entry.getKey(), curPrice.get(0).getPrice());
				}
			}

		}
		Map<String, Double> portValues = new HashMap<String, Double>();
		portValues = calculatePorfolioValue(currentpriceMap, costpriceMap, sorted);
		for (Map.Entry<String, Double> entry : sorted.entrySet()) {
			PortSummaryTable p_summary = new PortSummaryTable();
			p_summary.setPort_name(portName);
			p_summary.setTicker(ticker);
			p_summary.setSource_date(entry.getKey());
			p_summary.setShare_quantity(entry.getValue());
			if (!costpriceMap.get(entry.getKey()).isNaN()) {
				p_summary.setCost_price(costpriceMap.get(entry.getKey()));
			} else {
				p_summary.setCost_price(1.0);
			}
			if (!currentpriceMap.get(entry.getKey()).isNaN()) {
				p_summary.setCurrent_price(currentpriceMap.get(entry.getKey()));
			}

			p_summary.setPortfoli_value(portValues.get(entry.getKey()));
			p_summary.setDelete_flag(0);
			session.save(p_summary);
			session.flush();
			session.clear();
		}
		session.getTransaction().commit();
		session.close();
	}

	/*
	 * after inserting the newly cashrow, make delete flag on all the records
	 * from summary table on that date and re calculate all the things for the
	 * date an dinsert new records on summary on that date
	 */
	public void summarytableDataDropAndInsert(String port_name, String start_date,String end_date) throws ParseException, SQLException {
		// TODO Auto-generated method stub
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		String SQL_QUERY = "select u from PortSummaryTable u where u.port_name='" + port_name + "' and u.source_date>='"
				+ start_date + "' and u.source_date<='"+ end_date +"' and u.delete_flag<>1";
		Query query = session.createQuery(SQL_QUERY);
		ArrayList<PortSummaryTable> rslt = (ArrayList<PortSummaryTable>) query.getResultList();
		if (rslt.size() > 0) {
			for (int i = 0; i < rslt.size(); i++) {
				rslt.get(i).setDelete_flag(1);
				session.update(rslt.get(i));
			}
		}
		session.getTransaction().commit();
		session.close();

		// callforSummarydataInsert(port_name,d_date);
	}
 /*
  * portfolio value table dat insertion contraint 
  * get all the previous day records fro the desired port name
  */
	public ArrayList<PortSummaryTable> prev_datePortSummary(String portName, String prev_date) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		String SQL_QUERY = "select u from PortSummaryTable u where u.port_name='" + portName + "' and u.source_date='"
				+ prev_date + "' and u.delete_flag<>1";

		Query query = session.createQuery(SQL_QUERY);
		ArrayList<PortSummaryTable> rslt = (ArrayList<PortSummaryTable>) query.getResultList();

		session.getTransaction().commit();
		session.close();

		return rslt;

	}

}
