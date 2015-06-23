package org.mpi.vasco.txstore.scratchpad.rdbms.tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import org.mpi.vasco.txstore.scratchpad.ScratchpadConfig;
import org.mpi.vasco.txstore.scratchpad.rdbms.DBScratchpad;
import org.mpi.vasco.txstore.scratchpad.rdbms.jdbc.PassThroughProxy;
import org.mpi.vasco.txstore.scratchpad.rdbms.jdbc.TxMudDriver;
import org.mpi.vasco.txstore.scratchpad.rdbms.resolution.AllOpsLockExecution;
import org.mpi.vasco.util.debug.Debug;






public class TestTPCW
{
	static Connection con;
	public static void main( String[] args) {
		Vector <String> report = new Vector<String>();
		try {
			Debug.debug = true;

			//Scratchpad setup ------------------------------------------------
			ScratchpadConfig config = 
				new ScratchpadConfig( "com.mysql.jdbc.Driver", //backend
						"jdbc:mysql://localhost:53306/testtpcw", //url+database
						"sa", "", //username+password
				"txstore.scratchpad.rdbms.DBScratchpad"); //padclass
			DBScratchpad.prepareDBScratchpad(config);
			config.putPolicy("address", new AllOpsLockExecution(true));
			config.putPolicy("author", new AllOpsLockExecution(true));
			config.putPolicy("cc_xacts", new AllOpsLockExecution(true));
			config.putPolicy("country", new AllOpsLockExecution(true));
			config.putPolicy("customer", new AllOpsLockExecution(true));
			config.putPolicy("item", new AllOpsLockExecution(true));
			config.putPolicy("order_line", new AllOpsLockExecution(true));
			config.putPolicy("orders", new AllOpsLockExecution(true));
			config.putPolicy("shopping_cart", new AllOpsLockExecution(true));
			config.putPolicy("shopping_cart_line", new AllOpsLockExecution(true));
			PassThroughProxy.config = config;

			//Scratchpad initialize ------------------------------------------------
			Class.forName("txstore.scratchpad.rdbms.jdbc.TxMudDriver");
			con = DriverManager.getConnection( "jdbc:txmud:tpcwdb_mysql_scratchpad");
			con.setAutoCommit(false);
			Debug.println("INFO: scratchpad connected");

			String[] name = getName(2); 
			System.out.println((name[0].equals("P=YUYNaVUsF") && name[1].equals("]qkI(kn Lr@S"))?"=========> method getName - OK":"=========> method getName - Failed");
//
			Book book = getBook(2); 
			System.out.println( book != null ? "=========> method getBook - OK: "+book.i_title :"=========> method getBook - Failed");
			System.out.println("Book date:"+book.i_pub_Date);

			Customer customer = getCustomer("OG"); 
			System.out.println( customer != null ?"=========> method getCustomer - OK":"=========> method getCustomer - Failed");
//
			Vector vSS = doSubjectSearch( "REFERENCE");
			System.out.println( vSS != null && vSS.size() > 0 ?"=========> method doSubjectSearch - OK":"=========> method method doSubjectSearch - Failed");		

			Vector vTS = doTitleSearch( "lBfmlSR;a%PSTdc}Bt|CTfJ)Pr=sAapp&p_E? NI=Go)kKhSOAe=LX");
			System.out.println( vTS != null && vTS.size() > 0 ?"=========> method doTitleSearch - OK":"=========> method method doTitleSearch - Failed");
//
			Vector vAS = doAuthorSearch( "yJp");
			System.out.println( vAS != null && vAS.size() > 0 ?"=========> method doAuthorSearch - OK":"=========> method method doAuthorSearch - Failed");
//
			Vector vGNP = getNewProducts( "REFERENCE");
			System.out.println( vGNP != null && vGNP.size() > 0 ?"=========> method getNewProducts - OK":"=========> method method getNewProducts - Failed");

			Vector vGBS = getBestSellers( "REFERENCE");
			System.out.println( vGBS != null && vGBS.size() > 0 ?"=========> method getBestSeller - OK":"=========> method method getBestSeller - Failed");
//
			String sGUN = GetUserName( 1);
			System.out.println( sGUN != null ?"=========> method getUserName - OK":"=========> method method getUserName - Failed");
//
			String sGP = GetPassword( "OG");
			System.out.println( sGP != null ?"=========> method getPassword - OK":"=========> method method getPassword - Failed");
//
			Vector vGMRO = new Vector();
			GetMostRecentOrder( "ALINALBAUL", vGMRO);
			System.out.println( vGMRO != null && vGMRO.size() > 0 ?"=========> method getMostRecentOrder - OK":"=========> method method getMostRecentOrder - Failed");
//
////
			Vector vGR = new Vector();
			Vector vGRT = new Vector();
			getRelated(1,vGR,vGRT);
			System.out.println( vGR != null && vGR.size() ==5  && (vGR.get(0).equals(new Integer(77))  && vGRT.get(0).equals("img77/thumb_77.gif")) ?"=========> method getRelated - OK":"=========> method method getRelated - Failed");
//			
////
//			
			

//test cart 
			int shopping_id = createEmptyCart();
			System.out.println( shopping_id >= 0 ?"=========> method createEmptyCart - OK":"=========> method method createEmptyCart - Failed");
//			
			System.out.println("Starting doCart method ----------------------------------------------------------");
			Vector quantities = new Vector();
			Vector ids = new Vector();
			ids.addElement("0");
			quantities.addElement("1");
			Cart mycart = doCart(shopping_id, new Integer(2), ids, quantities); //Book book = getBook(2); I_ID=2
			System.out.println( mycart != null && mycart.lines.size()==2 ?"=========> method doCart - OK":"=========> method method doCart - Failed");
////
			System.out.println("Starting getCart method ----------------------------------------------------------");
			Cart mycart2 = getCart(shopping_id, 10);
			System.out.println( mycart2 != null && mycart2.lines.size()==2 ?"=========> method getCart - OK":"=========> method method getCart - Failed");
////
			System.out.println("Starting refresh method ----------------------------------------------------------");
			refreshSession(2);
//
//			
			System.out.println("Starting createNewCustomer method ----------------------------------------------------------");
			Random r = new Random();
			Customer c = new Customer();
			Date now = new Date(System.currentTimeMillis());
			c.c_id = r.nextInt()+100000000;
			c.c_uname = "daniel";			c.c_passwd = "pass";				c.c_fname = "porto";
			c.c_lname = "lname";			c.c_phone = "91574335";				c.c_email = "daniel@mpi-sws.org";
			c.c_since = now;				c.c_last_visit = now;				c.c_login = now;
			c.c_expiration = now;			c.c_discount = 0.0;					c.c_balance = 0.0;
			c.c_ytd_pmt = 0.0;				c.c_birthdate = now;				c.c_data = now.toString();
			c.addr_id = 1;					c.addr_street1 = "Mainzerstrasse";	c.addr_street2 = "Mainzerstrasse";
			c.addr_city = "Saarbrucken";	c.addr_state = "Saarland";			c.addr_zip = "66111";
			c.addr_co_id = 4;		c.co_name = "Germany";			
			createNewCustomer(c);
////			
////			
			doBuyConfirm(shopping_id, 2, "VISA",328941017, "F|_&aEwSui|;A)gy!.G@}Z)/", new Date((long) (System.currentTimeMillis()+2*12*24*60.60*1000)), "my home");
//			doBuyConfirm
//			getCDiscount --ok doBuyConfirm
//			getCAddrID --enterOrder ok
//			getCAddr --ok doBuyConfirm
//			enterCCXact--ok doBuyConfirm
//			clearCart--ok doBuyConfirm
//			enterAddress--ok doBuyConfirm
//			enterOrder--ok doBuyConfirm
//			addOrderLine -- enterOrder ok
//			getStock --enterOrder ok
//			setStock --enterOrder ok
//			vertifyDBConsistency
			
//			adminUpdate( 1, 10.0, "nuno", "daniel");
//			System.out.println( "=========> method adminUpdate - OK");


			System.out.println( "Test TPCW completed with success");

		} catch( Exception e) {
			e.printStackTrace();
		}
	}


