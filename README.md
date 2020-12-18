Role User Analyzer
 - A simple tool to analyze users with certain roles and the entitlements to suggest roles and orphan entitlements.

Getting Started
 - Extract zip file, and execute the RoleUserAnalysis.java class.

Prerequisites
 - Java is installed, and $JAVA_HOME is set as a environment variable.
 - To run the tool, provide required input data for analysis in Role_Definitions.xlsx and User_Access.xlsx or configure the values of roleDefinationUrl and userAccessUrl in the filename.properties file to the excel files you want to analyse.
 - Execute RoleUserAnalysis.java
 - Verify SuggestedRole.xlsx for suggested roles and OrphanEntitlements.xlsx for orphan entitlements
 
Built With
 - Apache POI - To perform I/O operations on excel. Refer https://poi.apache.org/

Other aspects focused on
 - A way for reading and writing to excel.

Usage
 - Apache POI library provides an API to perform I/O operations on excel in an effective manner.
 - Main class org.confluxsys.RoleUserAnalysis.RoleUserAnalysis analyzes the roles and users and identify orphan entitlements and role memberships.