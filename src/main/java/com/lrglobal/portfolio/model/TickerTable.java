package com.lrglobal.portfolio.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="ticker_table")
public class TickerTable {
	
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ticker_id")
	private long tickerId;
	
	@Column(name="ticker_name")
	private String tickerName;
	
	@Column(name="sector")
	private String sector;

	public long getTickerId() {
		return tickerId;
	}

	public void setTickerId(long tickerId) {
		this.tickerId = tickerId;
	}

	public String getTickerName() {
		return tickerName;
	}

	public void setTickerName(String tickerName) {
		this.tickerName = tickerName;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	@Override
	public String toString() {
		return "TickerTable [tickerId=" + tickerId + ", tickerName=" + tickerName + ", sector=" + sector
				+ ", getTickerId()=" + getTickerId() + ", getTickerName()=" + getTickerName() + ", getSector()="
				+ getSector() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}
	
	

}
