package L9;

import java.sql.*;

public class Lab4_ex {
  public static void main(String args[]){

    Connection con = null;

    try {
      Statement stmt;
      ResultSet rs;

      // Define URL of database server for
      // database named 'user' on the faure.
      String url =
            "jdbc:mariadb://helmi:3306/brentj";

      // Get a connection to the database for a
      // user named 'user' with the password
      // password.
      con = DriverManager.getConnection(
                        url,"brentj", "CHANGE ME ON INDIVIDUAL MACHINE");

      // Display URL and connection information
      System.out.println("URL: " + url);
      System.out.println("Connection: " + con);

      // Get a Statement object
      stmt = con.createStatement();

	try{
        rs = stmt.executeQuery("SELECT * FROM Author");
        while (rs.next()) {
          System.out.println (rs.getString("AuthorID"));
      }
      }catch(Exception e){
        System.out.print(e);
        System.out.println(
                  "No Author table to query");
      }//end catch

      con.close();
    }catch( Exception e ) {
      e.printStackTrace();

    }//end catch

  }//end main

}//end class Lab4A_ex
