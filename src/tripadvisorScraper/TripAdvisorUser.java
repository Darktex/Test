package tripadvisorScraper;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TripAdvisorUser {
	private String id;
	private String userName;

	private Integer totalReviewsCount;
	private Integer restaurantReviewsCount;
	private Integer reviewsInCitiesCount; // "Reviews in N cities"
	private Integer helpfulCount; // How many people found his reviews helpful
	//private RatingDistribution ratingDistribution;
	
	private Element container;
	
	public TripAdvisorUser(Element passport) {
		container = passport;
		id = mineId();
		userName = mineUserName();
		
		totalReviewsCount = mineTotalReviewCounts(); // No other way to do it but to mine them individually.
		restaurantReviewsCount = mineRestaurantReviewsCount();
		reviewsInCitiesCount = mineReviewsInCitiesCount();
		helpfulCount = mineHelpfulCount();
	}
	
	private Integer mineTotalReviewCounts() {
		Elements conts = container.select("div.memberBadging > div.totalReviewBadge > span.badgeText");
		if (conts.isEmpty() ) return 0;
		Element cont = conts.first();
		String countsStr = cont.ownText();
		countsStr = countsStr.substring(0, countsStr.indexOf(" review") );
		return Integer.valueOf(countsStr);
	}

	private Integer mineRestaurantReviewsCount() {
		Elements conts = container.select("div.memberBadging > div.totalReviewBadge" +
				" > div.contributionReviewBadge > span.badgeText");
		if (conts.isEmpty() ) return 0;
		Element cont = conts.first();
		String countsStr = cont.ownText();
		countsStr = countsStr.substring(0, countsStr.indexOf(" restaurant review") );
		return Integer.valueOf(countsStr);
	}

	private Integer mineReviewsInCitiesCount() {
		Elements conts = container.select("div.memberBadging > div.passportStampsBadge > span.badgeText"); 
		if (conts.isEmpty() ) return 0;
		Element cont = conts.first();
		String countsStr = cont.ownText();
		countsStr = countsStr.substring("Reviews in ".length() );
		countsStr =	countsStr.substring(0, countsStr.indexOf(" cit"));
		return Integer.valueOf(countsStr);
	}

	private Integer mineHelpfulCount() {
		Elements conts = container.select("div.memberBadging > div.helpfulVotesBadge > span.badgeText"); 
		if (conts.isEmpty() ) return 0;
		Element cont = conts.first();
		String countsStr = cont.ownText();
		countsStr = countsStr.substring(0, countsStr.indexOf(" helpful vote") );
		return Integer.valueOf(countsStr);
	}

	private String mineId() {
		Elements conts = container.select("div.member_info > div.memberOverlayLink > div.avatar");
		if (conts.isEmpty() ) return "-1";
		Element cont = conts.first();
		String linkAndId = cont.attr("class");
		String idStr = linkAndId.substring(linkAndId.indexOf("profile_") + "profile_".length() );
		return idStr;
	}
	
	private String mineUserName() {
		Elements conts = container.select("div.member_info > div.memberOverlayLink > div.username > span");
		if (conts.isEmpty() ) return "Anon";
		Element cont = conts.first();
		String name = cont.text();
		return name;
	}

	@Override
	public String toString() {
		return "TripAdvisorUser [id=" + id + ", userName=" + userName
				+ ", totalReviewsCount=" + totalReviewsCount
				+ ", restaurantReviewsCount=" + restaurantReviewsCount
				+ ", reviewsInCitiesCount=" + reviewsInCitiesCount
				+ ", helpfulCount=" + helpfulCount + "]";
	}

	
	
}
