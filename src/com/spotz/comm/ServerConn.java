package com.spotz.comm;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.net.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.spotz.LoginActivity;
import com.spotz.MainActivity;
import com.spotz.utils.Const;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


public class ServerConn{

	//
	public static final int metNONE = 0, metGET = 1, metPOST = 2;

	public static final String charset = "UTF-8";

	//default connection timeout - 2 secs
	public static int defConTimeout = 2000;
	public static String TAG = "ServerConn";

	public static void main(String args[]){
		Scanner in=new Scanner(System.in);
		System.out.print("Write a message to the server: ");
		String url="http://api.myhotspotz.net/app/";
		try{
			String query = String.format("param1=%s", URLEncoder.encode(in.nextLine(), charset));
			System.out.println("Server's GET response: " +getResponse(Connect(metGET,url,query,null)));
			System.out.println("Server's POST response: "+getResponse(Connect(metPOST,url,query,null)));
		}catch (Exception e){			System.out.println(e.getMessage());	}
		finally{in.close();}

	}


	/**Check if we are connected a network regardless if it's wifi's or mobile's
	 * @return true if we are connected to a network, otherwise false	 */
	public static boolean isNetworkAvailable() {
		boolean ret;
		
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) LoginActivity.instance.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		ret = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
		if(Const.D & !ret){
			Log.e(Const.TAG+"-srvConn","there is no network Available ");	
		}
		return ret;
	}


	public static boolean haveNetworkConnection() {
		ConnectivityManager cm = (ConnectivityManager) MainActivity.instance.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) 
			if ( ni.isConnected() /*&& ni.getTypeName().equalsIgnoreCase("WIFI")*/ )
				return true;
		return false;
	}
	
	/** create a new connection
	 * @params int mMethod:	0 for no query, 1 for metod GET, 2 for metod POST
	 * @params string mUrl:	string containing the url
	 * @params string query:	string containing the query
	 * @return an HttpURLConnection or null if there is no network available	 */	
	public static HttpURLConnection Connect(int mMethod, String mUrl, String query, String[][] optionparams)
			throws Exception{

		if( !ServerConn.isNetworkAvailable() )
			return null;

		if ( !mUrl.startsWith("http://"))
			mUrl = "http://" + mUrl;
			
		if(Const.D){
			Log.v(Const.TAG+"-srvConn","Opening Connection("+mMethod+") to url: "+mUrl+query);	
		}
		
		//query = URLEncoder.encode(query,"UTF-8");
		if (query != null)
			mUrl = mUrl+query;

		
		HttpURLConnection conn = null;
		//set the type of request
		switch(mMethod){
		case metGET:

			conn = (HttpURLConnection) new URL(mUrl).openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(defConTimeout);
			break;
		case metPOST:
			if(Const.D) Log.d(TAG,"HTTP1 = "+mUrl);
			conn = (HttpURLConnection) new URL(mUrl).openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true); // this set POST method
			conn.setConnectTimeout(defConTimeout);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			for (int i = 0; i < optionparams.length; i++) {
				for (int j = 1; j < optionparams[i].length; j++) {
					params.add(new BasicNameValuePair(optionparams[i][0], optionparams[i][j]));
					//Log.d(TAG,optionparams[i][0]+" - "+optionparams[i][j]);
				}
			}
			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(
			        new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(params));
			writer.flush();
			writer.close();
			os.close();

			if(Const.D) Log.d(TAG,"HTTP = "+mUrl);
			break;
		default:
			conn = (HttpURLConnection) new URL(mUrl).openConnection();
			conn.setUseCaches(true);
			conn.setConnectTimeout(defConTimeout);
			break;
		}
		return conn;
	}
	

	private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException{
	    StringBuilder result = new StringBuilder();
	    boolean first = true;

	    for (NameValuePair pair : params){
	        if (first)
	            first = false;
	        else
	            result.append("&");

	        result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
	        result.append("=");
	        result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
	    }

	    return result.toString();
	}
	/**Get a response from a httpURLconnection as String
	 * @param conn an opened connection
	 * @return the servers response or null if no connection exists
	 * @throws Exception	 */
	public static String getResponse(HttpURLConnection conn)throws Exception{
		if ( conn == null )
			return null;
		
		StringBuffer result = new StringBuffer("");
		
		Scanner reader;	
		//get the response and append it 
		reader = new Scanner(conn.getInputStream());
		while (reader.hasNextLine()) {
			result.append(reader.nextLine());
		}
		if(Const.D) Log.d(TAG,"ACAAA="+result.toString());
		reader.close();
		conn.disconnect();
		conn = null;
		
		return result.toString();
	}


	/**Connect to a server by its URL using GET method, obviously the URL
	 * should have the necessary query parameters included.
	 * @param mUrl servers URL
	 * @return a string containing the servers response or null if no network is available
	 * @throws Exception
	 */
	public static String getResponse(String mUrl) throws Exception	{
		if( !ServerConn.isNetworkAvailable() )
			return null;
		HttpURLConnection conn;
		Scanner rd;
		StringBuilder response=new StringBuilder();
		try {
			conn = (HttpURLConnection) new URL(mUrl).openConnection();
			conn.setRequestMethod("GET");
			rd = new Scanner(conn.getInputStream());
			while (rd.hasNextLine()) {
				response.append(rd.nextLine());
			}
			rd.close();
		} catch (Exception e) {
			throw e;
		}
		return response.toString();
	}



	public static void printConnProps(HttpURLConnection conn)throws IOException{
		System.out.println("method: "+conn.getRequestMethod());
		System.out.println("response code: "+conn.getResponseCode());
		System.out.println("response Message: "+conn.getResponseMessage());
		System.out.println("content type: "+conn.getContentType());
		System.out.println("content length: "+conn.getContentLength());
		System.out.println("content: "+(String)conn.getContent().toString());
		System.out.println("header field: "+conn.getHeaderFields());
		System.out.println("Url: "+conn.getURL());
		System.out.println("connection: "+(String)conn.toString());
		System.out.println("\n");
	}


	/**
	 * This method is for practice and test only, it has no real funcitonaliti
	 * @param methodType
	 * @param Info
	 * @return
	 */
	@SuppressWarnings("unused")
	private static String ApacheREST(int methodType, String Info){

		HttpClient httpClient = new DefaultHttpClient();

		switch(methodType){
			case 0:
				HttpPost post = new HttpPost("http://10.0.2.2:2731/Api/Clientes/Cliente");
				post.setHeader("content-type", "application/json");
				try{
					JSONObject dato = new JSONObject();//build request JSON
					dato.put("info", Info);
					StringEntity entity = new StringEntity(dato.toString());
					post.setEntity(entity);// we add it to the post request
					HttpResponse resp = httpClient.execute(post);//get server response
					String respStr = EntityUtils.toString(resp.getEntity());
					return respStr;
				}
				catch(Exception ex){if(Const.D) Log.e("ServicioRest","Error!", ex);	}
				break;
			case 1:
				HttpPut put = new HttpPut("http://10.0.2.2:2731/Api/Clientes/Cliente");
				put.setHeader("content-type", "application/json");
				try{
					//Construimos el objeto cliente en formato JSON
					JSONObject dato = new JSONObject();
	
					dato.put("Info", Info);
					StringEntity entity = new StringEntity(dato.toString());
					put.setEntity(entity);
	
					HttpResponse resp = httpClient.execute(put);
					String respStr = EntityUtils.toString(resp.getEntity());
					return respStr;
				}
				catch(Exception ex){if(Const.D) Log.e("ServicioRest","Error!", ex);	}
				break;
			case 2:
				 
				HttpDelete del = new HttpDelete("http://10.0.2.2:2731/Api/Clientes/Cliente/12");
				del.setHeader("content-type", "application/json");
				try{
				   HttpResponse resp = httpClient.execute(del);
				   String respStr = EntityUtils.toString(resp.getEntity());
				   return respStr;
				}
				catch(Exception ex){if(Const.D) Log.e("ServicioRest","Error!", ex);	}
				break;
				
			case 3:
				HttpGet get = new HttpGet("http://10.0.2.2:2731/Api/Clientes/Cliente/15");
			 	get.setHeader("content-type", "application/json");		 
			 	try{
			        HttpResponse resp = httpClient.execute(get);
			        String respStr = EntityUtils.toString(resp.getEntity());		 
			        JSONObject respJSON = new JSONObject(respStr);
			        int idCli = respJSON.getInt("Id");
			        String nombCli = respJSON.getString("Nombre");
			        int telefCli = respJSON.getInt("Telefono");
			 	}
				catch(Exception ex){if(Const.D) Log.e("ServicioRest","Error!", ex);	}
			break;
		}
		return null;
	}




