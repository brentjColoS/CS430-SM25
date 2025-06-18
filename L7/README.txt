-- Library Database README File
-- CS 430 | Lab 7
-- Due 6/17/2025 11:59 PM
-- Written by Brent Jackson

Deleting Grace Slick will fail due to ON DELETE CASCADE not removing the already related BookAuthor entries, or lack of cascading in general.

Adding in ISBN 96-42013-10513 with the PubID 90000 will fail because PubID 90000 does not exist in the Publisher table and violates the foreign key constraint.