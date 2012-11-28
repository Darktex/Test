package amazonScraper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;



import edu.princeton.cs.algs4.Date;
import edu.princeton.cs.introcs.StdOut;

public class Review {

	private String id;

	private Element containingHTMLElement;

	private User author;
	private Product product;

	private String title;
	private String text;
	private Integer rating; // 1 to 5
	private Date date;

	private boolean isVerified; // If has the Amazon Verified Purchase badge
	private boolean isVine; // If this review comes from the Vine program

	private Integer[] helpful; // Number of people that found the review helpful on the total (default 0-0)

	private ArrayList<Comment> comments;
	private ArrayList<String> linkedPages;


	/**
	 * @param id The id of the review on Amazon. You can find the permalink with it.
	 * @param author The author of the review (see the Author class)
	 * @param product The product the review is about (see the Product class)
	 * @param title Review title
	 * @param text Review text
	 * @param rating Rating (in stars, 1 to 5)
	 * @param date The date in which it was posted (time of the day does not apply)
	 * @param isVerified If has the Amazon Verified Purchase badge
	 * @param isVine If this review comes from the Vine program
	 * @param helpful A pair of Integers. Number of people that found the review helpful on the total (default 0-0)
	 * @param comments A list of all the comments
	 * @param linkedPages A list of all the links in the review
	 */
	public Review(String id, User author, Product product,
			String title, String text, Integer rating, Date date,
			boolean isVerified, boolean isVine, Integer[] helpful, ArrayList<Comment> comments, ArrayList<String> linkedPages) {
		this.containingHTMLElement = null; // Not relevant, we have got everything we need
		this.id = id;
		this.author = author;
		this.product = product;
		this.title = title;
		this.text = text;
		this.rating = rating;
		this.date = date;
		this.isVerified = isVerified;
		this.isVine = isVine;
		this.helpful = helpful;
		this.comments = comments;
		this.linkedPages = linkedPages;
	}

	/** This constructor requires a Internet connection to fetch what it needs 
	 * @throws IOException */
	public Review(String id, User author) throws IOException {
		this.id = id;
		this.author = author;
		this.containingHTMLElement = getContainingHTMLElement();

		this.product = fetchProduct();
		this.title = fetchTitle();
		this.text = fetchText();
		this.rating = fetchRating();
		this.date = fetchDate();
		this.isVerified = fetchIsVerified();
		this.isVine = fetchIsVine();
		this.helpful = fetchHelpful();
		this.linkedPages = fetchLinkedPages();
		this.comments = fetchComments();
	}

	/** This constructor does not require a Internet connection */
	public Review(String id, User author, Element containingElement) {
		this.id = id;
		this.author = author;
		this.containingHTMLElement = containingElement;
		this.product = fetchProduct();
		this.title = fetchTitle();
		this.text = fetchText();
		this.rating = fetchRating();
		this.date = fetchDate();
		this.isVerified = fetchIsVerified();
		this.isVine = fetchIsVine();
		this.helpful = fetchHelpful();
		this.linkedPages = fetchLinkedPages();
		this.comments = fetchComments();
	}



	private ArrayList<Comment> fetchComments() {
		// TODO
		ArrayList<Comment> result = new ArrayList<Comment>();

		String prefix = "http://www.amazon.com/review/";

		return result;
	}

	private ArrayList<String> fetchLinkedPages() {
		ArrayList<String> result = new ArrayList<String>();
		List<TextNode> textNodes = containingHTMLElement.textNodes();
		if (textNodes.isEmpty())
			return result;

		// start at the first text node. There are some empty nodes at the beginning we don't want to consider.
		int startindex;
		for (startindex = 0; startindex < textNodes.size() && textNodes.get(startindex).isBlank(); startindex++);
		Node currentNode = textNodes.get(startindex);

		while (currentNode != null)
		{
			// append deep text of all subsequent nodes
			if (currentNode instanceof Element)
			{
				Element currentElement = (Element) currentNode;
				Elements anchorElements = currentElement.select("a[href]")
						.select(":not(:contains(Comment))")
						.select(":not(:contains(Permalink))")
						.select(":not(a[href=http://www.amazon.com/review/"+ this.id +"]")
						.select(":not(img)");
				if (!anchorElements.isEmpty()) {
					for (Element anchorElement : anchorElements)
						result.add(anchorElement.attr("href"));
				}
			}
			currentNode = currentNode.nextSibling();
		}
		return result;
	}

	private Integer[] fetchHelpful() {
		Elements helpful = containingHTMLElement
				.select("div[style=margin-bottom:0.5em;]:contains(people found the following review helpful)");
		String helpfultext = helpful.text();
		Integer helpfulScores[] = {0, 0};
		if (!helpful.isEmpty()) {			
			int pos1 = helpfultext.indexOf(" of ");
			int pos2 = helpfultext.indexOf(" people");

			helpfulScores[0] = Integer.parseInt(helpfultext.substring(0, pos1));
			helpfulScores[1] = Integer.parseInt(helpfultext.substring(pos1 + 4, pos2));
		}
		return helpfulScores;
	}

	private boolean fetchIsVine() {
		Elements vineQuery = containingHTMLElement
				.select("div[style=margin-bottom:0.5em;] > span.small > b:contains(Customer review from the Amazon Vineª Program)");
		if (!vineQuery.isEmpty()) return true;
		return false;
	}

	private boolean fetchIsVerified() {
		Elements verifiedQuery = containingHTMLElement
				.select("span.crVerifiedStripe > b:contains(Amazon Verified Purchase)");
		if (!verifiedQuery.isEmpty()) return true;
		return false;
	}