/*public static uploadFile(){
		String param = "value";
		File textFile = new File("/path/to/file.txt");
		File binaryFile = new File("/path/to/file.bin");
		String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
		String CRLF = "\r\n"; // Line separator required by multipart/form-data.
		URLConnection connection = new URL(url).openConnection();
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		PrintWriter writer = null;
		try {
			OutputStream output = connection.getOutputStream();
			writer = new PrintWriter(new OutputStreamWriter(output, charset), true); // true = autoFlush, important!
			// Send normal param.
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
			writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
			writer.append(CRLF);
			writer.append(param).append(CRLF).flush();
			// Send text file.
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + textFile.getName() + "\"").append(CRLF);
			writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
			writer.append(CRLF).flush();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile), charset));
				for (String line; (line = reader.readLine()) != null;) {
					writer.append(line).append(CRLF);
				}
			} finally {
				if (reader != null) try { reader.close(); } catch (IOException logOrIgnore) {}
			}
			writer.flush();
			// Send binary file.
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
			writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
			writer.append("Content-Transfer-Encoding: binary").append(CRLF);
			writer.append(CRLF).flush();
			InputStream input = null;
			try {
				input = new FileInputStream(binaryFile);
				byte[] buffer = new byte[1024];
				for (int length = 0; (length = input.read(buffer)) > 0;) {
					output.write(buffer, 0, length);
				}
				output.flush(); // Important! Output cannot be closed. Close of writer will close output as well.
			} finally {
				if (input != null) try { input.close(); } catch (IOException logOrIgnore) {}
			}
			writer.append(CRLF).flush(); // CRLF is important! It indicates end of binary boundary.
			// End of multipart/form-data.
			writer.append("--" + boundary + "--").append(CRLF);
		} finally {
			if (writer != null) writer.close();
		}
	}
 */

}