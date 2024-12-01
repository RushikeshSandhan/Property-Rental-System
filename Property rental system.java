import java.sql.*; 
import java.util.Scanner; 
public class HomeRentalSystem { 
// Database connection details 
private static final String DB_URL = "jdbc:mysql://localhost:3306/HomeRental"; 
private static final String USER = "root"; 
private static final String PASSWORD = "Samurana@4554"; 
public static void main(String[] args) { 
try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) { 
System.out.println("Connected to the database!"); 
Scanner scanner = new Scanner(System.in); 
int choice; 
do { 
System.out.println("\n--- Home Rental System ---"); 
System.out.println("1. Add User"); 
System.out.println("2. Add Home Listing"); 
System.out.println("3. View Available Homes"); 
System.out.println("4. Rent a Home"); 
System.out.println("5. View Rentals"); 
System.out.println("6. Exit"); 
System.out.print("Enter your choice: "); 
choice = scanner.nextInt(); 
 
 
                scanner.nextLine(); // Consume newline 
 
                switch (choice) { 
                    case 1: 
                        addUser(connection, scanner); 
                        break; 
                    case 2: 
                        addHomeListing(connection, scanner); 
                        break; 
                    case 3: 
                        viewHomes(connection); 
                        break; 
                    case 4: 
                        rentHome(connection, scanner); 
                        break; 
                    case 5: 
                        viewRentals(connection); 
                        break; 
                    case 6: 
                        System.out.println("Exiting..."); 
                        break; 
                    default: 
                        System.out.println("Invalid choice. Please try again."); 
                } 
            } while (choice != 6); 
 
        } catch (SQLException e) { 
            e.printStackTrace(); 
} 
} 
// Add a user (Landlord or Tenant) 
private static void addUser(Connection connection, Scanner scanner) throws SQLException { 
System.out.print("\nEnter Name: "); 
String name = scanner.nextLine(); 
System.out.print("Enter Email: "); 
String email = scanner.nextLine(); 
System.out.print("Enter User Type (Landlord/Tenant): "); 
String userType = scanner.nextLine(); 
String query = "INSERT INTO Users (Name, Email, UserType) VALUES (?, ?, ?)"; 
try (PreparedStatement pstmt = connection.prepareStatement(query)) { 
pstmt.setString(1, name); 
pstmt.setString(2, email); 
pstmt.setString(3, userType); 
pstmt.executeUpdate(); 
System.out.println("User added successfully!"); 
} 
} 
// Add a home listing 
private static void addHomeListing(Connection connection, Scanner scanner) throws SQLException { 
System.out.print("\nEnter Home Title: "); 
String title = scanner.nextLine(); 
System.out.print("Enter Description: "); 
String description = scanner.nextLine(); 
System.out.print("Enter Rent: "); 
double rent = scanner.nextDouble(); 
scanner.nextLine(); // Consume newline 
System.out.print("Enter Location: "); 
String location = scanner.nextLine(); 
System.out.print("Enter Landlord ID: "); 
int landlordID = scanner.nextInt(); 
scanner.nextLine(); // Consume newline 
// Check if the user is a Landlord 
String checkLandlordQuery = "SELECT * FROM Users WHERE UserID = ? AND UserType = 
'Landlord'"; 
try (PreparedStatement checkStmt = connection.prepareStatement(checkLandlordQuery)) { 
checkStmt.setInt(1, landlordID); 
try (ResultSet rs = checkStmt.executeQuery()) { 
if (!rs.next()) { 
System.out.println("Error: Landlord ID does not exist or is not a Landlord."); 
return; 
} 
} 
} 
String query = "INSERT INTO Homes (Title, Description, Rent, Location, LandlordID) VALUES 
(?, ?, ?, ?, ?)"; 
try (PreparedStatement pstmt = connection.prepareStatement(query)) { 
pstmt.setString(1, title); 
pstmt.setString(2, description); 
pstmt.setDouble(3, rent); 
pstmt.setString(4, location); 
pstmt.setInt(5, landlordID); 
pstmt.executeUpdate(); 
System.out.println("Home listing added successfully!"); 
} 
} 
// View all available homes 
private static void viewHomes(Connection connection) throws SQLException { 
String query = "SELECT * FROM Homes WHERE HomeID NOT IN (SELECT HomeID FROM 
Rentals)"; 
try (Statement stmt = connection.createStatement(); 
ResultSet rs = stmt.executeQuery(query)) { 
System.out.println("\n--- Available Homes ---"); 
while (rs.next()) { 
System.out.printf("ID: %d, Title: %s, Description: %s, Rent: %.2f, Location: %s, 
LandlordID: %d\n", 
rs.getInt("HomeID"), rs.getString("Title"), rs.getString("Description"), 
rs.getDouble("Rent"), rs.getString("Location"), rs.getInt("LandlordID")); 
} 
} 
} 
// Rent a home 
private static void rentHome(Connection connection, Scanner scanner) throws SQLException { 
System.out.print("\nEnter Home ID: "); 
int homeID = scanner.nextInt(); 
System.out.print("Enter Tenant ID: "); 
int tenantID = scanner.nextInt(); 
scanner.nextLine(); // Consume newline 
System.out.print("Enter Start Date (YYYY-MM-DD): "); 
String startDate = scanner.nextLine(); 
// Check if the home is available and if the tenant exists 
String checkHomeQuery = "SELECT * FROM Homes WHERE HomeID = ? AND HomeID NOT IN 
(SELECT HomeID FROM Rentals)"; 
String checkTenantQuery = "SELECT * FROM Users WHERE UserID = ? AND UserType = 
'Tenant'"; 
try (PreparedStatement checkHomeStmt = connection.prepareStatement(checkHomeQuery); 
PreparedStatement checkTenantStmt = connection.prepareStatement(checkTenantQuery)) { 
checkHomeStmt.setInt(1, homeID); 
checkTenantStmt.setInt(1, tenantID); 
try (ResultSet homeRs = checkHomeStmt.executeQuery(); 
ResultSet tenantRs = checkTenantStmt.executeQuery()) { 
if (!homeRs.next()) { 
System.out.println("Error: Home ID does not exist or is already rented."); 
return; 
} 
if (!tenantRs.next()) { 
System.out.println("Error: Tenant ID does not exist."); 
return; 
} 
} 
} 
String query = "INSERT INTO Rentals (HomeID, TenantID, StartDate) VALUES (?, ?, ?)"; 
try (PreparedStatement pstmt = connection.prepareStatement(query)) { 
pstmt.setInt(1, homeID); 
pstmt.setInt(2, tenantID); 
pstmt.setDate(3, Date.valueOf(startDate)); 
pstmt.executeUpdate(); 
System.out.println("Home rented successfully!"); 
} 
} 
// View all rentals 
private static void viewRentals(Connection connection) throws SQLException { 
String query = "SELECT r.RentalID, h.Title AS HomeTitle, u.Name AS TenantName, r.StartDate, 
r.EndDate " + 
"FROM Rentals r " + 
"JOIN Homes h ON r.HomeID = h.HomeID " + 
"JOIN Users u ON r.TenantID = u.UserID"; 
try (Statement stmt = connection.createStatement(); 
ResultSet rs = stmt.executeQuery(query)) { 
System.out.println("\n--- Rentals ---"); 
while (rs.next()) { 
System.out.printf("Rental ID: %d, Home: %s, Tenant: %s, Start Date: %s, End Date: %s\n", 
rs.getInt("RentalID"), rs.getString("HomeTitle"), 
rs.getString("TenantName"), rs.getDate("StartDate"), 
rs.getDate("EndDate") == null ? "Ongoing" : rs.getDate("EndDate")); 
} 
} 
} 
}