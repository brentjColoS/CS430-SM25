import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Lab10Jackson {

    private static final String DB_URL = "jdbc:mariadb://helmi:3306/brentj";
    private static final String USER = "brentj";
    private static final String PASS = "Change Me";
    private Connection conn;
    private Scanner sc;

    // Connection Manager
    public Lab10Jackson() {
        sc = new Scanner(System.in);
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            System.exit(1);
        }
    }

    // Primary Runner
    public void run() {
        System.out.println(repeatChar("\n", 15));
        System.out.println("╭────────────────────────────────────────────────────────╮");
        System.out.println("│      Welcome to the Library Availability Checker!      │");
        System.out.println("│          Please enter your Member ID to begin.         │");
        System.out.println("│             ~ Written by: Brent Jackson ~              │");
        System.out.println("╰────────────────────────────────────────────────────────╯");

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
                    boolean added = addNewMember(memberID);
                    if (!added) {
                        // User typed exit during addNewMember - restart while loop
                        continue;
                    }
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
                System.out.println("\n" + repeatChar("═", 100) + "\n");

                System.out.print("Enter ISBN: ");
                String isbn = sc.nextLine().trim();
                searchByISBN(isbn);
            } else if (choice.equals("2")) {
                System.out.println("\n" + repeatChar("═", 100) + "\n");
                System.out.print("Enter part (or all) of book title: ");
                String title = sc.nextLine().trim();
                searchByTitle(title);
            } else if (choice.equals("3")) {
                System.out.println("\n" + repeatChar("═", 100) + "\n");

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

    private boolean addNewMember(int memberID) {
        try {
            System.out.print("Enter full name (First Last) or type 'exit' to return to main menu: ");
            String name = sc.nextLine().trim();
            if (name.equalsIgnoreCase("exit")) {
                System.out.println("Returning to main menu.");
                return false;
            }
            if (name.isEmpty()) {
                System.out.println("Name cannot be empty.");
                return true;
            }

            String gender;
            while (true) {
                System.out.print("Enter gender (M/F) or type 'exit' to return to main menu: ");
                gender = sc.nextLine().trim().toUpperCase();
                if (gender.equalsIgnoreCase("exit")) {
                    System.out.println("Returning to main menu.");
                    return false;
                }
                if (gender.equals("M") || gender.equals("F")) break;
                System.out.println("Invalid gender. Must be 'M' or 'F'.");
            }

            String dob;
            while (true) {
                System.out.print("Enter date of birth (YYYY-MM-DD) or type 'exit' to return to main menu: ");
                dob = sc.nextLine().trim();
                if (dob.equalsIgnoreCase("exit")) {
                    System.out.println("Returning to main menu.");
                    return false;
                }
                try {
                    LocalDate.parse(dob);
                    break;
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format. Use YYYY-MM-DD.");
                }
            }

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Member (MemberID, Name, Gender, DateOfBirth) VALUES (?, ?, ?, ?)"
            );
            ps.setInt(1, memberID);
            ps.setString(2, name);
            ps.setString(3, gender);
            ps.setString(4, dob);
            ps.executeUpdate();
            System.out.println("New member added.");
        } catch (SQLException e) {
            System.out.println("Error adding member: " + e.getMessage());
        }

        return true;
    }

    private void searchByISBN(String isbn) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT B.Title, L.Name AS LibraryName, LA.Floor, LA.Shelf, " +
                "(LA.TotalCopies - IFNULL(BR.CheckedOut,0)) AS AvailableCopies " +
                "FROM Book B " +
                "JOIN LocatedAt LA ON B.ISBN = LA.ISBN " +
                "JOIN Library L ON LA.LibraryID = L.LibraryID " +
                "LEFT JOIN (" +
                "  SELECT ISBN, LibraryID, COUNT(*) AS CheckedOut " +
                "  FROM Borrowed WHERE DateReturned IS NULL GROUP BY ISBN, LibraryID" +
                ") BR ON LA.ISBN = BR.ISBN AND LA.LibraryID = BR.LibraryID " +
                "WHERE B.ISBN = ?;"
            );
            ps.setString(1, isbn);
            ResultSet rs = ps.executeQuery();

            boolean bookInStock = false;
            boolean anyAvailable = false;
            String bookTitle = "";

            ArrayList<String> availableInfo = new ArrayList<>();
            ArrayList<String> checkedOutInfo = new ArrayList<>();

            while (rs.next()) {
                bookInStock = true;
                if (bookTitle.isEmpty()) {
                    bookTitle = rs.getString("Title");
                }
                int available = rs.getInt("AvailableCopies");
                String info = "Library: " + rs.getString("LibraryName")
                            + ", Floor: " + rs.getString("Floor")
                            + ", Shelf: " + rs.getString("Shelf");

                if (available > 0) {
                    anyAvailable = true;
                    availableInfo.add(info + ", Copies available: " + available);
                } else {
                    checkedOutInfo.add(info + ", All copies are checked out.");
                }
            }

            System.out.println();


            if (!bookInStock) {
                System.out.println(" This library system does not currently have the book in stock.");
            } else {
                System.out.println("Title: " + bookTitle);
                System.out.println();

                if (anyAvailable) {
                    for (String s : availableInfo) {
                        System.out.println("Available -> " + s);
                    }
                } else {
                    System.out.println(" All copies of this book are currently checked out at all libraries.");
                }
            }

            System.out.println("\n" + repeatChar("═", 100) + "\n");


        } catch (SQLException e) {
            System.out.println("Error searching by ISBN: " + e.getMessage());
        }
    }


    private void searchByTitle(String titlePart) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT ISBN, Title FROM Book WHERE Title LIKE ?;"
            );
            ps.setString(1, "%" + titlePart + "%");
            ResultSet rs = ps.executeQuery();

            ArrayList<String> isbns = new ArrayList<>();
            ArrayList<String> titles = new ArrayList<>();

            while (rs.next()) {
                isbns.add(rs.getString("ISBN"));
                titles.add(rs.getString("Title"));
            }

            if (isbns.isEmpty()) {
                System.out.println(" No titles matching that found.");
                System.out.println("\n" + repeatChar("═", 100) + "\n");

                return;
            }

            int chosenIndex = 0;

            if (isbns.size() == 1) {
                chosenIndex = 0;
            } else {
                System.out.println("Multiple titles found:");
                for (int i = 0; i < titles.size(); i++) {
                    System.out.println((i + 1) + ". " + titles.get(i) + " (ISBN: " + isbns.get(i) + ")");
                }
                System.out.println();


                System.out.print("Enter number to select title: ");
                String choiceStr = sc.nextLine().trim();

                int choice;
                try {
                    choice = Integer.parseInt(choiceStr);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                    System.out.println("\n" + repeatChar("═", 100) + "\n");

                    return;
                }

                if (choice < 1 || choice > isbns.size()) {
                    System.out.println("Invalid choice.");
                    System.out.println("\n" + repeatChar("═", 100) + "\n");

                    return;
                }

                chosenIndex = choice - 1;
            }

            String selectedISBN = isbns.get(chosenIndex);

            ps = conn.prepareStatement(
                "SELECT L.Name AS LibraryName, LA.Floor, LA.Shelf, (LA.TotalCopies - IFNULL(BR.CheckedOut,0)) AS AvailableCopies " +
                "FROM LocatedAt LA " +
                "JOIN Library L ON LA.LibraryID = L.LibraryID " +
                "LEFT JOIN (SELECT ISBN, LibraryID, COUNT(*) AS CheckedOut FROM Borrowed WHERE DateReturned IS NULL GROUP BY ISBN, LibraryID) BR " +
                "ON LA.ISBN = BR.ISBN AND LA.LibraryID = BR.LibraryID " +
                "WHERE LA.ISBN = ?;"
            );
            ps.setString(1, selectedISBN);
            rs = ps.executeQuery();

            boolean bookInStock = false;
            boolean anyAvailable = false;

            ArrayList<String> availableInfo = new ArrayList<>();
            ArrayList<String> checkedOutInfo = new ArrayList<>();

            while (rs.next()) {
                bookInStock = true;
                int available = rs.getInt("AvailableCopies");
                String info = "Library: " + rs.getString("LibraryName")
                            + ", Floor: " + rs.getString("Floor")
                            + ", Shelf: " + rs.getString("Shelf");

                if (available > 0) {
                    anyAvailable = true;
                    availableInfo.add(info + ", Copies available: " + available);
                } else {
                    checkedOutInfo.add(info + ", All copies are checked out.");
                }
            }

            System.out.println();

            System.out.println("Title: " + titles.get(chosenIndex));
            System.out.println("ISBN: " + selectedISBN);
            System.out.println();


            if (!bookInStock) {
                System.out.println("This library system does not currently have the book in stock.");
            } else if (anyAvailable) {
                for (String s : availableInfo) {
                    System.out.println("Available -> " + s);
                }
            } else {
                System.out.println("All copies of this book are currently checked out at all libraries.");
            }

            System.out.println("\n" + repeatChar("═", 100) + "\n");


        } catch (SQLException e) {
            System.out.println("Error searching by title: " + e.getMessage());
            System.out.println("\n" + repeatChar("═", 100) + "\n");

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
                System.out.println("\n" + repeatChar("═", 100) + "\n");

                return;
            }

            int chosenID;
            String chosenName;

            // Ask user to pick
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
                    System.out.println("\n" + repeatChar("═", 100) + "\n");

                    return;
                }
                if (choice < 1 || choice > ids.size()) {
                    System.out.println("Invalid choice.");
                    System.out.println("\n" + repeatChar("═", 100) + "\n");

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

            ArrayList<String> bookIsbns = new ArrayList<>();
            ArrayList<String> bookTitles = new ArrayList<>();

            while (rs.next()) {
                bookIsbns.add(rs.getString("ISBN"));
                bookTitles.add(rs.getString("Title"));
            }

            if (bookIsbns.isEmpty()) {
                System.out.println("This author has no books in the library.");
                System.out.println("\n" + repeatChar("═", 100) + "\n");

                return;
            }

            int selectedBookIndex = 0;

            if (bookIsbns.size() == 1) {
                selectedBookIndex = 0;
            } else {
                System.out.println("Books by this author:");
                for (int i = 0; i < bookTitles.size(); i++) {
                    System.out.println((i + 1) + ". " + bookTitles.get(i) + " (ISBN: " + bookIsbns.get(i) + ")");
                }
                System.out.println();


                System.out.print("Enter number to select book: ");
                String choiceStr = sc.nextLine().trim();
                System.out.println();


                int choice;
                try {
                    choice = Integer.parseInt(choiceStr);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                    System.out.println("\n" + repeatChar("═", 100) + "\n");

                    return;
                }
                if (choice < 1 || choice > bookIsbns.size()) {
                    System.out.println("Invalid choice.");
                    System.out.println("\n" + repeatChar("═", 100) + "\n");

                    return;
                }
                selectedBookIndex = choice - 1;
            }

            String selectedISBN = bookIsbns.get(selectedBookIndex);

            System.out.println("Title: " + bookTitles.get(selectedBookIndex));
            System.out.println("ISBN: " + selectedISBN);
            System.out.println();


            ps = conn.prepareStatement(
                "SELECT L.Name AS LibraryName, LA.Floor, LA.Shelf, (LA.TotalCopies - IFNULL(BR.CheckedOut,0)) AS AvailableCopies " +
                "FROM LocatedAt LA " +
                "JOIN Library L ON LA.LibraryID = L.LibraryID " +
                "LEFT JOIN (SELECT ISBN, LibraryID, COUNT(*) AS CheckedOut FROM Borrowed WHERE DateReturned IS NULL GROUP BY ISBN, LibraryID) BR " +
                "ON LA.ISBN = BR.ISBN AND LA.LibraryID = BR.LibraryID " +
                "WHERE LA.ISBN = ?;"
            );
            ps.setString(1, selectedISBN);
            rs = ps.executeQuery();

            boolean bookInStock = false;
            boolean anyAvailable = false;

            ArrayList<String> availableInfo = new ArrayList<>();
            ArrayList<String> checkedOutInfo = new ArrayList<>();

            while (rs.next()) {
                bookInStock = true;
                int available = rs.getInt("AvailableCopies");
                String info = "Library: " + rs.getString("LibraryName")
                            + ", Floor: " + rs.getString("Floor")
                            + ", Shelf: " + rs.getString("Shelf");

                if (available > 0) {
                    anyAvailable = true;
                    availableInfo.add(info + ", Copies available: " + available);
                } else {
                    checkedOutInfo.add(info + ", All copies are checked out.");
                }
            }

            if (!bookInStock) {
                System.out.println("This library system does not currently have the book in stock.");
            } else if (anyAvailable) {
                for (String s : availableInfo) {
                    System.out.println("Available -> " + s);
                }
            } else {
                System.out.println("All copies of this book are currently checked out at all libraries.");
            }

            System.out.println("\n" + repeatChar("═", 100) + "\n");


        } catch (SQLException e) {
            System.out.println("Error searching by author: " + e.getMessage());
            System.out.println("\n" + repeatChar("═", 100) + "\n");

        }
    }



    private void closeConn() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    public static String repeatChar(String s, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
        sb.append(s);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Lab10Jackson app = new Lab10Jackson();
        app.run();
    }
}
