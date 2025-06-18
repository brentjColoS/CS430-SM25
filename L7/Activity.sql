-- Library Database Activity Script
-- CS 430 | Lab 7
-- Due 6/17/2025 11:59 PM
-- Written by Brent Jackson

-- 1. Add a new book to the Main library
-- Assumes the Book isn't already in the Book table
INSERT INTO Book (ISBN, Title, PubID, YearPublished)
VALUES ('96-42013-10510', 'Growing your own Weeds', 10000, '2012-06-24');

INSERT INTO LocatedAt (ISBN, LibraryID, Shelf, Floor, TotalCopies, CopiesNotCheckedOut)
VALUES ('96-42013-10510', 1, 8, 2, 1, 1);

-- 2. Modify the number of copies of ISBN 96-42103-10907 to 8 in the Main library
UPDATE LocatedAt
SET TotalCopies = 8
WHERE ISBN = '96-42103-10907' AND LibraryID = 1;

-- 3. Delete Grace Slick from the Author table
-- This will fail if Grace Slick is linked in BookAuthor
DELETE FROM Author
WHERE FirstName = 'Grace' AND LastName = 'Slick';

-- 4. Add Commander Adams to the author table, ID 305, with office phone
INSERT INTO Author (AuthorID, LastName, FirstName)
VALUES (305, 'Adams', 'Commander');

INSERT INTO Phone (PNumber, Type)
VALUES ('970-555-5555', 'Office');

INSERT INTO AuthorPhone (AuthorID, PNumber)
VALUES (305, '970-555-5555');

-- 5. Add the same book to South Park library
-- Should not re-insert into Book if already added above
-- Only insert into LocatedAt
INSERT INTO LocatedAt (ISBN, LibraryID, Shelf, Floor, TotalCopies, CopiesNotCheckedOut)
VALUES ('96-42013-10510', 2, 8, 3, 1, 1);

-- 6. Delete the book 'Missing Tomorrow' from Main Library
-- Get the ISBN first, then delete from LocatedAt
DELETE FROM LocatedAt
WHERE ISBN = '96-42103-11003' AND LibraryID = 1;

-- 7. Add 2 new copies of Eating in the Fort in South Park
UPDATE LocatedAt
SET TotalCopies = TotalCopies + 2,
    CopiesNotCheckedOut = CopiesNotCheckedOut + 2
WHERE ISBN = '96-42103-11604' AND LibraryID = 2;

-- 8. Add new book with non-existent publisher (PubID 90000)
-- This will fail due to FK constraint on Book.PubID
INSERT INTO Book (ISBN, Title, PubID, YearPublished)
VALUES ('96-42013-10513', 'Growing your own Weeds', 90000, '2012-06-24');

INSERT INTO LocatedAt (ISBN, LibraryID, Shelf, Floor, TotalCopies, CopiesNotCheckedOut)
VALUES ('96-42013-10513', 1, 8, 2, 1, 1);

-- 9. Print contents of the Audit table
SELECT * FROM Audit;
