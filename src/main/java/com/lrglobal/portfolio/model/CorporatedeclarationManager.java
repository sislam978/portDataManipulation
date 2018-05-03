package com.lrglobal.portfolio.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;


public class CorporatedeclarationManager {
	
	public SessionFactory sessionFactory;
	// connection initialization. dont bother much. this connection process will need in every database transaction
	// no need to change it either
	public void setup(){
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
		        .configure("hibernate.cfg.xml") // configures settings from hibernate.cfg.xml
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
    
    public String insertCDT(CorporateDeclaration cdt){
    	Session session= sessionFactory.openSession();
    	session.beginTransaction();
    	
    	String SQL_QUERY="select u from CorporateDeclaration u where u.tickerName='" + cdt.getTickerName() + 
				"' and u.cashDividend='"+cdt.getCashDividend()+"' and u.stockSplit='"+cdt.getStockSplit()+
				"' and u.rightShareCost='"+cdt.getRightShareCost()+"'and u.record_date='"+cdt.getRecord_date()+"'";
    	
    	Query query = session.createQuery(SQL_QUERY);
    	
    	ArrayList<CorporateDeclaration> rslt= (ArrayList<CorporateDeclaration>) query.getResultList();
    	if(!rslt.isEmpty()){
    		return "Insert valid data";
    	}
    	else{
    		session.save(cdt);
    	}
    	
    	session.getTransaction().commit();
    	session.close();
    	return "Successfull";
    }
    
    public void cashDividendAdjustment(String ticker,String d_date) throws ParseException{
    	Session session= sessionFactory.openSession();
    	session.beginTransaction();
    	
    	ArrayList<CorporateDeclaration> rsltCD=desiredRecords(ticker,d_date);
     		
		PriceTableManager ptm=new PriceTableManager();
		ptm.setup();
		ArrayList<PriceTable> rsltpt=ptm.getRecordsPT(ticker,d_date);
		ptm.exit();
    	
		if(rsltpt.isEmpty() || rsltCD.isEmpty()){
			return;
		}
		PriceTableManager ptm1=new PriceTableManager();
		ptm1.setup();
		ptm1.cashdividendSetInPriceChange(rsltCD,rsltpt);
		ptm1.exit();
		
    	session.getTransaction().commit();
    	session.close();
    }


	public  ArrayList<CorporateDeclaration> desiredRecords(String ticker,String d_date) throws ParseException {
		// TODO Auto-generated method stub
		Session session =sessionFactory.openSession();
		session.beginTransaction();
		
		String SQL_QUERY="select u from CorporateDeclaration u where u.record_date='" + d_date + "' and u.tickerName='"+ticker+"'";
		
		Query query=session.createQuery(SQL_QUERY);
		ArrayList<CorporateDeclaration> rslt=(ArrayList<CorporateDeclaration>)query.getResultList();
		
		session.getTransaction().commit();
		session.close();
		return rslt;
	}

}
