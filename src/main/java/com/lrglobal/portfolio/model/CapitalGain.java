package com.lrglobal.portfolio.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="capital_gain")
public class CapitalGain {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="capitalgainId")
	private long cpId;
	
	@Column(name="port_name")
	private String portName;
	
	@Column(name="ticker")
	private String ticker;
	
	@Column(name="capital_gain")
	private Double capital_gain;

	@Column(name="source_date")
	private String source_date;

	
	public long getCpId() {
		return cpId;
	}

	public void setCpId(long cpId) {
		this.cpId = cpId;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public Double getCapital_gain() {
		return capital_gain;
	}

	public void setCapital_gain(Double capital_gain) {
		this.capital_gain = capital_gain;
	}

	public String getSource_date() {
		return source_date;
	}

	public void setSource_date(String source_date) {
		this.source_date = source_date;
	}

	@Override
	public String toString() {
		return "CapitalGain [portName=" + portName + ", ticker=" + ticker + ", capital_gain=" + capital_gain
				+ ", source_date=" + source_date + "]";
	}
	
	

}
