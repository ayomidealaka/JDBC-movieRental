package jdbcConnect;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

//import from JSch (Java Secure Channel) jar file - http://www.jcraft.com/jsch/


public class jdbcConnect {
    public  Session session;						// SSH tunnel session
    Connection connection;

    /**
     * Open SSH Tunnel to SSH server and forward the specified port on the local machine to the MySQL port on the MySQL server on the SSH server
     * @param sshUser SSH username
     * @param sshPassword SSH password
     * @param sshHost hostname or IP of SSH server
     * @param sshPort SSH port on SSH server
     * @param remoteHost hostname or IP of MySQL server on SSH server (from the perspective of the SSH Server)
     * @param localPort port on the local machine to be forwarded
     * @param remotePort MySQL port on remoteHost
     */
    private void openSSHTunnel( String sshUser, String sshPassword, String sshHost, int sshPort, String remoteHost, int localPort, int remotePort ){
        try{
            final JSch jsch = new JSch();							// create a new Java Secure Channel
            session = jsch.getSession( sshUser, sshHost, sshPort);	// get the tunnel
            session.setPassword(sshPassword );						// set the password for the tunnel

            final Properties config = new Properties();				// create a properties object
            config.put( "StrictHostKeyChecking", "no" );			// set some properties
            session.setConfig( config );							// set the properties object to the tunnel

            session.connect();										// open the tunnel
            System.out.println("\nSSH Connecting ***********************************************************************************************************************");
            System.out.println("Success: SSH tunnel open - you are connecting to "+sshHost+ "on port "+sshPort+ " with username " + sshUser);

            // set up port forwarding from a port on your local machine to a port on the MySQL server on the SSH server
            session.setPortForwardingL(localPort, remoteHost, remotePort);
            // output a list of the ports being forwarded

            System.out.println("Success: Port forwarded - You have forwared port "+ localPort + " on the local machine to port " + remotePort + " on " + remoteHost + " on " +sshHost);
        }
        catch(Exception e ){
            e.printStackTrace();
        }
    }

    /**
     * Close SSH tunnel to a remote server
     */
    private void closeSshTunnel(int localPort){
        try {
            // remove the port forwarding and output a status message
            System.out.println("\nSSH Connection Closing ******************************************************************************************************************");
            session.delPortForwardingL(localPort);
            System.out.println("Success: Port forwarding removed");
            // catch any exceptions
        } catch (JSchException e) {
            System.out.println("Error: port forwarding removal issue");
            e.printStackTrace();
        }
        // disconnect the SSH tunnel
        session.disconnect();
        System.out.println("Success: SSH tunnel closed\n");
    }

