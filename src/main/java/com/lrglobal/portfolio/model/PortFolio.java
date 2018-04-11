package com.lrglobal.portfolio.model;

import java.util.Comparator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;


@Entity
@Table(name="portfolio_table")
public class PortFolio {
	
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="portfolio_id")
	private long portfolio_id;
	
	@Column(name="portfolio_name")
	private String portfoli_name;
	
	
	@Column(name="ticker")
	private String ticker;
	
	@Column(name="NumberOfShare")
	private Double number_of_share;
	
	@Column(name="CostPrice")
	private Double cost_price;
	
	@Column(name="CurrentPrice")
	private Double current_price;
	
	@Column(name="PreviousPrice")
	private Double prev_price;
	
	
	@Column(name="Sign")
	private String sign;
	
	
	@Column(name="Commission")
	private Double commission;	
	
	@Column(name="source_date")
	private String source_date;
	
	@Column(name="portfolioValue")
	private Double portfolio_value;
	
	@Column(name="price_change")
	private Double price_change;
	
//	@Column(name="WeightInPortfolio")
//	private Double weight_in_portfolio;
	
	@Column(name="created_by")
	private Integer created_by;
	
	@Column(name="created_on")
	private String created_on;
	
	@Column(name="deleted_by")
	private Integer deleted_by;
	
	@Column(name="deleted_on")
	private String deleted_on;
	
//	@Column(name="Sector")
//	private String sector;

	public long getPortfolio_id() {
		return portfolio_id;
	}

	public void setPortfolio_id(long portfolio_id) {
		this.portfolio_id = portfolio_id;
	}

	public String getPortfoli_name() {
		return portfoli_name;
	}

	public void setPortfoli_name(String portfoli_name) {
		this.portfoli_name = portfoli_name;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public Double getNumber_of_share() {
		return number_of_share;
	}

	public void setNumber_of_share(Double number_of_share) {
		this.number_of_share = number_of_share;
	}

	public Double getCost_price() {
		return cost_price;
	}

	public void setCost_price(Double cost_price) {
		this.cost_price = cost_price;
	}

	public Double getCurrent_price() {
		return current_price;
	}

	public void setCurrent_price(Double current_price) {
		this.current_price = current_price;
	}

	public Double getPrev_price() {
		return prev_price;
	}

	public void setPrev_price(Double prev_price) {
		this.prev_price = prev_price;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public Double getCommission() {
		return commission;
	}

	public void setCommission(Double commission) {
		this.commission = commission;
	}

	public String getSource_date() {
		return source_date;
	}

	public void setSource_date(String source_date) {
		this.source_date = source_date;
	}

	public Double getPortfolio_value() {
		return portfolio_value;
	}

	public void setPortfolio_value(Double portfolio_value) {
		this.portfolio_value = portfolio_value;
	}

	public Double getprice_change() {
		return price_change;
	}

	public void setprice_change(Double price_change) {
		this.price_change = price_change;
	}

//	public Double getWeight_in_portfolio() {
//		return weight_in_portfolio;
//	}
//
//	public void setWeight_in_portfolio(Double weight_in_portfolio) {
//		this.weight_in_portfolio = weight_in_portfolio;
//	}

	public Integer getCreated_by() {
		return created_by;
	}

	public void setCreated_by(Integer created_by) {
		this.created_by = created_by;
	}

	public String getCreated_on() {
		return created_on;
	}

	public void setCreated_on(String created_on) {
		this.created_on = created_on;
	}

	public Integer getDeleted_by() {
		return deleted_by;
	}

	public void setDeleted_by(Integer deleted_by) {
		this.deleted_by = deleted_by;
	}

	public String getDeleted_on() {
		return deleted_on;
	}

	public void setDeleted_on(String deleted_on) {
		this.deleted_on = deleted_on;
	}
	
	

//	public String getSector() {
//		return sector;
//	}
//
//	public void setSector(String sector) {
//		this.sector = sector;
//	}
	

	public static Comparator<PortFolio> sortingdata = new Comparator<PortFolio>() {

	public int compare(PortFolio s1, PortFolio s2) {
	   String port1 = s1.getPortfoli_name().toUpperCase();
	   String port2 = s2.getPortfoli_name().toUpperCase();

	   return port1.compareTo(port2);


    }};


	@Override
	public String toString() {
		return "PortFolio [portfolio_id=" + portfolio_id + ", portfoli_name=" + portfoli_name + ", ticker=" + ticker
				+ ", number_of_share=" + number_of_share + ", cost_price=" + cost_price + ", current_price="
				+ current_price + ", prev_price=" + prev_price + ", sign=" + sign + ", commission=" + commission
				+ ", source_date=" + source_date + ", portfolio_value=" + portfolio_value + ", price_change="
				+ price_change + ", created_by=" + created_by + ", created_on=" + created_on + ", deleted_by="
				+ deleted_by + ", deleted_on=" + deleted_on + ", getPortfolio_id()=" + getPortfolio_id()
				+ ", getPortfoli_name()=" + getPortfoli_name() + ", getTicker()=" + getTicker()
				+ ", getNumber_of_share()=" + getNumber_of_share() + ", getCost_price()=" + getCost_price()
				+ ", getCurrent_price()=" + getCurrent_price() + ", getPrev_price()=" + getPrev_price() + ", getSign()="
				+ getSign() + ", getCommission()=" + getCommission() + ", getSource_date()=" + getSource_date()
				+ ", getPortfolio_value()=" + getPortfolio_value() + ", getprice_change()=" + getprice_change()
				+ ", getCreated_by()=" + getCreated_by() + ", getCreated_on()=" + getCreated_on() + ", getDeleted_by()="
				+ getDeleted_by() + ", getDeleted_on()=" + getDeleted_on() + ", getClass()=" + getClass()
				+ ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
	}
	
	

}
