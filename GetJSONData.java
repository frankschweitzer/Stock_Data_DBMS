import java.math.BigDecimal;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Iterator;
import java.util.Scanner;
import com.google.gson.*; // from:  com.google.code.gson:gson:2.9.0
/**
 *  This code:
 *    - connects to a web site for retrieving stock sales data
 *    - retrieves the most recent 100 days of data for a single stock
 *    - parses the JSON data to access specific key:value pairs
 */

public class GetJSONData {
    /*
     * Method for doing the actual connection and retrieval of JSON data from web at URL
     * Returns the data as a single, large String
     */
    //NO Changes to this method
    public static String getDataString(URL url){
        String bigString = ""; // result
        try {
            URLConnection connection = url.openConnection();
            InputStreamReader inputStream = new InputStreamReader(connection.getInputStream(), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStream);
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                responseBuilder.append(line);
            }
            bufferedReader.close();
            bigString = responseBuilder.toString();
// System.out.println(bigString);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
        return bigString;
    }
    /*
     *  Example method for pretty printing JSON data contained in a String
     */
    static void prettyPrintJSON(String JSONString){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(JSONString);
        String prettyJsonString = gson.toJson(jsonElement);
        System.out.println(prettyJsonString);
    }
    /*
     *  Illustration of methods for breaking a String containing stock data in JSON format
     *  into constituent components. Values are printed for purposes of illustration.
     *  Will need to be inserted into 'stockdata' table.
     */
    //just prints rn, make it insert
    public static void parseDataAndInsert(String JSONString, Connection conn) throws SQLException {
        JsonElement JSONelement = JsonParser.parseString(JSONString);
        JsonObject JSONObject = JSONelement.getAsJsonObject();
        JsonObject metaData = ( JsonObject) JSONObject.get("Meta Data");
        System.out.println("Just the Meta Data: " + metaData);
        JsonObject timeSeries = ( JsonObject) JSONObject.get("Time Series (Daily)");
        System.out.println("Just the daily time series data: " + timeSeries);
        String symbol = metaData.get("2. Symbol").getAsString();
        System.out.println("Just the stock symbol: " + symbol);
// Iterate through the timeSeries data and print the detail data for each day
// Only printing the first 10 for this sample code
        String cmd = "INSERT INTO pricedata (symbol, pricedate, openprice, highprice, lowprice, closeprice, volume) " + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement( cmd );
        Iterator<String> it = timeSeries.keySet().iterator();
        int count = 0;
        System.out.println("\nData for symbol: " + symbol);
        while( it.hasNext() && (count++ < 100) ){
            String key = it.next();
            JsonObject value = ( JsonObject) timeSeries.get(key);
            String open = value.get("1. open").getAsString();
            String high = value.get("2. high").getAsString();
            String low = value.get("3. low").getAsString();
            String close = value.get("4. close").getAsString();
            String volume = value.get("5. volume").getAsString();
            System.out.format("%s %s %s %s %s %s %s\n", symbol, key, open, high, low, close, volume);
            stmt.setString(1, symbol);
            stmt.setDate(2, Date.valueOf(key));
            stmt.setDouble(3, Double.parseDouble(open));
            stmt.setDouble(4, Double.parseDouble(high));
            stmt.setDouble(5, Double.parseDouble(low));
            stmt.setDouble(6, Double.parseDouble(close));
            stmt.setDouble(7, Double.parseDouble(volume));
            stmt.executeUpdate();
        }
        stmt.close();
// TODO:  insert the most recent 100 days of data into the 'pricedata' table for each
        //stock for each day
    }

    public static ArrayList<String> doSelect(Connection conn, String indust) throws SQLException {
        ArrayList<String> list = new ArrayList<>();
        String query = "SELECT * FROM symbols WHERE industry = '"+indust+"'";
        Statement stmt = conn.createStatement();
// execute the query and get a ResultSet
        ResultSet rs = stmt.executeQuery(query);
        System.out.format("\n%-10s %-32s %-10s %-5s %-20s %-20s\n", "symbol", "name", "country", "IPOyear", "sector", "industry");
// loop through the result set, retrieving data and printing
        while (rs.next()) {
            String symbol = rs.getString(1);
            //might want to delete below and change query later
            String name = rs.getString(2);
            String country = rs.getString(3); //make condition for if null...split...if length = 0...set null
            Date IPOyear = rs.getDate(4);
            String sector = rs.getString(5);
            String industry = rs.getString(6);
            System.out.format("%-10s %-32s %-10s %-5s %-20s %-20s\n", symbol, name, country, IPOyear, sector, industry);
            String line = symbol + "," + name + "," + country + "," + IPOyear + "," + sector + "," + industry;
            list.add(line);
        }
        return list;
    }

    /*
     *   Example driver code.
     *   Gets a single stock symbol from user.
     *   Retrieves past 100 days of data for that stock.
     *   Pretty prints the data for validation.
     *   Parses JSON data and prints (will need to insert into 'stockdata' table)
     */
    public static void main (String[]args) throws Exception
    {
/* TODO:
            -   ask the user to identify an Industry
            -   retrieve the symbols of all the stocks in that Industry from the 'symbols" table
            -   For each stock, get the trading data and insert into the 'pricedata' table
         */
        Scanner kb = new Scanner(System.in);
        System.out.print("What is the industry of interest? "); //what is this
        String industry = kb.nextLine();
        URL url = null; // USE YOUR API Key!!
        // note the details of the connection process
            String dbURL = "jdbc:mysql://falcon.cs.wfu.edu/schwfj20_stockData";
            Connection connection = DriverManager.getConnection(dbURL, "schwfj20", "!06592485!");
            ArrayList<String> symbols = doSelect(connection, industry);

            for (int i=0;i<symbols.size();i++){
                String[] currentStock  = symbols.get(i).split(",", -1);
                String currentSymbol = currentStock[0];
                System.out.println(currentSymbol);
                    url = new
                            URL("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol="
                            + currentSymbol
                            + "&outputsize=compact&apikey=23SC9PW7Y7EW4247");
                //doInsert(connection, currentStock);
                String bigString = getDataString(url);
                prettyPrintJSON(bigString);
                parseDataAndInsert(bigString, connection);
            }
    }

}
