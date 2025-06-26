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
                    String libraryName = sectionNode.getElementsByTagName("Library").item(0).getTextContent().trim();
                    String checkoutDateRaw = sectionNode.getElementsByTagName("Checkout_date").item(0).getTextContent().trim();
                    String checkinDateRaw = sectionNode.getElementsByTagName("Checkin_date").item(0).getTextContent().trim();

                    String checkoutDate = "N/A".equalsIgnoreCase(checkoutDateRaw) ? null : reformatDate(checkoutDateRaw);
                    String checkinDate = "N/A".equalsIgnoreCase(checkinDateRaw) ? null : reformatDate(checkinDateRaw);

                    if (!exists("SELECT * FROM Member WHERE MemberID = ?", memberID)) {
                        System.out.println("ERROR: MemberID " + memberID + " not found.");
                        continue;
                    }

                    if (!exists("SELECT * FROM Book WHERE ISBN = ?", isbn)) {
                        System.out.println("ERROR: ISBN " + isbn + " not found.");
                        continue;
                    }

                    Integer libraryID = getLibraryIDByName(libraryName);
                    if (libraryID == null) {
                        System.out.println("ERROR: Library name '" + libraryName + "' not found.");
                        continue;
                    }

                    if (checkinDate != null) {
                        // Checkin
                        if (exists("SELECT * FROM Borrowed WHERE MemberID = ? AND ISBN = ? AND LibraryID = ? AND DateReturned IS NULL",
                                   memberID, isbn, libraryID.toString())) {
                            PreparedStatement ps = conn.prepareStatement(
                                "UPDATE Borrowed SET DateReturned = ? WHERE MemberID = ? AND ISBN = ? AND LibraryID = ? AND DateReturned IS NULL"
                            );
                            ps.setString(1, checkinDate);
                            ps.setString(2, memberID);
                            ps.setString(3, isbn);
                            ps.setInt(4, libraryID);
                            ps.executeUpdate();
                            System.out.println("Checkin updated: MemberID " + memberID + ", ISBN " + isbn + ", Library: " + libraryName);
                        } else {
                            System.out.println("ERROR: No open checkout found to check in: MemberID " + memberID + ", ISBN " + isbn + ", Library: " + libraryName);
                        }
                    } else if (checkoutDate != null) {
                        // Duplicate checkout prevention
                        if (exists("SELECT * FROM Borrowed WHERE MemberID = ? AND ISBN = ? AND LibraryID = ? AND DateReturned IS NULL",
                                   memberID, isbn, libraryID.toString())) {
                            System.out.println("ERROR: Duplicate checkout attempt - MemberID " + memberID + ", ISBN " + isbn + ", Library: " + libraryName);
                            continue;
                        }

						// Check that the book is actually located at this library
						if (!exists("SELECT * FROM LocatedAt WHERE ISBN = ? AND LibraryID = ?", isbn, libraryID.toString())) {
							System.out.println("ERROR: Book ISBN " + isbn + " not found at Library " + libraryName);
							continue;
						}

                        // Insert checkout
                        PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO Borrowed (MemberID, ISBN, DateBorrowed, DateReturned, LibraryID) VALUES (?, ?, ?, NULL, ?)"
                        );
                        ps.setString(1, memberID);
                        ps.setString(2, isbn);
                        ps.setString(3, checkoutDate);
                        ps.setInt(4, libraryID);
                        ps.executeUpdate();
                        System.out.println("Checkout recorded: MemberID " + memberID + ", ISBN " + isbn + ", Library: " + libraryName);
                    } else {
                        System.out.println("ERROR: Missing both Checkout and Checkin dates for MemberID " + memberID + ", ISBN " + isbn);
                    }
                }
            }

            conn.close();

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Convert MM/DD/YYYY → YYYY-MM-DD
    private String reformatDate(String mmddyyyy) {
        try {
            String[] parts = mmddyyyy.split("/");
            if (parts.length == 3) {
                return parts[2] + "-" + parts[0] + "-" + parts[1];
            }
        } catch (Exception e) {
            System.out.println("ERROR formatting date: " + mmddyyyy);
        }
        return null;
    }

    // Look up LibraryID from Library name
    private Integer getLibraryIDByName(String libraryName) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT LibraryID FROM Library WHERE Name = ?");
            ps.setString(1, libraryName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("LibraryID");
            }
        } catch (SQLException e) {
            System.out.println("ERROR looking up LibraryID: " + e.getMessage());
        }
        return null;
    }

    // General record existence checker
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
