package com.lrglobal.portfolio.model;

import java.util.ArrayList;

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

}
