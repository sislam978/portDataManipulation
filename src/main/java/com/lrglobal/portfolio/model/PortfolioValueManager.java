package com.lrglobal.portfolio.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;

public class PortfolioValueManager {
	public static final double constants = 1000;

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

	public void Insert(String portName,String source_date, String E_date) throws ParseException {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		
		SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
		Date dateStart = input_format.parse(source_date);
		
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
			double portValue = 0;
			/*
			 * calculating portfolio cost from summary table records
			 * equation summation of (portfolio_cost=quantity*cost_price)
			 */
			double portfolio_cost=0;
			for (int i = 0; i < rslt.size(); i++) {
				portValue += rslt.get(i).getPortfoli_value();
				double costValue=rslt.get(i).getShare_quantity()*rslt.get(i).getCost_price();
				portfolio_cost+=costValue;
			}
			PortfolioValue portfolioValue = new PortfolioValue();
			portfolioValue.setPortName(rslt.get(0).getPort_name());
			portfolioValue.setSource_date(rslt.get(0).getSource_date());
			portfolioValue.setPortfolio_value(portValue);
			portfolioValue.setDelete_flag(0);
			portfolioValue.setPortfolio_cost(portfolio_cost);
			session.save(portfolioValue);
		}

		session.getTransaction().commit();
		session.close();
	}

	public ArrayList<PortfolioValue> getSinglePortValueData(String portName, String d_date) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		ArrayList<PortfolioValue> rslt = new ArrayList<PortfolioValue>();

		String SQL_QUERY = "select u from PortfolioValue u where u.portName='" + portName + "' and u.source_date='"
				+ d_date + "' and u.delete_flag<>1";
		Query query = session.createQuery(SQL_QUERY);
		rslt = (ArrayList<PortfolioValue>) query.getResultList();

		session.getTransaction().commit();
		session.close();

		return rslt;
	}

	public void insertIndexinEachRow(String portName,String source_date,String end_date) throws ParseException {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		/*
		 * Taking prev date in the output
		 */
		SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
		Date End_Date = input_format.parse(end_date);
		Date dateStart = input_format.parse(source_date);

		Calendar start = Calendar.getInstance();
		start.setTime(dateStart);

		Calendar end = Calendar.getInstance();
		end.setTime(End_Date);

		for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			String src_date = input_format.format(date);

			Calendar start1 = Calendar.getInstance();
			start1.setTime(date);
			start1.add(Calendar.DAY_OF_MONTH, -1);
			Date pd = start1.getTime();
			String prev_date = input_format.format(pd);

			/*
			 * Selecty all the records from portsummary table for further
			 * calculation then equation for port index calculation in portfolio
			 * value table is SUM of(P_i-1 * price change_i *
			 * weightinPortfolio_i-1 ) here p is portfolio value and weight ion
			 * portfolio both are coming from port summary table and then for
			 * each ticker it will take price change from price table and
			 * calculate the described equation for each ticker and then sum The
			 * final summation is the change in index which will store in
			 * portfolio value table within a certain portname and date.
			 * Hibernate update operation will need
			 */

			PortSummaryTableManager pstm=new PortSummaryTableManager();
			pstm.setup();
			ArrayList<PortSummaryTable> rslt = pstm.prev_datePortSummary(portName,prev_date);
			pstm.exit();
			double desiredvalue = 0;
			
			for (int i = 0; i < rslt.size(); i++) {

				String SQL_PRICE = "select u from PriceTable u where u.ticker='" + rslt.get(i).getTicker()
						+ "' and u.price_date='" + src_date + "'";
				Query priceQuery = session.createQuery(SQL_PRICE);
				ArrayList<PriceTable> priceChange=(ArrayList<PriceTable>)priceQuery.getResultList();
				if (priceChange.size() > 0) {
					double vv = rslt.get(i).getWeightInPortfolio() * priceChange.get(0).getPrice_change();
					desiredvalue += vv;
				}
				else{
					double vv = rslt.get(i).getWeightInPortfolio() *1.0;
					desiredvalue += vv;
				}

			}
			/*
			 * certain portfolio value table record update with change in index
			 */
			String SQL_INDEX = "select u from PortfolioValue u where u.portName='" + portName + "' and u.source_date='"
					+ src_date + "' and u.delete_flag<>1";
			Query indexQuery = session.createQuery(SQL_INDEX);
			ArrayList<PortfolioValue> indexUpDate = (ArrayList<PortfolioValue>) indexQuery.getResultList();
			if(rslt.size()<1){
				indexUpDate.get(0).setChangePortIndex(1.0);
				session.update(indexUpDate.get(0));
			}
			else{
				indexUpDate.get(0).setChangePortIndex(desiredvalue);
				session.update(indexUpDate.get(0));
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

	public void delete() {
		// code to remove a Data
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.getTransaction().commit();
		session.close();
	}

	public Map<String, Double> getALlValueFOrChart(String portName, String startDate, String endDate) {
		// TODO Auto-generated method stub

		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Map<String, Double> rslt = new HashMap<String, Double>();
		Query query = session.getNamedQuery("getALLPortValueDataForChart").setParameter("q_portName", portName)
				.setParameter("q_sdate", startDate).setParameter("q_edate", endDate);
		ArrayList<PortfolioValue> rsltList = (ArrayList<PortfolioValue>) query.getResultList();
		double changeIndex = 0;
		double portfolioIndex = 1;
		double prev_value = 1;
		if (rsltList.get(0).getChangePortIndex() <= 0.0) {
			changeIndex = 1;
		} else {
			changeIndex = rsltList.get(0).getChangePortIndex();
		}
		portfolioIndex = changeIndex * constants;
		prev_value = portfolioIndex;
		rslt.put(rsltList.get(0).getSource_date(), portfolioIndex);

		for (int i = 1; i < rsltList.size(); i++) {
			if (!rsltList.get(0).getChangePortIndex().isNaN()) {
				double va = 0.0;
				if (rsltList.get(i).getChangePortIndex() <= 0.0) {
					va = 1;
				} else {
					va = rsltList.get(i).getChangePortIndex();
				}
				portfolioIndex = prev_value * va;
				prev_value = portfolioIndex;
				if (!rslt.containsKey(rsltList.get(i).getSource_date())) {
					rslt.put(rsltList.get(i).getSource_date(), portfolioIndex);
				}
			}

		}

		session.getTransaction().commit();
		session.close();
		return rslt;
	}
	public Map<String,Double> portValuesForChart(String portName, String start_date,String end_date){
		
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Map<String, Double> rslt = new HashMap<String, Double>();
		Query query = session.getNamedQuery("getALLPortValueDataForChart").setParameter("q_portName", portName)
				.setParameter("q_sdate", start_date).setParameter("q_edate", end_date);
		ArrayList<PortfolioValue> rsltList = (ArrayList<PortfolioValue>) query.getResultList();

		for (int i = 0; i < rsltList.size(); i++) {
			if (!rslt.containsKey(rsltList.get(i).getSource_date())) {
				rslt.put(rsltList.get(i).getSource_date(), rsltList.get(i).getPortfolio_value());
			}
		}

		session.getTransaction().commit();
		session.close();
		return rslt;
	}
	
	/*
	 * portfolio value table select data to on delete flag and call insertion method of portfolio value table
	 */
		public void deleteandReInsertonDate(String port_name, String start_date, String end_date) throws ParseException {
			// TODO Auto-generated method stub
			Session session=sessionFactory.openSession();
			session.beginTransaction();
			
			String SQL_QUERY="select u from PortfolioValue u where u.portName='" + port_name + "' and u.source_date>='"
					+ start_date + "' and u.source_date<= '"+ end_date +"' and u.delete_flag<>1";
			Query query =session.createQuery(SQL_QUERY);
			
			ArrayList<PortfolioValue> rslt=(ArrayList<PortfolioValue>)query.getResultList();
			if(rslt.size()>0){
				for(int  i=0;i<rslt.size();i++){
					rslt.get(i).setDelete_flag(1);
					session.update(rslt.get(i));
				}
				
			}
			session.getTransaction().commit();
			session.close();			
		}
		
		public double calculateTotalGain(String portName, String d_date){
			Session session=sessionFactory.openSession();
			session.beginTransaction();
			
			double Total_Gain=0;
			String SQL_QUERY="select u from PortfolioValue u where u.portName='" + portName + "' and u.source_date>='"
					+ d_date + "' and u.delete_flag<>1";
			Query query =session.createQuery(SQL_QUERY);
			
			ArrayList<PortfolioValue> rslt=(ArrayList<PortfolioValue>)query.getResultList();
			if(rslt.size()>0){
				Total_Gain=rslt.get(0).getPortfolio_value()-rslt.get(0).getPortfolio_cost();
				
			}
			session.getTransaction().commit();
			session.close();	
			return Total_Gain;
		}

		public void updateRowFor_CapitalGain(String portName, String d_date){
			Session session=sessionFactory.openSession();
			session.beginTransaction();

			String SQL_QUERY="select u from PortfolioValue u where u.portName='" + portName + "' and u.source_date='"
					+ d_date + "' and u.delete_flag<>1";
			
			Query query=session.createQuery(SQL_QUERY);
			ArrayList<PortfolioValue> rslt=(ArrayList<PortfolioValue>) query.getResultList();
			if(!rslt.isEmpty()){
				CapitalGainManager cgm=new CapitalGainManager();
				cgm.setup();
				double netCapitalGain=cgm.calculateNetCapitalGain(portName, d_date);
				rslt.get(0).setNetCapital_gain(netCapitalGain);
				session.update(rslt.get(0));
			}
			
			session.getTransaction().commit();
			session.close();
		}
		
		public double calculateSummationOfcapitalgain(String portName, String start_date,String end_date){
			Session session=sessionFactory.openSession();
			session.beginTransaction();

			String SQL_QUERY="select u from PortfolioValue u where u.portName='" + portName + "' and u.source_date>='"
					+ start_date + "' and u.source_date<='"+ end_date +"'and u.delete_flag<>1";
			
			Query query=session.createQuery(SQL_QUERY);
			ArrayList<PortfolioValue> rslt=(ArrayList<PortfolioValue>) query.getResultList();
			double sumOfCapitalGain=0;
			for(int i=0;i<rslt.size();i++){
				if(rslt.get(i).getNetCapital_gain()!=null){
					sumOfCapitalGain+=rslt.get(i).getNetCapital_gain();
				}
				
			}
			
			session.getTransaction().commit();
			session.close();
			return sumOfCapitalGain;
		}
}
