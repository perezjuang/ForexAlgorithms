import io.socket.emitter.Emitter;
import io.socket.client.IO;
import io.socket.client.Socket;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import okhttp3.OkHttpClient;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 *
 * @author Daniel
 */
public class App {
    static String trading_api_host_demo = "api-demo.fxcm.com";
    static String trading_api_port = "443";
    static String demo_connection = "https://" + trading_api_host_demo + ":"+ trading_api_port;
    static String login_token = "25e7bb74fafe7aab29efd848d4d1f8b4e79bc483";
    static Socket server_socket;
    static String bearer_access_token;
    static String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    static String accountID="";
  
    
    public static void main(String[] args) throws URISyntaxException {
        IO.Options options = new IO.Options();
        options.forceNew = true;

        final OkHttpClient client = new OkHttpClient();
        options.webSocketFactory = client;
        options.callFactory = client;
        options.query = "access_token="+ login_token;

        //final Socket socket = IO.socket(url + ":" + port, options);
        server_socket = IO.socket(demo_connection, options);
        server_socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                bearer_access_token = "Bearer " + server_socket.id() + login_token;
                System.out.println("connected, Server Socket ID: " + server_socket.id());
                System.out.println("bearer token: " + bearer_access_token);
                
                try {
                    getTableData();
                    getHistoricalData();
                    getHistoricalDataByOfferIDTimeFrame("1","m1","100");
                    //sendMarketOrder();
                    server_socket.close();
                } catch (Exception ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        server_socket.io().on(io.socket.engineio.client.Socket.EVENT_CLOSE, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("engine close");
                client.dispatcher().executorService().shutdown();
            }
        });
        server_socket.open();
    }


    public static String sendGetRequest(String path, String param) throws Exception {
        final String path2 = demo_connection + path +"?" + param;
        final URL url = new URL(path2);

        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // Set the appropriate header fields in the request header.
        connection.setRequestMethod("GET");

        connection.setRequestProperty("Authorization", bearer_access_token);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("host", trading_api_host_demo);
        connection.setRequestProperty("port", trading_api_port);
        connection.setRequestProperty("path", path +"/?"+ param);
        connection.setRequestProperty("User-Agent", "request");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded'");

        final String response = getResponseString(connection);
        final int responseCode = connection.getResponseCode();
        
        System.out.println("\nSending 'GET' request: " + path2);
        System.out.println("Response Code : " + responseCode);
        
        if (responseCode == 200) {
            return response;
        } else {
            throw new Exception(response);
        }
    }

    public static String sendPostRequest(String path, String param)
    {
        URL url;
        HttpURLConnection connection = null;  
        try {
            //Create connection
            url = new URL(demo_connection + path);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", "" + Integer.toString(param.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");  
          
            connection.setRequestProperty("Authorization", bearer_access_token);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("host", trading_api_host_demo);
            connection.setRequestProperty("port", trading_api_port);
            connection.setRequestProperty("path", path);
            connection.setRequestProperty("User-Agent", "request");


            connection.setUseCaches (false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (connection.getOutputStream ());
            wr.writeBytes (param);
            wr.flush ();
            wr.close ();

            //Get Response	
            final String response = getResponseString(connection);

            /*
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer(); 
            while((line = rd.readLine()) != null) {
              response.append(line);
              response.append('\r');
            }
            rd.close();
            */
            
            System.out.println("\nSending 'POST' request: " + path);
            System.out.println("Response : " + response);
            
            return response.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if(connection != null) {
                connection.disconnect(); 
          }
        }
    }
    
    private static String getResponseString(HttpURLConnection conn) throws IOException {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) 
        {
            final StringBuilder stringBuffer = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append('\r');
            }
            reader.close();
            return stringBuffer.toString();
        }
    }
    
            
    static void sendMarketOrder() throws Exception
    {
        if (accountID == "")
        {
            System.out.println("Account ID missing");
        }
        else
        {
            String cmd = "account_id=" + accountID +
              "&symbol=EUR%2FUSD" +
              "&is_buy=true" +
              "&rate=0" + 
              "&amount=2" + 
              "&order_type=AtMarket" +
              "&time_in_force=GTC";

            sendPostRequest("/trading/open_trade", cmd);
        }
    }
    
    static void getHistoricalData() throws Exception
    {
        String priceList = sendGetRequest("/candles/1/m1", "num=100");
        System.out.println(priceList);
        
    }
    
        static void getHistoricalDataByOfferIDTimeFrame(String offerId,String timeFrame,String numberOfCandles) throws Exception
    {
            String priceList = sendGetRequest("/candles/" + offerId + "/" + timeFrame, "num=" + numberOfCandles);
            JSONObject obj = new JSONObject(priceList);
            JSONArray arrJson = obj.getJSONArray("candles");

            for(int i=0;i<arrJson.length();i++)                                      
                System.out.println(arrJson.getString(i));
            
        System.out.println(priceList);
        
    }
    
    static void getTableData() throws Exception
    {
        String cmd = "models=Account";
        String accountList = sendGetRequest("/trading/get_model", cmd);
        System.out.println(accountList);
        
        if (accountList != null)
        {
            JSONObject obj = new JSONObject(accountList);
            JSONArray arr = obj.getJSONArray("accounts");
            accountID = arr.getJSONObject(0).getString("accountId");
        }
    }
    
    
    private static String convertObjectArrayToString(Object[] arr, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (Object obj : arr)
			sb.append(obj.toString()).append(delimiter);
		return sb.substring(0, sb.length() - 1);

	}
}


