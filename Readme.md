Table Creation
- I used mysql workbench in order to complete this assignment which allowed me to create tables directly using sql code within the workbench. I was also able to change and modify tables to set desired primary keys and refrences to other tables(such as for the student ID's). I didn't have much trouble until I had to sort out some syntax errors and refrence errors when creating major and minor tables

Data Population
- I used a website knwown as Mockaroo which allowed me to create dummy data, and they also have a formatting option to format them into sql insert queries. I also used python in order to generate other data such as Student IDs and then also used python to create tons of random insert statments to then put my data into the database tables

Command Line Argument:
EX: "java connectDB.java jdbc:mysql://localhost:3306/StudentDatabase root '' "
- URL, USERNAME, PASSWORD
- The password for the example for my local database was " ", or in other words nothing, as shown in example above
- export CLASSPATH=/Users/evanlowell/Desktop/2023/Databases/PrinInfoProjects/homework3/lib/mysql-connector-j-8.0.32.jar      
-Above was how to add my jdbc driver to classpath
- CLASSPATH was:cd /Users/evanlowell/Desktop/2023/Databases/PrinInfoProjects/homework3/lib/