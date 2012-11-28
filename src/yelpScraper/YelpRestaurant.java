package yelpScraper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.princeton.cs.introcs.Out;
import edu.princeton.cs.introcs.StdOut;

public class YelpRestaurant {
	private String name;
	private String category;
	private Address address;
	private String phoneNumber;
	private String website;
	private Float rating; // should probably be an enum, but meh
	private ArrayList<YelpReview> reviews;
	//private RatingDistribution ratingDistribution;
	private Element restaurantInfoContainer; // used by the mineX() methods.
	private String url;
	
	public YelpRestaurant(String _url) {

		url = stripTags(_url);
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("URL constructor failed to connect");
			e.printStackTrace();
		}

		restaurantInfoContainer = doc.select("div#bizBox").first();
		
		name = mineName();
		category = mineCategory();
		address = mineAddress();
		phoneNumber = minePhoneNumber();
		website = mineWebsite();
		rating = mineScore();
		reviews = mineReviews(doc.select("div#bizReviews").first());
	}
	
	private String stripTags(String _url) {
		Integer index;
		if ( ( index = _url.indexOf("#query") ) == -1) return _url;
		else return _url.substring(0, index);
	}

	private String mineName() {
		Element nameDiv = restaurantInfoContainer.select("div#bizInfoHeader > h1").first();
		return nameDiv.text();
	}
	
	private String mineCategory() {
		Element categoryContainer = restaurantInfoContainer.select("p#bizCategories > span#cat_display > a").first();
		return categoryContainer.text();
	}
	
	private Address mineAddress() {
		Element addressContainer = restaurantInfoContainer.select("address").first();
		String numAndStreet = addressContainer.select("span[itemprop=streetAddress]").text();
		String[] split = splitNumAndStreet(numAndStreet);
		
		String city = addressContainer.select("span[itemprop=addressLocality]").text();
		String region = addressContainer.select("span[itemprop=addressRegion]").text();
		String zipcode = addressContainer.select("span[itemprop=postalCode]").text();
		
		Address result = new Address(split[0], split[1], zipcode, city, region);
		return result;
	}
	
	private String minePhoneNumber() {
		Elements phoneContainer = restaurantInfoContainer.select("span#bizPhone");
		if (!phoneContainer.isEmpty()) {
			String phoneText = phoneContainer.first().text();
			return phoneText;
		}
		else return "";
	}
	
	private String mineWebsite() {
		Elements urlContainer = restaurantInfoContainer.select("div#bizUrl");
		if (!urlContainer.isEmpty())
			return urlContainer.first().text();
		else return "";
	}
	
	private Float mineScore() {
		Element scoreContainer = restaurantInfoContainer.select("div#bizRating > div > div.rating > i > img").first();
		String scoreText = scoreContainer.attr("alt");
		scoreText = scoreText.substring(0, scoreText.indexOf(" star rating"));
		Float result = Float.valueOf(scoreText);
		return result;
	}
	
	/** Number is in return value[0], street name is in [1] */
	private static String[] splitNumAndStreet(String address) {
		String[] result = new String[2];
		Pattern numberFirst = Pattern.compile("(\\d+)\\s+(\\D+(\\s \\D+)?)");
		Pattern streetFirst = Pattern.compile("(\\D+(\\s \\D+)?)\\s+(\\d+)");
		
		Matcher numFirstMatcher = numberFirst.matcher(address);
		Matcher streetFirstMatcher = streetFirst.matcher(address);
		
		if (numFirstMatcher.matches() && !streetFirstMatcher.matches() ) { // Num first
			result[0] = numFirstMatcher.group(1);
			result[1] = numFirstMatcher.group(2);
		}
		else if (!numFirstMatcher.matches() && streetFirstMatcher.matches()) { // Address first
			result[0] = numFirstMatcher.group(3); // the group (\\d+) is opened third
			result[1] = numFirstMatcher.group(1);
		}
		
		else { // Both match or none match, i.e. unknown format.
			result[0] = "-1"; // Something wrong, we give up
			result[1] = address;
		}
					
		return result;
	}
	
	
	private ArrayList<YelpReview> mineReviews(Element container) {
		ArrayList<YelpReview> result = new ArrayList<YelpReview>();
		
		Integer totalNumberOfReviews = mineTotalNumberOfReviews(container);
		
		for (int i = 0; 40 * i < totalNumberOfReviews; i++) { // 40 reviews per page. 
			// We start with 1-40, so if we have 40 reviews we stop, if we have 41 we continue.
			Element pageContainer = container; // changes with page
			if (i > 0) { // to get comments in page i > 0, we need another connection.
				Document doc = null;
				try {
					doc = Jsoup.connect(url + "?start="+Integer.toString(40 * i ) ).get();
				} catch (IOException e) {
					System.out.println("Failed to connect to load comments in page " + i);
					e.printStackTrace();
				}
				pageContainer = doc.select("div#bizReviews").first();
			}
			Elements reviewcontainers = pageContainer.select("div#reviews-other > ul > li.review");
			for (Element reviewcontainer : reviewcontainers) {
				result.add(new YelpReview(reviewcontainer));
			}
		}
		
		return result;
	}
	
	private Integer mineTotalNumberOfReviews(Element container) {
		String containerStr = container.select("h2#total_reviews").first().ownText(); 
		//format:  "<space> TOTAL <space> reviews for <RESTAURANT>"
		String trimmedStr = 
				containerStr.substring(
						0, 
						containerStr.indexOf(" reviews for ")
				);
		return Integer.valueOf(trimmedStr);
	}

	@Override
	public String toString() {
		return "Restaurant [\nname=" + name + ", category=" + category
				+ ", address=" + address + ", phoneNumber=" + phoneNumber
				+ ", website=" + website + ", score=" + rating + ".\nReviews:\n"
				+ reviews + "\n]\n";
	}

	public static void main(String args[]) {
		ArrayList<YelpRestaurant> restaurants = new ArrayList<YelpRestaurant>();
		
		Out Out = new Out("/Users/davide/Desktop/consolelog.txt");
		
		for (String arg: args) {
			YelpRestaurant q = new YelpRestaurant(arg);
			Out.println(q);
			restaurants.add(q);
		}
		Out.println("Total reviews mined: " + restaurants.get(0).reviews.size());
	}
	
}
