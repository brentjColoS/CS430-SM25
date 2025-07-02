-- Library Database Query Script
-- CS 430 | Lab 9
-- Due 6/25/2025 11:59 PM
-- Written by Brent Jackson

-- Task 5a: Print the contents of the Borrowed table
SELECT * FROM Borrowed
ORDER BY MemberID, ISBN;

-- Task 5b: For each member with a book currently checked out,
-- print Last name, First name, MemberID, Title, and Library name

-- The CONCAT is to break apart the Member Name into first/last and order it correctly for this call
SELECT 
    CONCAT(
        SUBSTRING_INDEX(M.Name, ' ', -1), ' ',
        SUBSTRING_INDEX(M.Name, ' ', 1)
    ) AS MemberName,
    M.MemberID, 
    B.Title, 
    L.Name AS LibraryName
FROM 
    Borrowed BR
JOIN 
    Member M ON BR.MemberID = M.MemberID
JOIN 
    Book B ON BR.ISBN = B.ISBN
JOIN 
    Library L ON BR.LibraryID = L.LibraryID
WHERE 
    BR.DateReturned IS NULL
ORDER BY 
    MemberName, B.Title;