	//--------------------------------------------------------------------------------
	//TPCW methods

	// Get a connection from the pool.
	public static synchronized Connection getConnection() {
		return(con);
	}

	// Return a connection to the pool.
	public static synchronized void returnConnection(Connection con){
		;
	}

	//nmp: ok
	public static String[] getName(int c_id) {
		String name[] = new String[2];
		try {

			Connection con = getConnection();
			//	    out.println("About to preparestatement!");
			//            out.flush();
			PreparedStatement get_name = con.prepareStatement
			("SELECT c_fname,c_lname FROM customer WHERE c_id = ?");

			// Set parameter
			get_name.setInt(1, c_id);
			// 	    out.println("About to execute query!");
			//            out.flush();

			ResultSet rs = get_name.executeQuery();

			// Results
			rs.next();
			name[0] = rs.getString("c_fname");
			name[1] = rs.getString("c_lname");
			rs.close();
			get_name.close();
			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return name;



	}

	//nmp: ok (required getXXX name->position in Book)
	public static Book getBook(int i_id) {
		Book book = null;
		try {
			// Prepare SQL
			Connection con = getConnection();
			PreparedStatement statement = con.prepareStatement
			("SELECT * FROM item,author WHERE item.i_a_id = author.a_id AND i_id = ?");

			// Set parameter
			statement.setInt(1, i_id);
			ResultSet rs = statement.executeQuery();

			// Results
			rs.next();
			book = new Book(rs);
			rs.close();
			statement.close();
			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return book;
	}

	//nmp: ok (required getXXX name->position in Customer)
	public static Customer getCustomer(String UNAME){
		Customer cust = null;
		try {
			// Prepare SQL
			Connection con = getConnection();
			PreparedStatement statement = con.prepareStatement
			("SELECT * FROM customer, address, country WHERE customer.c_addr_id = address.addr_id AND address.addr_co_id = country.co_id AND customer.c_uname = ?");

			// Set parameter
			statement.setString(1, UNAME);
			ResultSet rs = statement.executeQuery();

			// Results
			if(rs.next())
				cust = new Customer(rs);
			else {
				System.err.println("ERROR: NULL returned in getCustomer!");
				rs.close();
				statement.close();
				returnConnection(con);
				return null;
			}

			statement.close();
			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return cust;
	}

	//nmp: ok (required getXXX name->position in Book)
	public static Vector doSubjectSearch(String search_key) {
		Vector vec = new Vector();
		try {
			// Prepare SQL
			Connection con = getConnection();
			PreparedStatement statement = con.prepareStatement
			("SELECT * FROM item, author WHERE item.i_a_id = author.a_id AND item.i_subject = ? ORDER BY item.i_title limit 50");

			// Set parameter
			statement.setString(1, search_key);
			ResultSet rs = statement.executeQuery();

			// Results
			while(rs.next()) {
				vec.addElement(new Book(rs));
			}
			rs.close();
			statement.close();
			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return vec;	
	}

	//nmp: ok (required getXXX name->position in Book)
	public static Vector doTitleSearch(String search_key) {
		Vector vec = new Vector();
		try {
			// Prepare SQL
			Connection con = getConnection();
			PreparedStatement statement = con.prepareStatement
			("SELECT * FROM item, author WHERE item.i_a_id = author.a_id AND substring(soundex(item.i_title),0,4)=substring(soundex(?),0,4) ORDER BY item.i_title limit 50");

			// Set parameter
			statement.setString(1, search_key+"%");
			ResultSet rs = statement.executeQuery();

			// Results
			while(rs.next()) {
				vec.addElement(new Book(rs));
			}
			rs.close();
			statement.close();
			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return vec;	
	}

	//nmp: ok (required getXXX name->position in Book)
	public static Vector doAuthorSearch(String search_key) {
		Vector vec = new Vector();
		try {
			// Prepare SQL
			Connection con = getConnection();
			PreparedStatement statement = con.prepareStatement
			("SELECT * FROM author, item WHERE substring(soundex(author.a_lname),0,4)=substring(soundex(?),0,4) AND item.i_a_id = author.a_id ORDER BY item.i_title limit 50");

			// Set parameter
			statement.setString(1, search_key+"%");
			ResultSet rs = statement.executeQuery();

			// Results
			while(rs.next()) {
				vec.addElement(new Book(rs));
			}
			rs.close();
			statement.close();
			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return vec;	
	}

	//nmp: ok (required getXXX name->position in Book)
	public static Vector getNewProducts(String subject) {
		Vector vec = new Vector();  // Vector of Books
		try {
			// Prepare SQL
			Connection con = getConnection();
			PreparedStatement statement = con.prepareStatement
			("SELECT i_id, i_title, a_fname, a_lname " +"FROM item, author " +"WHERE item.i_a_id = author.a_id " +"AND item.i_subject = ? " +"ORDER BY item.i_pub_date DESC,item.i_title " +"limit 50");

			// Set parameter
			statement.setString(1, subject);
			ResultSet rs = statement.executeQuery();

			// Results
			while(rs.next()) {
				vec.addElement(new ShortBook(rs));
			}
			rs.close();
			statement.close();
			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return vec;	
	}

	//nmp: ok (required getXXX name->position in Book)
	public static Vector getBestSellers(String subject) {
		Vector vec = new Vector();  // Vector of Books
		try {
			System.err.println("--------------------------------------------------------------------------------");
			System.err.println("getRelated method. sql: "+"SELECT i_id, i_title, a_fname, a_lname " +"FROM item, author, order_line " +"WHERE item.i_id = order_line.ol_i_id " +"AND item.i_a_id = author.a_id " +"AND order_line.ol_o_id > (SELECT MAX(o_id)-3333 FROM orders) " +"AND item.i_subject = ? " +"GROUP BY i_id, i_title, a_fname, a_lname " +"ORDER BY SUM(ol_qty) DESC " +"limit 50" +" prepare statement: \n\n");
			// Prepare SQL
			Connection con = getConnection();
			//The following is the original, unoptimized best sellers query.
			PreparedStatement statement = con.prepareStatement
			("SELECT i_id, i_title, a_fname, a_lname " +"FROM item, author, order_line " +"WHERE item.i_id = order_line.ol_i_id " +"AND item.i_a_id = author.a_id " +"AND order_line.ol_o_id > (SELECT MAX(o_id)-3333 FROM orders) " +"AND item.i_subject = ? " +"GROUP BY i_id, i_title, a_fname, a_lname " +"ORDER BY SUM(ol_qty) DESC " +"limit 50");
			//This is Mikko's optimized version, which depends on the fact that
			//A table named "bestseller" has been created.
			/*PreparedStatement statement = con.prepareStatement
		("SELECT bestseller.i_id, i_title, a_fname, a_lname, ol_qty " + 
		 "FROM item, bestseller, author WHERE item.i_subject = ?" +
		 " AND item.i_id = bestseller.i_id AND item.i_a_id = author.a_id " + 
		 " ORDER BY ol_qty DESC FETCH FIRST 50 ROWS ONLY");*/

			// Set parameter
			statement.setString(1, subject);
			ResultSet rs = statement.executeQuery();

			// Results
			while(rs.next()) {
				vec.addElement(new ShortBook(rs));
			}
			rs.close();
			statement.close();
			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return vec;	
	}

	//nmp: ok
	public static void getRelated(int i_id, Vector i_id_vec, Vector i_thumbnail_vec) {
		try {
			System.err.println("--------------------------------------------------------------------------------");
			System.err.println("getRelated method prepare statement sql: "+"SELECT J.i_id,J.i_thumbnail from item I, item J where (I.i_related1 = J.i_id or I.i_related2 = J.i_id or I.i_related3 = J.i_id or I.i_related4 = J.i_id or I.i_related5 = J.i_id) and I.i_id = ?"+"\n");

			// Prepare SQL
			System.err.println("\n request connection from the connection pool");
			Connection con = getConnection();
			PreparedStatement statement = con.prepareStatement
			("SELECT J.i_id,J.i_thumbnail from item I, item J where (I.i_related1 = J.i_id or I.i_related2 = J.i_id or I.i_related3 = J.i_id or I.i_related4 = J.i_id or I.i_related5 = J.i_id) and I.i_id = ?");

			// Set parameter
			statement.setInt(1, i_id);
			ResultSet rs = statement.executeQuery();

			// Clear the vectors
			i_id_vec.removeAllElements();
			i_thumbnail_vec.removeAllElements();

			// Results
			while(rs.next()) {
				i_id_vec.addElement(new Integer(rs.getInt(1)));
				i_thumbnail_vec.addElement(rs.getString(2));
			}
			rs.close();
			statement.close();
			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}

	}

	//nmp: ok
	public static void adminUpdate(int i_id, double cost, String image, String thumbnail) {
		try {
			// Prepare SQL
			Connection con = getConnection();
			PreparedStatement statement = con.prepareStatement
			("UPDATE item SET i_cost = ?, i_image = ?, i_thumbnail = ?, i_pub_date = CURRENT_DATE WHERE i_id = ?");

			// Set parameter
			statement.setDouble(1, cost);
			statement.setString(2, image);
			statement.setString(3, thumbnail);
			statement.setInt(4, i_id);
			statement.executeUpdate();
			statement.close();
			PreparedStatement related = con.prepareStatement
			("SELECT ol_i_id " +"FROM orders, order_line " + "WHERE orders.o_id = order_line.ol_o_id " +"AND NOT (order_line.ol_i_id = ?) " 
					+"AND orders.o_c_id IN (SELECT o_c_id " +"  FROM orders, order_line " 
					+" WHERE orders.o_id = order_line.ol_o_id " 
					+" AND orders.o_id > (SELECT MAX(o_id)-10000 FROM orders)" 
					+" AND order_line.ol_i_id = ?) " 
					+"GROUP BY ol_i_id " 
					+"ORDER BY SUM(ol_qty) DESC " +"limit 5");

			// Set parameter
			related.setInt(1, i_id);	
			related.setInt(2, i_id);
			ResultSet rs = related.executeQuery();

			int[] related_items = new int[5];
			// Results
			int counter = 0;
			int last = 0;
			while(rs.next()) {
				last = rs.getInt(1);
				related_items[counter] = last;
				counter++;
			}

			// This is the case for the situation where there are not 5 related books.
			for (int i=counter; i<5; i++) {
				last++;
				related_items[i] = last;
			}
			rs.close();
			related.close();

			{
				// Prepare SQL
				statement = con.prepareStatement
				("UPDATE item SET i_related1 = ?, i_related2 = ?, i_related3 = ?, i_related4 = ?, i_related5 = ? WHERE i_id = ?");

				// Set parameter
				statement.setInt(1, related_items[0]);
				statement.setInt(2, related_items[1]);
				statement.setInt(3, related_items[2]);
				statement.setInt(4, related_items[3]);
				statement.setInt(5, related_items[4]);
				statement.setInt(6, i_id);
				statement.executeUpdate();
			}
			statement.close();
			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}	
	}

	//nmp: ok (required getXXX name->position inside this method)
	public static String GetUserName(int C_ID){
		String u_name = null;
		try {
			// Prepare SQL
			Connection con = getConnection();
			PreparedStatement get_user_name = con.prepareStatement
			("SELECT c_uname FROM customer WHERE c_id = ?");

			// Set parameter
			get_user_name.setInt(1, C_ID);
			ResultSet rs = get_user_name.executeQuery();

			// Results
			rs.next();
			u_name = rs.getString("c_uname");
			rs.close();

			get_user_name.close();
			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return u_name;
	}

	//nmp: ok (required getXXX name->position inside this method)
	public static String GetPassword(String C_UNAME){
		String passwd = null;
		try {
			// Prepare SQL
			Connection con = getConnection();
			PreparedStatement get_passwd = con.prepareStatement
			("SELECT c_passwd FROM customer WHERE c_uname = ?");

			// Set parameter
			get_passwd.setString(1, C_UNAME);
			ResultSet rs = get_passwd.executeQuery();

			// Results
			rs.next();
			passwd = rs.getString("c_passwd");
			rs.close();

			get_passwd.close();
			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return passwd;
	}

	//This function gets the value of I_RELATED1 for the row of
	//the item table corresponding to I_ID
	private static int getRelated1(int I_ID, Connection con){
		int related1 = -1;
		try {
			PreparedStatement statement = con.prepareStatement
			("SELECT i_related1 FROM item where i_id = ?");
			statement.setInt(1, I_ID);
			ResultSet rs = statement.executeQuery();
			rs.next();
			related1 = rs.getInt(1);//Is 1 the correct index?
			rs.close();
			statement.close();

		}catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return related1;
	}

	//nmp: ok (required getXXX name->position inside this method + Order)
	public static Order GetMostRecentOrder(String c_uname, Vector order_lines){
		try {
			order_lines.removeAllElements();
			int order_id;
			Order order;

			// Prepare SQL
			Connection con = getConnection();

			//	    System.out.println("cust_id: " + getCustomer(c_uname).c_id);

			{
				// *** Get the o_id of the most recent order for this user
				PreparedStatement get_most_recent_order_id = con.prepareStatement
				("SELECT o_id " +"FROM customer, orders " +"WHERE customer.c_id = orders.o_c_id " +"AND c_uname = ? " +"ORDER BY o_date, orders.o_id DESC " +"limit 1");

				// Set parameter
				get_most_recent_order_id.setString(1, c_uname);
				ResultSet rs = get_most_recent_order_id.executeQuery();

				if (rs.next()) {
					order_id = rs.getInt("o_id");
				} else {
					// There is no most recent order
					rs.close();
					get_most_recent_order_id.close();
					con.commit();
					returnConnection(con);
					return null;
				}
				rs.close();
				get_most_recent_order_id.close();
			}

			{
				// *** Get the order info for this o_id
				PreparedStatement get_order = con.prepareStatement
				("SELECT orders.*, customer.*, " +"  cc_xacts.cx_type, " +"  ship.addr_street1 AS ship_addr_street1, " +"  ship.addr_street2 AS ship_addr_street2, " +"  ship.addr_state AS ship_addr_state, " +"  ship.addr_zip AS ship_addr_zip, " +"  ship_co.co_name AS ship_co_name, " +"  bill.addr_street1 AS bill_addr_street1, " +"  bill.addr_street2 AS bill_addr_street2, " +"  bill.addr_state AS bill_addr_state, " +"  bill.addr_zip AS bill_addr_zip, " +"  bill_co.co_name AS bill_co_name " +"FROM customer, orders, cc_xacts," +"  address AS ship, " +"  country AS ship_co, " +"  address AS bill,  " + "  country AS bill_co " +"WHERE orders.o_id = ? " +"  AND cx_o_id = orders.o_id " +"  AND customer.c_id = orders.o_c_id " +"  AND orders.o_bill_addr_id = bill.addr_id " +"  AND bill.addr_co_id = bill_co.co_id " +"  AND orders.o_ship_addr_id = ship.addr_id " +"  AND ship.addr_co_id = ship_co.co_id " +"  AND orders.o_c_id = customer.c_id");

				// Set parameter
				get_order.setInt(1, order_id);
				ResultSet rs2 = get_order.executeQuery();

				// Results
				if (!rs2.next()) {
					// FIXME - This case is due to an error due to a database population error
					con.commit();
					rs2.close();
					//		    get_order.close();
					returnConnection(con);
					return null;
				}
				order = new Order(rs2);
				rs2.close();
				get_order.close();
			}

			{
				// *** Get the order_lines for this o_id
				PreparedStatement get_order_lines = con.prepareStatement
				("SELECT * " + "FROM order_line, item " + "WHERE ol_o_id = ? " +"AND ol_i_id = i_id");

				// Set parameter
				get_order_lines.setInt(1, order_id);
				ResultSet rs3 = get_order_lines.executeQuery();

				// Results
				while(rs3.next()) {
					order_lines.addElement(new OrderLine(rs3));
				}
				rs3.close();
				get_order_lines.close();
			}

			con.commit();
			returnConnection(con);
			return order;
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	// ********************** Shopping Cart code below ************************* 

	// Called from: TPCW_shopping_cart_interaction 
	//nmp: ok (required getXXX name->position inside this method)
	public static int createEmptyCart(){
		int SHOPPING_ID = 0;
		//	boolean success = false;
		Connection con = null;
		try {
			con = getConnection();
		}
		catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}

		//while(success == false) {
		try {
			PreparedStatement get_next_id = con.prepareStatement
			("SELECT COUNT(*) FROM shopping_cart");
//			("SELECT MAX(sc_id) FROM shopping_cart");
			synchronized(Cart.class) {
				ResultSet rs = get_next_id.executeQuery();
				rs.next();
				SHOPPING_ID = rs.getInt(1);
				SHOPPING_ID = SHOPPING_ID - (SHOPPING_ID % TxMudDriver.NUMDATACENTERS) + TxMudDriver.NUMDATACENTERS + TxMudDriver.THISDATACENTER;
				rs.close();

				PreparedStatement insert_cart = con.prepareStatement
				("INSERT into shopping_cart (sc_id, sc_time) " + "VALUES (" + SHOPPING_ID +"," + "CURRENT_TIMESTAMP)");
				insert_cart.executeUpdate();
				get_next_id.close();
				con.commit();
			}
			returnConnection(con);
		}catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return SHOPPING_ID;
	}

	public static Cart doCart(int SHOPPING_ID, Integer I_ID, Vector ids, Vector quantities) {	
		Cart cart = null;
		try {
			Connection con = getConnection();

			if (I_ID != null) {
				addItem(con, SHOPPING_ID, I_ID.intValue()); 
			}
			refreshCart(con, SHOPPING_ID, ids, quantities);
			addRandomItemToCartIfNecessary(con, SHOPPING_ID);
			resetCartTime(con, SHOPPING_ID);
			cart = getCart(con, SHOPPING_ID, 0.0);

			// Close connection
			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return cart;
	}

	//This function finds the shopping cart item associated with SHOPPING_ID
	//and I_ID. If the item does not already exist, we create one with QTY=1,
	//otherwise we increment the quantity.

	private static void addItem(Connection con, int SHOPPING_ID, int I_ID){
		try {
			// Prepare SQL
			PreparedStatement find_entry = con.prepareStatement
			("SELECT scl_qty FROM shopping_cart_line WHERE scl_sc_id = ? AND scl_i_id = ?");

			// Set parameter
			find_entry.setInt(1, SHOPPING_ID);
			find_entry.setInt(2, I_ID);
			ResultSet rs = find_entry.executeQuery();

			// Results
			if(rs.next()) {
				//The shopping cart id, item pair were already in the table
				int currqty = rs.getInt("scl_qty");
				currqty+=1;
				PreparedStatement update_qty = con.prepareStatement
				("UPDATE shopping_cart_line SET scl_qty = ? WHERE scl_sc_id = ? AND scl_i_id = ?");
				update_qty.setInt(1, currqty);
				update_qty.setInt(2, SHOPPING_ID);
				update_qty.setInt(3, I_ID);
				update_qty.executeUpdate();
				update_qty.close();
			} else {//We need to add a new row to the table.

				//Stick the item info in a new shopping_cart_line
				PreparedStatement put_line = con.prepareStatement
				("INSERT into shopping_cart_line (scl_sc_id, scl_qty, scl_i_id) VALUES (?,?,?)");
				put_line.setInt(1, SHOPPING_ID);
				put_line.setInt(2, 1);
				put_line.setInt(3, I_ID);
				put_line.executeUpdate();
				put_line.close();
			}
			rs.close();
			find_entry.close();
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void refreshCart(Connection con, int SHOPPING_ID, Vector ids, 
			Vector quantities){
		int i;
		try {
			for(i = 0; i < ids.size(); i++){
				String I_IDstr = (String) ids.elementAt(i);
				String QTYstr = (String) quantities.elementAt(i);
				int I_ID = Integer.parseInt(I_IDstr);
				int QTY = Integer.parseInt(QTYstr);

				if(QTY == 0) { // We need to remove the item from the cart
					PreparedStatement statement = con.prepareStatement
					("DELETE FROM shopping_cart_line WHERE scl_sc_id = ? AND scl_i_id = ?");
					statement.setInt(1, SHOPPING_ID);
					statement.setInt(2, I_ID);
					statement.executeUpdate();
					statement.close();
				} 
				else { //we update the quantity
					PreparedStatement statement = con.prepareStatement
					("UPDATE shopping_cart_line SET scl_qty = ? WHERE scl_sc_id = ? AND scl_i_id = ?");
					statement.setInt(1, QTY);
					statement.setInt(2, SHOPPING_ID);
					statement.setInt(3, I_ID);
					statement.executeUpdate(); 
					statement.close();
				}
			}
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void addRandomItemToCartIfNecessary(Connection con, int SHOPPING_ID){
		// check and see if the cart is empty. If it's not, we do
		// nothing.
		int related_item = 0;

		try {
			// Check to see if the cart is empty
			PreparedStatement get_cart = con.prepareStatement
			("SELECT COUNT(*) from shopping_cart_line where scl_sc_id = ?");
			get_cart.setInt(1, SHOPPING_ID);
			ResultSet rs = get_cart.executeQuery();
			rs.next();
			if (rs.getInt(1) == 0) {
				// Cart is empty
				int rand_id = getRandomI_ID();
				related_item = getRelated1(rand_id,con);
				addItem(con, SHOPPING_ID, related_item);
			}

			rs.close();
			get_cart.close();
		}catch (java.lang.Exception ex) {
			ex.printStackTrace();
			System.out.println("Adding entry to shopping cart failed: shopping id = " + SHOPPING_ID + " related_item = " + related_item);
		}
	}


	// Only called from this class 
	private static void resetCartTime(Connection con, int SHOPPING_ID){
		try {
			PreparedStatement statement = con.prepareStatement
			("UPDATE shopping_cart SET sc_time = CURRENT_TIMESTAMP WHERE sc_id = ?");

			// Set parameter
			statement.setInt(1, SHOPPING_ID);
			statement.executeUpdate();
			statement.close();
		}catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
	}

	public static Cart getCart(int SHOPPING_ID, double c_discount) {
		Cart mycart = null;
		try {
			Connection con = getConnection();
			mycart = getCart(con, SHOPPING_ID, c_discount);
			con.commit();
			returnConnection(con);
		}catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return mycart;
	}

	//time .05s
	private static Cart getCart(Connection con, int SHOPPING_ID, double c_discount){
		Cart mycart = null;
		try {
			PreparedStatement get_cart = con.prepareStatement
			("SELECT * " + "FROM shopping_cart_line, item " + "WHERE scl_i_id = item.i_id AND scl_sc_id = ?");
			get_cart.setInt(1, SHOPPING_ID);
			ResultSet rs = get_cart.executeQuery();
			mycart = new Cart(rs, c_discount);
			rs.close();
			get_cart.close();
		}catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return mycart;
	}

	// ************** Customer / Order code below ************************* 

	//This should probably return an error code if the customer
	//doesn't exist, but ...
	public static void refreshSession(int C_ID) {
		try {
			
			
			// Prepare SQL
			Connection con = getConnection();
			PreparedStatement updateLogin = con.prepareStatement
			//("UPDATE customer SET c_login = NOW(), c_expiration = (CURRENT_TIMESTAMP + INTERVAL 2 HOUR) WHERE c_id = ?");
			("UPDATE customer SET c_login = ?, c_expiration = ? WHERE c_id = ?");

			// Set parameter
			
			Date now = new Date(System.currentTimeMillis());  
			Date nextExpiryTime = new Date(now.getTime()+2*60*60*1000);
			
			updateLogin.setDate(1, now);
			updateLogin.setDate(2, nextExpiryTime);
			updateLogin.setInt(3, C_ID);
			updateLogin.executeUpdate();

			con.commit();
			updateLogin.close();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
	}    

	public static Customer createNewCustomer(Customer cust) {
		try {
			// Get largest customer ID already in use.
			Connection con = getConnection();

			cust.c_discount = (int) (java.lang.Math.random() * 51);
			cust.c_balance =0.0;
			cust.c_ytd_pmt = 0.0;
			// FIXME - Use SQL CURRENT_TIME to do this
			cust.c_last_visit = new Date(System.currentTimeMillis());
			cust.c_since = new Date(System.currentTimeMillis());
			cust.c_login = new Date(System.currentTimeMillis());
			cust.c_expiration = new Date(System.currentTimeMillis() + 
					7200000);//milliseconds in 2 hours
			PreparedStatement insert_customer_row = con.prepareStatement
			("INSERT into customer (c_id, c_uname, c_passwd, c_fname, c_lname, c_addr_id, c_phone, c_email, c_since, c_last_login, c_login, c_expiration, c_discount, c_balance, c_ytd_pmt, c_birthdate, c_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			insert_customer_row.setString(4,cust.c_fname);
			insert_customer_row.setString(5,cust.c_lname);
			insert_customer_row.setString(7,cust.c_phone);
			insert_customer_row.setString(8,cust.c_email);
			insert_customer_row.setDate(9, new 
					java.sql.Date(cust.c_since.getTime()));
			insert_customer_row.setDate(10, new java.sql.Date(cust.c_last_visit.getTime()));
			insert_customer_row.setDate(11, new java.sql.Date(cust.c_login.getTime()));
			insert_customer_row.setDate(12, new java.sql.Date(cust.c_expiration.getTime()));
			insert_customer_row.setDouble(13, cust.c_discount);
			insert_customer_row.setDouble(14, cust.c_balance);
			insert_customer_row.setDouble(15, cust.c_ytd_pmt);
			insert_customer_row.setDate(16, new java.sql.Date(cust.c_birthdate.getTime()));
			insert_customer_row.setString(17, cust.c_data);

			cust.addr_id = enterAddress(con,  
					cust.addr_street1, 
					cust.addr_street2,
					cust.addr_city,
					cust.addr_state,
					cust.addr_zip,
					cust.co_name);
			PreparedStatement get_max_id = con.prepareStatement
			("SELECT max(c_id) FROM customer");

			synchronized(Customer.class) {
				// Set parameter
				ResultSet rs = get_max_id.executeQuery();

				// Results
				rs.next();
				cust.c_id = rs.getInt(1);//Is 1 the correct index?
				rs.close();
				cust.c_id = cust.c_id - (cust.c_id % TxMudDriver.NUMDATACENTERS) + TxMudDriver.NUMDATACENTERS + TxMudDriver.THISDATACENTER;

				cust.c_uname = DigSyl(cust.c_id, 0);
				cust.c_passwd = cust.c_uname.toLowerCase();


				insert_customer_row.setInt(1, cust.c_id);
				insert_customer_row.setString(2,cust.c_uname);
				insert_customer_row.setString(3,cust.c_passwd);
				insert_customer_row.setInt(6, cust.addr_id);
				insert_customer_row.executeUpdate();
				con.commit();
				insert_customer_row.close();
			}
			get_max_id.close();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return cust;
	}

	//BUY CONFIRM 

	public static BuyConfirmResult doBuyConfirm(int shopping_id,
			int customer_id,
			String cc_type,
			long cc_number,
			String cc_name,
			Date cc_expiry,
			String shipping) {

		BuyConfirmResult result = new BuyConfirmResult();
		try {
			Connection con = getConnection();
			double c_discount = getCDiscount(con, customer_id);
			result.cart = getCart(con, shopping_id, c_discount);
			int ship_addr_id = getCAddr(con, customer_id);
			result.order_id = enterOrder(con, customer_id, result.cart, ship_addr_id, shipping, c_discount);
			enterCCXact(con, result.order_id, cc_type, cc_number, cc_name, cc_expiry, result.cart.SC_TOTAL, ship_addr_id);
			clearCart(con, shopping_id);
			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	public static BuyConfirmResult doBuyConfirm(int shopping_id,
			int customer_id,
			String cc_type,
			long cc_number,
			String cc_name,
			Date cc_expiry,
			String shipping,
			String street_1, String street_2,
			String city, String state,
			String zip, String country) {


		BuyConfirmResult result = new BuyConfirmResult();
		try {
			Connection con = getConnection();
			double c_discount = getCDiscount(con, customer_id);
			result.cart = getCart(con, shopping_id, c_discount);
			int ship_addr_id = enterAddress(con, street_1, street_2, city, state, zip, country);
			result.order_id = enterOrder(con, customer_id, result.cart, ship_addr_id, shipping, c_discount);
			enterCCXact(con, result.order_id, cc_type, cc_number, cc_name, cc_expiry, result.cart.SC_TOTAL, ship_addr_id);
			clearCart(con, shopping_id);
			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}


	//DB query time: .05s
	public static double getCDiscount(Connection con, int c_id) {
		double c_discount = 0.0;
		try {
			// Prepare SQL
			PreparedStatement statement = con.prepareStatement
			("SELECT c_discount FROM customer WHERE customer.c_id = ?");

			// Set parameter
			statement.setInt(1, c_id);
			ResultSet rs = statement.executeQuery();

			// Results
			rs.next();
			c_discount = rs.getDouble(1);
			rs.close();
			statement.close();
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return c_discount;
	}

	//DB time: .05s
	public static int getCAddrID(Connection con, int c_id) {
		int c_addr_id = 0;
		try {
			// Prepare SQL
			PreparedStatement statement = con.prepareStatement
			("SELECT c_addr_id FROM customer WHERE customer.c_id = ?");

			// Set parameter
			statement.setInt(1, c_id);
			ResultSet rs = statement.executeQuery();

			// Results
			rs.next();
			c_addr_id = rs.getInt(1);
			rs.close();
			statement.close();
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return c_addr_id;
	}

	public static int getCAddr(Connection con, int c_id) {
		int c_addr_id = 0;
		try {
			// Prepare SQL
			PreparedStatement statement = con.prepareStatement
			("SELECT c_addr_id FROM customer WHERE customer.c_id = ?");

			// Set parameter
			statement.setInt(1, c_id);
			ResultSet rs = statement.executeQuery();

			// Results
			rs.next();
			c_addr_id = rs.getInt(1);
			rs.close();
			statement.close();
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return c_addr_id;
	}

	public static void enterCCXact(Connection con,
			int o_id,        // Order id
			String cc_type,
			long cc_number,
			String cc_name,
			Date cc_expiry,
			double total,   // Total from shopping cart
			int ship_addr_id) {

		// Updates the CC_XACTS table
		if(cc_type.length() > 10)
			cc_type = cc_type.substring(0,10);
		if(cc_name.length() > 30)
			cc_name = cc_name.substring(0,30);

		try {
			// Prepare SQL
			PreparedStatement statement = con.prepareStatement
			("INSERT into cc_xacts (cx_o_id, cx_type, cx_num, cx_name, cx_expire, cx_xact_amt, cx_xact_date, cx_co_id) " + "VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE, (SELECT co_id FROM address, country WHERE addr_id = ? AND addr_co_id = co_id))");

			// Set parameter
			statement.setInt(1, o_id);           // cx_o_id
			statement.setString(2, cc_type);     // cx_type
			statement.setLong(3, cc_number);     // cx_num
			statement.setString(4, cc_name);     // cx_name
			statement.setDate(5, (java.sql.Date) cc_expiry);     // cx_expiry
			statement.setDouble(6, total);       // cx_xact_amount
			statement.setInt(7, ship_addr_id);   // ship_addr_id
			statement.executeUpdate();
			statement.close();
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void clearCart(Connection con, int shopping_id) {
		// Empties all the lines from the shopping_cart_line for the
		// shopping id.  Does not remove the actually shopping cart
		try {
			// Prepare SQL
			PreparedStatement statement = con.prepareStatement
			("DELETE FROM shopping_cart_line WHERE scl_sc_id = ?");

			// Set parameter
			statement.setInt(1, shopping_id);
			statement.executeUpdate();
			statement.close();
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
	}

	public static int enterAddress(Connection con,  // Do we need to do this as part of a transaction?
			String street1, String street2,
			String city, String state,
			String zip, String country) {
		// returns the address id of the specified address.  Adds a
		// new address to the table if needed
		int addr_id = 0;

		// Get the country ID from the country table matching this address.

		// Is it safe to assume that the country that we are looking
		// for will be there?
		try {
			PreparedStatement get_co_id = con.prepareStatement
			("SELECT co_id FROM country WHERE co_name = ?");
			get_co_id.setString(1, country);
			ResultSet rs = get_co_id.executeQuery();
			rs.next();
			int addr_co_id = rs.getInt("co_id");
			rs.close();
			get_co_id.close();

			//Get address id for this customer, possible insert row in
			//address table
			PreparedStatement match_address = con.prepareStatement
			("SELECT addr_id FROM address " + "WHERE addr_street1 = ? " +"AND addr_street2 = ? " + "AND addr_city = ? " + "AND addr_state = ? " + "AND addr_zip = ? " + "AND addr_co_id = ?");
			match_address.setString(1, street1);
			match_address.setString(2, street2);
			match_address.setString(3, city);
			match_address.setString(4, state);
			match_address.setString(5, zip);
			match_address.setInt(6, addr_co_id);
			rs = match_address.executeQuery();
			if(!rs.next()){//We didn't match an address in the addr table
				PreparedStatement insert_address_row = con.prepareStatement
				("INSERT into address (addr_id, addr_street1, addr_street2, addr_city, addr_state, addr_zip, addr_co_id) " + "VALUES (?, ?, ?, ?, ?, ?, ?)");
				insert_address_row.setString(2, street1);
				insert_address_row.setString(3, street2);
				insert_address_row.setString(4, city);
				insert_address_row.setString(5, state);
				insert_address_row.setString(6, zip);
				insert_address_row.setInt(7, addr_co_id);

				PreparedStatement get_max_addr_id = con.prepareStatement
				("SELECT max(addr_id) FROM address");
				synchronized(Address.class) {
					ResultSet rs2 = get_max_addr_id.executeQuery();
					rs2.next();
//					addr_id = rs2.getInt(1)+1;
					addr_id = rs2.getInt(1);
					rs2.close();
					addr_id = addr_id - (addr_id % TxMudDriver.NUMDATACENTERS) + TxMudDriver.NUMDATACENTERS + TxMudDriver.THISDATACENTER;
					//Need to insert a new row in the address table
					insert_address_row.setInt(1, addr_id);
					insert_address_row.executeUpdate();
				}
				get_max_addr_id.close();
				insert_address_row.close();
			} else { //We actually matched
				addr_id = rs.getInt("addr_id");
			}
			match_address.close();
			rs.close();
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return addr_id;
	}


	public static int enterOrder(Connection con, int customer_id, Cart cart, int ship_addr_id, String shipping, double c_discount) {
		// returns the new order_id
		int o_id = 0;
		// - Creates an entry in the 'orders' table 
		try {
			PreparedStatement insert_row = con.prepareStatement
//			("INSERT into orders (o_id, o_c_id, o_date, o_sub_total, o_tax, o_total, o_ship_type, o_ship_date, o_bill_addr_id, o_ship_addr_id, o_status) " 
//					   + "VALUES (?,?,CURRENT_DATE,?,8.25,?,?,CURRENT_DATE + INTERVAL ? DAY, ?, ?, 'Pending')");
			("INSERT into orders (o_id, o_c_id, o_date, o_sub_total, o_tax, o_total, o_ship_type, o_ship_date, o_bill_addr_id, o_ship_addr_id, o_status) " 
					   + "VALUES (?,?,CURRENT_DATE,?,8.25,?,?,?,?,?,'Pending')");

			
			
			insert_row.setInt(2, customer_id);
			insert_row.setDouble(3, cart.SC_SUB_TOTAL);
			insert_row.setDouble(4, cart.SC_TOTAL);
			insert_row.setString(5, shipping);
			
			Date now = new Date(System.currentTimeMillis());  
			Date shipdate = new Date(now.getTime()+getRandom(7)*24*60*60*1000);
			//insert_row.setInt(6, getRandom(7));
			insert_row.setDate(6, shipdate);
			
			
			insert_row.setInt(7, getCAddrID(con, customer_id));
			insert_row.setInt(8, ship_addr_id);

			PreparedStatement get_max_id = con.prepareStatement
//			("SELECT count(o_id) FROM orders");
			("SELECT max(o_id) FROM orders");
			//selecting from order_line is really slow!
			synchronized(Order.class) {
				ResultSet rs = get_max_id.executeQuery();
				rs.next();
//				o_id = rs.getInt(1) + 1;
				o_id = rs.getInt(1) ;
				rs.close();
				o_id = o_id - (o_id % TxMudDriver.NUMDATACENTERS) + TxMudDriver.NUMDATACENTERS + TxMudDriver.THISDATACENTER;

				insert_row.setInt(1, o_id);
				insert_row.executeUpdate();
			}
			get_max_id.close();
			insert_row.close();
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}

		Enumeration e = cart.lines.elements();
		int counter = 0;
		while(e.hasMoreElements()) {
			// - Creates one or more 'order_line' rows.
			CartLine cart_line = (CartLine) e.nextElement();
			addOrderLine(con, counter, o_id, cart_line.scl_i_id, 
					cart_line.scl_qty, c_discount, 
					getRandomString(20, 100));
			counter++;

			// - Adjusts the stock for each item ordered
			int stock = getStock(con, cart_line.scl_i_id);
			if ((stock - cart_line.scl_qty) < 10) {
				setStock(con, cart_line.scl_i_id, 
						stock - cart_line.scl_qty + 21);
			} else {
				setStock(con, cart_line.scl_i_id, stock - cart_line.scl_qty);
			}
		}
		return o_id;
	}

	public static void addOrderLine(Connection con, 
			int ol_id, int ol_o_id, int ol_i_id, 
			int ol_qty, double ol_discount, String ol_comment) {
		int success = 0;
		try {
			PreparedStatement insert_row = con.prepareStatement
			("INSERT into order_line (ol_id, ol_o_id, ol_i_id, ol_qty, ol_discount, ol_comments) " + "VALUES (?, ?, ?, ?, ?, ?)");

			insert_row.setInt(1, ol_id);
			insert_row.setInt(2, ol_o_id);
			insert_row.setInt(3, ol_i_id);
			insert_row.setInt(4, ol_qty);
			insert_row.setDouble(5, ol_discount);
			insert_row.setString(6, ol_comment);
			insert_row.executeUpdate();
			insert_row.close();
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
	}

	public static int getStock(Connection con, int i_id) {
		int stock = 0;
		try {
			PreparedStatement get_stock = con.prepareStatement
			("SELECT i_stock FROM item WHERE i_id = ?");

			// Set parameter
			get_stock.setInt(1, i_id);
			ResultSet rs = get_stock.executeQuery();

			// Results
			rs.next();
			stock = rs.getInt("i_stock");
			rs.close();
			get_stock.close();
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
		return stock;
	}

	public static void setStock(Connection con, int i_id, int new_stock) {
		try {
			PreparedStatement update_row = con.prepareStatement
			("UPDATE item SET i_stock = ? WHERE i_id = ?");
			update_row.setInt(1, new_stock);
			update_row.setInt(2, i_id);
			update_row.executeUpdate();
			update_row.close();
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void verifyDBConsistency(){
		try {
			Connection con = getConnection();
			int this_id;
			int id_expected = 1;
			//First verify customer table
			PreparedStatement get_ids = con.prepareStatement
			("SELECT c_id FROM customer");
			ResultSet rs = get_ids.executeQuery();
			while(rs.next()){
				this_id = rs.getInt("c_id");
				while(this_id != id_expected){
					System.out.println("Missing C_ID " + id_expected);
					id_expected++;
				}
				id_expected++;
			}

			id_expected = 1;
			//Verify the item table
			get_ids = con.prepareStatement
			("SELECT i_id FROM item");
			rs = get_ids.executeQuery();
			while(rs.next()){
				this_id = rs.getInt("i_id");
				while(this_id != id_expected){
					System.out.println("Missing I_ID " + id_expected);
					id_expected++;
				}
				id_expected++;
			}

			id_expected = 1;
			//Verify the address table
			get_ids = con.prepareStatement
			("SELECT addr_id FROM address");
			rs = get_ids.executeQuery();
			while(rs.next()){
				this_id = rs.getInt("addr_id");
				//		System.out.println(this_cid+"\n");
				while(this_id != id_expected){
					System.out.println("Missing ADDR_ID " + id_expected);
					id_expected++;
				}
				id_expected++;
			}



			con.commit();
			returnConnection(con);
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
	}
	//===================================================================================
	// utils
	// Defined in TPC-W Spec Clause 4.6.2.8
	private static final String [] digS = {
		"BA","OG","AL","RI","RE","SE","AT","UL","IN","NG"
	};
	public static final int NUM_ITEMS = 1000;

	public static int getRandom(int i) {  // Returns integer 1, 2, 3 ... i
		return ((int) (java.lang.Math.random() * i)+1);
	}
	//Not very random function. If called in swift sucession, it will
	//return the same string because the system time used to seed the
	//random number generator won't change. 
	public static String getRandomString(int min, int max){
		String newstring = new String();
		Random rand = new Random();
		int i;
		final char[] chars = {'a','b','c','d','e','f','g','h','i','j','k',
				'l','m','n','o','p','q','r','s','t','u','v',
				'w','x','y','z','A','B','C','D','E','F','G',
				'H','I','J','K','L','M','N','O','P','Q','R',
				'S','T','U','V','W','X','Y','Z','!','@','#',
				'$','%','^','&','*','(',')','_','-','=','+',
				'{','}','[',']','|',':',';',',','.','?','/',
				'~',' '}; //79 characters
		int strlen = (int) Math.floor(rand.nextDouble()*(max-min+1));
		strlen += min;
		for(i = 0; i < strlen; i++){
			char c = chars[(int) Math.floor(rand.nextDouble()*79)];
			newstring = newstring.concat(String.valueOf(c));
		}
		return newstring;
	}

	public static String DigSyl(int d)
	{
		String s = "";

		for (;d!=0;d=d/10) {
			int c = d % 10;
			s = digS[c]+s;      
		}

		return(s);
	}
	public static String DigSyl(int d, int n)
	{
		String s = "";

		if (n==0) return(DigSyl(d));	
		for (;n>0;n--) {
			int c = d % 10;
			s = digS[c]+s;
			d = d /10;
		}

		return(s);
	}

	public static int getRandomI_ID(){
		Random rand = new Random();
		Double temp = new Double(Math.floor(rand.nextFloat() * NUM_ITEMS));
		return temp.intValue();
	}
}//class


//TPCW POJOS

class Book {
	// Construct a book from a ResultSet
	//TODO need to modify name of columns with position
	public Book(ResultSet rs) {
		// The result set should have all of the fields we expect.
		// This relies on using field name access.  It might be a bad
		// way to break this up since it does not allow us to use the
		// more efficient select by index access method.  This also
		// might be a problem since there is no type checking on the
		// result set to make sure it is even a reasonble result set
		// to give to this function.

		try {
			i_id = rs.getInt("i_id");
			i_title = rs.getString("i_title");
			i_pub_Date = rs.getDate("i_pub_date");
			i_publisher = rs.getString("i_publisher");
			i_subject = rs.getString("i_subject");
			i_desc = rs.getString("i_desc");
			i_related1 = rs.getInt("i_related1");
			i_related2 = rs.getInt("i_related2");
			i_related3 = rs.getInt("i_related3");
			i_related4 = rs.getInt("i_related4");
			i_related5 = rs.getInt("i_related5");
			i_thumbnail = rs.getString("i_thumbnail");
			i_image = rs.getString("i_image");
			i_srp = rs.getDouble("i_srp");
			i_cost = rs.getDouble("i_cost");
			i_avail = rs.getDate("i_avail");
			i_isbn = rs.getString("i_isbn");
			i_page = rs.getInt("i_page");
			i_backing = rs.getString("i_backing");
			i_dimensions = rs.getString("i_dimensions");
			a_id = rs.getInt("a_id");
			a_fname = rs.getString("a_fname");
			a_lname = rs.getString("a_lname");		
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
	}
	// From Item
	public int i_id;
	public String i_title;
	//public int i_a_id;   // Redundant
	public Date i_pub_Date;
	public String i_publisher;
	public String i_subject;
	public String i_desc;
	public int i_related1;
	public int i_related2;
	public int i_related3;
	public int i_related4;
	public int i_related5;
	public String i_thumbnail;
	public String i_image;
	public double i_srp;
	public double i_cost;
	public Date i_avail;
	public String i_isbn;
	public int i_page;
	public String i_backing;
	public String i_dimensions;

	// From Author
	public int a_id;
	public String a_fname;
	public String a_lname;
}
class Customer {

	public int c_id;
	public String c_uname;
	public String c_passwd;
	public String c_fname;
	public String c_lname;
	public String c_phone;
	public String c_email;
	public Date c_since;
	public Date c_last_visit;
	public Date c_login;
	public Date c_expiration;
	public double c_discount;
	public double c_balance;
	public double c_ytd_pmt;
	public Date c_birthdate;
	public String c_data;

	//From the addess table
	public int addr_id;
	public String addr_street1;
	public String addr_street2;
	public String addr_city;
	public String addr_state;
	public String addr_zip;
	public int addr_co_id;

	//From the country table
	public String co_name;

	public Customer(){}

	public Customer(ResultSet rs) {
		// The result set should have all of the fields we expect.
		// This relies on using field name access.  It might be a bad
		// way to break this up since it does not allow us to use the
		// more efficient select by index access method.  This also
		// might be a problem since there is no type checking on the
		// result set to make sure it is even a reasonble result set
		// to give to this function.

		try {
			c_id = rs.getInt("c_id");
			c_uname = rs.getString("c_uname");
			c_passwd = rs.getString("c_passwd");
			c_fname = rs.getString("c_fname");
			c_lname = rs.getString("c_lname");

			c_phone = rs.getString("c_phone");
			c_email = rs.getString("c_email");
			c_since = rs.getDate("c_since");
			c_last_visit = rs.getDate("c_last_login");
			c_login = rs.getDate("c_login");
			c_expiration = rs.getDate("c_expiration");
			c_discount = rs.getDouble("c_discount");
			c_balance = rs.getDouble("c_balance");
			c_ytd_pmt = rs.getDouble("c_ytd_pmt");
			c_birthdate = rs.getDate("c_birthdate");
			c_data = rs.getString("c_data");

			addr_id = rs.getInt("addr_id");
			addr_street1 = rs.getString("addr_street1");
			addr_street2 = rs.getString("addr_street2");
			addr_city = rs.getString("addr_city");
			addr_state = rs.getString("addr_state");
			addr_zip = rs.getString("addr_zip");
			addr_co_id = rs.getInt("addr_co_id");

			co_name = rs.getString("co_name");

		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
	}

}

class ShortBook {
	// Construct a book from a ResultSet
	public ShortBook(ResultSet rs) {
		// The result set should have all of the fields we expect.
		// This relies on using field name access.  It might be a bad
		// way to break this up since it does not allow us to use the
		// more efficient select by index access method.  This also
		// might be a problem since there is no type checking on the
		// result set to make sure it is even a reasonble result set
		// to give to this function.

		try {
			i_id = rs.getInt("i_id");
			i_title = rs.getString("i_title");
			a_fname = rs.getString("a_fname");
			a_lname = rs.getString("a_lname");		
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
	}
	// From Item
	public int i_id;
	public String i_title;
	public String a_fname;
	public String a_lname;
}
class Order {
	public Order(ResultSet rs) {
		try {
			o_id = rs.getInt("o_id");
			c_fname = rs.getString("c_fname");
			c_lname = rs.getString("c_lname");
			c_passwd = rs.getString("c_passwd");
			c_uname = rs.getString("c_uname");
			c_phone = rs.getString("c_phone");
			c_email = rs.getString("c_email");
			o_date = rs.getDate("o_date");
			o_subtotal = rs.getDouble("o_sub_total");
			o_tax = rs.getDouble("o_tax");
			o_total = rs.getDouble("o_total");
			o_ship_type = rs.getString("o_ship_type");
			o_ship_date = rs.getDate("o_ship_date");
			o_status = rs.getString("o_status");
			cx_type = rs.getString("cx_type");

			bill_addr_street1 = rs.getString("bill_addr_street1");
			bill_addr_street2 = rs.getString("bill_addr_street2");
			bill_addr_state = rs.getString("bill_addr_state");
			bill_addr_zip = rs.getString("bill_addr_zip");
			bill_co_name = rs.getString("bill_co_name");

			ship_addr_street1 = rs.getString("ship_addr_street1");
			ship_addr_street2 = rs.getString("ship_addr_street2");
			ship_addr_state = rs.getString("ship_addr_state");
			ship_addr_zip = rs.getString("ship_addr_zip");
			ship_co_name = rs.getString("ship_co_name");
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
	}

	public int o_id;
	public String c_fname;
	public String c_lname;
	public String c_passwd;
	public String c_uname;
	public String c_phone;
	public String c_email;
	public Date o_date;
	public double o_subtotal;
	public double o_tax;
	public double o_total;
	public String o_ship_type;
	public Date o_ship_date;
	public String o_status;

	//Billing address
	public String bill_addr_street1;
	public String bill_addr_street2;
	public String bill_addr_state;
	public String bill_addr_zip;
	public String bill_co_name;

	//Shipping address
	public String ship_addr_street1;
	public String ship_addr_street2;
	public String ship_addr_state;
	public String ship_addr_zip;
	public String ship_co_name;

	public String cx_type;
}
class OrderLine {
	public OrderLine(ResultSet rs) {
		try {
			ol_i_id = rs.getInt("ol_i_id");
			i_title = rs.getString("i_title");
			i_publisher = rs.getString("i_publisher");
			i_cost = rs.getDouble("i_cost");
			ol_qty = rs.getInt("ol_qty");
			ol_discount = rs.getDouble("ol_discount");
			ol_comments = rs.getString("ol_comments");
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
	}

	public int ol_i_id;
	public String i_title;
	public String i_publisher;
	public double i_cost;
	public int ol_qty;
	public double ol_discount;
	public String ol_comments;
}
class Cart {

	public double SC_SUB_TOTAL;
	public double SC_TAX;
	public double SC_SHIP_COST;
	public double SC_TOTAL;

	public Vector lines;

	public Cart (ResultSet rs, double C_DISCOUNT) throws java.sql.SQLException{
		int i;
		int total_items;
		lines = new Vector();
		while(rs.next()){//While there are lines remaining
			CartLine line = new CartLine(rs.getString("i_title"),
					rs.getDouble("i_cost"),
					rs.getDouble("i_srp"),
					rs.getString("i_backing"),
					rs.getInt("scl_qty"),
					rs.getInt("scl_i_id"));
			lines.addElement(line);
		}

		SC_SUB_TOTAL = 0;
		total_items = 0;
		for(i = 0; i < lines.size(); i++){
			CartLine thisline = (CartLine) lines.elementAt(i);
			SC_SUB_TOTAL += thisline.scl_cost * thisline.scl_qty;
			total_items += thisline.scl_qty;
		}

		//Need to multiply the sub_total by the discount.
		SC_SUB_TOTAL = SC_SUB_TOTAL * ((100 - C_DISCOUNT)*.01);
		SC_TAX = SC_SUB_TOTAL * .0825;
		SC_SHIP_COST = 3.00 + (1.00 * total_items);
		SC_TOTAL = SC_SUB_TOTAL + SC_SHIP_COST + SC_TAX;
	}
}
class BuyConfirmResult {
	public Cart cart;
	public int order_id;
}
class CartLine{
	public String scl_title;
	public double scl_cost;
	public double scl_srp;
	public String scl_backing;
	public int scl_qty;
	public int scl_i_id;

	public CartLine(String title, double cost, double srp, String backing,
			int qty, int id){
		scl_title = title;
		scl_cost = cost;
		scl_srp = srp;
		scl_backing = backing;
		scl_qty = qty;
		scl_i_id = id;
	}
}
class Address {
}
