package com.lrglobal.portfolio.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="testtable")
public class TestTable {
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy= GenerationType.AUTO)
	private int id;
	
	@Column(name="name")
	private String name;
	@Column(name="email")
	private String mail;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	@Override
	public String toString() {
		return "TestTable [id=" + id + ", name=" + name + ", mail=" + mail + ", getId()=" + getId() + ", getName()="
				+ getName() + ", getMail()=" + getMail() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}
	
	

}
