package leaderos.web.tr.timed.utils;

public class TimeObject {
	
	String name;
	String product;
	long unix;
	
	public TimeObject(String name, String product, Long unix) {
		this.name = name;
		this.product = product;
		this.unix = unix;
	}
	
	public String getName() {
		return this.name;
	}
	public String getProduct() {
		return this.product;
	}
	public long getTime() {
		return this.unix;
	}

}
