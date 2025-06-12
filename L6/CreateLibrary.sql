-- Library Database Schema
-- CS 430 | Lab 6
-- Due 6/11/2025 11:59 PM
-- Written by Brent Jackson

-- Drop any tables if they exist that way there are no conflicts for multiple runs of the script
DROP TABLE IF EXISTS Borrowed;
DROP TABLE IF EXISTS BookAuthor;
DROP TABLE IF EXISTS AuthorPhone;
DROP TABLE IF EXISTS PublisherPhone;
DROP TABLE IF EXISTS Phone;
DROP TABLE IF EXISTS Member;
DROP TABLE IF EXISTS Book;
DROP TABLE IF EXISTS Publisher;
DROP TABLE IF EXISTS Author;

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
    LastName VARCHAR(50) NOT NULL,
    FirstName VARCHAR(50) NOT NULL,
    DOB DATE NOT NULL,
    PRIMARY KEY (MemberID)
);

-- Borrowed
CREATE TABLE Borrowed (
    MemberID INT NOT NULL,
    ISBN VARCHAR(20) NOT NULL,
    CheckoutDate DATE NOT NULL,
    CheckinDate DATE,
    PRIMARY KEY (MemberID, ISBN, DateBorrowed, DateReturned),
    FOREIGN KEY (MemberID) REFERENCES Member(MemberID) ON DELETE CASCADE,
    FOREIGN KEY (ISBN) REFERENCES Book(ISBN) ON DELETE CASCADE
);
