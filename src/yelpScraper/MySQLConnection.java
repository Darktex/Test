package yelpScraper;

import java.sql.*;

public class MySQLConnection {
	public Connection con;
	
	
	public MySQLConnection() {
		System.out.println("Inserting values in Mysql database table!");
		con = null;
		String url = "jdbc:mysql://localhost:8889/";
		String db = "AmazonReviews";
		String driver = "com.mysql.jdbc.Driver";
		try{
			Class.forName(driver);
			con = DriverManager.getConnection(url+db,"root","root");
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}