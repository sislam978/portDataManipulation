package com.lrglobal.portfolio.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;

import com.lrglobal.portfolio.datageneration.ReadPriceCSV;

public class PriceTableManager {
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

	public void Insert(String fileNameDefined) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		
		ReadPriceCSV csv=new ReadPriceCSV();
		ArrayList<PriceTable> rslt=csv.getAllData(fileNameDefined);
		
		for(int i=0;i<rslt.size();i++){
			System.out.println("number : "+i);
			session.save(rslt.get(i));
			if(i%20==0){
				session.flush();
				session.clear();
			}
		}
				
		session.getTransaction().commit();
		session.close();
	}
	public void updateRowPriceChange(){
		Session session=sessionFactory.openSession();
		session.beginTransaction();
		String SQL_QUERY1= "select u from PriceTable u group by u.ticker";
		Query queryTicker= session.createQuery(SQL_QUERY1);
		ArrayList<PriceTable> ticker_rslt= (ArrayList<PriceTable>) queryTicker.getResultList();
		
		for(int i=0;i<ticker_rslt.size();i++){
			//System.out.println("number iterations: "+i);
			String SQL_QUERY ="select u from PriceTable u where u.ticker='" + ticker_rslt.get(i).getTicker() + "' order by price_date";
			Query query_price=session.createQuery(SQL_QUERY);
			ArrayList<PriceTable> rslt=(ArrayList<PriceTable>) query_price.getResultList();
			if(rslt.size()>0){
				rslt.get(0).setPrice_change(0.0);
				session.saveOrUpdate(rslt.get(0));
				//System.out.println(i +" : "+rslt.get(i).getPrice());
				for(int j=1;j<rslt.size();j++){
					if(rslt.get(j-1).getPrice()>0){
						double price_change= rslt.get(j).getPrice()/rslt.get(j-1).getPrice();
						rslt.get(j).setPrice_change(price_change);
						session.saveOrUpdate(rslt.get(j));
					}
					else {
						rslt.get(j).setPrice_change(1.0);
						session.saveOrUpdate(rslt.get(j));
					}
				}
			}
			session.flush();
			session.clear();
		}
		//
		
		session.getTransaction().commit();
		session.close();
	}

	public ArrayList<PriceTable> getRecordsPT(String ticker,String d_date) {
		// code to get a Data
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		
		String SQL_QUERY="select u from PriceTable u where u.price_date='" + d_date + "' and u.ticker='"+ticker+"'";
		Query query=session.createQuery(SQL_QUERY);
		
		ArrayList<PriceTable> rslt= (ArrayList<PriceTable>) query.getResultList();
		
		session.getTransaction().commit();
		session.close();
		return rslt;
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

//	public ArrayList<PriceTable> getRecordsPTwithRange(String prev_date, String d_date) {
//		// TODO Auto-generated method stub
//		Session session = sessionFactory.openSession();
//		session.beginTransaction();
//		
//		String SQL_QUERY="select u from PriceTable u where u.price_date<='" + d_date + "' and u.price_date>='"+ prev_date +"'";
//		Query query=session.createQuery(SQL_QUERY);
//		
//		ArrayList<PriceTable> rslt= (ArrayList<PriceTable>) query.getResultList();
//		
//		session.getTransaction().commit();
//		session.close();
//		return rslt;
//	}

	public void cashdividendSetInPriceChange(ArrayList<CorporateDeclaration> rsltCD, ArrayList<PriceTable> rsltpt)
			throws ParseException {
		// TODO Auto-generated method stub

		Session session = sessionFactory.openSession();
		session.beginTransaction();

		String cashDividend_ticker = rsltCD.get(0).getTickerName();
		String price_ticker = rsltpt.get(0).getTicker();
		if (cashDividend_ticker.equals(price_ticker)) {
			SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
			Date dateStart = input_format.parse(rsltpt.get(0).getPrice_date());
			Calendar prev = Calendar.getInstance();
			prev.setTime(dateStart);
			prev.add(Calendar.DATE, -1); // number of days to add
			String prev_date = input_format.format(prev.getTime());

			String SQL_QUERY = "select u from PriceTable u where u.price_date='" + prev_date + "' and u.ticker='"
					+ price_ticker + "'";
			Query query = session.createQuery(SQL_QUERY);

			ArrayList<PriceTable> rslt = (ArrayList<PriceTable>) query.getResultList();

			double newPriceChane = (rsltpt.get(0).getPrice() + rsltCD.get(0).getCashDividend())
					/ rslt.get(0).getPrice();

			rsltpt.get(0).setPrice_change(newPriceChane);
			session.update(rsltpt.get(0));
		}

		session.getTransaction().commit();
		session.close();
	}


}
