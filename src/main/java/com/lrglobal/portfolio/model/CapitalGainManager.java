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

public class CapitalGainManager {
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
    
    public void bulkInsertCapitalgain(String portName,String start_date,String end_date) throws ParseException{
    	
    	SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
		Date dateStart = input_format.parse(start_date);
		Date EndDate = input_format.parse(end_date);

		Calendar start = Calendar.getInstance();
		start.setTime(dateStart);
		
		Calendar end = Calendar.getInstance();
		end.setTime(EndDate);
		end.add(Calendar.DATE, 1);
		/*
		 * prev date for considering the summary records of previous date
		 */
		for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			// Do your job here with `date`.
			String d_date = input_format.format(date);
			insertCapitalgain(portName, d_date);
			
		}
    }
    
    public void insertCapitalgain(String portName,String d_date) throws ParseException{
    	Session session=sessionFactory.openSession();
    	session.beginTransaction();
    	
    	String SQL_QUERY="select u from PortFolio u where u.portfoli_name='" + portName + "' and u.source_date='"
				+ d_date +"' and u.sign='" + "SELL" + "' and u.ticker<>'"+"CASH"+"' and u.delete_flag<> 1";
    	Query port_query = session.createQuery(SQL_QUERY);
    	ArrayList<PortFolio> rsltPortFolio=(ArrayList<PortFolio>)port_query.getResultList();
    	
    	for(int i=0;i<rsltPortFolio.size();i++){
    		
    		double capital_value=CalculateCapitalgain(rsltPortFolio.get(i).getPortfoli_name(), 
    				rsltPortFolio.get(i).getTicker(), d_date);
    		
    		CapitalGain capgain=new CapitalGain();
    		capgain.setPortName(portName);
    		capgain.setTicker(rsltPortFolio.get(i).getTicker());
    		capgain.setCapital_gain(capital_value);
    		capgain.setSource_date(d_date);
    		session.save(capgain);
    	}
    	
    	session.getTransaction().commit();
    	session.close();
    }
    public double CalculateCapitalgain(String portName,String ticker, String d_date) throws ParseException{
    	
    	Session session=sessionFactory.openSession();
    	session.beginTransaction();
    	/*
    	 * select the data from portfolio table 
    	 */
    	String SQL_QUERY="select u from PortFolio u where u.portfoli_name='" + portName + "' and u.source_date='"
				+ d_date + "'and u.ticker='"+ticker+"' and u.sign='" + "SELL" + "' and u.delete_flag<> 1";
    	Query port_query = session.createQuery(SQL_QUERY);
    	ArrayList<PortFolio> rsltPortFolio=(ArrayList<PortFolio>)port_query.getResultList();
    	
    	/*
    	 * select the data from portfolio summary table.
    	 */
    	SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
    	Date datetoChange = input_format.parse(d_date);
    	Calendar prev = Calendar.getInstance();
		prev.setTime(datetoChange);
		prev.add(Calendar.DATE, -1); // number of days to add
		String prev_date = input_format.format(prev.getTime());
    	
    	String SQL_QUERY1= "select u from PortSummaryTable u where u.port_name='" + portName + "' and u.source_date='"
				+ prev_date + "' and u.ticker='"+ticker+"'and u.delete_flag<>1";
    	Query summary_Query=session.createQuery(SQL_QUERY1);    	
    	ArrayList<PortSummaryTable>rsltSummary=(ArrayList<PortSummaryTable>)summary_Query.getResultList();
    	
    	double net_capital_gain=0;
    	
    	if(!rsltPortFolio.isEmpty() && !rsltSummary.isEmpty()){
    		
    		PortFolioManager pfm=new PortFolioManager();
    		pfm.setup();
    		double netSell=pfm.calculateShare(rsltPortFolio);
    		netSell=0-netSell;
    		pfm.exit();
    		
    		net_capital_gain=netSell-rsltPortFolio.get(0).getNumber_of_share()*rsltSummary.get(0).getCost_price();
    		
    	}
    	
    	session.getTransaction().commit();
    	session.close();
    	
    	return net_capital_gain;
    }
    
    public double calculateNetCapitalGain(String portName, String d_date){
    	Session session=sessionFactory.openSession();
    	session.beginTransaction();
    	String SQL_QUERY="select u from CapitalGain u where u.portName='" + portName + "' and u.source_date='"
				+ d_date + "'";
    	Query query=session.createQuery(SQL_QUERY);
    	ArrayList<CapitalGain> rslt=(ArrayList<CapitalGain>)query.getResultList();
    	
    	double net_gain=0;
    	for(int i=0;i<rslt.size();i++){
    		net_gain+=rslt.get(i).getCapital_gain();
    	}
    	
    	session.getTransaction().commit();
    	session.close();
    	return net_gain;
    }

}
