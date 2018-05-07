package com.lrglobal.portfolio.model;

public class PortfolioProfitnLoss {
	
	public String portName;
	
	public double total_commission;
	
	public double totalCashDividend;
	
	public double total_gain;
	
	public String start_date;
	
	public String end_date;
	
	public double current_profit;

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public double getTotal_commission() {
		return total_commission;
	}

	public void setTotal_commission(double total_commission) {
		this.total_commission = total_commission;
	}

	public double getTotalCashDividend() {
		return totalCashDividend;
	}

	public void setTotalCashDividend(double totalCashDividend) {
		this.totalCashDividend = totalCashDividend;
	}

	public double getTotal_gain() {
		return total_gain;
	}

	public void setTotal_gain(double total_gain) {
		this.total_gain = total_gain;
	}

	public String getStart_date() {
		return start_date;
	}

	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}

	public String getEnd_date() {
		return end_date;
	}

	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}

	
	public double getCurrent_profit() {
		return current_profit;
	}

	public void setCurrent_profit(double current_profit) {
		this.current_profit = current_profit;
	}

	@Override
	public String toString() {
		return "PortfolioProfitnLoss [portName=" + portName + ", total_commission=" + total_commission
				+ ", totalCashDividend=" + totalCashDividend + ", total_gain=" + total_gain + ", start_date="
				+ start_date + ", end_date=" + end_date + ", current_profit=" + current_profit + "]";
	}
	
	
	

}
