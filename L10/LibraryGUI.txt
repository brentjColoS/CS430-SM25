import javax.swing.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class LibraryGUI {

    private static final String DB_URL = "jdbc:mariadb://helmi:3306/brentj";
    private static final String USER = "brentj";
    private static final String PASS = "Change Me";
    private Connection conn;

    public LibraryGUI() {
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            showError("Connection failed: " + e.getMessage());
        }
    }

    public void run() {
        while (true) {
            String input = JOptionPane.showInputDialog(null, "Enter Member ID (or Cancel to quit):");
            if (input == null) break;

            int memberID;
            try {
                memberID = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                showError("Invalid ID.");
                continue;
            }

            if (!memberExists(memberID)) {
                int add = JOptionPane.showConfirmDialog(null, "Member not found. Add new member?", "Add Member", JOptionPane.YES_NO_OPTION);
                if (add == JOptionPane.YES_OPTION) {
                    addNewMember(memberID);
                } else {
                    continue;
                }
            }

            String[] options = { "ISBN", "Partial Book Title", "Author Name" };
            int choice = JOptionPane.showOptionDialog(null, "Search by:", "Book Search",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (choice == 0) {
                String isbn = JOptionPane.showInputDialog("Enter ISBN:");
                if (isbn != null) searchByISBN(isbn.trim());
            } else if (choice == 1) {
                String title = JOptionPane.showInputDialog("Enter part of book title:");
                if (title != null) searchByTitle(title.trim());
            } else if (choice == 2) {
                String author = JOptionPane.showInputDialog("Enter author name:");
                if (author != null) searchByAuthor(author.trim());
            }
        }

        closeConn();
    }

    private boolean memberExists(int memberID) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Member WHERE MemberID = ?");
            ps.setInt(1, memberID);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            showError("Error checking member: " + e.getMessage());
            return false;
        }
    }

    private void addNewMember(int memberID) {
        try {
            String name = JOptionPane.showInputDialog("Enter full name:");
            if (name == null || name.trim().isEmpty()) {
                showError("Name cannot be empty.");
                return;
            }

            String gender = JOptionPane.showInputDialog("Enter gender (M/F):");
            if (gender == null) return;
            gender = gender.trim().toUpperCase();
            if (!gender.equals("M") && !gender.equals("F")) {
                showError("Invalid gender. Must be 'M' or 'F'.");
                return;
            }

            String dob = JOptionPane.showInputDialog("Enter date of birth (YYYY-MM-DD):");
            if (dob == null) return;
            dob = dob.trim();
            try {
                LocalDate.parse(dob);
            } catch (DateTimeParseException e) {
                showError("Invalid date format. Use YYYY-MM-DD.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement("INSERT INTO Member (MemberID, Name, Gender, DateOfBirth) VALUES (?, ?, ?, ?)");
            ps.setInt(1, memberID);
            ps.setString(2, name);
            ps.setString(3, gender);
            ps.setString(4, dob);
            ps.executeUpdate();
            showMessage("New member added.");
        } catch (SQLException e) {
            showError("Error adding member: " + e.getMessage());
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

            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                int available = rs.getInt("AvailableCopies");
                result.append("Title: ").append(rs.getString("Title"))
                      .append("\nLibrary: ").append(rs.getString("LibraryName"))
                      .append("\nShelf: ").append(rs.getString("Shelf"));
                if (available > 0) {
                    result.append("\nCopies available: ").append(available).append("\n\n");
                } else {
                    result.append("\nAll copies are currently checked out.\n\n");
                }
            }
            if (result.length() == 0) {
                showMessage("This ISBN does not exist in any library.");
            } else {
                showMessage(result.toString());
            }
        } catch (SQLException e) {
            showError("Error searching by ISBN: " + e.getMessage());
        }
    }

    private void searchByTitle(String title) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT B.ISBN, B.Title FROM Book B WHERE B.Title LIKE ?;"
            );
            ps.setString(1, "%" + title + "%");
            ResultSet rs = ps.executeQuery();

            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                result.append("ISBN: ").append(rs.getString("ISBN"))
                      .append("\nTitle: ").append(rs.getString("Title"))
                      .append("\n\n");
            }
            if (result.length() == 0) {
                showMessage("No titles matching that found.");
            } else {
                showMessage(result.toString());
            }
        } catch (SQLException e) {
            showError("Error searching by title: " + e.getMessage());
        }
    }

    private void searchByAuthor(String author) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT DISTINCT B.ISBN, B.Title FROM Book B " +
                "JOIN BookAuthor BA ON B.ISBN = BA.ISBN " +
                "JOIN Author A ON BA.AuthorID = A.AuthorID " +
                "WHERE A.LastName LIKE ? OR A.FirstName LIKE ?;"
            );
            ps.setString(1, "%" + author + "%");
            ps.setString(2, "%" + author + "%");
            ResultSet rs = ps.executeQuery();

            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                result.append("ISBN: ").append(rs.getString("ISBN"))
                      .append("\nTitle: ").append(rs.getString("Title"))
                      .append("\n\n");
            }
            if (result.length() == 0) {
                showMessage("No books found by that author.");
            } else {
                showMessage(result.toString());
            }
        } catch (SQLException e) {
            showError("Error searching by author: " + e.getMessage());
        }
    }

    private void closeConn() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            showError("Error closing connection: " + e.getMessage());
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    private void showError(String error) {
        JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        LibraryGUI app = new LibraryGUI();
        app.run();
    }
}
