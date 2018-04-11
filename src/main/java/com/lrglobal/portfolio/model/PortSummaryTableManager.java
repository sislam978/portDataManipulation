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

import org.apache.tomcat.jni.Mmap;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;

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

	public void Insert() throws SQLException, ParseException {
		// code to save a Data
		// Before that I have to Read an excel file and take the inputs from the
		// csvfile.
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Scanner inScanner = new Scanner(System.in);
		Query query = session.getNamedQuery("getAllPortName");
		ArrayList<PortFolio> portname = (ArrayList<PortFolio>) query.getResultList();

		System.out.println("Take startdate with yyyy-MM-dd format:");
		String startdate = inScanner.nextLine();
		System.out.println("Take enddate with yyyy-MM-dd format:");
		String enddate = inScanner.nextLine();
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

				System.out.println("size array : " + rslt.size());
				Map<String, Double> tickermap = new HashMap<String, Double>();
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

				for (Map.Entry<String, Double> entry : tickermap.entrySet()) {

					String ticker = entry.getKey();
					String portName = portname.get(i).getPortfoli_name();
					Query queryCostPrice = session.getNamedQuery("getAllFromProvidedRangeForCostPrice")
							.setParameter("q_tickerName", ticker).setParameter("q_startdate", startdate)
							.setParameter("q_enddate", UptoendDate).setParameter("q_portName", portName);
					ArrayList<PortFolio> rsltCost = (ArrayList<PortFolio>) queryCostPrice.getResultList();

					System.out.println("array size for cost price data from port table: " + rsltCost.size());
					double cost_price = getCostpriceSinglePort(rsltCost);
					if (!MapCostPrice.containsKey(entry.getKey())) {
						MapCostPrice.put(entry.getKey(), cost_price);
					}
				}

				/*
				 * get current price and calculate portfolio_value
				 */
				for (Map.Entry<String, Double> entry : tickermap.entrySet()) {
					System.out.println(entry.getKey() + "/" + entry.getValue());
					// Query query_currentprice =
					// session.getNamedQuery("getCurrentPrice")
					// .setParameter("q_tickerName",
					// entry.getKey()).setParameter("q_date", enddate);

					String SQL_QUERY = "select u from PriceTable u where u.ticker='" + entry.getKey()
							+ "' and u.price_date='" + UptoendDate + "'";
					Query currentpriceQuery = session.createQuery(SQL_QUERY);
					// current price row
					ArrayList<PriceTable> singleResult = (ArrayList<PriceTable>) currentpriceQuery.getResultList();
					// ResultSet rs=(ResultSet) query.getResultList();
					// String s=rs.getString(1);
					double current_price = 0.0;
					if (singleResult.size() > 0) {
						current_price = singleResult.get(0).getPrice();
					}

					// seting current price column data in port summary table
					if (!MapCurrentPrice.containsKey(entry.getKey())) {
						MapCurrentPrice.put(entry.getKey(), current_price);
					}
					/*
					 * calculate portfolio value and insert into the ticker port
					 * value map
					 */
					if (current_price == 0) {
						double port_value = MapCostPrice.get(entry.getKey()) * entry.getValue();
						if (!MapportfolioValue.containsKey(entry.getKey())) {
							MapportfolioValue.put(entry.getKey(), port_value);
						}
					} else {
						double portvalue = current_price * entry.getValue();
						if (!MapportfolioValue.containsKey(entry.getKey())) {
							MapportfolioValue.put(entry.getKey(), portvalue);
						}
					}

				}
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

	public void read() {
		// code to get a Data
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.getTransaction().commit();
		session.close();
	}

	public void addNetSell() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.getTransaction().commit();
		session.close();
	}

	public void update() {
		// code to modify a Data
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.getTransaction().commit();
		session.close();
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

	public void delete() {
		// code to remove a Data
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.getTransaction().commit();
		session.close();
	}

	public void rowUpdateWeightInPortfolio() throws ParseException {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		Scanner inScanner = new Scanner(System.in);
		System.out.println("Enter the desired Port Name");
		String portName = inScanner.nextLine();
		System.out.println("Enter the desired date");
		String source_date = inScanner.nextLine();

		SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
		Date dateStart = input_format.parse(source_date);
		System.out.println("Up to the date we wan to calculate:");
		String E_date = inScanner.nextLine();
		Date EndDate = input_format.parse(E_date);

		Calendar start = Calendar.getInstance();
		start.setTime(dateStart);
		Calendar end = Calendar.getInstance();
		end.setTime(EndDate);

		for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			// Do your job here with `date`.
			String src_date = input_format.format(date);

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
		}

		session.getTransaction().commit();
		session.close();
	}

	public void exportData() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		String SQL_QUERY = "select u from PortSummaryTable u where u.port_name='" + "DBH" + "' and u.source_date='"
				+ "2018-04-02" + "' and share_quantity<>0";
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

}
