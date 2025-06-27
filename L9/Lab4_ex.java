import java.sql.*;

public class Lab4_ex {
    public static void main(String[] args) {
        Connection con = null;

        try {
            // Define URL of database server for database named 'brentj' on helmi
            String url = "jdbc:mariadb://helmi:3306/brentj";

            // Get a connection to the database
            con = DriverManager.getConnection(url, "brentj", "CHANGE ME ON INDIVIDUAL MACHINE");

            // Display URL and connection information
            System.out.println("URL: " + url);
            System.out.println("Connection: " + con);

            // Get a Statement object
            Statement stmt = con.createStatement();

            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM Author");

                while (rs.next()) {
                    System.out.println(rs.getString("AuthorID"));
                }

                rs.close();
                stmt.close();

            } catch (SQLException e) {
                System.out.println("No Author table to query: " + e.getMessage());
            }

            con.close();

        } catch (Exception e) {
            System.out.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
