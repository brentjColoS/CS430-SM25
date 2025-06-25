-- Library Database Query Script
-- CS 430 | Lab 7
-- Due 6/17/2025 11:59 PM
-- Written by Brent Jackson

-- Task 5

-- 1. List the contents of the Library relation in order according to name.
SELECT DISTINCT * FROM Library
ORDER BY Name;

-- 2. List the contents of the LocatedAt relation in alphabetic order according to ISBN.
SELECT DISTINCT * FROM LocatedAt
ORDER BY ISBN;

-- 3. For each book that has copies in both libraries, list the book name, number of copies, and library sorted by book name.
SELECT DISTINCT
    B.Title,
    L.Name AS LibraryName,
    LA.TotalCopies
FROM LocatedAt LA
JOIN Book B ON LA.ISBN = B.ISBN
JOIN Library L ON LA.LibraryID = L.LibraryID
WHERE LA.ISBN IN (
    SELECT ISBN
    FROM LocatedAt
    GROUP BY ISBN
    HAVING COUNT(DISTINCT LibraryID) > 1
)
ORDER BY B.Title;

-- 4. For each library, list library name, and the number of distinct titles sorted by library.
SELECT 
    L.Name AS LibraryName,
    COUNT(DISTINCT LA.ISBN) AS NumTitles
FROM Library L
JOIN LocatedAt LA ON L.LibraryID = LA.LibraryID
GROUP BY L.LibraryID, L.Name
ORDER BY L.Name;

-- Task 6 is located at the bottom of CreateLibrary.sql

-- Task 7: Create a view that gives Book name, list of authors, and library name on one line.
CREATE SQL SECURITY INVOKER VIEW BookAuthorLibraryView AS
SELECT 
    b.Title AS BookTitle,
    GROUP_CONCAT(DISTINCT CONCAT(a.FirstName, ' ', a.LastName) ORDER BY a.LastName SEPARATOR ', ') AS Authors,
    l.Name AS LibraryName
FROM 
    Book b
    JOIN BookAuthor ba ON b.ISBN = ba.ISBN
    JOIN Author a ON ba.AuthorID = a.AuthorID
    JOIN LocatedAt la ON b.ISBN = la.ISBN
    JOIN Library l ON la.LibraryID = l.LibraryID
GROUP BY 
    b.Title, l.Name;

-- Using this view, provide a list of books, authors, shelf, and library name sorted by book name.
SELECT DISTINCT
    v.BookTitle,
    v.Authors,
    la.Shelf,
    v.LibraryName
FROM 
    BookAuthorLibraryView v
    JOIN Book b ON v.BookTitle = b.Title
    JOIN LocatedAt la ON b.ISBN = la.ISBN
WHERE la.LibraryID = (SELECT LibraryID FROM Library WHERE Name = v.LibraryName LIMIT 1)
ORDER BY 
    v.BookTitle;
