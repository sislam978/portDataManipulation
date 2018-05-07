package com.lrglobal.portfolio.model;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.criterion.Order;
import org.hibernate.query.Query;

import com.lrglobal.portfolio.datageneration.ReadPortFolioDatafromCSV;

public class PortFolioManager {

	public static final double Cashcost_price = 1;
	public static final double Cashcurrent_price = 1;
	public static final String buy = "BUY";
	public static final String sell = "SELL";
	public static final String cashTicker = "CASH";

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

	public void Insert(String fileName) {
		// code to save a Data
		// Before that I have to Read an excel file and take the inputs from the
		// csvfile.
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		ReadPortFolioDatafromCSV readPortFolioDatafromCSV = new ReadPortFolioDatafromCSV();
		ArrayList<PortFolio> rslt = readPortFolioDatafromCSV.getAllData(fileName);
		// Collections.sort(rslt,PortFolio.sortingdata);

		for (int i = 0; i < rslt.size(); i++) {
			PortFolio pp = rslt.get(i);

			String SQL_QUERY = "select u from PortFolio u where u.portfoli_name='" + rslt.get(i).getPortfoli_name()
					+ "' and u.ticker='" + rslt.get(i).getTicker() + "' and u.sign='" + rslt.get(i).getSign()
					+ "' and u.source_date='" + rslt.get(i).getSource_date() + "' and u.delete_flag<>1";

			Query query = session.createQuery(SQL_QUERY);
			List<PortFolio> list = (List<PortFolio>) query.getResultList();
			/*
			 * conditional check if the query return any data in the list that
			 * means there is already exist the intended entry data thats why
			 * inside the condition we just skip the insertion and also the
			 * amount of data which are duplicate by extracting the size of the
			 * data
			 */
			if (list.size() > 0) {
				PortFolio finalPort = new PortFolio();
				if (list.get(0).getNumber_of_share() != null && rslt.get(i).getNumber_of_share() != null) {
					double quantity = list.get(0).getNumber_of_share() + rslt.get(i).getNumber_of_share();
					if (list.get(0).getCost_price() != null && rslt.get(i).getCost_price() != null) {
						double weightedsum = list.get(0).getNumber_of_share() * list.get(0).getCost_price()
								+ rslt.get(i).getNumber_of_share() * rslt.get(i).getCost_price();

						double avgCostPrice = weightedsum / quantity;
						list.get(0).setCost_price(avgCostPrice);
					}
					list.get(0).setNumber_of_share(quantity);

					session.saveOrUpdate(list.get(0));
				} else if (rslt.get(i).getNumber_of_share() != null) {
					list.get(0).setCost_price(rslt.get(i).getCost_price());
					list.get(0).setNumber_of_share(rslt.get(i).getNumber_of_share());
					session.saveOrUpdate(list.get(0));
				}
			} else {
				if (pp.getTicker().equals("CASH")) {
					pp.setCreated_by(1);
				}
				pp.setDelete_flag(0);
				session.save(pp);
			}
			/*
			 * using the snippet for batch insertion
			 */
			if (i % 250 == 0) {
				session.flush();
				session.clear();
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

	// inserting new record in portfolio table with introducing CASH ticker
	public void cashrow_insert(String port_name, String d_date) throws ParseException, SQLException {

		Session session = sessionFactory.openSession();
		session.beginTransaction();
		/*
		 * create a query for taking in consideration of those portfolio records
		 * which have parameters as same as the function parameters
		 */
		Query query = session.getNamedQuery("getAllOndatePortfolio").setParameter("q_portName", port_name)
				.setParameter("q_date", d_date);

		ArrayList<PortFolio> rslt = (ArrayList<PortFolio>) query.getResultList();
		PortFolio portFolio = new PortFolio();
		/*
		 * sending the considered records to the function to calculate Cash
		 * quantity
		 */
		double cash_NumShare = calculateShare(rslt);
		// double
		/*
		 * The conditional statement check whether the cash number poitive or
		 * negative if poitive then we have to set the CASH Ticker record sign
		 * SELL If negative then set BUY and we have to set number of share
		 * always poitive the sign will decide the record whether it is buy or
		 * sell
		 */
		if (cash_NumShare > 0) {
			portFolio.setSign(sell);
			portFolio.setNumber_of_share(cash_NumShare);
		} else {
			portFolio.setSign(buy);
			double total = 0 - cash_NumShare;
			portFolio.setNumber_of_share(total);
		}
		/*
		 * setting other attributes from defined constant and function
		 * parameters CASH ticker current price , cost price are set as 1 and
		 * cash ticker is CASH
		 */
		portFolio.setCurrent_price(Cashcurrent_price);
		portFolio.setCost_price(Cashcost_price);
		portFolio.setTicker(cashTicker);
		portFolio.setPortfoli_name(port_name);
		portFolio.setSource_date(d_date);
		/*
		 * Now we have to balance the cash record for that date if there have
		 * any records previously on the date If the data found, update the
		 * quantity and sign attribute of the object and then update the records
		 * in the database. If not then trigger directly save method from
		 * hibernate.
		 */
		String SQL_QUERY = "select u from PortFolio u where u.portfoli_name='" + port_name + "' and u.ticker='"
				+ cashTicker + "' and u.source_date='" + d_date + "' and u.created_by is null and u.delete_flag<>1";
		Query queryPort = session.createQuery(SQL_QUERY);
		ArrayList<PortFolio> rsltport = (ArrayList<PortFolio>) queryPort.getResultList();
		if (rsltport.size() > 0) {
			if (cash_NumShare > 0) {

				rsltport.get(0).setNumber_of_share(cash_NumShare);
				rsltport.get(0).setSign(sell);
			} else {
				rsltport.get(0).setNumber_of_share(0 - cash_NumShare);
				rsltport.get(0).setSign(buy);
			}
			session.update(rsltport.get(0));

		} else {
			portFolio.setDelete_flag(0);
			session.save(portFolio);
		}

		session.getTransaction().commit();
		session.close();

		// summarytableDataDropAndInsert(port_name,d_date);

	}

	// calculating the cash weight value in the method
	private double calculateShare(ArrayList<PortFolio> rslt) {
		// TODO Auto-generated method stub
		// double share_quantity=0;
		double weighted_sum = 0;

		/*
		 * Here according to the buy sell sign the summation of the total
		 * quantity will be calculated for cash ticker
		 */
		for (int i = 0; i < rslt.size(); i++) {
			double comm = calculate_commision(rslt.get(i));
			if (rslt.get(i).getSign().equals(buy)) {
				weighted_sum += (rslt.get(i).getCost_price() * rslt.get(i).getNumber_of_share());
				weighted_sum += comm;
			} else {
				weighted_sum -= (rslt.get(i).getCost_price() * rslt.get(i).getNumber_of_share());
				weighted_sum += comm;
			}
		}
		return weighted_sum;
	}

	private double calculate_commision(PortFolio portFolio) {
		// TODO Auto-generated method stub
		double commision_value = 0;

		if (portFolio.getCommission() == null) {
			commision_value = 0;
		} else {
			commision_value = portFolio.getCommission() * portFolio.getNumber_of_share() * portFolio.getCost_price();
		}
		return commision_value;
	}

	/*
	 * The method is create for cash update for whole table As we take all the
	 * distinct date from the query and start looping through the dates for the
	 * portfolio. in each loop iteration the cashrow_insert method will call
	 * then insert the calculated cash with full records for that date.
	 */
	public void bulkInsertForCashTicker(String portName) throws ParseException, SQLException {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		Query queryDate = session.getNamedQuery("getDistinctDate").setParameter("q_portName", portName);
		ArrayList<PortFolio> dates = (ArrayList<PortFolio>) queryDate.getResultList();

		for (int i = 0; i < dates.size(); i++) {
			cashrow_insert(portName, dates.get(i).getSource_date());
		}

		session.getTransaction().commit();
		session.close();
	}

	/*
	 * Api requested data insertion. API will send the record which need to
	 * insert. First check whether the intended record is already stored in
	 * database or not. the query here check with provided constraints in the
	 * parameters of the query. If the query result is 0 the save method will
	 * call to save and return a successful response from the api calling method
	 */
	public String insertRecordsApi(PortFolio rslt) throws ParseException, SQLException {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		String SQL_QUERY = "select u from PortFolio u where u.portfoli_name='" + rslt.getPortfoli_name()
				+ "' and u.ticker='" + rslt.getTicker() + "' and u.sign='" + rslt.getSign() + "' and u.source_date='"
				+ rslt.getSource_date() + "'and u.number_of_share='" + rslt.getNumber_of_share()
				+ "' and u.cost_price='" + rslt.getCost_price() + "' and u.delete_flag<>1";

		Query query = session.createQuery(SQL_QUERY);
		List<PortFolio> list = (List<PortFolio>) query.getResultList();
		if (list.size() > 0) {
			return "not successfull";
		} else {
			if (rslt.getTicker().equals("CASH")) {
				rslt.setCreated_by(1);
			}
			rslt.setDelete_flag(0);
			session.save(rslt);
		}

		session.getTransaction().commit();
		session.close();

		// cashrow_insert(rslt.getPortfoli_name(), rslt.getSource_date());

		return "sucessfull";
	}

	public void updateCashonDate() {
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

	public String takeinitialdate(String portName) {

		Session session = sessionFactory.openSession();
		session.beginTransaction();

		Query query = session.getNamedQuery("getinitialdate").setParameter("q_PortName", portName);
		ArrayList<PortFolio> rslt = (ArrayList<PortFolio>) query.getResultList();

		session.getTransaction().commit();
		session.close();
		return rslt.get(0).getSource_date();
	}

	public ArrayList<PortFolio> sendDesiredDateData(String portName, String desired_date) {

		Session session = sessionFactory.openSession();
		session.beginTransaction();
		ArrayList<PortFolio> rslt = new ArrayList<PortFolio>();
		String SQL_QUERY = "select u from PortFolio u where u.portfoli_name='" + portName
				+ "' and u.source_date='" + desired_date + "' and u.delete_flag<>1";

		Query query = session.createQuery(SQL_QUERY);
		rslt = (ArrayList<PortFolio>) query.getResultList();
		ArrayList<PortFolio> vv_rslt = new ArrayList<PortFolio>();
		for (int i = 0; i < rslt.size(); i++) {
			if (rslt.get(i).getTicker().equals("CASH")) {

				if (rslt.get(i).getCreated_by() != null) {
					vv_rslt.add(rslt.get(i));
				}

			} else if (!rslt.get(i).getTicker().equals("CASH")) {
				vv_rslt.add(rslt.get(i));
			}
		}
		session.getTransaction().commit();
		session.close();
		return vv_rslt;
	}

	public void cashDividendCASHrow(String portName, String ticker, String d_date) throws ParseException {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		CorporatedeclarationManager cdm = new CorporatedeclarationManager();
		cdm.setup();
		ArrayList<CorporateDeclaration> rsltCD = cdm.desiredRecords(ticker, d_date);
		cdm.exit();

		SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
		Date dateStart = input_format.parse(d_date);
		Calendar prev = Calendar.getInstance();
		prev.setTime(dateStart);
		prev.add(Calendar.DATE, -1); // number of days to add
		String prev_date = input_format.format(prev.getTime());

		String SQL_QUERY = "select u from PortSummaryTable u where u.port_name='" + portName + "' and u.ticker='" + ticker + "' and u.source_date='"
				+ prev_date + "'and u.delete_flag<>1";

		Query query = session.createQuery(SQL_QUERY);
		ArrayList<PortSummaryTable> rslt = (ArrayList<PortSummaryTable>) query.getResultList();

		if(rslt.isEmpty()){
			return;
		}
		double Cash_Quantity=rslt.get(0).getShare_quantity()*rsltCD.get(0).getCashDividend();
		String SQL_QUERY1 = "select u from PortFolio u where u.portfoli_name='" + portName + "' and u.ticker='" + cashTicker + "' and u.source_date='"
				+ d_date + "'and u.CashDividendFlag='"+rsltCD.get(0).getTickerName()+"'";
		Query query1=session.createQuery(SQL_QUERY1);
		ArrayList<PortFolio> cashPortfolio=(ArrayList<PortFolio>)query1.getResultList();
		
		if(cashPortfolio.isEmpty()){
			PortFolio pf=new PortFolio();
			pf.setPortfoli_name(portName);
			pf.setCurrent_price(Cashcurrent_price);
			pf.setCost_price(Cashcost_price);
			pf.setSign(buy);
			pf.setTicker(cashTicker);
			pf.setNumber_of_share(Cash_Quantity);
			pf.setSource_date(d_date);
			pf.setDelete_flag(0);
			pf.setCreated_by(1);
			pf.setCashDividendFlag(rsltCD.get(0).getTickerName());
			session.save(pf);
		}
		
		session.getTransaction().commit();
		session.close();
	}
	
	/*
	 * total commission= (cost_price-commission)*quantity * commission
	 * select all the data from portfolio table with provided range for certain portfolio name 
	 * the loop through and in each loop calculate the commission and add it to the total commission 
	 */
	public double calculateTotalCommission(String portName, String start_date,String end_date){
		
		Session session=sessionFactory.openSession();
		session.beginTransaction();
		
		double total_commission=0;
		
		String SQL_QUERY="select u from PortFolio u where u.portfoli_name='" + portName + "' and u.source_date>='"
				+ start_date + "'and u.source_date<='"+end_date+"' and u.delete_flag<> 1 and u.CashDividendFlag is null";
		
		Query query=session.createQuery(SQL_QUERY);
		ArrayList<PortFolio> rslt= (ArrayList<PortFolio>) query.getResultList();
		for(int i=0;i<rslt.size();i++){
			double cost_price=0;
			if(rslt.get(i).getCommission()!=null){
				cost_price=rslt.get(i).getCost_price()-rslt.get(i).getCommission();
				double commission=cost_price*rslt.get(i).getNumber_of_share()*rslt.get(i).getCommission();
				total_commission+=commission;
			}
			else{
				total_commission+=0;
			}
		}
		session.getTransaction().commit();
		session.close();
		return total_commission;
	}
	
	public double calculateCashDividend(String portName, String start_date,String end_date){
		
		Session session=sessionFactory.openSession();
		session.beginTransaction();
		
		double total_cashDividend=0;
		String SQL_QUERY="select u from PortFolio u where u.portfoli_name='" + portName + "' and u.source_date>='"
				+ start_date + "'and u.source_date<='"+end_date+"' and u.delete_flag<> 1 and u.CashDividendFlag is not null";
		
		Query query=session.createQuery(SQL_QUERY);
		ArrayList<PortFolio> rslt= (ArrayList<PortFolio>) query.getResultList();
		for(int i=0;i<rslt.size();i++){
			if(rslt.get(i).getNumber_of_share()==null){
				total_cashDividend+=0;
			}
			else{
				total_cashDividend+=rslt.get(i).getNumber_of_share();
			}
		}
		session.getTransaction().commit();
		session.close();
		return total_cashDividend;
	}

}
