-- Library Database README File
-- CS 430 | Lab 9
-- Due 6/25/2025 11:59 PM
-- Written by Brent Jackson

CS430 Lab 9 – Library Activity Via XML

This project reads an XML file of library transactions, updates the database accordingly, and actively outputs a report of the activities.

Setup & Running Instructions:

1. Start MariaDB and run the following commands to create and populate the database:

   SOURCE CreateLibrary.sql;
   SOURCE PopulateLibrary.sql;

2. Exit the MariaDB shell:

   exit;

3. Navigate to the Directory holding Lab9.java in your terminal (In my case L9):

   cd L9

4. Compile and run the Java program, redirecting output to a file:

   javac Lab9.java
   java Lab9 > activity_output.txt

5. Re-enter MariaDB and run the queries:

   SOURCE Queries.sql;

Done! The file activity_output.txt contains the log of processing activity from the XML file. If you would like the output from the DB, you can optionally run tee output.txt for it.