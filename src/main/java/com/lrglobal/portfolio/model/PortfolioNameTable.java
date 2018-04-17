package com.lrglobal.portfolio.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="portfolio_name_table")
public class PortfolioNameTable {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="port_nameId")
	private long portNameId;
	
	@Column(name="port_name")
	private String portname;
	
	@Column(name="user_name")
	private String UserName;

	public long getPortNameId() {
		return portNameId;
	}

	public void setPortNameId(long portNameId) {
		this.portNameId = portNameId;
	}

	public String getPortname() {
		return portname;
	}

	public void setPortname(String portname) {
		this.portname = portname;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	@Override
	public String toString() {
		return "PortfolioNameTable [portNameId=" + portNameId + ", portname=" + portname + ", UserName=" + UserName
				+ ", getPortNameId()=" + getPortNameId() + ", getPortname()=" + getPortname() + ", getUserName()="
				+ getUserName() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}
	
	

}
