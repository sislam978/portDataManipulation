package com.lrglobal.portfolio.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="corporate_declaration")
public class CorporateDeclaration {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="dividend_id")
	private long dividendId;
	
	@Column(name="ticker_name")
	private String tickerName;
	
	@Column(name="cash_dividend")
	private Double cashDividend;
	
	@Column(name="stock_split")
	private Double stockSplit;
	
	@Column(name="right_share_cost")
	private Double rightShareCost;
	
	@Column(name="record_date")
	private String record_date;
	
	@Column(name="created_by")
	private Integer created_by;
	
	@Column(name="created_on")
	private String created_on;
	
	@Column(name="deleted_by")
	private Integer deleted_by;
	
	@Column(name="deleted_on")
	private String deleted_on;

	public long getDividendId() {
		return dividendId;
	}

	public void setDividendId(long dividendId) {
		this.dividendId = dividendId;
	}

	public String getTickerName() {
		return tickerName;
	}

	public void setTickerName(String tickerName) {
		this.tickerName = tickerName;
	}

	public Double getCashDividend() {
		return cashDividend;
	}

	public void setCashDividend(Double cashDividend) {
		this.cashDividend = cashDividend;
	}

	public Double getStockSplit() {
		return stockSplit;
	}

	public void setStockSplit(Double stockSplit) {
		this.stockSplit = stockSplit;
	}

	public Double getRightShareCost() {
		return rightShareCost;
	}

	public void setRightShareCost(Double rightShareCost) {
		this.rightShareCost = rightShareCost;
	}

	public String getRecord_date() {
		return record_date;
	}

	public void setRecord_date(String record_date) {
		this.record_date = record_date;
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
		return "CorporateDeclaration [tickerName=" + tickerName + ", cashDividend=" + cashDividend + ", stockSplit="
				+ stockSplit + ", rightShareCost=" + rightShareCost + ", record_date=" + record_date + "]";
	}
	
	
	
}
