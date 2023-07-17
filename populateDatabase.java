import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class populateDatabase {

        static Connection connection = null;
        static String databaseName = "StudentDatabase";
        static String url = "jdbc:mysql://localhost:3306/" + databaseName;
        static String username = "root";
        static String password = "";
    
        public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(url, username, password);
    
            // Populate the student table with 100 random student names and 5-digit IDs
            Random rand = new Random();
            String[] firstNames = {"Alice", "Bob", "Charlie", "Dave", "Emily", "Frank", "Grace", "Harry", "Isaac", "James"};
            String[] lastNames = {"Adams", "Brown", "Clark", "Davis", "Evans", "Ford", "Garcia", "Hall", "Irwin", "Johnson"};
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO student (first_name, last_name, id) VALUES (?, ?, ?)");
            for (int i = 0; i < 100; i++) {
                String firstName = firstNames[rand.nextInt(firstNames.length)];
                String lastName = lastNames[rand.nextInt(lastNames.length)];
                int id = rand.nextInt(90000) + 10000; // random 5-digit ID between 10000 and 99999
                ps.setString(1, firstName);
                ps.setString(2, lastName);
                ps.setString(3, String.valueOf(id));
                ps.executeUpdate();
            }
            System.out.println("Student table populated");
        }
    
    }
    
