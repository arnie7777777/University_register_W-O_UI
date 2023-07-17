import java.sql.*;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;



public class connectDB{
    // static String databaseName = "StudentDatabase";
    // static String url = "jdbc:mysql://localhost:3306/" + databaseName;
    // static String username = "root";
    // static String password = "";




  
  public static void searchCoursesByStudent(Connection connection, int studentId) throws SQLException {
      String query = "SELECT * FROM courses WHERE name IN (SELECT name FROM is_taking WHERE sid = ?)";
      PreparedStatement statement = connection.prepareStatement(query);
      statement.setInt(1, studentId);
      ResultSet result = statement.executeQuery();
      System.out.println("Courses taken by student with ID " + studentId + ":");
      while (result.next()) {
        String name = result.getString("name");
        String instructor = result.getString("instructor");
        System.out.println(name + " taught by " + instructor);
      }
    }
  
  public static void searchGradesByStudent(Connection connection, int studentId) throws SQLException {
      String query = "SELECT * FROM has_taken WHERE sid = ?";
      PreparedStatement statement = connection.prepareStatement(query);
      statement.setInt(1, studentId);
      ResultSet result = statement.executeQuery();
      System.out.println("Grades for student with ID " + studentId + ":");
      while (result.next()) {
        String name = result.getString("name");
        String grade = result.getString("grade");
        System.out.println(name + ": " + grade);
      }
    }

  public static void searchStudentsByName(Connection connection, String name) throws SQLException {
      String query = "SELECT COUNT(*) AS total_students, s.id, s.first_name, s.last_name, " +
      "  IFNULL(m.dname, 'Undecided') AS major, IFNULL(mi.dname, 'None') AS minor, " +
      "  SUM(CASE WHEN ht.grade = 'A' THEN c.credits * 4 " +
      "           WHEN ht.grade = 'B' THEN c.credits * 3 " +
      "           WHEN ht.grade = 'C' THEN c.credits * 2 " +
      "           WHEN ht.grade = 'D' THEN c.credits * 1 " +
      "           ELSE 0 END) / SUM(c.credits) AS gpa, " +
      "  SUM(c.credits) AS credits_completed " +
      "FROM students s " +
      "  INNER JOIN has_taken ht ON s.id = ht.sid " +
      "  INNER JOIN classes c ON ht.name = c.name " +
      "  LEFT JOIN majors m ON s.id = m.sid " +
      "  LEFT JOIN minors mi ON s.id = mi.sid " +
      "WHERE LOWER(s.first_name) LIKE LOWER(?) OR LOWER(s.last_name) LIKE LOWER(?) " +
      "GROUP BY s.id, s.first_name, s.last_name, m.dname, mi.dname " +
      "ORDER BY s.last_name, s.first_name";
      PreparedStatement statement = connection.prepareStatement(query);
      statement.setString(1, "%" + name.toLowerCase() + "%");
      statement.setString(2, "%" + name.toLowerCase() + "%");
      ResultSet result = statement.executeQuery();

      // int i = 0;
      // System.out.println("Students found (" + result.getMetaData().getColumnCount() + " fields):");
      while (result.next()) {
        // int totalStudents = result.getInt("total_students");
        // System.out.println(totalStudents + " students found");
        // i=i+1;
        // int numStudents = result.getInt("num_students");
        int id = result.getInt("id");
        String firstName = result.getString("first_name");
        String lastName = result.getString("last_name");
        String major = result.getString("major");
        String minor = result.getString("minor");
        double gpa = result.getDouble("gpa");
        int creditsCompleted = result.getInt("credits_completed");
        System.out.printf("%s %s (ID: %d, Major: %s, Minor: %s, GPA: %.2f, Credits Completed: %d)\n", 
        firstName, lastName, id, major, minor, gpa, creditsCompleted);
    }
      // System.out.printf("%s, (Total Students Found: ", i);
      // System.out.println(totalStudents + " students found");
    
}

