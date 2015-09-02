package com.cyanspring.common.staticdata;

public enum RefDataCommodity {

	STOCK("S"), INDEX("I"), FUTURES("F");
	
	private String value;
	
	private RefDataCommodity(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
}
