-- Library Database Query Script
-- CS 430 | Lab 9
-- Due 6/25/2025 11:59 PM
-- Written by Brent Jackson

-- Task 5a: Print the contents of the Borrowed table
SELECT * FROM Borrowed
ORDER BY MemberID, ISBN;

-- Task 5b: For each member with a book currently checked out,
-- print Last name, First name, MemberID, Title, and Library name

SELECT 
    m.LastName,
    m.FirstName,
    m.MemberID,
    b.Title AS BookTitle,
    l.Name AS LibraryName
FROM Borrowed br
JOIN Member m ON br.MemberID = m.MemberID
JOIN Book b ON br.ISBN = b.ISBN
JOIN LocatedAt la ON la.ISBN = b.ISBN
JOIN Library l ON l.LibraryID = la.LibraryID
WHERE br.DateReturned IS NULL
ORDER BY m.LastName, m.FirstName, b.Title;