	private Integer fetchRating() {
		String strReviewScore = containingHTMLElement.select( // Score
				"span[style=margin-left: -5px;] > img").attr("title");
		Integer reviewScore = Integer.parseInt(strReviewScore.substring(0,
				1));
		return reviewScore;
	}

	private String fetchText() {
		List<TextNode> textNodes = containingHTMLElement.textNodes();
		if (textNodes.isEmpty())
			return "";

		StringBuilder result = new StringBuilder();
		// start at the first text node. There are some empty nodes at the beginning we don't want to consider.
		int startindex;
		for (startindex = 0; startindex < textNodes.size() && textNodes.get(startindex).isBlank(); startindex++);
		Node currentNode = textNodes.get(startindex);

		while (currentNode != null)
		{
			// append deep text of all subsequent nodes
			if (currentNode instanceof TextNode)
			{
				TextNode currentText = (TextNode) currentNode;
				result.append(currentText.text());
			}
			else if (currentNode instanceof Element)
			{
				Element currentElement = (Element) currentNode;
				Elements anchorElements = currentElement.select("a[href]").select(":not(:contains(Comment))").select(":not(:contains(Permalink))");
				if (!anchorElements.isEmpty()) {
					for (Element anchorElement : anchorElements)
						result.append(anchorElement.text());
				}
			}
			currentNode = currentNode.nextSibling();
		}
		return result.toString().trim();
	}

	private Date fetchDate() {
		String reviewDateStr = containingHTMLElement
				.select("nobr").first() // Date
				.text(); // it's in a nobr. This needs further processing
		Date reviewDate = parseDate(reviewDateStr);
		return reviewDate;
	}

	private String fetchTitle() {
		String reviewTitle = containingHTMLElement.select("b") // Title
				.first().text(); // it's in bold
		return reviewTitle;
	}

	private Product fetchProduct() {
		Product result;
		String prodLink = containingHTMLElement.select("span.h3color.tiny + a[href]").attr("href"); // getting link

		// Now we extract the ID from the link

		if (prodLink.isEmpty()) return new Product("0"); // Sometimes product links expire and are not there anymore. They get index 0

		int startpos = prodLink.indexOf("/dp/") + 4;

		String a = prodLink.substring(startpos);

		result = new Product(a);
		return result;
	}

	private Element getContainingHTMLElement() throws IOException {
		String prefix = "http://www.amazon.com/review/";
		String postfix = "/ref=cm_cd_pg_pg2?ie=UTF8";

		String url = prefix + id + postfix;

		Document doc = Jsoup.connect(url).get();

		Elements allContainers = doc
				.select("a[name~=[A-Z0-9]+] ~ div[style=margin-left:0.5em;]");
		Element dateAndTitleAndRatingContainer = allContainers.first();
		return dateAndTitleAndRatingContainer;
	}

	@Override
	public String toString() {
		return "\n------------\nReview [id=" + id + "\nAuthor=" + author.getId() + "\nProduct="
				+ product + "\nReview Title=" + title + "\nDate=" + date + "\nisVerified=" + isVerified
				+ ", isVine=" + isVine + "\n"
				+ "Rating=" + rating + "\nHelpful="
				+ Arrays.toString(helpful) + "\nComments=" + comments
				+ "\nLinkedPages=" + linkedPages + "]"+
				"\nText=" + text;
	}



	/**
	 * This takes a date in Amazon's format and returns a Date object.
	 * 
	 * @input [Month][:space:][0-9]{1-2}[:comma:][:space:][Year]
	 */
	private static Date parseDate(String strDate) {
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




	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Review other = (Review) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public void writeToDB() throws SQLException {
		
		MySQLConnection db = new MySQLConnection();
		Statement st = db.con.createStatement();
		
		String userCheckQuery = "SELECT * FROM  `User` WHERE  `id` =  '"+ author.getId() +"'";
		String productCheckQuery = "SELECT * FROM  `Product` WHERE  `id` =  '"+ product.getId() +"'";
		
		
		ResultSet userRes = st.executeQuery(userCheckQuery);

		if (!userRes.first()) st.execute("INSERT INTO `AmazonReviews`.`User` (`id`) VALUES ('"+ author.getId()  +"');");

		ResultSet productRes = st.executeQuery(productCheckQuery);
		if (!productRes.first()) st.execute("INSERT INTO `AmazonReviews`.`Product` (`id`) VALUES ('"+ product.getId()  +"');");
 
		
		String insertionQuery = "INSERT INTO  `AmazonReviews`.`Review` (" +
				"`id` , `user_id` ,`product_id` ,`title` ,`text` ,`date` ,`rating` ," +
				"`isVerified` ,`isVine` ,`helpfulN` ,`helfulTot`)" +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		
		PreparedStatement prep = db.con.prepareStatement(insertionQuery);
		prep.setString(1, id);
		prep.setString(2, author.getId());
		prep.setString(3, product.getId());
		prep.setString(4, title);
		prep.setString(5, text);
		prep.setString(6, mySQLformat(date));
		prep.setInt(7, rating);
		prep.setString(8, mySQLformat(isVerified));
		prep.setString(9, mySQLformat(isVine));
		prep.setInt(10, helpful[0]);
		prep.setInt(11, helpful[1]);
	
		
		
		StdOut.println("----\n" + prep + "\n--------");
		prep.execute();

	}
	

	private String mySQLformat(Date d) {
		return d.year() + "-" + d.month() + "-" + d.day();
	}
	
	private String mySQLformat(boolean b) {
		if (b) return "1";
		return "0";
	}

	private enum Month {
		JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER
	}


	public static void main(String args[]) {
		System.out.println("Abo");
	}
}