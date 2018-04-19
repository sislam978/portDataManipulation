package com.lrglobal.portfolio.model;

import java.util.ArrayList;
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
				for(int j=1;j<rslt.size();j++){
					if(rslt.get(j-1).getPrice()!=0){
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


}
