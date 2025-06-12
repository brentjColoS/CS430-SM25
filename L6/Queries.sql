-- Library Database Query Script
-- CS 430 | Lab 6
-- Due 6/11/2025 11:59 PM
-- Written by Brent Jackson

-- Task 5a) List the contents of the Book relation ordered by ISBN
SELECT * FROM Book ORDER BY ISBN;

-- Task 5b) List the contents of the Member relation ordered by last name then first name
SELECT * FROM Member ORDER BY LastName, FirstName;

-- Task 5c) List the contents of the Author relation ordered by last name then first name
SELECT * FROM Author ORDER BY LastName, FirstName;

-- Task 5d) List the contents of the Publisher relation ordered by PublisherName
SELECT * FROM Publisher ORDER BY PublisherName;

-- Task 5e) List the contents of the Phone relation ordered by PhoneNumber
SELECT * FROM Phone ORDER BY PhoneNumber;

-- Task 6) List the contents of each relationship relation table
SELECT * FROM BookAuthor ORDER BY AuthorID, ISBN;
SELECT * FROM Borrowed ORDER BY MemberID, ISBN;
SELECT * FROM AuthorPhone ORDER BY AuthorID, PhoneNumber;
SELECT * FROM PublisherPhone ORDER BY PublisherID, PhoneNumber;

-- Task 7) List the first and last names of Members whose last name begins with B
SELECT FirstName, LastName FROM Member WHERE LastName LIKE 'B%' ORDER BY LastName, FirstName;

-- Task 8) List the books published by 'Coyote Publishing' sorted by Title
SELECT B.Title FROM Book B
JOIN Publisher P ON B.PublisherID = P.PublisherID
WHERE P.PublisherName = 'Coyote Publishing'
ORDER BY B.Title;

-- Task 9) For each Member with a book currently checked out, list the books they have checked out
SELECT M.FirstName, M.LastName, M.MemberID, B.Title
FROM Member M
JOIN Borrowed BR ON M.MemberID = BR.MemberID
JOIN Book B ON BR.ISBN = B.ISBN
WHERE BR.DateReturned IS NULL
ORDER BY M.LastName, M.FirstName, B.Title;

-- Task 10) For each Author, list the titles of books they have written
SELECT A.FirstName, A.LastName, A.AuthorID, B.Title
FROM Author A
JOIN BookAuthor BA ON A.AuthorID = BA.AuthorID
JOIN Book B ON BA.ISBN = B.ISBN
ORDER BY A.LastName, A.FirstName, B.Title;

-- Task 11) List Authors and phone numbers for those who share a phone number with another author
SELECT DISTINCT A1.FirstName, A1.LastName, P.PhoneNumber
FROM Author A1
JOIN AuthorPhone AP1 ON A1.AuthorID = AP1.AuthorID
JOIN AuthorPhone AP2 ON AP1.PhoneNumber = AP2.PhoneNumber AND AP1.AuthorID <> AP2.AuthorID
JOIN Phone P ON AP1.PhoneNumber = P.PhoneNumber
ORDER BY P.PhoneNumber, A1.LastName, A1.FirstName;
