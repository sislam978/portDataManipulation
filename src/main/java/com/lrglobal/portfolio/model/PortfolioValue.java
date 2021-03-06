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
			name="PortsummaryDataforCertainDate",
			query ="CALL PortsummaryDataforCertainDate(:q_portName,:q_date)",
			hints=	@javax.persistence.QueryHint(name = "org.hibernate.callable", value = "true"),
			resultClass=PortSummaryTable.class
			),
	@NamedNativeQuery(
			name="priceChangeOnCertainDate",
			query ="CALL priceChangeOnCertainDate(:q_tickerName,:q_date)",
			hints=	@javax.persistence.QueryHint(name = "org.hibernate.callable", value = "true"),
			resultClass=PriceTable.class
			),
	@NamedNativeQuery(
			name="getALLPortValueDataForChart",
			query ="CALL getALLPortValueDataForChart(:q_portName,:q_sdate,:q_edate)",
			hints=	@javax.persistence.QueryHint(name = "org.hibernate.callable", value = "true"),
			resultClass=PortfolioValue.class
			)
})

@Entity
@Table(name="portfolio_value")
public class PortfolioValue {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="portvalue_id")
	private long portvalueId;
	
	@Column(name="portfolio_name")
	private String portName;
	
	@Column(name="portfolio_value")
	private Double portfolio_value;
	
	@Column(name="change_price")
	private Double change_from_last_day;
	
	@Column(name="cummulative_value")
	private Double cummulativeValue;
	
	@Column(name="change_in_index")
	private Double changePortIndex;
	
	@Column(name="source_date")
	private String source_date;
	
	@Column(name="created_by")
	private Integer created_by;
	@Column(name="created_on")
	private String created_on;
	
	@Column(name="deleted_by")
	private Integer deleted_by;
	
	@Column(name="deleted_on")
	private String deleted_on;
	
	@Column(name="delete_flag")
	private Integer delete_flag;
	
	@Column(name="portfolio_cost")
	private Double portfolio_cost;
	
	@Column(name="net_capital_gain")
	private Double netCapital_gain;

	public long getPortvalueId() {
		return portvalueId;
	}

	public void setPortvalueId(long portvalueId) {
		this.portvalueId = portvalueId;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public Double getPortfolio_value() {
		return portfolio_value;
	}

	public void setPortfolio_value(Double portfolio_value) {
		this.portfolio_value = portfolio_value;
	}

	public Double getChange_from_last_day() {
		return change_from_last_day;
	}

	public void setChange_from_last_day(Double change_from_last_day) {
		this.change_from_last_day = change_from_last_day;
	}

	public Double getCummulativeValue() {
		return cummulativeValue;
	}

	public void setCummulativeValue(Double cummulativeValue) {
		this.cummulativeValue = cummulativeValue;
	}

	public Double getChangePortIndex() {
		return changePortIndex;
	}

	public void setChangePortIndex(Double changePortIndex) {
		this.changePortIndex = changePortIndex;
	}

	public String getSource_date() {
		return source_date;
	}

	public void setSource_date(String source_date) {
		this.source_date = source_date;
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

	public Integer getDelete_flag() {
		return delete_flag;
	}

	public void setDelete_flag(Integer delete_flag) {
		this.delete_flag = delete_flag;
	}
	

	public Double getPortfolio_cost() {
		return portfolio_cost;
	}

	public void setPortfolio_cost(Double portfolio_cost) {
		this.portfolio_cost = portfolio_cost;
	}

	public Double getNetCapital_gain() {
		return netCapital_gain;
	}

	public void setNetCapital_gain(Double netCapital_gain) {
		this.netCapital_gain = netCapital_gain;
	}

	@Override
	public String toString() {
		return "PortfolioValue [portName=" + portName + ", portfolio_value=" + portfolio_value
				+ ", change_from_last_day=" + change_from_last_day + ", cummulativeValue=" + cummulativeValue
				+ ", changePortIndex=" + changePortIndex + ", source_date=" + source_date + ", portfolio_cost="
				+ portfolio_cost + ", netCapital_gain=" + netCapital_gain + "]";
	}
	
	
}
