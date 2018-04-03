package com.lrglobal.portfolio.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;





@NamedNativeQueries({

	@NamedNativeQuery(
			name="getAllPortName",
			query ="CALL getAllPortName()",
			hints=	@javax.persistence.QueryHint(name = "org.hibernate.callable", value = "true"),
			resultClass=PortFolio.class
			),
	@NamedNativeQuery(
			name="getAllFromProvidedRange",
			query ="CALL getAllFromProvidedRange(:q_portName,:q_startdate,:q_enddate)",
			hints=	@javax.persistence.QueryHint(name = "org.hibernate.callable", value = "true"),
			resultClass=PortFolio.class
			),
})
@Entity
@Table(name="portfolio_summary_table")
public class PortSummaryTable {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="port_summary_id")
	private long port_id;
	
	@Column(name="portfolio_name")
	private String port_name;
	
	@Column(name="ticker")
	private String ticker;
	
	@Column(name="num_of_share")
	private Double share_quantity;
	
	@Column(name="cost_price")
	private Double cost_price;
	
	@Column(name="source_date")
	private String source_date;
	
	@Column(name="portfolio_value")
	private Double portfoli_value;
	
	@Column(name="weight_in_portfolio")
	private Double weightInPortfolio;
	
	@Column(name="current_price")
	private Double current_price;
	
	@Column(name="created_by")
	private Integer created_by;
	
	@Column(name="created_on")
	private String created_on;
	
	@Column(name="deleted_by")
	private Integer deleted_by;
	
	@Column(name="deleted_on")
	private String deleted_on;

	public long getPort_id() {
		return port_id;
	}

	public void setPort_id(long port_id) {
		this.port_id = port_id;
	}

	public String getPort_name() {
		return port_name;
	}

	public void setPort_name(String port_name) {
		this.port_name = port_name;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public Double getShare_quantity() {
		return share_quantity;
	}

	public void setShare_quantity(Double share_quantity) {
		this.share_quantity = share_quantity;
	}

	public Double getCost_price() {
		return cost_price;
	}

	public void setCost_price(Double cost_price) {
		this.cost_price = cost_price;
	}

	public String getSource_date() {
		return source_date;
	}

	public void setSource_date(String source_date) {
		this.source_date = source_date;
	}

	public Double getPortfoli_value() {
		return portfoli_value;
	}

	public void setPortfoli_value(Double portfoli_value) {
		this.portfoli_value = portfoli_value;
	}

	public Double getWeightInPortfolio() {
		return weightInPortfolio;
	}

	public void setWeightInPortfolio(Double weightInPortfolio) {
		this.weightInPortfolio = weightInPortfolio;
	}

	public Double getCurrent_price() {
		return current_price;
	}

	public void setCurrent_price(Double current_price) {
		this.current_price = current_price;
	}

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

	@Override
	public String toString() {
		return "PortSummaryTable [port_id=" + port_id + ", port_name=" + port_name + ", ticker=" + ticker
				+ ", share_quantity=" + share_quantity + ", cost_price=" + cost_price + ", source_date=" + source_date
				+ ", portfoli_value=" + portfoli_value + ", weightInPortfolio=" + weightInPortfolio + ", current_price="
				+ current_price + ", created_by=" + created_by + ", created_on=" + created_on + ", deleted_by="
				+ deleted_by + ", deleted_on=" + deleted_on + ", getPort_id()=" + getPort_id() + ", getPort_name()="
				+ getPort_name() + ", getTicker()=" + getTicker() + ", getShare_quantity()=" + getShare_quantity()
				+ ", getCost_price()=" + getCost_price() + ", getSource_date()=" + getSource_date()
				+ ", getPortfoli_value()=" + getPortfoli_value() + ", getWeightInPortfolio()=" + getWeightInPortfolio()
				+ ", getCurrent_price()=" + getCurrent_price() + ", getCreated_by()=" + getCreated_by()
				+ ", getCreated_on()=" + getCreated_on() + ", getDeleted_by()=" + getDeleted_by() + ", getDeleted_on()="
				+ getDeleted_on() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}
	
	
}
