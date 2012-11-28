package yelpScraper;

/** Yelp follows this schema for postal addresses: http://schema.org/PostalAddress */

public class Address {
	
	public String number;
	public String street;
	public String zip;
	public String city;
	public String region; // State in the US. Did not find it elsewhere, tried a few restaurants in Italy.
	/**
	 * @param number
	 * @param street
	 * @param zip
	 * @param city
	 * @param country
	 */
	public Address(String number, String street, String zip, String city,
			String region) {
		this.number = number;
		this.street = street;
		this.zip = zip;
		this.city = city;
		this.region = region;
	}
	@Override
	public String toString() {
		return "Address [" + number + " " + street + "\n"
				+ zip + " " + city + ", " + region + "]";
	}
	
	
	
}
