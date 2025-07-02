import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LibraryGUI {

    private static final String DB_URL = "jdbc:mariadb://helmi:3306/brentj";
    private static final String USER = "brentj";
    private static final String PASS = "Change Me";
    private Connection conn;
    private Scanner sc;

    public LibraryGUI() {
        sc = new Scanner(System.in);
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            System.exit(1);
        }
    }

    public void run() {
        while (true) {
            System.out.print("Enter Member ID (or 'exit' to quit): ");
            String input = sc.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) break;

            int memberID;
            try {
                memberID = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID. Must be an integer.");
                continue;
            }

            if (!memberExists(memberID)) {
                System.out.print("Member not found. Add new member? (Y/N): ");
                String add = sc.nextLine().trim().toUpperCase();
                if (add.equals("Y")) {
                    addNewMember(memberID);
                } else {
                    continue;
                }
            }

            System.out.println("Search by:");
            System.out.println("1. ISBN");
            System.out.println("2. Partial Book Title");
            System.out.println("3. Author Name");
            System.out.print("Choice (1-3): ");
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print("Enter ISBN: ");
                String isbn = sc.nextLine().trim();
                searchByISBN(isbn);
            } else if (choice.equals("2")) {
                System.out.print("Enter part (or all) of book title: ");
                String title = sc.nextLine().trim();
                searchByTitle(title);
            } else if (choice.equals("3")) {
                System.out.print("Enter author name or part of it: ");
                String author = sc.nextLine().trim();
                searchByAuthor(author);
            } else {
                System.out.println("Invalid choice.");
            }
        }

        closeConn();
        sc.close();
    }

    private boolean memberExists(int memberID) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Member WHERE MemberID = ?");
            ps.setInt(1, memberID);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error checking member: " + e.getMessage());
            return false;
        }
    }

    private void addNewMember(int memberID) {
        try {
            System.out.print("Enter full name: ");
            String name = sc.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Name cannot be empty.");
                return;
            }

            String gender;
            while (true) {
                System.out.print("Enter gender (M/F): ");
                gender = sc.nextLine().trim().toUpperCase();
                if (gender.equals("M") || gender.equals("F")) break;
                System.out.println("Invalid gender. Must be 'M' or 'F'.");
            }

            String dob;
            while (true) {
                System.out.print("Enter date of birth (YYYY-MM-DD): ");
                dob = sc.nextLine().trim();
                try {
                    LocalDate.parse(dob);
                    break;
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format. Use YYYY-MM-DD.");
                }
            }

            PreparedStatement ps = conn.prepareStatement("INSERT INTO Member (MemberID, Name, Gender, DateOfBirth) VALUES (?, ?, ?, ?)");
            ps.setInt(1, memberID);
            ps.setString(2, name);
            ps.setString(3, gender);
            ps.setString(4, dob);
            ps.executeUpdate();
            System.out.println("New member added.");
        } catch (SQLException e) {
            System.out.println("Error adding member: " + e.getMessage());
        }
    }

    private void searchByISBN(String isbn) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT B.Title, L.Name AS LibraryName, LA.Shelf, (LA.TotalCopies - IFNULL(BR.CheckedOut,0)) AS AvailableCopies " +
                "FROM Book B JOIN LocatedAt LA ON B.ISBN = LA.ISBN " +
                "JOIN Library L ON LA.LibraryID = L.LibraryID " +
                "LEFT JOIN (SELECT ISBN, LibraryID, COUNT(*) AS CheckedOut FROM Borrowed WHERE DateReturned IS NULL GROUP BY ISBN, LibraryID) BR " +
                "ON LA.ISBN = BR.ISBN AND LA.LibraryID = BR.LibraryID " +
                "WHERE B.ISBN = ?;"
            );
            ps.setString(1, isbn);
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                int available = rs.getInt("AvailableCopies");
                System.out.println();
                System.out.println("Title: " + rs.getString("Title"));
                System.out.println("Library: " + rs.getString("LibraryName"));
                System.out.println("Floor: " + rs.getString("Floor"));
                System.out.println("Shelf: " + rs.getString("Shelf"));
                if (available > 0) {
                    System.out.println("Copies available: " + available);
                } else {
                    System.out.println("All copies are currently checked out.");
                }
                System.out.println();
            }
            if (!found) {
                System.out.println("This ISBN does not exist in any library.");
            }
        } catch (SQLException e) {
            System.out.println("Error searching by ISBN: " + e.getMessage());
        }
    }

    private void searchByTitle(String title) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT B.ISBN, B.Title FROM Book B WHERE B.Title LIKE ?;");
            ps.setString(1, "%" + title + "%");
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println();
                System.out.println("ISBN: " + rs.getString("ISBN"));
                System.out.println("Title: " + rs.getString("Title"));
                System.out.println();
            }
            if (!found) {
                System.out.println("No titles matching that found.");
            }
        } catch (SQLException e) {
            System.out.println("Error searching by title: " + e.getMessage());
        }
    }

    private void searchByAuthor(String authorPart) {
        try {
            // First find matching authors
            PreparedStatement ps = conn.prepareStatement(
                "SELECT AuthorID, LastName, FirstName FROM Author WHERE LastName LIKE ? OR FirstName LIKE ?;"
            );
            ps.setString(1, "%" + authorPart + "%");
            ps.setString(2, "%" + authorPart + "%");
            ResultSet rs = ps.executeQuery();

            ArrayList<Integer> ids = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();

            while (rs.next()) {
                ids.add(rs.getInt("AuthorID"));
                names.add(rs.getString("FirstName") + " " + rs.getString("LastName"));
            }

            if (ids.isEmpty()) {
                System.out.println("No authors found matching: " + authorPart);
                System.out.println();
                return;
            }

            int chosenID;
            String chosenName;

            // If multiple matches, ask user to pick
            if (ids.size() == 1) {
                chosenID = ids.get(0);
                chosenName = names.get(0);
            } else {
                System.out.println("Multiple authors found:");
                for (int i = 0; i < names.size(); i++) {
                    System.out.println((i + 1) + ". " + names.get(i));
                }
                System.out.println();

                System.out.print("Enter number to select author: ");
                String choiceStr = sc.nextLine().trim();
                System.out.println();

                int choice;
                try {
                    choice = Integer.parseInt(choiceStr);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                    System.out.println();
                    return;
                }
                if (choice < 1 || choice > ids.size()) {
                    System.out.println("Invalid choice.");
                    System.out.println();
                    return;
                }
                chosenID = ids.get(choice - 1);
                chosenName = names.get(choice - 1);
            }

            // Print selected author
            System.out.println();
            System.out.println("Author: " + chosenName);
            System.out.println();

            // Query books by selected author
            ps = conn.prepareStatement(
                "SELECT B.ISBN, B.Title FROM Book B " +
                "JOIN BookAuthor BA ON B.ISBN = BA.ISBN " +
                "WHERE BA.AuthorID = ?;"
            );
            ps.setInt(1, chosenID);
            rs = ps.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("ISBN: " + rs.getString("ISBN"));
                System.out.println("Title: " + rs.getString("Title"));
                System.out.println();
            }
            if (!found) {
                System.out.println("This author has no books in the library.");
                System.out.println();
            }

        } catch (SQLException e) {
            System.out.println("Error searching by author: " + e.getMessage());
            System.out.println();
        }
    }

    private void closeConn() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        LibraryGUI app = new LibraryGUI();
        app.run();
    }
}
