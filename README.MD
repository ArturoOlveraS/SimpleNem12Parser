## How to Run

### Import into Eclipse
1. File → Import
2. Existing Projects into Workspace
3. Select extracted folder
4. Finish

### Run Tests
Right click:
test/simplenem12/SimpleNem12ParserImplTest.java
→ Run As → JUnit Test


###Requirements
Java 21
Eclipse IDE
JUnit 5 (already included in project)



------------------------------------------------------

### Task 1.  Write a list of any questions you would have to clarify these requirements.



1) What should the parser do if the first record is not 100, or the last is not 900? Throw an exception? Ignore the file?

2) Should the parser validate the order of records strictly (e.g. 300 before 200)?

3) What should happen if: An NMI is not exactly 10 characters?  An unknown EnergyUnit appears? A Quality value is not A or E?

4) Should malformed lines be:  Rejected entirely? Skipped? Cause the whole parse to fail?

5) What should happen if two 300 records have the same date for the same meter? 

6) Is it guaranteed that all 300 records for a meter appear contiguously after its 200 record?

7) Why are there negative volumes? Are they meaningful, or should they be rejected?

8) Can a line 200 exist without any 300 records?

9) Is ordering of meters or volumes significant?

10) What should happen if a 300 record appears after 900?



------------------------------------------------------



### Task 2. Create a new class (e.g. SimpleNem12ParserImpl) that implements interface `SimpleNem12Parser`. 

Overview

This project implements a parser for a simplified version of the Australian NEM12 electricity meter data format.

The parser reads a CSV file and converts it into structured Java objects representing:

Meter reads (MeterRead)
Meter volumes (MeterVolume)
Energy units (EnergyUnit)
Quality indicators (Quality)

The implementation includes:
	
Validation of file structure
Validation of field formats
JUnit 5 test suite
Sample CSV test cases

### Project Structure

```
SimpleNem12Parser
├── src/
│   └── simplenem12/
│        ├── EnergyUnit.java
│        ├── MeterRead.java
│        ├── MeterVolume.java
│        ├── Quality.java
│        ├── SimpleNem12Parser.java
│        ├── SimpleNem12ParserImpl.java
│        └── TestHarness.java
│
├── test/
│   └── simplenem12/
│        └── SimpleNem12ParserImplTest.java
│
├──input/
│  └── SimpleNem12.csv
│
├── resources/
│   └── nem12/
│       ├── valid.csv
│       ├── bad_100_not_first.csv
│       ├── bad_nmi_length.csv
│       ├── bad_300_without_200.csv
│       └── bad_900_not_last.csv
│
├── README.md
│
├── .classpath
│
├── .project
│
├── .settings/

```



### The parser enforces the following rules:


The first line of the .csv file must be 100

New meter read block begins with 200

The number 300 represents a meter volume entry

The final non-empty line of the file must be 900
 
The volume record must be 200,NMI,EnergyUnit

NMI must be exactly 10 characters

EnergyUnit must match the EnergyUnit enum (e.g., KWH)  

Date must follow yyyyMMdd

Volume must be a valid signed decimal number

Quality must be either A (Actual) or E (Estimate)








