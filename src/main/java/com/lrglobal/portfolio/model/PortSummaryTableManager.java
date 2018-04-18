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
	 * Quantity calculation for portfolio summary
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
				}
			}
		}
		return tickermap;
	}

	public double getCostpriceSinglePort(ArrayList<PortFolio> rslt) {
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

	// method for bulk insert for all ports at a time
	public void Insert(String startdate, String enddate) throws SQLException, ParseException {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		Query query = session.getNamedQuery("getAllPortName");
		ArrayList<PortFolio> portname = (ArrayList<PortFolio>) query.getResultList();

		// format the dates for calendar doing for loop
		SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
		Date dateStart = input_format.parse(startdate);
		Date EndDate = input_format.parse(enddate);

		Calendar start = Calendar.getInstance();
		start.setTime(dateStart);
		Calendar end = Calendar.getInstance();
		end.setTime(EndDate);

		for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			// Do your job here with `date`.
			String UptoendDate = input_format.format(date);

			for (int i = 0; i < portname.size(); i++) {

				Query queryPort = session.getNamedQuery("getAllFromProvidedRange")
						.setParameter("q_portName", portname.get(i).getPortfoli_name())
						.setParameter("q_startdate", startdate).setParameter("q_enddate", UptoendDate);
				ArrayList<PortFolio> rslt = (ArrayList<PortFolio>) queryPort.getResultList();

				System.out.println("size array of query for certain port: " + rslt.size());
				Map<String, Double> tickermap = new HashMap<String, Double>();
				/*
				 * calculating weighted sum of quantity an mapping with ticker
				 */
				tickermap = gettickerValuesForSinglePort(tickermap, rslt);
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
				String portName = portname.get(i).getPortfoli_name();
				MapCostPrice = calculateCostprice(tickermap, startdate, UptoendDate, portName);

				/*
				 * get current price and calculate portfolio_value
				 */
				MapCurrentPrice = calculateCurrentprice(tickermap, UptoendDate);
				MapportfolioValue = calculatePorfolioValue(MapCurrentPrice, MapCostPrice, tickermap);

				/*
				 * This is the insertion code for port summary table;
				 */
				for (Map.Entry<String, Double> entry : tickermap.entrySet()) {
					PortSummaryTable portSummary = new PortSummaryTable();
					portSummary.setPort_name(portname.get(i).getPortfoli_name());
					portSummary.setTicker(entry.getKey());
					portSummary.setShare_quantity(entry.getValue());
					portSummary.setSource_date(UptoendDate);
					portSummary.setCost_price(MapCostPrice.get(entry.getKey()));
					portSummary.setCurrent_price(MapCurrentPrice.get(entry.getKey()));
					portSummary.setPortfoli_value(MapportfolioValue.get(entry.getKey()));
					// save method of hibernate
					session.save(portSummary);
				}

			}
		}

		session.getTransaction().commit();
		session.close();
	}

	/*
	 * creating map for portfolio value and calculation
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
			double cost_price = getCostpriceSinglePort(rsltCost);
			if (!MapCostPrice.containsKey(entry.getKey())) {
				MapCostPrice.put(entry.getKey(), cost_price);
			}
		}
		session.getTransaction().commit();
		session.close();
		return MapCostPrice;
	}

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
	 * weight in portfolio calculation nad update each row in port summary table
	 */
	public void rowUpdateWeightInPortfolio(String portName, String src_date) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		Query query = session.getNamedQuery("PortsummaryDataforCertainDate").setParameter("q_portName", portName)
				.setParameter("q_date", src_date);
		ArrayList<PortSummaryTable> rslt = (ArrayList<PortSummaryTable>) query.getResultList();

		String SQL_QUERY = "select u from PortfolioValue u where u.portName='" + portName + "' and u.source_date='"
				+ src_date + "'";
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
 * Bulk calculation for certain date range an update portfolio records with inserting weight in portfolio column value
 */
	public void BulkUpdateSummaryRecords(String portName,String from_date, String to_date) throws ParseException{
		SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
		Date dateStart = input_format.parse(from_date);
		Date EndDate = input_format.parse(to_date);
		
		Calendar start = Calendar.getInstance();
		start.setTime(dateStart);
		Calendar end = Calendar.getInstance();
		end.setTime(EndDate);
		for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			String src_date = input_format.format(date);
			rowUpdateWeightInPortfolio(portName,src_date);
		}
	}
	/*
	 * get portfolio summary table data under each port name and certain date
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
				+ "2010-06-12" + "' and share_quantity<>0";
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
	 * CASH ticker row insert in summary table
	 */
	public void inserRowInSummaryTickerWise(String ticker, String portName,String from_date,String to_date) throws ParseException {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		String SQL_QUERY = "select u from PortFolio u where u.ticker='" + ticker + "' " + "and u.portfoli_name='"
				+ portName + "' order by source_date";
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
		double initialvalue=datewiseMap.get(rslt.get(0).getSource_date());
		int k=0;
		double val=0;
		/*
		 * calculating weight so that the summation will work from the beging of the date for a ticker
		 */
        TreeMap<String, Double> sorted = new TreeMap<>();
        
        // Copy all data from hashMap into TreeMap
        sorted.putAll(datewiseMap);
        
		for (Map.Entry<String, Double> entry : sorted.entrySet()) {
			if(k==0){
				k++;
				continue;
			}
			initialvalue+=entry.getValue();
			sorted.put(entry.getKey(), initialvalue);
		}
		
		for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			String src_date = input_format.format(date);
			if(!sorted.containsKey(src_date)){
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.add(Calendar.DATE, -1);
				date=cal.getTime();
				String prev_date=input_format.format(date);
				sorted.put(src_date, sorted.get(prev_date));
			}
		}
		Map<String, Double> costpriceMap = new HashMap<String, Double>();
		double currentprice = 0;
		//double costPriceT = 0;
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
			double costprice = getCostpriceSinglePort(costcalArray);
			if(costprice<=0) {
				costprice=1;
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
		Map<String,Double> portValues=new HashMap<String,Double>();
		portValues=calculatePorfolioValue(currentpriceMap,costpriceMap,sorted);
		for(Map.Entry<String, Double> entry : sorted.entrySet()){
			PortSummaryTable p_summary=new PortSummaryTable();
			p_summary.setPort_name(portName);
			p_summary.setTicker(ticker);
			p_summary.setSource_date(entry.getKey());
			p_summary.setShare_quantity(entry.getValue());
			if(!costpriceMap.get(entry.getKey()).isNaN()){
				p_summary.setCost_price(costpriceMap.get(entry.getKey()));
			}
			else{
				p_summary.setCost_price(1.0);
			}
			if(!currentpriceMap.get(entry.getKey()).isNaN()){
				p_summary.setCurrent_price(currentpriceMap.get(entry.getKey()));
			}
			
			p_summary.setPortfoli_value(portValues.get(entry.getKey()));
			session.save(p_summary);
			session.flush();
			session.clear();
		}
		session.getTransaction().commit();
		session.close();
	}

}
