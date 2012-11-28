package amazonScraper;

import java.util.ArrayList;

public class Product {
	private String id;
	private ArrayList<Review> allReviews;
	/**
	 * @param id
	 * @param allReviews
	 */
	public Product(String id, ArrayList<Review> allReviews) {
		this.id = id;
		this.allReviews = allReviews;
	}
	
	public Product(String id) {
		this.id = id;
		this.allReviews = new ArrayList<Review>();
	}

	@Override
	public String toString() {
		return "Product [id=" + id + ", allReviews=" + allReviews + "]";
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
		Product other = (Product) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getId() {
		return id;
	}
	
	
}
