package com.lrglobal.portfolio.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="price_table")
public class PriceTable {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="price_id")
	private long priceTableId;
	
	@Column(name="ticker_name")
	private String ticker;
	
	@Column(name="price_value")
	private Double price;
	
	@Column(name="price_date")
	private String price_date;
	
	@Column(name="created_by")
	private Integer created_by;
	
	@Column(name="created_on")
	private String created_on;
	
	@Column(name="deleted_by")
	private Integer deleted_by;
	
	@Column(name="deleted_on")
	private String deleted_on;
	
	@Column(name="price_change")
	private Double price_change; 
	
	
	public long getPriceTableId() {
		return priceTableId;
	}

	public void setPriceTableId(long priceTableId) {
		this.priceTableId = priceTableId;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getPrice_date() {
		return price_date;
	}

	public void setPrice_date(String price_date) {
		this.price_date = price_date;
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

	
	public Double getPrice_change() {
		return price_change;
	}

	public void setPrice_change(Double price_change) {
		this.price_change = price_change;
	}

	@Override
	public String toString() {
		return "PriceTable [ticker=" + ticker + ", price=" + price + ", price_date=" + price_date + ", price_change="
				+ price_change + "]";
	}
	
	
}
