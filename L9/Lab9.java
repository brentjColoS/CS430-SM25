package L9;

import java.io.File;
import java.sql.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class Lab9 {

    private static final String DB_URL = "jdbc:mariadb://helmi:3306/brentj";
    private static final String USER = "brentj";
    private static final String PASS = "Change Me";
    private Connection conn;

    public void readXML(String fileName) {
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            File file = new File(fileName);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeLst = doc.getElementsByTagName("Borrowed_by");

            for (int s = 0; s < nodeLst.getLength(); s++) {
                Node fstNode = nodeLst.item(s);

                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element sectionNode = (Element) fstNode;

                    String memberID = sectionNode.getElementsByTagName("MemberID").item(0).getTextContent().trim();
                    String isbn = sectionNode.getElementsByTagName("ISBN").item(0).getTextContent().trim();
                    String checkoutDate = sectionNode.getElementsByTagName("Checkout_date").item(0).getTextContent().trim();
                    String checkinDate = sectionNode.getElementsByTagName("Checkin_date").item(0).getTextContent().trim();

                    if (!exists("SELECT * FROM Member WHERE MemberID = ?", memberID)) {
                        System.out.println("ERROR: MemberID " + memberID + " not found.");
                        continue;
                    }

                    if (!exists("SELECT * FROM Book WHERE ISBN = ?", isbn)) {
                        System.out.println("ERROR: ISBN " + isbn + " not found.");
                        continue;
                    }

                    if (!checkinDate.isEmpty()) {
                        // It's a checkin
                        if (exists("SELECT * FROM Borrowed WHERE MemberID = ? AND ISBN = ? AND DateReturned IS NULL", memberID, isbn)) {
                            PreparedStatement ps = conn.prepareStatement(
                                "UPDATE Borrowed SET DateReturned = ? WHERE MemberID = ? AND ISBN = ? AND DateReturned IS NULL"
                            );
                            ps.setString(1, checkinDate);
                            ps.setString(2, memberID);
                            ps.setString(3, isbn);
                            ps.executeUpdate();
                            System.out.println("Checkin updated: MemberID " + memberID + ", ISBN " + isbn);
                        } else {
                            System.out.println("ERROR: No matching checkout found to check in for MemberID " + memberID + ", ISBN " + isbn);
                        }
                    } else {
                        // It's a checkout
                        PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO Borrowed (MemberID, ISBN, DateBorrowed, DateReturned) VALUES (?, ?, ?, NULL)"
                        );
                        ps.setString(1, memberID);
                        ps.setString(2, isbn);
                        ps.setString(3, checkoutDate);
                        ps.executeUpdate();
                        System.out.println("Checkout recorded: MemberID " + memberID + ", ISBN " + isbn);
                    }
                }
            }

            conn.close();

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean exists(String query, String... params) {
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("ERROR checking existence: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        try {
            Lab9 lab = new Lab9();
            lab.readXML("/s/bach/j/under/brentj/Documents/CS430/CS430/L9/Libdata.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
