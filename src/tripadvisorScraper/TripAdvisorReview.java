package tripadvisorScraper;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.princeton.cs.algs4.Date;

public class TripAdvisorReview {
	String id;
	TripAdvisorUser author;

	Float globalRating; // Ratings. This is the final one.
	Float valueRating;
	Float atmosphereRating;
	Float serviceRating;
	Float foodRating;

	Integer helpfulCounter;

	Date date;
	String text;
	Element containingElement;

	public TripAdvisorReview(Element container) {
		containingElement = container;

		id = mineId();
		date = mineDate();
		author = mineAuthor();

		globalRating = mineGlobalRating();

//		Float[] specificRatings = mineSpecificRatings();
//		valueRating = specificRatings[0];
//		atmosphereRating = specificRatings[1];
//		serviceRating = specificRatings[2];
//		foodRating = specificRatings[3];

		text = mineText();
	}

	private String mineId() {
		String idstr = containingElement.attr("id"); // it's "review_<ID>".
		return idstr.substring("review_".length());
	}

	private TripAdvisorUser mineAuthor() {
		Element container = containingElement.select("div.col1of2")
				.first();
		return new TripAdvisorUser(container);
	}

	private Float mineGlobalRating() {
		Element scoreContainer = containingElement.select(
				"div.col2of2 > div.rating.reviewItemInline "
						+ "> span.rate > img.sprite-ratings").first();
		String scoreText = scoreContainer.attr("alt");
		scoreText = scoreText.substring(0, scoreText.indexOf(" of 5 stars"));
		Float result = Float.valueOf(scoreText);
		return result;
	}

	private Date mineDate() {
		Element dateContainer = containingElement.select(
				"div.col2of2 > div.rating.reviewItemInline "
						+ "> span.ratingDate").first();
		String dateInStr = dateContainer.ownText();
		dateInStr = dateInStr.substring("Reviewed ".length());
		return parseDate(dateInStr);
	}

	private Date parseDate(String strDate) {
		int firstSpace = strDate.indexOf(" ");
		int commaPos = strDate.indexOf(", ");
		String strMonth = strDate.substring(0, firstSpace);
		Month month = Month.valueOf(strMonth.toUpperCase());
		Integer dayDate = Integer.parseInt(strDate.substring(firstSpace + 1,
				commaPos));
		Integer monthNum = month.ordinal() + 1; // enum is 0-indexed
		Integer year = Integer.parseInt(strDate.substring(commaPos + 2));

		return new Date(monthNum, dayDate, year);
	}

	private Float[] mineSpecificRatings() {
		// TODO Can't work without the "More" button
		Float[] result = new Float[4];
		Elements ratingsContainer = containingElement
				.select("div.rating-list li.recommend-answer");
		if (ratingsContainer.size() != 4)
			throw new Error("Found " + ratingsContainer.size()
					+ " specific ratings instead of 4");
		int i = 0;
		for (Element ratingContainer : ratingsContainer) { // Only 4 of them.
			String ratingStr = ratingContainer.select("span.rate > img").first().attr("alt");
			ratingStr = ratingStr.substring(0, ratingStr.indexOf(" of 5 stars"));
			result[i++] = Float.valueOf(ratingStr);
		}
		
		return result;
	}

	private String mineText() {
		Element container = containingElement.select(
				"div.entry > p").first();
		String dirty = container.ownText();

		String clean = dirty.replaceAll(" Ê", "<br/>"); // Weird encoding
														// problem, that space I
														// am replacing is Â  in
														// UTF-8

		return clean;
	}

	private enum Month {
		JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER
	}

	@Override
	public String toString() {
		return "\tTripAdvisorReview [id=" + id + ", author=" + author
				+ ", globalRating=" + globalRating + ", valueRating="
				+ valueRating + ", atmosphereRating=" + atmosphereRating
				+ ", serviceRating=" + serviceRating + ", foodRating="
				+ foodRating + ", helpfulCounter=" + helpfulCounter + ", date="
				+ date + ", text=" + text + "]\n";
	}

}