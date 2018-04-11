package com.lrglobal.portfolio.model;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;

import com.lrglobal.portfolio.datageneration.ReadPortFolioDatafromCSV;
import com.lrglobal.portfolio.datageneration.ReadTickerCSV;

public class TickerTableManager {
	

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

    
    public void Insert(String fileName) {
    	Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	
    	ReadTickerCSV readTickerCSV=new ReadTickerCSV();
    	ArrayList<TickerTable> rslt= readTickerCSV.getAllData(fileName);
    	
    	for(int i=0;i<rslt.size();i++){
    		session.save(rslt.get(i));
    		if(i%20==0){
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
    
    public void addNetSell(){
    	Session session =sessionFactory.openSession();
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
