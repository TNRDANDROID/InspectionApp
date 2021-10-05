package com.nic.Inspection.api;

import android.content.Context;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;



public class PostMethod {

	public  static String param[];
	public static String paramValue[];
	String Url;
	DefaultHttpClient httpClient = null;

	Context context;
	static String responseText;

	public PostMethod(Context con, String Url) {
		this.context = con;
		this.Url = Url;
	}

	public String post() {

		try {
			SSLSocketFactory sslFactory = new SimpleSSLSocketFactory(null);
			sslFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			// Enable HTTP parameters
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			// Register the HTTP and HTTPS Protocols. For HTTPS, register our custom SSL Factory object.
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sslFactory, 443));

			// Create a new connection manager using the newly created registry and then create a new HTTP client using this connection manager
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
			httpClient = new DefaultHttpClient(ccm, params);

			HttpPost httppost = new HttpPost(Url);

			int size = param.length;

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
					size);

			for (int i = 0; i < size; i++) {
				nameValuePairs.add(new BasicNameValuePair(param[i],
						paramValue[i]));
			}
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			//HttpResponse response = httpclient.execute(httppost);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseText = httpClient.execute(httppost, responseHandler);
			// System.out.println("Response is : " + responseText);

			return responseText;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public String post1() {

		try {
			SSLSocketFactory sslFactory = new SimpleSSLSocketFactory(null);
			sslFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			// Enable HTTP parameters
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			// Register the HTTP and HTTPS Protocols. For HTTPS, register our custom SSL Factory object.
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sslFactory, 443));

			// Create a new connection manager using the newly created registry and then create a new HTTP client using this connection manager
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
			httpClient = new DefaultHttpClient(ccm, params);

			HttpPost httppost = new HttpPost(Url);

			int size = param.length;

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
					size);

			for (int i = 0; i < size; i++) {
				nameValuePairs.add(new BasicNameValuePair(param[i],
						paramValue[i]));
			}
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
			httppost.setEntity(formEntity);			
			HttpResponse res = httpClient.execute(httppost);
			
		    BufferedReader in = null; 
			StringBuffer sb = null;
		    try {
		    	
		    	 in = new BufferedReader(new InputStreamReader(res.getEntity().getContent(), "UTF-8")); 
			     sb = new StringBuffer(""); 
			     String line = ""; 
			     String NL = System.getProperty("line.separator"); 
			     while ((line = in.readLine()) != null) { 
			    	 sb.append(line + NL); 
			     }	 
			} catch (Exception e) {
				System.out.println("XML Pasing Excpetion = " + e);
			}
			
		    return sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
