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

	@Column(name="created_on")
	private String created_on;
	
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
	

	public String getCreated_on() {
		return created_on;
	}

	public void setCreated_on(String created_on) {
		this.created_on = created_on;
	}

	@Override
	public String toString() {
		return "PortfolioNameTable [portNameId=" + portNameId + ", portname=" + portname + ", UserName=" + UserName
				+ ", created_on=" + created_on + ", getPortNameId()=" + getPortNameId() + ", getPortname()="
				+ getPortname() + ", getUserName()=" + getUserName() + ", getCreated_on()=" + getCreated_on()
				+ ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()=" + super.toString()
				+ "]";
	}
	
	

}
