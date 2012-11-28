package yelpScraper;

import org.jsoup.nodes.Element;



public class YelpReview {
	String id;
	YelpUser author;
	Float rating;
	
	Integer usefulCounter;
	Integer funnyCounter;
	Integer coolCounter;
	
	String text;
	Element containingElement;
	
	public YelpReview(Element container) {
		containingElement = container;
		id = mineId();
		author = mineAuthor();
		rating = mineRating();
		usefulCounter = mineUsefulCounter();
		funnyCounter = mineFunnyCounter();
		coolCounter = mineCoolCounter();
		text = mineText();
	}

	private String mineId() {
		String idstr = containingElement.attr("id"); // it's "review_<ID>".
		return idstr.substring("review_".length() );
	}

	private YelpUser mineAuthor() {
		Element container = containingElement.select("div.user-passport").first();
		return new YelpUser(container);
	}

	private Float mineRating() {
		Element scoreContainer = containingElement.select("div.rating-container > div > i > img").first();
		String scoreText = scoreContainer.attr("alt");
		scoreText = scoreText.substring(0, scoreText.indexOf(" star rating"));
		Float result = Float.valueOf(scoreText);
		return result;
	}

	private Integer mineUsefulCounter() {
		Element countersContainer = containingElement.select("div.rateReview.clearfix > ul").first();
		Element usefulContainer = countersContainer.select("li.useful").first();
		String usefulStr = usefulContainer.select("span").get(1).text(); // The second span contains the number
		
		if (usefulStr.equals("Ê") ) return 0; // They put a non-breaking space when there are none
		
		usefulStr = usefulStr.substring(1, usefulStr.length()-1); // removes the parentheses that enclose the counter
		Integer result = Integer.valueOf(usefulStr);
		
		return result;
	}

	private Integer mineFunnyCounter() {
		Element countersContainer = containingElement.select("div.rateReview.clearfix > ul").first();
		Element funnyContainer = countersContainer.select("li.funny").first();
		String funnyStr = funnyContainer.select("span").get(1).text();
		
		if (funnyStr.equals("Ê")) return 0; // They put a non-breaking space when there are none
		
		funnyStr = funnyStr.substring(1, funnyStr.length()-1); // removes the parentheses that enclose the counter
		Integer result = Integer.valueOf(funnyStr);
		
		return result;
	}

	private Integer mineCoolCounter() {
		Element countersContainer = containingElement.select("div.rateReview.clearfix > ul").first();
		Element coolContainer = countersContainer.select("li.cool").first();
		String coolStr = coolContainer.select("span").get(1).text();
		
		if (coolStr.equals("Ê") ) return 0; // They put a non-breaking space when there are none
		
		coolStr = coolStr.substring(1, coolStr.length()-1); // removes the parentheses that enclose the counter
		Integer result = Integer.valueOf(coolStr);
		
		return result;
	}

	private String mineText() {
		Element container = containingElement.select("div.media-story > p.review_comment").first();
		String dirty = container.ownText();
		
		String clean = dirty.replaceAll(" Ê", "<br/>"); // Weird encoding problem, that space I am replacing is Â  in UTF-8
		
		return clean;
	}

	@Override
	public String toString() {
		return "\tYelpReview [id=" + id + ", author=" + author + ", rating="
				+ rating + ", usefulCounter=" + usefulCounter
				+ ", funnyCounter=" + funnyCounter + ", coolCounter="
				+ coolCounter + ", text=" + text + "]\n";
	}

	


	
}