  public static void searchStudentsByYear(Connection connection, String yearlevel) throws SQLException {
    // try (Scanner scanner = new Scanner(System.in)) 
      // System.out.print("Enter a year level (freshman, sophomore, junior, senior): ");
      // scanner.close();
      // String yearLevel = scanner.nextLine().toLowerCase();
      // Set the credit threshold based on the year level
      int creditsThreshold = 0;
      switch (yearlevel) {
          case "freshman":
              creditsThreshold = 30;
              break;
          case "sophomore":
              creditsThreshold = 60;
              break;
          case "junior":
              creditsThreshold = 90;
              break;
          case "senior":
              creditsThreshold = 120;
              break;
          default:
              System.out.println("Invalid year level entered.");
              return;
      }

      // Construct the parameterized SQL query with the appropriate filters
      String sql = "SELECT s.id, (SELECT SUM(c.credits) FROM has_taken ht INNER JOIN classes c ON ht.name = c.name WHERE ht.sid = s.id) AS total_credits " +
      "FROM students s " +
      "WHERE (SELECT SUM(c.credits) FROM has_taken ht INNER JOIN classes c ON ht.name = c.name WHERE ht.sid = s.id) >= ? AND " +
      "(SELECT SUM(c.credits) FROM has_taken ht INNER JOIN classes c ON ht.name = c.name WHERE ht.sid = s.id) < ?";

      // Create a PreparedStatement object and set the parameter values
      PreparedStatement pstmt = connection.prepareStatement(sql);
      pstmt.setInt(1, creditsThreshold - 30);
      pstmt.setInt(2, creditsThreshold);

      // Execute the query and process the results
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
          int studentId = rs.getInt("id");
          int totalCredits = rs.getInt("total_credits");
          System.out.println("Student ID " + studentId + " took " + totalCredits + " credits.");
      }
    }

  public static void searchStudentsByGpa2(Connection connection, String operator, String gpa) throws SQLException {
    // Scanner scanner = new Scanner(System.in);
    // System.out.print("Enter a GPA threshold: ");
    // double gpaThreshold = scanner.nextDouble();
    // scanner.close();
    // Construct the SQL query with the appropriate joins and calculations
    String sql = "SELECT s.id, s.last_name, " +
                "SUM(CASE WHEN ht.grade = 'A' THEN c.credits * 4 " +
                "         WHEN ht.grade = 'B' THEN c.credits * 3 " +
                "         WHEN ht.grade = 'C' THEN c.credits * 2 " +
                "         WHEN ht.grade = 'D' THEN c.credits * 1 " +
                "         ELSE 0 END) / SUM(c.credits) AS gpa " +
                "FROM students s " +
                "INNER JOIN has_taken ht ON s.id = ht.sid " +
                "INNER JOIN classes c ON ht.name = c.name " +
                "GROUP BY s.id, s.last_name " +
                "HAVING gpa <= ?";

    // Create a PreparedStatement object and set the parameter value
    PreparedStatement pstmt = connection.prepareStatement(sql);
    pstmt.setString(1, gpa);

    // Execute the query and process the results
    ResultSet rs = pstmt.executeQuery();
    while (rs.next()) {
        int studentId = rs.getInt("id");
        String studentName = rs.getString("last_name");
        double gpa2 = rs.getDouble("gpa");
        System.out.println(studentName + " (ID " + studentId + ") has a GPA of " + gpa2);
            }
    }

  public static void searchStudentsByGpa1(Connection connection, String operator, String gpa4) throws SQLException {
    // try (Scanner scanner = new Scanner(System.in)) {
    //   System.out.print("Enter a GPA threshold: ");
      // scanner.close(); 
      // double gpaThreshold = scanner.nextDouble();

      // Construct the SQL query with the appropriate joins and calculations
      String sql = "SELECT s.id, s.last_name, " +
                  "SUM(CASE WHEN ht.grade = 'A' THEN c.credits * 4 " +
                  "         WHEN ht.grade = 'B' THEN c.credits * 3 " +
                  "         WHEN ht.grade = 'C' THEN c.credits * 2 " +
                  "         WHEN ht.grade = 'D' THEN c.credits * 1 " +
                  "         ELSE 0 END) / SUM(c.credits) AS gpa " +
                  "FROM students s " +
                  "INNER JOIN has_taken ht ON s.id = ht.sid " +
                  "INNER JOIN classes c ON ht.name = c.name " +
                  "GROUP BY s.id, s.last_name " +
                  "HAVING gpa >= ?";

      // Create a PreparedStatement object and set the parameter value
      PreparedStatement pstmt = connection.prepareStatement(sql);
      pstmt.setString(1, gpa4);

      // Execute the query and process the results
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
          int studentId = rs.getInt("id");
          String studentName = rs.getString("last_name");
          double gpa2 = rs.getDouble("gpa");
          System.out.println(studentName + " (ID " + studentId + ") has a GPA of " + gpa2);
              }
    }


  public static void searchStudentsByMajor(Connection connection, String major) throws SQLException {
    String query = "SELECT * FROM students WHERE id IN (SELECT sid FROM majors WHERE name = ?)";
    PreparedStatement statement = connection.prepareStatement(query);
    statement.setString(1, major);
    ResultSet result = statement.executeQuery();
    System.out.println("Students with major " + major + ":");
    while (result.next()) {
      int id = result.getInt("id");
      String firstName = result.getString("first_name");
      String lastName = result.getString("last_name");
      System.out.println(id + " " + firstName + " " + lastName);
    }
  }

  public static void searchStudentsByMinor(Connection connection, String minor) throws SQLException {
    String query = "SELECT * FROM students WHERE id IN (SELECT sid FROM minors WHERE name = ?)";
    PreparedStatement statement = connection.prepareStatement(query);
    statement.setString(1, minor);
    ResultSet result = statement.executeQuery();
    System.out.println("Students with minor " + minor + ":");
    while (result.next()) {
      int id = result.getInt("id");
      String firstName = result.getString("first_name");
      String lastName = result.getString("last_name");
      System.out.println(id + " " + firstName + " " + lastName);
    }
  }


  public static void searchByDepartment(Connection connection, String dname)throws SQLException{
  // Assuming you already have a connection to the database

    // try (Scanner scanner = new Scanner(System.in)) {
    //     // System.out.print("Enter a Department Name: ");
    //     String departmentName = scanner.nextLine();
        // String departmentName = "Computer Science"; // replace with desired department name
      
        PreparedStatement stmt = connection.prepareStatement("SELECT s.id, s.last_name, " +
                        "SUM(CASE WHEN ht.grade = 'A' THEN c.credits * 4 " +
                        "         WHEN ht.grade = 'B' THEN c.credits * 3 " +
                        "         WHEN ht.grade = 'C' THEN c.credits * 2 " +
                        "         WHEN ht.grade = 'D' THEN c.credits * 1 " +
                        "         ELSE 0 END) / SUM(c.credits) AS gpa " +
                        "FROM students s " +
                        "INNER JOIN has_taken ht ON s.id = ht.sid " +
                        "INNER JOIN classes c ON ht.name = c.name " +
                        "INNER JOIN majors m ON s.id = m.sid and m.dname = ? " +
                        "GROUP BY s.id, s.last_name");
        
        stmt.setString(1, dname);
        ResultSet rs = stmt.executeQuery();
        
        double totalGpa = 0.0;
        int numStudents = 0;
        
        while (rs.next()) {
            double studentGpa = rs.getDouble("gpa");
            totalGpa += studentGpa;
            numStudents++;
        }
        
        double avgGpa = totalGpa / numStudents;
        
        System.out.println("Department: " + dname);
        System.out.println("Number of students: " + numStudents);
        System.out.println("Average GPA: " + avgGpa);
        
    }

          // Handle exceptions
  public static void searchByCredits(Connection connection, String credits)throws SQLException{
    // try (Scanner scanner = new Scanner(System.in)) {
    //   System.out.print("Enter Credits Amount: ");
    //   String minCredits = scanner.nextLine();
      // scanner.close();
      // String majorName = "Computer Science";
      // int minCredits = 90;
      PreparedStatement stmt = connection.prepareStatement("SELECT s.id, s.last_name, " +
                              "SUM(CASE WHEN ht.grade = 'A' THEN c.credits * 4 " +
                              "         WHEN ht.grade = 'B' THEN c.credits * 3 " +
                              "         WHEN ht.grade = 'C' THEN c.credits * 2 " +
                              "         WHEN ht.grade = 'D' THEN c.credits * 1 " +
                              "         ELSE 0 END) / SUM(c.credits) AS gpa " +
                              "FROM students s " +
                              "INNER JOIN has_taken ht ON s.id = ht.sid " +
                              "INNER JOIN classes c ON ht.name = c.name " +
                              "INNER JOIN majors m ON s.id = m.sid " +
                              "GROUP BY s.id, s.last_name " +
                              "HAVING SUM(c.credits) >= ?");
      // stmt.setString(1, majorName);
      stmt.setString(1, credits);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
          int id = rs.getInt("id");
          String lastName = rs.getString("last_name");
          double gpa = rs.getDouble("gpa");
          System.out.println("Student " + id + " (" + lastName + "): GPA = " + gpa);
    }
  }


  public static void searchByClass(Connection connection, String cname)throws SQLException{
    // try (Scanner scanner = new Scanner(System.in)) {
      // System.out.print("Enter a Class Name: ");
      // String className = scanner.nextLine();

        PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(CASE WHEN ht.grade = 'A' THEN 1 END) AS 'A', "+
        "COUNT(CASE WHEN ht.grade = 'B' THEN 1 END) AS 'B', "+ 
        "COUNT(CASE WHEN ht.grade = 'C' THEN 1 END) AS 'C', "+
        "COUNT(CASE WHEN ht.grade = 'D' THEN 1 END) AS 'D' " +
        "FROM has_taken ht INNER JOIN classes c ON ht.name = c.name WHERE c.name = ?");
        stmt.setString(1, cname);
        ResultSet rs = stmt.executeQuery();

        int numA = 0, numB = 0, numC = 0, numD = 0;

        if (rs.next()) {
            numA = rs.getInt("A");
            numB = rs.getInt("B");
            numC = rs.getInt("C");
            numD = rs.getInt("D");
        }

        System.out.println("Class: " + cname);
        System.out.println("Grades of Previous Enrolees: ");
        System.out.println("A: " + numA);
        System.out.println("B: " + numB);
        System.out.println("C: " + numC);
        System.out.println("D: " + numD);
      }


    public static void main(String[] args) throws IOException {
      // String url = "jdbc:mysql://localhost:3306/university";
      // String username = "root";
      // String password = "password";
      // System.out.print("Please input your arguments like so: \n " +
      // "static String databaseName = StudentDatabase " +
      // "static String url = jdbc:mysql://localhost:3306/ + databaseName;" +
      // "static String username = root; " +
      // "static String password = ;)");

      // for (int i = 0; i< args.length; i++){
      //   System.out.println(args[i]);
      // }
    // static String databaseName = "StudentDatabase";
    // static String url = "jdbc:mysql://localhost:3306/" + databaseName;
    // static String username = "root";
    // static String password = "";

      String url = args[0];
      String username = args[1];
      String password = args[2];
      // String databaseName = args[3];
      
      System.out.print("Welcome to the University Database. Queries Available: ");
      System.out.print("1. Search students by name ");
      System.out.print("2. Search Students by year ");
      System.out.print("3.Search for students with GPA >= threshold ");
      System.out.print("4.Search for students with a GPA <= threshold ");
      System.out.print("5.Get department statistics ");
      System.out.print("6. Get class statistics ");
      System.out.print("7. Get Statistics from Credit Amount: ");
      System.out.print("8.Exit the Application ");
      System.out.print("Which Query would you like to run(1-8)? ");


      
      try (Connection connection = DriverManager.getConnection(url, username, password)) {
        System.out.println("Connected to the database");
      //   createTables(connection);
      //   populateData(connection);

      try (Scanner scanner = new Scanner(System.in)) {
        int userPick = -1;

        do {
          System.out.println("Please choose a function to call (1-8), or enter 0 to exit:");
          // System.out.println("1. Function 1");
          // System.out.println("2. Function 2");
          // System.out.println("3. Function 3");
          // System.out.println("4. Function 4");
          // System.out.println("5. Function 5");
          // System.out.println("6. Function 6");
          // System.out.println("7. Function 7");
          // System.out.println("8. Function 8");

          userPick = scanner.nextInt();
          // System.out.print("Welcome to the University Database. Queries Available: ");
          // System.out.print("1. Search students by name ");
          // System.out.print("2. Search Students by year ");
          // System.out.print("3.Search for students with GPA >= threshold ");
          // System.out.print("4.Search for students with a GPA <= threshold ");
          // System.out.print("5.Get department statistics ");
          // System.out.print("6. Get class statistics ");
          // System.out.print("7. Get Statistics from Credit Amount: ");
          // System.out.print("8.Exit the Application ");
          // System.out.print("Which Query would you like to run(1-8)? ");
        

          InputStreamReader input = new InputStreamReader(System.in);
          BufferedReader reader = new BufferedReader(input);

          switch (userPick) {
                case 1:
                    System.out.print("Please enter what name you'd like to search ");
                    String name = reader.readLine();
                    
                    searchStudentsByName(connection, name);
                    // input.close();
                    // scanner.close();
                    break;

                case 2:
                    // System.out.print("Please enter what year would you like to serach for(fr, so, jr, sr)? ");
                    // String name2 = reader.readLine();
                    System.out.print("Enter a year level (freshman, sophomore, junior, senior): ");
                    String yearlevel = reader.readLine();
                    searchStudentsByYear(connection, yearlevel);
                    // scanner.close();
                    break;
                case 3:
                    // Scanner input = new Scanner(System.in);
                    System.out.print("Search for students with a GPA >= threshold(give a float i.e. 3.2) ");
                    String gpa = reader.readLine();
                    // String name3 = reader.readLine();  
                    // double num = scanner.nextDouble();
                    
                    searchStudentsByGpa1(connection, ">=", gpa);
                    // searchStudentsByGpa(connection, name3, ">=");

                    break;
                case 4:
                    System.out.print("Search for students with a GPA <= threshold(give a float i.e. 3.2) ");
                    String gpa4 = reader.readLine();
                    // double num2 = scanner.nextDouble();
                    // System.out.print("Pick a GPA threshold: ");
                    searchStudentsByGpa2(connection, "<=", gpa4);
                    break;
                case 5: 
                    System.out.print("Pick a Department: ");
                    String ddname = reader.readLine();
                    searchByDepartment(connection, ddname);
                    break;
                case 6: //*** # of students in each department and their collective average
                  System.out.print("Search for a given Class: ");
                  String cname = reader.readLine();
                  searchByClass(connection, cname); 
                  break;

                case 7:
                  System.out.print("Please pick the amount of credits (=<) and return the Student and GPA: ");
                  String credits = reader.readLine();
                  searchByCredits(connection, credits);  
                  break;
                case 8:
                    System.out.print("All over");
                    scanner.close();
                    break;
                default:
                    System.out.println("Invalid input");
                    // return;
            }
        } while(userPick !=0);
        }

        } catch (SQLException e) {
          System.out.println("Error connecting to the database: " + e.getMessage());
        }
      }
      // scanner.close();
    }