package com.example.ark;

public class Contact {
	private String number;
	private String name;
	
	Contact(String num, String nam) {
		this.number = num;
		this.name = nam;
	}
	
	public String getNum() {
		return number;
	}
	
	public String getName() {
		return name;
	}
}
