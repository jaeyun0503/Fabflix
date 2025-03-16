import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
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
		Context initContext = new InitialContext();
		DataSource ds = (DataSource) initContext.lookup("java:comp/env/jdbc/ReadOnly");

		try (Connection connection = ds.getConnection()) {
			String query;
			if ("customer".equalsIgnoreCase(type)) {
				query = "SELECT * FROM customers WHERE email = ?";
			} else if ("employee".equalsIgnoreCase(type)) {
				query = "SELECT * FROM employees WHERE email = ?";
			} else {
				query = "SELECT * FROM customers WHERE email = ?";
			}

			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, email);

			ResultSet rs = statement.executeQuery();

			boolean success = false;
			if (rs.next()) {
				String encryptedPassword = rs.getString("password");
				success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
			}

			rs.close();
			statement.close();

			System.out.println("verify " + email + " - " + password);
			System.out.println("success? " + success);
			return success;
		}
	}

}
