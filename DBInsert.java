import java.sql.*;
import java.util.ArrayList;

public class DBInsert {
    // this method inserts two rows of data using a prepared statement
    public static void doInsert(Connection conn) throws Exception {
        String cmd = "INSERT INTO symbols (symbol, name, country, IPOyear, sector, industry) " + "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement( cmd );

        GetCSVData data = new GetCSVData();
        ArrayList<String> dataCSV = data.getCSV();

        for (int i=1;i<dataCSV.size();i++){
            String[] line = dataCSV.get(i).split(",", -1);
            System.out.println(line.length);
            for (int j=1;j<=line.length;j++){
                    if (line[j-1].equals(" ")||line[j-1].equals("")){
                        stmt.setNull(j, Types.VARCHAR);
                    }
                    else{
                        stmt.setString(j, line[j-1]);
                    }
                System.out.println(line[j-1]);
            }
//            for (int k=line.length+1;k<=6;k++){
//                stmt.setNull(k, Types.VARCHAR);
//            }
            stmt.executeUpdate();
        }
// prepare and insert row 1

//        stmt.setString(1, "A");
//        stmt.setString(2, "Agilent Technologies Inc. Common Stock");
//        stmt.setString(3, "United States");
//        stmt.setString(4, "1999");
//        stmt.setString(5, "Capital Goods");
//        stmt.setString(6, "Electrical Products");
//        stmt.executeUpdate();
//// prepare and insert row 2 - note how NULL is processed
//        stmt.setString(1, "AA");
//        stmt.setString(2, "Alcoa Corporation Common Stock");
//        stmt.setNull(3, Types.VARCHAR);
//        stmt.setString(4, "2016");
//        stmt.setString(5, "Basic Industries");
//        stmt.setString(6, "Metal Fabrications");
//        stmt.executeUpdate();
        stmt.close();
//        System.out.println("Two rows inserted successfully.");
    }

    // retrieve all the data from symbols and print it
    public static void doSelect(Connection conn) throws SQLException {
        String query = "SELECT * FROM symbols";
        Statement stmt = conn.createStatement();
// execute the query and get a ResultSet
        ResultSet rs = stmt.executeQuery(query);
        System.out.format("\n%-10s %-32s %-10s %-5s %-20s %-20s\n", "symbol", "name", "country", "IPOyear", "sector", "industry" );
// loop through the result set, retrieving data and printing
        while(rs.next()){
            String symbol = rs.getString(1);
            String name   = rs.getString(2);
            String country = rs.getString(3);
            Date IPOyear = rs.getDate(4);
            String sector = rs.getString(5);
            String industry = rs.getString(6);
            System.out.format("%-10s %-32s %-10s %-5s %-20s %-20s\n", symbol, name, country, IPOyear, sector, industry );
        }
    }
    public static void main (String args []) throws Exception {
// Attempt to connect to the database
        try {  // note the details of the connection process
            String dbURL = "jdbc:mysql://falcon.cs.wfu.edu/schwfj20_stockData";
            Connection connection = DriverManager.getConnection(dbURL,"schwfj20", "!06592485!");
// do example INSERT
            doInsert(connection);
// do example SELECT
           // doSelect(connection);
        } catch (SQLException ex) {
// handle exceptions
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: "     + ex.getSQLState());
            System.out.println("VendorError: "  + ex.getErrorCode());
        } // end catch
    }
}