package amazonScraper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import edu.princeton.cs.introcs.StdOut;

public class User {
	private String id;
	private ArrayList<Review> allReviews;

	public User(String id) throws IOException, SQLException {
		this.id = id;
		allReviews = fetchAllReviews();
	}

	private ArrayList<Review> fetchAllReviews() throws IOException, SQLException {
		ArrayList<Review> result = new ArrayList<Review>();
		String prefix = "http://www.amazon.com/gp/cdp/member-reviews/";
		for (int i = 1; ; i++) {
			String postfix = "?ie=UTF8&display=public&page="+i+"&sort_by=MostRecentReview";

			String url = prefix + id + postfix;
			
			StdOut.println("\\\\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

			Document doc = null;
			try {
				doc = Jsoup.connect(url).get();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Elements allContainers = doc
					.select("a[name~=[A-Z0-9]+] ~ div[style=margin-left:0.5em;]");

			if (!allContainers.select(":contains(We're sorry, but this customer's list of reviews is currently not available)").isEmpty()) break;

			// Each of this containers is a DIV containing everything about the
			// review
			for (Element container : allContainers) {
				Element dateAndTitleAndRatingContainer = container.select(
						"div[style=margin-left:0.5em;]").first();

				String reviewLink = dateAndTitleAndRatingContainer.select("a[href]:contains(Permalink)").first().attr("href");
				String reviewId = extractReviewId(reviewLink);
				//Review r = new Review(reviewId, this);
				Review r = new Review(reviewId, this, dateAndTitleAndRatingContainer);
				StdOut.println(r);
				//r.writeToDB();
				result.add(r);
			}
		}
		return result;
	}
	
	public void printAllReviews() {
		for (Review r : allReviews)
			StdOut.println(r + "\n--------------------");
	}

	public String getId() {
		return id;
	}
	
	/** @param reviewLink
	 *            The review link, ex
	 *            "http://www.amazon.com/review/R31XKHJ8Q8BVQ9/ref=cm_cr_rdp_perm"
	 * @return its id, ex R31XKHJ8Q8BVQ9
	 */
	private static String extractReviewId(String reviewLink) {
		int startpos = 29;// The id starts at position 29
		return reviewLink.substring(startpos);
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
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public static void main(String args[]) throws IOException, SQLException {
		User u = new User(args[0]);
		u.printAllReviews();
	}

}
