-- Library Database README File
-- CS 430 | Lab 10
-- Due 7/1/2025 11:59 PM
-- Written by Brent Jackson

CS430 Lab 10 – Library Availability Checker

Overview:
This is a text-based Java program that connects to a MariaDB library database. It allows library members to check book availability by ISBN, partial title, or author name.
Users can also add new members if they are not already in the system.

Features:
Prompts for Member ID and checks if the member exists.
Allows adding a new member if not found.
Search options include:
ISBN, Partial or Full book title, Partial or Full Author name

Displays availability, library location, floor, shelf, and number of available copies.
Uses prepared statements to prevent SQL injection.
Prints formatted boxed welcome messages for clean output.

How to Run:

Compile with:
javac Lab10Jackson.java

Run with:
java Lab10Jackson

Requirements:

Java 8 or higher

MariaDB JDBC driver in your classpath

A MariaDB database with tables for Member, Book, Author, BookAuthor, LocatedAt, Borrowed, and Library

Notes:

Typing 'exit' at the Member ID prompt quits the program.

Typing 'exit' while adding a member returns to the main menu without adding the member.

The program uses a repeatChar method to print lines and box characters for formatting.

Author:
Brent Jackson