-- Library Database Schema
-- CS 430 | Lab 9
-- Due 6/25/2025 11:59 PM
-- Written by Brent Jackson

-- Drop any tables if they exist that way there are no conflicts for multiple runs of the script
DROP TABLE IF EXISTS Borrowed;
DROP TABLE IF EXISTS BookAuthor;
DROP TABLE IF EXISTS AuthorPhone;
DROP TABLE IF EXISTS PublisherPhone;
DROP TABLE IF EXISTS LocatedAt;
DROP TABLE IF EXISTS Book;
DROP TABLE IF EXISTS Member;
DROP TABLE IF EXISTS Phone;
DROP TABLE IF EXISTS Publisher;
DROP TABLE IF EXISTS Author;
DROP TABLE IF EXISTS Library;
DROP TABLE IF EXISTS Audit;

-- Author table
CREATE TABLE Author (
    AuthorID INT NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    FirstName VARCHAR(50) NOT NULL,
    PRIMARY KEY (AuthorID)
);

-- Phone table
CREATE TABLE Phone (
    PNumber VARCHAR(15) NOT NULL,
    Type VARCHAR(20) NOT NULL,
    PRIMARY KEY (PNumber)
);

-- AuthorPhone
CREATE TABLE AuthorPhone (
    AuthorID INT NOT NULL,
    PNumber VARCHAR(15) NOT NULL,
    PRIMARY KEY (AuthorID, PNumber),
    FOREIGN KEY (AuthorID) REFERENCES Author(AuthorID) ON DELETE CASCADE,
    FOREIGN KEY (PNumber) REFERENCES Phone(PNumber) ON DELETE CASCADE
);

-- Publisher
CREATE TABLE Publisher (
    PubID INT NOT NULL,
    Pub_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (PubID)
);

-- PublisherPhone
CREATE TABLE PublisherPhone (
    PubID INT NOT NULL,
    PNumber VARCHAR(15) NOT NULL,
    PRIMARY KEY (PubID, PNumber),
    FOREIGN KEY (PubID) REFERENCES Publisher(PubID) ON DELETE CASCADE,
    FOREIGN KEY (PNumber) REFERENCES Phone(PNumber) ON DELETE CASCADE
);

-- Book
CREATE TABLE Book (
    ISBN VARCHAR(20) NOT NULL,
    Title VARCHAR(100) NOT NULL,
    PubID INT NOT NULL,
    YearPublished DATE NOT NULL,
    PRIMARY KEY (ISBN),
    FOREIGN KEY (PubID) REFERENCES Publisher(PubID) ON DELETE CASCADE
);

-- BookAuthor
CREATE TABLE BookAuthor (
    ISBN VARCHAR(20) NOT NULL,
    AuthorID INT NOT NULL,
    PRIMARY KEY (ISBN, AuthorID),
    FOREIGN KEY (ISBN) REFERENCES Book(ISBN) ON DELETE CASCADE,
    FOREIGN KEY (AuthorID) REFERENCES Author(AuthorID) ON DELETE CASCADE
);

-- Member
CREATE TABLE Member (
    MemberID INT NOT NULL,
    Name VARCHAR(50) NOT NULL,
    Gender ENUM('M', 'F') NOT NULL,
    DateOfBirth DATE NOT NULL,
    PRIMARY KEY (MemberID)
);

-- Library
CREATE TABLE Library (
    LibraryID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100),
    Street VARCHAR(100),
    City VARCHAR(100),
    State VARCHAR(50)
);

-- Located At
CREATE TABLE LocatedAt (
    ISBN VARCHAR(20) NOT NULL,
    LibraryID INT NOT NULL,
    Shelf VARCHAR(10),
    Floor VARCHAR(10),
    TotalCopies INT,
    CopiesNotCheckedOut INT CHECK (CopiesNotCheckedOut >= 0),
    PRIMARY KEY (ISBN, LibraryID),
    FOREIGN KEY (ISBN) REFERENCES Book(ISBN) ON DELETE CASCADE,
    FOREIGN KEY (LibraryID) REFERENCES Library(LibraryID) ON DELETE CASCADE
);

-- Borrowed
CREATE TABLE Borrowed (
    MemberID INT NOT NULL,
    ISBN VARCHAR(20) NOT NULL,
    LibraryID INT NOT NULL,
    DateBorrowed DATE NOT NULL,
    DateReturned DATE,
    PRIMARY KEY (MemberID, ISBN, DateBorrowed),
    FOREIGN KEY (MemberID) REFERENCES Member(MemberID) ON DELETE CASCADE,
    FOREIGN KEY (ISBN) REFERENCES Book(ISBN) ON DELETE CASCADE,
    FOREIGN KEY (LibraryID) REFERENCES Library(LibraryID) ON DELETE CASCADE,
    FOREIGN KEY (ISBN, LibraryID) REFERENCES LocatedAt(ISBN, LibraryID) ON DELETE RESTRICT
);

-- Audit
CREATE TABLE Audit (
    AuditID INT PRIMARY KEY AUTO_INCREMENT,
    TableName VARCHAR(50),
    ActionType ENUM('INSERT', 'UPDATE', 'DELETE'),
    ActionTimestamp DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Triggers to update CopiesNotCheckedOut and log changes in Audit
DELIMITER //

-- Log adding a new Author
CREATE TRIGGER trg_author_insert
AFTER INSERT ON Author
FOR EACH ROW
BEGIN
    INSERT INTO Audit (TableName, ActionType)
    VALUES ('Author', 'INSERT');
END;
//

-- Log adding a book to a library
CREATE TRIGGER trg_locatedat_insert
AFTER INSERT ON LocatedAt
FOR EACH ROW
BEGIN
    INSERT INTO Audit (TableName, ActionType)
    VALUES ('LocatedAt', 'INSERT');
END;
//

-- Log deleting a book from a library
CREATE TRIGGER trg_locatedat_delete
AFTER DELETE ON LocatedAt
FOR EACH ROW
BEGIN
    INSERT INTO Audit (TableName, ActionType)
    VALUES ('LocatedAt', 'DELETE');
END;
//

-- Log updating number of copies in LocatedAt
CREATE TRIGGER trg_locatedat_update
AFTER UPDATE ON LocatedAt
FOR EACH ROW
BEGIN
    IF OLD.TotalCopies <> NEW.TotalCopies OR OLD.CopiesNotCheckedOut <> NEW.CopiesNotCheckedOut THEN
        INSERT INTO Audit (TableName, ActionType)
        VALUES ('LocatedAt', 'UPDATE');
    END IF;
END;
//

-- Optional: Log adding a Book to the system
CREATE TRIGGER trg_book_insert
AFTER INSERT ON Book
FOR EACH ROW
BEGIN
    INSERT INTO Audit (TableName, ActionType)
    VALUES ('Book', 'INSERT');
END;
//

-- Optional: Log deleting a Book entirely (not just from a library)
CREATE TRIGGER trg_book_delete
AFTER DELETE ON Book
FOR EACH ROW
BEGIN
    INSERT INTO Audit (TableName, ActionType)
    VALUES ('Book', 'DELETE');
END;
//

DELIMITER ;
