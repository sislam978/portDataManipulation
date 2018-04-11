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

	public void Insert() throws ParseException {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Scanner inScanner = new Scanner(System.in);
		System.out.println("Enter the desired Port Name");
		String portName = inScanner.nextLine();
		System.out.println("Enter the desired date");
		String source_date = inScanner.nextLine();

		SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
		Date dateStart = input_format.parse(source_date);
		System.out.println("Up to the date we wan to calculate:");
		String E_date=inScanner.nextLine();
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
			for (int i = 0; i < rslt.size(); i++) {
				portValue += rslt.get(i).getPortfoli_value();
			}
			PortfolioValue portfolioValue = new PortfolioValue();
			portfolioValue.setPortName(rslt.get(0).getPort_name());
			portfolioValue.setSource_date(rslt.get(0).getSource_date());
			portfolioValue.setPortfolio_value(portValue);

			session.save(portfolioValue);
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

}
