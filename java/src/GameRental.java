/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

import java.util.Arrays;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class GameRental {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of GameRental store
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public GameRental(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end GameRental

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            GameRental.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      GameRental esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the GameRental object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new GameRental (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println();
            System.out.println(
                              "\n\n -----------\n" +
                              "| MAIN MENU |\n" +
                              " -----------\n");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            System.out.println();
            String authorizedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorizedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorizedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println();
                System.out.println(
                                    "\n\n ---------\n" +
                                    "| OPTIONS |\n" +
                                    " ---------\n");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Catalog");
                System.out.println("4. Place Rental Order");
                System.out.println("5. View Full Rental Order History");
                System.out.println("6. View Past 5 Rental Orders");
                System.out.println("7. View Rental Order Information");
                System.out.println("8. View Tracking Information");

                //the following functionalities basically used by employees & managers
                System.out.println("9. Update Tracking Information");

                //the following functionalities basically used by managers
                System.out.println("10. Update Catalog");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");
                System.out.println();

                switch (readChoice()){
                   case 1: viewProfile(esql, authorizedUser); break;
                   case 2: updateProfile(esql); break;
                   case 3: viewCatalog(esql); break;
                   case 4: placeOrder(esql); break;
                   case 5: viewAllOrders(esql, authorizedUser); break;
                   case 6: viewRecentOrders(esql, authorizedUser); break;
                   case 7: viewOrderInfo(esql); break;
                   case 8: viewTrackingInfo(esql); break;
                   case 9: updateTrackingInfo(esql); break;
                   case 10:
                     if (esql.getUserRole(authorizedUser).equals("manager")) {
                        updateCatalog(esql);
                     }
                     else {
                        System.out.println("Access denied: Only managers can update the catalog.");
                     }
                     break;
                   case 11: updateUser(esql); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice! Try again!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n****************************************\n" +
         "***          User Interface          ***\n" +
         "****************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(GameRental esql){
      try {
        System.out.print("\tInput user login: ");
        String login = in.readLine();
        System.out.print("\tInput user password: ");
        String password = in.readLine();
        boolean validRole = false;
         String role = ""; 
         while(!validRole){
            System.out.print("\tInput user role: ");
            role = in.readLine();
            if(role.equals("customer") || role.equals("employee") || role.equals("manager")) {
               validRole  = true; 
            }
            else {
               System.out.println("Input a valid role");
            }
        }
        boolean validPhone = false;
         String phoneNum = "";
         while(!validPhone){
            System.out.print("\tInput user phone number (in the format: +1-999-999-9999): ");
               phoneNum = in.readLine();
            if(phoneNum.length() == 15) { // Check for proper length
               validPhone  = true; 
            }
            else {
               System.out.println("Please input a phone number in the proper format");
            }
        }

        // Check if inputted login already exists
        String checkLogin = String.format("SELECT * FROM Users WHERE login = '%s'", login);
        int userNum = esql.executeQuery(checkLogin);
        if (userNum > 0) {
            System.out.println("User login already exists. Please choose a different login.");
            return;
        }

        String usersQuery = String.format("INSERT INTO Users (login, password, role, phoneNum) VALUES ('%s','%s','%s','%s')", login, password, role, phoneNum);

        esql.executeUpdate(usersQuery);
        System.out.println("User has been created!");
    } catch (Exception e) {
        System.err.println("User cannot be created");
    }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(GameRental esql){
      try {
        System.out.print("\tInput user login: ");
        String login = in.readLine();
        System.out.print("\tInput user password: ");
        String password = in.readLine();

         // Input validation in case users just click "Enter"
        if (login.isEmpty() || password.isEmpty()) {
            System.out.println();
            System.out.println("Login and password cannot be empty. Please try again.");
            System.out.println();
            return null;
        }

        String query = String.format("SELECT * FROM Users WHERE login = '%s' AND password = '%s'", login, password);
        int numUser = esql.executeQuery(query);
        if (numUser > 0) {
            System.out.println("User has been logged in!");
            return login;
        } else {
            System.out.println("Invalid login");
            return null;
        }


    } catch (Exception e) {
        System.err.println(e.getMessage());
        return null;
    }
   }//end

// Rest of the functions definition go in here

   public static void viewProfile(GameRental esql, String authorizedUser) {
      try {
         // Get role of logged-in user
         String userRole = esql.getUserRole(authorizedUser);

         String login = null;
         boolean validLogin = false;

         while (!validLogin) {
               System.out.print("\tInput user's login to view: ");
               login = in.readLine();

               // Check if inputted login exists in database
               String checkLoginQuery = String.format("SELECT COUNT(*) FROM Users WHERE login = '%s'", login);
               List<List<String>> result = esql.executeQueryAndReturnResult(checkLoginQuery);
               int count = Integer.parseInt(result.get(0).get(0));

               if (count > 0) {
                  validLogin = true;
               } else {
                  System.out.println("Invalid login. Please try again.");
               }
         }

         // Check if logged-in user is a Customer (Assuming that Employees and Managers can view anyones profile)
         if ( (!authorizedUser.equals(login))&&(userRole.equals("customer"))) {
               System.out.println("Access denied: Customers can only view their own profile.");
               return;
         }

         // Construct query to view the profile
         String usersQuery = String.format("SELECT * FROM Users WHERE login = '%s'", login);

         esql.executeQueryAndPrintResult(usersQuery);

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void updateProfile(GameRental esql) {
      try {
        System.out.print("\tInput user login: ");
        String login = in.readLine();

        boolean validPhone = false;
        String phoneNum = "";
        while(!validPhone){
            System.out.print("\tInput user phone number (in the format: +1-999-999-9999): ");
               phoneNum = in.readLine();
            if(phoneNum.length() == 15) { // Check for proper length
               validPhone = true; 
            }
            else {
               System.out.println("Please input a phone number in the proper format");
            }
         }

        System.out.print("\tInput new user password: ");
        String password = in.readLine();

        String usersQuery = String.format("UPDATE Users SET phoneNum = '%s', password = '%s' WHERE login = '%s'", phoneNum, password, login);
        esql.executeUpdate(usersQuery);
        System.out.println("Profile has been updated!");
    } catch (Exception e) {
        System.err.println(e.getMessage());
    }
   }

   public static void viewCatalog(GameRental esql) {
      try {
         // List of valid genres
         List<String> genres = Arrays.asList(
               "sports", "action", "racing", "role-playing", "adventure", 
               "simulation", "platform", "misc", "shooter", "puzzle", 
               "fighting", "strategy"
         );

         // Input validation for genres
         String genre = "";
         while (true) {
               System.out.print("\tInput genre (press 'Enter' if all genres are desired): ");
               genre = in.readLine().toLowerCase(); // remove case sensitivity for genres
               if (genre.isEmpty() || genres.contains(genre)) {
                  break;
               }
               else {
                  System.out.println("Invalid genre. Please input a valid genre or press 'Enter' for all genres.");
               }
         }

         System.out.print("\tInput minimum price (press 'Enter' if no limit): ");
         String minPrice = in.readLine();
         System.out.print("\tInput maximum price (press 'Enter' if no limit): ");
         String maxPrice = in.readLine();
         System.out.println();

         String catalogQuery = "SELECT * FROM Catalog";
         boolean valid = false;
         
         // If any option is provided, add the given condition
         if (!genre.isEmpty()) {
            catalogQuery += " WHERE LOWER(genre) = '" + genre + "'";
            valid = true;
         }
         
         if (!minPrice.isEmpty()) {
            if (!valid) {
               catalogQuery += " WHERE";
               valid = true;
            }
            else {
               catalogQuery += " AND";
            }
            catalogQuery += " price >= " + minPrice;
         }

         if (!maxPrice.isEmpty()) {
            if (!valid) {
              catalogQuery += " WHERE";
            }
            else {
               catalogQuery += " AND";
            }
            catalogQuery += " price <= " + maxPrice;
         }
         
         catalogQuery += " ORDER BY price";
         
         // Print query to check syntax issues
         System.out.println("Executing query: " + catalogQuery);
         
         esql.executeQueryAndPrintResult(catalogQuery);

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void placeOrder(GameRental esql) {
      try {
         System.out.print("\tInput user login: ");
         String login = in.readLine();

         System.out.print("\tInput number of games: ");
         int numOfGames = Integer.parseInt(in.readLine());

         double totalPrice = 0.0;
         String rentalOrderID = "RO" + System.currentTimeMillis();
         String orderTimestamp = "current_timestamp";
         String dueDate = "current_timestamp + interval '7 days'";

         // Collect + Store game details before inserting
         List<String> gameIDs = new ArrayList<>();
         List<Integer> unitsOrderedList = new ArrayList<>();
         List<Double> gamePrices = new ArrayList<>();

         for (int i = 0; i < numOfGames; i++) {
            System.out.print("\tInput game ID: ");
            String gameID = in.readLine();
            System.out.print("\tInput units ordered: ");
            int unitsOrdered = Integer.parseInt(in.readLine());

            String gameQuery = String.format("SELECT price FROM Catalog WHERE gameID = '%s'", gameID);
            List<List<String>> gameResult = esql.executeQueryAndReturnResult(gameQuery);
            double gamePrice = Double.parseDouble(gameResult.get(0).get(0));

            totalPrice += gamePrice * unitsOrdered;

            gameIDs.add(gameID);
            unitsOrderedList.add(unitsOrdered);
            gamePrices.add(gamePrice);
         }

         // Insert rental order into RentalOrder table
         String rentalOrderQuery = String.format("INSERT INTO RentalOrder (rentalOrderID, login, noOfGames, totalPrice, orderTimestamp, dueDate) " + "VALUES ('%s', '%s', %d, %f, %s, %s)", rentalOrderID, login, numOfGames, totalPrice, orderTimestamp, dueDate);

         esql.executeUpdate(rentalOrderQuery);

         // Insert each game into GamesInOrder table
         for (int i = 0; i < numOfGames; i++) {
               String gamesInOrderQuery = String.format("INSERT INTO GamesInOrder (rentalOrderID, gameID, unitsOrdered) " + "VALUES ('%s', '%s', %d)", rentalOrderID, gameIDs.get(i), unitsOrderedList.get(i));

               esql.executeUpdate(gamesInOrderQuery);
         }

         // Generate unique tracking ID
         String trackingID = "T" + System.currentTimeMillis();

         // Insert tracking information into TrackingInfo table
         String trackingQuery = String.format("INSERT INTO TrackingInfo (trackingID, rentalOrderID, status, currentLocation, courierName, additionalComments, lastUpdateDate) " + "VALUES ('%s', '%s', 'Processing', 'Warehouse', 'Default Courier', '', current_timestamp)", trackingID, rentalOrderID);
         esql.executeUpdate(trackingQuery);

         System.out.println("Order has been placed with Tracking ID: " + trackingID);

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewAllOrders(GameRental esql, String authorizedUser) {
      try {
         System.out.print("\tInput user login: ");
         String login = in.readLine();

         // Gets role of authorized user
         String userRole = esql.getUserRole(authorizedUser);

         // Checks if user is a customer and trying to view another user's orders
         if (userRole.equals("customer") && !login.equals(authorizedUser)) {
            System.out.println("Access denied: Customers can only view their own rental order history.");
            return;
         }

         String rentalQuery = String.format("SELECT * FROM RentalOrder WHERE login = '%s'", login);

         esql.executeQueryAndPrintResult(rentalQuery);

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewRecentOrders(GameRental esql, String authorizedUser) {
      try {
         System.out.print("\tInput user login: ");
         String login = in.readLine();

         // Gets role of authorized user
         String userRole = esql.getUserRole(authorizedUser);

         // Checks if user is a customer and trying to view another user's orders
         if (userRole.equals("customer") && !login.equals(authorizedUser)) {
            System.out.println("Access denied: Customers can only view their own recent rental orders.");
            return;
         }

         String rentalQuery = String.format("SELECT * FROM RentalOrder WHERE login = '%s' ORDER BY orderTimestamp DESC LIMIT 5", login);

         esql.executeQueryAndPrintResult(rentalQuery);

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewOrderInfo(GameRental esql) {
      try {
         System.out.print("\tInput rental order ID: ");
         String rentalOrderID = in.readLine();

         // Query to get rental order information along with trackingID
         String query = String.format(
               "SELECT R.*, T.trackingID " +
               "FROM RentalOrder R " +
               "LEFT JOIN TrackingInfo T ON R.rentalOrderID = T.rentalOrderID " +
               "WHERE R.rentalOrderID = '%s'", rentalOrderID);

         esql.executeQueryAndPrintResult(query);

         // Query to get games in the rental order
         String gameQuery = String.format("SELECT * FROM GamesInOrder WHERE rentalOrderID = '%s'", rentalOrderID);

         esql.executeQueryAndPrintResult(gameQuery);

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewTrackingInfo(GameRental esql) {
      try {
            System.out.print("\tInput rental order ID: ");
            String rentalOrderID = in.readLine();

            String trackingQuery = String.format("SELECT * FROM TrackingInfo WHERE rentalOrderID = '%s'", rentalOrderID);

            esql.executeQueryAndPrintResult(trackingQuery);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
   }

   public static void updateTrackingInfo(GameRental esql) {
      try {
            //ID
            System.out.print("\tInput tracking ID: ");
            String trackingID = in.readLine();

            //Changes
            System.out.print("\tInput new status: ");
            String status = in.readLine();
            System.out.print("\tInput new (current) location: ");
            String currentLocation = in.readLine();
            System.out.print("\tInput new courier name: ");
            String courierName = in.readLine();
            System.out.print("\tInput new additional comments: ");
            String additionalComments = in.readLine();

            String trackingQuery = String.format("UPDATE TrackingInfo SET status = '%s', currentLocation = '%s', courierName = '%s', additionalComments = '%s', lastUpdateDate = current_timestamp WHERE trackingID = '%s'", status, currentLocation, courierName, additionalComments, trackingID);

            esql.executeUpdate(trackingQuery);

            System.out.println("Tracking information has been updated!");

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
   }

   public static void updateCatalog(GameRental esql) {
      try {
            //ID
            System.out.print("\tInput game ID: ");
            String gameID = in.readLine();

            //Changes
            System.out.print("\tInput new game name: ");
            String gameName = in.readLine();
            System.out.print("\tInput new genre: ");
            String genre = in.readLine();
            System.out.print("\tInput new price: ");
            String price = in.readLine();
            System.out.print("\tInput new description: ");
            String description = in.readLine();
            System.out.print("\tInput new image URL: ");
            String imageURL = in.readLine();

            String catalogQuery = String.format("UPDATE Catalog SET gameName = '%s', genre = '%s', price = %s, description = '%s', imageURL = '%s' WHERE gameID = '%s'", gameName, genre, price, description, imageURL, gameID);

            esql.executeUpdate(catalogQuery);

            System.out.println("Catalog has been updated!");

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
   }

   public static void updateUser(GameRental esql) {
      try {
            //ID
            System.out.print("\tInput user login: ");
            String login = in.readLine();

            //Changes
            System.out.print("\tInput new role: ");
            String role = in.readLine();
            System.out.print("\tInput new phone number: ");
            String phoneNum = in.readLine();
            System.out.print("\tInput new number of overdue games: ");
            int numOverDueGames = Integer.parseInt(in.readLine());

            String usersQuery = String.format("UPDATE Users SET role = '%s', phoneNum = '%s', numOverDueGames = %d WHERE login = '%s'", role, phoneNum, numOverDueGames, login);

            esql.executeUpdate(usersQuery);

            System.out.println("User information has been updated!");

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
   }


   // Additional Helper Functions

   // Returns user's role (utilizes similar method to executeUpdate)
   public String getUserRole(String login) throws SQLException {
      String role = null;
      String query = String.format("SELECT role FROM Users WHERE login = '%s'", login);

      // Create Statement object
      Statement stmt = this._connection.createStatement();
      
      // Execute query instruction
      ResultSet rs = stmt.executeQuery(query);
      
      // Gets role from result set
      if (rs.next()) {
         role = rs.getString("role");
      }
      
      // close the statement
      stmt.close();
      
      return role;
   }
}//end GameRental