    /**
     * Open a connection with MySQL server. If there is an SSH Tunnel required it will open this too.
     */
    public void openConnection(String mysqlHost, int localPort, String mysqlDatabaseName, String mysqlUsername, String mysqlPassword){
        try{
            // create a new JDBC driver to facilitate the conversion of MySQL to java and vice versa
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            // connect to the MySQL database through the SSH tunnel you have created using the variable above
            String jdbcConnectionString = "jdbc:mysql://"+mysqlHost+":"+localPort+"/"+mysqlDatabaseName+"?user="+mysqlUsername+"&password="+mysqlPassword;
            System.out.println("\nMySQL Connecting *********************************************************************************************************************");
            System.out.println("JDBC connection string "+jdbcConnectionString);
            connection = DriverManager.getConnection(jdbcConnectionString);
            System.out.println("Connection:"+connection.toString());
            System.out.println("Success: MySQL connection open");

            // testing connection
            //testConnection();

        }
        // catch various exceptions and print error messages
        catch (SQLException e){
            System.err.println("> SQLException: " + e.getMessage());
            e.printStackTrace();
        }
        catch (InstantiationException e) {
            System.err.println("> InstantiationException: " + e.getMessage());
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            System.err.println("> IllegalAccessException: " + e.getMessage());
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            System.err.println("> ClassNotFoundException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        System.out.println("\nMySQL Connection Closing ****************************************************************************************************************");
        try {
            connection.close(); // close database connection
            System.out.println("Success: MySQL connection closed.");
        } catch (SQLException e) {
            System.out.println("Error: Could not close MySQL connection");
            System.err.println(e);
            e.printStackTrace();}
    }

    /**
     * Test the connection by printing out everything in the Customer table.
     */

    public void testConnection()
    {
        try {
            Statement st = connection.createStatement(); 							// create an SQL statement
            ResultSet rs = st.executeQuery("SELECT * from Employees");  // retrieve an SQL results set

            // output the results set to the user
            System.out.println("\nTesting connection.");
            System.out.println("\nTesting connection..");
            System.out.println("\nTesting connection...");
            System.out.println("\nTesting connection....");
            System.out.println("\nTesting connection.....");
            System.out.println("\nConnection Successful....");

            while (rs.next()){
                int CustomerID = rs.getInt("SSN");
                String Name = rs.getString("FirstName");
                String City = rs.getString("LastName");
                int Street = rs.getInt("Department");

                System.out.print(CustomerID + " ");
                System.out.print(Name + " ");
                System.out.print(City + " ");
                System.out.print(Street + " \n");

            }

            if (st != null) {
                st.close();		//close the SQL statement
            }
            if (rs != null){	//close the Result Set
                rs.close();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    public void testConnection2()
    {
		    	// output the results set to the user
		        System.out.println("\nTesting connection.");
		        System.out.println("\nTesting connection....");
		        System.out.println("\nTesting connection.......");
		       
		        System.out.println("\nTesting connection by printing Databases......");
        try {
            Statement st = connection.createStatement(); 							// create an SQL statement
            ResultSet rs = st.executeQuery("SHOW DATABASES;");  // retrieve an SQL results set

           while (rs.next()){
                String Database = rs.getString("Database");
                //String Name = rs.getString("FirstName");
               // String City = rs.getString("LastName");
                //int Street = rs.getInt("Department");

                System.out.println(Database + " ");
                //System.out.print(Name + " ");
               	//System.out.print(City + " ");
                //System.out.print(Street + " \n");

           }

            if (st != null) {
                st.close();		//close the SQL statement
            }
            if (rs != null){	//close the Result Set
                rs.close();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("");
        System.out.println("\nConnection Succesful!!!!");
        System.out.println("");
    }
    
    public void mainMenu()
    {
    	Scanner input = new Scanner(System.in);
        System.out.println(">>>>>>Welcome Employee<<<<<<");
        System.out.println(">>>>>>>>>>Main Menu<<<<<<<<<<");
        System.out.println("1. Register New Customer (Press 1)");
        System.out.println("2. Show Reservations (Press 2)");
        System.out.println("3. Make Reservations (Press 3)");
        System.out.println("4. Show Statistics (Press 4)");
        System.out.println("5. Exit (Press 5)");
        System.out.print("Enter Value: ");

        int choice = input.nextInt();
        
       /* while ((choice != 1) || (choice != 2) || (choice != 3)|| (choice != 4) || (choice != 5)){
            System.out.println("Incorrect Choice. Please enter either 1, 2, 3, 4 or 5"); //Displays this if users enters the wrong input.
            System.out.println(">>>>>>Welcome Employee<<<<<<"); //Re-prompts question until user  enters right input
            System.out.println(">>>>>>>>>>Main Menu<<<<<<<<<<");
            System.out.println("1. Register New Customer (Press 1)");
            System.out.println("2. Show Reservations (Press 2)");
            System.out.println("3. Make Reservations (Press 3)");
            System.out.println("4. Show Statistics (Press 4)");
            System.out.println("5. Exit (Press 5)");
            
            choice = input.nextInt();
            
            if ((choice == 1) || (choice == 2) || (choice == 3)|| (choice == 4) || (choice == 5)){
                break; //breaks loop if user enters right input
            }
        }*/
        
        switch (choice) {
            case 1: registerCustomer();
            break;
            case 2: viewReservations();
            break;
            case 3: makeReservations();
            break;
            case 4: showStatistics();
            break;
            case 5: System.out.println("Are you sure you want to exit ☹️ ?? \n" //Displays after user exits
            								+ "Yes (Press 1) \t \t No (Press 2)");
		            						
            							int exitChoice = input.nextInt();
		            if(exitChoice == 1) {
		            	System.out.println("GoodBye");
		            	break;
		            }if (exitChoice == 2) {
		            mainMenu();    
		            }     
        };
        }
    
    public void registerCustomer() //Registers New Customer
    {
        		System.out.println("\n*****Register New Customer*****");
        		System.out.println("\n>>>>>Existing Customers<<<<<");
        		customer();
            System.out.println("\tEnter Customer ID: ");
            Scanner input = new Scanner(System.in);
            int customerId = input.nextInt();
            input.nextLine();
            System.out.println("\tEnter Customer Name: ");
            String customerName = input.nextLine();
            System.out.println("\tEnter City: ");
            String customerCity = input.nextLine();
            System.out.println("\tEnter Street: ");
            String CustomerStreet = input.nextLine();
            System.out.println("\tEnter Address: ");
            String CustomerAddress = input.nextLine();
            char coma = '"'; 
        try {
            Statement st = connection.createStatement(); 	// create an SQL statement
            String Query = "INSERT INTO Customer(CustomerID, Name, City, Street, Address) VALUES('" + customerId + "', '" + customerName + "', '" + customerCity + "', '" +CustomerStreet + "', '" + CustomerAddress + "');";
            st.executeUpdate(Query); // retrieve an SQL results set

            // Message After inserting new record.
            System.out.println("\nCustomer Successfully Added!!!");
            System.out.println("Customer: " + customerId + "\t" + "Customer Name: " + customerName + "\n" );
            

            if (st != null) {
                st.close();		//close the SQL statement
            }
          //  if (rs != null){	//close the Result Set
             //   rs.close();
           // }

        }
        catch (SQLIntegrityConstraintViolationException e) {  //Catch Duplicate entry violation
        		System.out.println("Customer ID already already exists.");
        		registerCustomer();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("\nTo Register new customer. (Press 1)" + "\tTo return to Main Menu. (Press 2)");
        int choice = input.nextInt();
        
        switch (choice) {
        		case 1 :registerCustomer();
        		break;
        		case 2: mainMenu();
        }
    }
    
    public void customer() //Shows Customer table
    {
        try {
            Statement st = connection.createStatement(); 							// create an SQL statement
            ResultSet rs = st.executeQuery("select CustomerID, Name from Customer;"); // retrieve an SQL results set for customer table

            // output the results set to the user
        
            while (rs.next()){
                int CustomerID = rs.getInt("CustomerID");
                String Name = rs.getString("Name");
                

                System.out.print("Customer: " + CustomerID + " ");
                System.out.print("Name: " + Name + "\n");            

                
                System.out.println(" ");
            }

            if (st != null) {
                st.close();		//close the SQL statement
            }
            if (rs != null){	//close the Result Set
            		rs.close();
            }else if(rs == null){
            		System.out.println("\nTable is Empty");
            }

        
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void film() //Shows Customer table
    {
        try {
            Statement st = connection.createStatement(); 							// create an SQL statement
            ResultSet rs = st.executeQuery("SELECT * FROM Film"); // retrieve an SQL results set for film table

            // output the results set to the user
        
            while (rs.next()){
                int FilmID = rs.getInt("FilmID");
                String Title = rs.getString("Title");
                Double RentalPrice = rs.getDouble("RentalPrice");
                String Kind = rs.getString("Kind");

                System.out.print("FilmID: " + FilmID + " ");
                System.out.print("\tMovie Tile: " + Title + " ");
                System.out.print("\tRental Price: " + RentalPrice + " ");
                System.out.print("\tKind: " + Kind + " \n");

                
                System.out.println(" ");

            }
            if (st != null) {
                st.close();		//close the SQL statement
            }
            if (rs != null){	//close the Result Set
                rs.close();

        }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void reserved() //Shows Customer table
    {
        try {
            Statement st = connection.createStatement(); 	// create an SQL statement
            ResultSet rs = st.executeQuery("SELECT * FROM Reserved"); // retrieve an SQL results set for reserved table

            // output the results set to the user
        
            while (rs.next()){
                int CustomerID = rs.getInt("CustomerID");
                String FilmID = rs.getString("FilmID");
             
                System.out.print("Customer: " + CustomerID + " ");
                System.out.print("FilmID: " + FilmID + "\n");
               
                System.out.println(" ");

            }
            if (st != null) {
                st.close();		//close the SQL statement
            }
            if (rs != null){	//close the Result Set
                rs.close();
           

        }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    public void viewReservations() //Make new reservations
    {
        	    System.out.println("View Film Reservations");
        	    System.out.println(">>>>>>>>List of Customers<<<<<<<<");
        	    customer();
        	    System.out.println("*********************************");
        	    System.out.println("\tEnter Customer ID: ");
        	    Scanner input = new Scanner(System.in);
        	    int customerID = input.nextInt();
        	    char coma = '"';
        	    
            System.out.println("\n>>>List of films reserved for Customer " + customerID +"<<<" );

        try {
            Statement st = connection.createStatement(); 	// create an SQL statement
            ResultSet rs = st.executeQuery("SELECT Reserved.CustomerID, Film.FilmID, Film.Title, Film.RentalPrice, Film.Kind FROM Film,Reserved WHERE Film.FilmID = Reserved.CustomerID AND Reserved.CustomerID ="+ coma+customerID+coma + ';' ); // retrieve an SQL results set"

            // output the results set to the user
            // output the results set to the user
            
            while (rs.next()){
                int CustomerID = rs.getInt("CustomerID");
                String FilmID = rs.getString("FilmID");
                String Title = rs.getString("Title");
                String RentalPrice = rs.getString("RentalPrice");
                String Kind = rs.getString("Kind");
             
                System.out.print("Customer: " + CustomerID + "\t");
                System.out.print("FilmID: " + FilmID + "\t");
                System.out.print("Film Title: " + Title + "\t");
                System.out.print("Rental Price: " + RentalPrice + "\t");
                System.out.print("Kind: " + Kind + "\t");
               
                System.out.println(" ");

            System.out.println("\n");

            }
            if (st != null) {
                st.close();		//close the SQL statement
            }
            if (rs != null){	//close the Result Set
                rs.close();
            }

     
        } catch (SQLException e) {
            e.printStackTrace();
        }
        mainMenu();
    }
    
    public void makeReservations() //Make new reservations
    {
        	    System.out.println("Make a Film Reservation");
        	    System.out.println(">>>>>>>>List of Customers<<<<<<<<");
        	    customer();
        	    System.out.println(">>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<");
        	    System.out.println("\tEnter Customer ID: ");
        	    Scanner input = new Scanner(System.in);
        	    int customerID = input.nextInt();
        	    char coma = '"';
        	    System.out.println(">>>>>>>>>>List of Films<<<<<<<<<<");
        	    film();
        	    System.out.println(">>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<");
        	    System.out.println("\tEnter Film ID: ");
        	    int filmId = input.nextInt(); 
        	    System.out.println("You chose " + filmId + "\n" );
        	    

        try {
            Statement st1 = connection.createStatement(); 	// create an SQL statement
            st1.executeUpdate("INSERT INTO Reserved(customerID, FilmID ) VALUES(" + coma+customerID+coma + "," + coma+filmId+coma+");"); // SQL to inserts an input into Reserved table set"

	        
            System.out.println("Film Reservation Successful ");
            System.out.println("Customer: " + customerID + "\t" + "FilmID: " + filmId + "\n" );

            System.out.println("\n");
            
            
            
            if (st1 != null) {
	            st1.close();		//close the SQL statement    
	        }
	        //if (rs != null){	//close the Result Set if query returns null
	        		//rs.close();
	        //}   		
	     
       }
        catch (SQLIntegrityConstraintViolationException e) {  //Catch Duplicate entry violation
        		System.out.println("Reservation already exists for Customer.");
        		System.out.println("\nTo Make new reservation. (Press 1)" + "\tTo return to Main Menu. (Press 2)");
        	        int choice = input.nextInt();
        	        switch (choice) {
        	        		case 1 :makeReservations();
        	        		break;
        	        		case 2: mainMenu();
        	        }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("\nTo View new reservation. (Press 1)" + "\tTo return to Main Menu. (Press 2)");
        int choice = input.nextInt();
        
        switch (choice) {
        		case 1 : viewReservations();
        		break;
        		case 2: mainMenu();
        }
    }
        
    
   
    public void showStatistics() //Show Statistics
    {
    			Scanner input = new Scanner(System.in);
        	    System.out.println(">>>>>>>>View Film Statistics<<<<<<<<");
        	    System.out.println("1. Show Most Popular Movie. (Press 1)");
        	    System.out.println("2. Show Least Popular Movie. (Press 2)");
        	    System.out.println("3. Show  All Movie Reservations. (Press 3)");
        	    System.out.println("4. Show Total Amount of Customers. (Press 4)");
        	    System.out.println("5. Return to Main Menu. (Press 5)");
        	    
        	    int choice = input.nextInt();
        	    
        	   	switch (choice){
        	    	case 1: mostPopularMovie();
        	    		break;
        	    case 2:	leastPopularMovie();
        	    		break;
        	   	case 3:	allMovieReservations();
        	    		break;
        	    	case 4: totalCustomerAmount();
        	    		break;
        	    	case 5: mainMenu();
        	   	}
     }
        	        
    public void mostPopularMovie() //Shows Customer table
    {
		System.out.println(">>>>>>>>Most Popular Movie<<<<<<<<");
        try {
            Statement st = connection.createStatement(); 							// create an SQL statement
            ResultSet rs = st.executeQuery("SELECT Film.FilmID, Film.Title, Film.RentalPrice, Film.Kind FROM Film, Reserved,Customer where Film.FilmID = Reserved.FilmID and Customer.CustomerID = Reserved.CustomerID GROUP BY Reserved.FilmID order by count(Reserved.FilmID) desc limit 1;" + 
            		""); // retrieve an SQL results set for film table

            // output the results set to the user
        
            while (rs.next()){
                int FilmID = rs.getInt("Film.FilmID");
                String Title = rs.getString("Film.Title");
                Double RentalPrice = rs.getDouble("Film.RentalPrice");
                String Kind = rs.getString("Film.Kind");

                System.out.print("FilmID: " + FilmID + " ");
                System.out.print("Movie Tile: " + Title + " ");
                System.out.print("Rental Price: " + RentalPrice + " ");
                System.out.print("Kind: " + Kind + " \n");

                
                System.out.println(" ");

            }
            if (st != null) {
                st.close();		//close the SQL statement
            }
            if (rs != null){	//close the Result Set
                rs.close();

        }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        showStatistics();
    }
    
    public void leastPopularMovie() //Shows Customer table
    {
    			System.out.println(">>>>>>>>Least Popular Movie<<<<<<<<");
        try {
            Statement st = connection.createStatement(); 							// create an SQL statement
            ResultSet rs = st.executeQuery("SELECT Film.FilmID, Film.Title, Film.RentalPrice, Film.Kind FROM Film, Reserved,Customer where Film.FilmID = Reserved.FilmID and Customer.CustomerID = Reserved.CustomerID GROUP BY Reserved.FilmID order by count(Reserved.FilmID) asc limit 1;" + 
            		""); // retrieve an SQL results set for film table

            // output the results set to the user
        
            while (rs.next()){
                int FilmID = rs.getInt("Film.FilmID");
                String Title = rs.getString("Film.Title");
                Double RentalPrice = rs.getDouble("Film.RentalPrice");
                String Kind = rs.getString("Film.Kind");

                System.out.print("FilmID: " + FilmID + " ");
                System.out.print("Movie Tile: " + Title + " ");
                System.out.print("Rental Price: " + RentalPrice + " ");
                System.out.print("Kind: " + Kind + " \n");

                
                System.out.println(" ");

            }
            if (st != null) {
                st.close();		//close the SQL statement
            }
            if (rs != null){	//close the Result Set
                rs.close();

        }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        showStatistics();
    }

    public void allMovieReservations() //Shows Customer table
    {
		System.out.println(">>>>>>>>All Movie Reservations<<<<<<<<");
        try {
            Statement st = connection.createStatement(); 							// create an SQL statement
            ResultSet rs = st.executeQuery("SELECT Reserved.CustomerID, Film.FilmID, Film.Title, Film.RentalPrice, Film.Kind FROM Film,Reserved WHERE Film.FilmID = Reserved.FilmID;" ); // retrieve an SQL results set"

            // output the results set to the user
            // output the results set to the user
            
            while (rs.next()){
                int CustomerID = rs.getInt("CustomerID");
                String FilmID = rs.getString("FilmID");
                String Title = rs.getString("Title");
                String RentalPrice = rs.getString("RentalPrice");
                String Kind = rs.getString("Kind");
             
                System.out.print("Customer: " + CustomerID + "\t");
                System.out.print("FilmID: " + FilmID + "\t");
                System.out.print("Film Title: " + Title + "\t");
                System.out.print("Rental Price: " + RentalPrice + "\t");
                System.out.print("Kind: " + Kind + "\t");
               
                System.out.println(" ");

            System.out.println("\n");

            }
            if (st != null) {
                st.close();		//close the SQL statement
            }
            if (rs != null){	//close the Result Set
                rs.close();
            }

     
        } catch (SQLException e) {
            e.printStackTrace();
        }
        showStatistics();
    }
    
    public void totalCustomerAmount() //Shows Total amount of Customers
    {
			    System.out.println(">>>>>>>>Total Amount Of Customers<<<<<<<<");
			    customer();
			    System.out.println("*****************************************");
        try {
            Statement st = connection.createStatement(); 							// create an SQL statement
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Customer;"); // retrieve an SQL results set for customer table

            // output the results set to the user
        
            while (rs.next()){
                int count = rs.getInt("Count(*)");
                
                System.out.print("Total Amount of Customer is: " + count );
                System.out.println(" ");
            }

            if (st != null) {
                st.close();		//close the SQL statement
            }
            if (rs != null){	//close the Result Set
            		rs.close();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        showStatistics();
    }
    
    
    public static void main(String[] args) {
        System.out.println("Starting");
        String mysqlUsername = "s2992254";
        String mysqlPassword = "dessalin";
        String mysqlDatabaseName = "s2992254";
        String sshUsername = "s2992254";
        String sshPassword = "dessalin";
        String sshRemoteHost = "knuth.gcd.ie";
        int shhRemotePort = 22;
        int localPort = 3310;
        String mysqlHost="localhost";
        int remoteMySQLPort = 3306;

        jdbcConnect con = new jdbcConnect();

        con.openSSHTunnel(sshUsername, sshPassword, sshRemoteHost, shhRemotePort, mysqlHost, localPort, remoteMySQLPort);
        con.openConnection(mysqlHost, localPort, mysqlDatabaseName, mysqlUsername, mysqlPassword);
        
        con.testConnection2();
        
        Scanner input = new Scanner(System.in);
        System.out.println(">>>>>>Welcome Employee<<<<<<");
        System.out.println(">>>>>>>>>>Main Menu<<<<<<<<<<");
        System.out.println("1. Register New Customer (Press 1)");
        System.out.println("2. Show Reservations (Press 2)");
        System.out.println("3. Make Reservations (Press 3)");
        System.out.println("4. Show Statistics (Press 4)");
        System.out.println("5. Exit (Press 5)");
        System.out.print("Enter Value: ");
        int choice = input.nextInt();
        
       /* while ((choice != 1) || (choice != 2) || (choice != 3)|| (choice != 4) || (choice != 5)){
            System.out.println("Incorrect Choice. Please enter either 1, 2, 3, 4 or 5"); //Displays this if users enters the wrong input.
            System.out.println(">>>>>>Welcome Employee<<<<<<"); //Re-prompts question until user  enters right input
            System.out.println(">>>>>>>>>>Main Menu<<<<<<<<<<");
            System.out.println("1. Register New Customer (Press 1)");
            System.out.println("2. Show Reservations (Press 2)");
            System.out.println("3. Make Reservations (Press 3)");
            System.out.println("4. Show Statistics (Press 4)");
            System.out.println("5. Exit (Press 5)");
            
            choice = input.nextInt();
            
            if ((choice == 1) || (choice == 2) || (choice == 3)|| (choice == 4) || (choice == 5)){
                break; //breaks loop if user enters right input
            }
        }*/
        
        switch (choice) {
            case 1: con.registerCustomer();
            break;
            case 2: con.viewReservations();
            break;
            case 3: con.makeReservations();
            break;
            case 4: con.showStatistics();
            break;
            case 5: System.out.println("Are you sure you want to exit ☹️ ?? \n" //Displays after user exits
            								+ "Yes (Press 1) \t \t No (Press 2)");
		            						
            							int exitChoice = input.nextInt();
		            if(exitChoice == 1) {
		            	System.out.println("GoodBye");
		            	break;
		            }if (exitChoice == 2) {
		            con.mainMenu();
		            }     
        };

        con.closeConnection();
        con.closeSshTunnel(localPort);
    }

}

    