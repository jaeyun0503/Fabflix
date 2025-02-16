import org.jasypt.util.password.StrongPasswordEncryptor;

import java.sql.*;

public class VerifyPassword {

	/*
	 * After you update the passwords in customers table,
	 *   you can use this program as an example to verify the password.
	 *   
	 * Verify the password is simple:
	 * success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
	 * 
	 * Note that you need to use the same StrongPasswordEncryptor when encrypting the passwords
	 * 
	 */
	public static void main(String[] args) throws Exception {

		System.out.println(verifyCredentials("a@email.com", "a2", "user"));
		System.out.println(verifyCredentials("a@email.com", "a3", "user"));

	}

	public static boolean validPassword(String email, String password, String type) throws Exception {
		return verifyCredentials(email, password, type);
	}

	private static boolean verifyCredentials(String email, String password, String type) throws Exception {
		
		String loginUser = "mytestuser";
		String loginPasswd = "My6$Password";
		String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
		PreparedStatement statement;
		String query = "";

		if (type.equals("customer")) {
			query = "SELECT * from customers where email= ?";
		}
		else if (type.equals("employee")) {
			query = "SELECT * from employees where email= ?";
		}
		statement = connection.prepareStatement(query);
		statement.setString(1, email);

		ResultSet rs = statement.executeQuery();

		boolean success = false;
		if (rs.next()) {
		    // get the encrypted password from the database
			String encryptedPassword = rs.getString("password");
			
			// use the same encryptor to compare the user input password with encrypted password stored in DB
			success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
		}

		rs.close();
		statement.close();
		connection.close();
		
		System.out.println("verify " + email + " - " + password);
		System.out.println(success);
		return success;
	}

}
