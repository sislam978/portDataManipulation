package com.lrglobal.portfolio.model;

import java.util.ArrayList;
import java.util.Collections;
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

import com.lrglobal.portfolio.datageneration.ReadPortFolioDatafromCSV;

public class PortSummaryTableManager {
	
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
    
    public void Insert() {
        // code to save a Data
    	// Before that I have to Read an excel file and take the inputs from the csvfile.
    	Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	Scanner inScanner=new Scanner(System.in);
    	Query query= session.getNamedQuery("getAllPortName");
    	ArrayList<PortFolio> portname=(ArrayList<PortFolio>)query.getResultList();
    	
    	System.out.println("Take startdate with yyyy-MM-dd format:");
		String startdate=inScanner.nextLine();
		System.out.println("Take enddate with yyyy-MM-dd format:");
		String enddate=inScanner.nextLine();
		
    	for(int i=0;i<portname.size();i++){
    	
    		Query queryPort= session.getNamedQuery("getAllFromProvidedRange")
    				.setParameter("q_portName", portname.get(i).getPortfoli_name())
    				.setParameter("q_startdate", startdate)
    				.setParameter("q_enddate",enddate);
    		ArrayList<PortFolio> rslt= (ArrayList<PortFolio>) queryPort.getResultList();
    		Map<String,Double> tickermap= new HashMap<String,Double>(); 
    		for(int j=0;j<rslt.size();j++){
    			if(tickermap.containsKey(rslt.get(j).getTicker())){
    				if(rslt.get(j).getSign().equals("BUY")){
    					Double vv=tickermap.get(rslt.get(j).getTicker());
    					vv+=rslt.get(j).getNumber_of_share();
    					tickermap.put(rslt.get(j).getTicker(), vv);
    				}
    				else{
    					Double vv=tickermap.get(rslt.get(j).getTicker());
    					vv-=rslt.get(j).getNumber_of_share();
    					tickermap.put(rslt.get(j).getTicker(), vv);
    				}
    			}
    			else{
    				if(rslt.get(j).getSign().equals("BUY")){
    					tickermap.put(rslt.get(i).getTicker(), rslt.get(i).getNumber_of_share());
    				}
    				else{
    					double v1= 0-rslt.get(i).getNumber_of_share();
    					tickermap.put(rslt.get(i).getTicker(), v1);
    				}
    			}
    		}
    		System.out.println("size array : "+rslt.size());
    		
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
