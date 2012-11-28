package tripadvisorScraper;

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

public class TripAdvisorRestaurant {
	private String name;
	private String category;
	private Address address;
	private String phoneNumber;
	private String website;
	private Float rating; // should probably be an enum, but meh
	private ArrayList<TripAdvisorReview> reviews;
	private RatingDistribution ratingDistribution;
	private Element restaurantInfoContainer; // used by the mineX() methods.
	private String url; // The URL to mine, not the restaurant's website URL
	
	public TripAdvisorRestaurant(String _url) {

		url = _url;
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("URL constructor failed to connect");
			e.printStackTrace();
		}

		restaurantInfoContainer = doc.select("div#HDPR_V1").first();
		
		name = mineName();
		//category = mineCategory();
		address = mineAddress();
		phoneNumber = minePhoneNumber();
		// TODO: website = mineWebsite();
		rating = mineRating();
		ratingDistribution = mineRatingDistribution();
		reviews = mineReviews(doc.select("div#REVIEWS").first());
	}
	
	private RatingDistribution mineRatingDistribution() {
		Integer[] ratings = new Integer[5];
		Element distribcontainer = restaurantInfoContainer.select("ul.barChart").first();
		Elements rowsContainer = distribcontainer.select("div.wrap.row");
		int i = 4; // Ratings are from Excellent to Terrible in TripAdvisor. Last is 4 due to zero-indexing
		for (Element rowContainer : rowsContainer) {
			String numStr = rowContainer.select("span.compositeCount").first().ownText();
			ratings[i--] = Integer.valueOf(numStr);
		}
		return new RatingDistribution(ratings);
	}

	private String mineName() {
		Element nameDiv = restaurantInfoContainer.select("h1#HEADING").first();
		return nameDiv.text();
	}
	
	private String mineCategory() {
		Element categoryContainer = restaurantInfoContainer.select("p#bizCategories > span#cat_display > a").first();
		return categoryContainer.text();
	}
	
	private Address mineAddress() {
		Element addressContainer = restaurantInfoContainer.select("div#HEADING_GROUP > div > address").first();
		String numAndStreet = addressContainer.select("span[property=v:street-address]").text();
		String[] split = splitNumAndStreet(numAndStreet);
		
		String city = addressContainer.select("span[property=v:locality]").text();
		String region = addressContainer.select("span[property=v:region]").text();
		String zipcode = addressContainer.select("span[property=v:postal-code]").text();
		
		Address result = new Address(split[0], split[1], zipcode, city, region);
		return result;
	}
	
	private String minePhoneNumber() {
		Elements phoneContainer = restaurantInfoContainer.select("div.sprite-greenPhone + div.fl");
		if (!phoneContainer.isEmpty()) {
			String phoneText = phoneContainer.first().text();
			return phoneText;
		}
		else return "";
	}
	
	private String mineWebsite() {
		// TODO
		throw new Error("Not yet implemented. You need to run scripts for this");
	}
	
	private Float mineRating() {
		Element scoreContainer = restaurantInfoContainer.select("div.rating[rel=v:rating] > span > img.sprite-ratings[property=v:average]").first();
		String scoreText = scoreContainer.attr("alt");
		scoreText = scoreText.substring(0, scoreText.indexOf(" of 5 stars"));
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
	
	
	private ArrayList<TripAdvisorReview> mineReviews(Element container) {
		ArrayList<TripAdvisorReview> result = new ArrayList<TripAdvisorReview>();
		
		Integer totalNumberOfReviews = mineTotalNumberOfReviews(container);
		
		for (int i = 0; 10 * i < totalNumberOfReviews; i++) { // 10 reviews per page. 
			// We start with 1-10, so if we have 10 reviews we stop, if we have 11 we continue.
			Element pageContainer = container; // changes with page
			if (i > 0) { // to get comments in page i > 0, we need another connection.
				Document doc = null;
				String prefix = url.substring(0, url.indexOf("-Reviews-"));
				String postfix = url.substring(url.indexOf("-Reviews-") + "-Reviews-".length());
				try {
					String page = prefix + "-or" + i + "0-" + postfix;
					doc = Jsoup.connect(page).get();
				} catch (IOException e) {
					System.out.println("Failed to connect to load comments in page " + i);
					e.printStackTrace();
				}
				pageContainer = doc.select("div#REVIEWS").first();
			}
			Elements reviewcontainers = pageContainer.select("div[id^=review_]"); // all the DIVs whose id starts with "review_"
			for (Element reviewcontainer : reviewcontainers) {
				result.add(new TripAdvisorReview(reviewcontainer));
			}
		}
		
		return result;
	}
	
	private Integer mineTotalNumberOfReviews(Element container) {
		String containerStr = container.select("div.ratings_and_types > h3.reviews_header").first().ownText(); 
		//format:  "N reviews from our community"
		String trimmedStr = 
				containerStr.substring(
						0, 
						containerStr.indexOf(" reviews from our community")
				);
		return Integer.valueOf(trimmedStr);
	}

	@Override
	public String toString() {
		return "Restaurant [\nname=" + name + ", category=" + category
				+ ", address=" + address + ", phoneNumber=" + phoneNumber
				+ ", website=" + website + ", score=" + rating + ".\n" + reviews.size() + " reviews:\n"
				+ reviews + "\n]\n";
	}

	public static void main(String args[]) {
		ArrayList<TripAdvisorRestaurant> restaurants = new ArrayList<TripAdvisorRestaurant>();
		Out out = new Out("/Users/davide/Desktop/TripAdvisor.txt");
		
		for (String arg: args) {
			TripAdvisorRestaurant q = new TripAdvisorRestaurant(arg);
			out.println(q);
			restaurants.add(q);
		}
		//StdOut.println("Total reviews mined: " + restaurants.get(0).reviews.size());
	}
	
}
