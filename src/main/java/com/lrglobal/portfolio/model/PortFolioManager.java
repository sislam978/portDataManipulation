package com.lrglobal.portfolio.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;

import com.lrglobal.portfolio.datageneration.ReadPortFolioDatafromCSV;





public class PortFolioManager {
	

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
        // code to save a Data
    	// Before that I have to Read an excel file and take the inputs from the csvfile.
    	Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	
    	ReadPortFolioDatafromCSV readPortFolioDatafromCSV=new ReadPortFolioDatafromCSV();
    	ArrayList<PortFolio> rslt=readPortFolioDatafromCSV.getAllData(fileName);
    	Collections.sort(rslt,PortFolio.sortingdata);
    	
    	for(int i=0;i<rslt.size();i++){
    		PortFolio pp=rslt.get(i);
    		
    		String SQL_QUERY="select u from PortFolio u where u.portfoli_name='" + rslt.get(i).getPortfoli_name() + 
    				"' and u.ticker='"+rslt.get(i).getTicker()+"' and u.sign='"+rslt.get(i).getSign()+
    				"' and u.source_date='"+rslt.get(i).getSource_date()+"'";

    		Query query=session.createQuery(SQL_QUERY);
    		List<PortFolio> list =(List<PortFolio>) query.getResultList();
    		/*
    		 * conditional check if the query return any data in the list that means there is already exist the intended entry data 
    		 * thats why inside the condition we just skip the insertion and also the amount of data which are duplicate by extracting the
    		 * size of the data */
    		if(list.size()>0){
    			i=i+list.size()-1;
    			continue;
    		}
    		session.save(pp);
    		if(i%250==0){
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
