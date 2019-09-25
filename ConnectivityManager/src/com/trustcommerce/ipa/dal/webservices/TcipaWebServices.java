package com.trustcommerce.ipa.dal.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import com.trustcommerce.ipa.dal.model.TransactionStatus;
import com.trustcommerce.ipa.dal.webservices.*;

public class TcipaWebServices {

	private static final String LOCAL_HOST = "http://localhost:4973";
	private static final String BRIDGE_URI = "/IPABridge/getkey";
	private static final int APP_ID = 44888;
	private static final String CLIENT_KEY = "swdasdfas234j-=21934=14oieuK#SDASER";
	private static final String KEY_TYPE = "pedal";
	private static final String SALT = "0238409asdlfkl3k43l4!!";
	
	
	private static final String TEST_URI = "http://localhost/IPADAL/initialize";
	

	
	public static void testOnly() {
		final HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			final HttpGet getRequest = new HttpGet(TEST_URI);
			getRequest.addHeader("accept", "application/json");
			final HttpResponse response = httpClient.execute(getRequest);


			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
				        + response.getStatusLine().getStatusCode());
			}
			final BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

		} catch (Exception e) {
			System.out.println("Illegal argument " + e.toString());
		}
	}
	
	
	public static void forwardSignatureCaptured(final String imageHex) throws DalWebServiceCallException {
		try {
			final HttpClient httpClient = HttpClientBuilder.create().build();
			final HttpGet getRequest = new HttpGet(LOCAL_HOST + "/IPABridge/gettoken");
			getRequest.addHeader("accept", "application/json");

			final HttpResponse response = httpClient.execute(getRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
				        + response.getStatusLine().getStatusCode());
			}

		
			httpClient.getConnectionManager().shutdown();

		} catch (ClientProtocolException e) {
			throw new DalWebServiceCallException(e.getMessage());
		} catch (IOException e) {
			throw new DalWebServiceCallException(e.getMessage());
		}
	}
	
	
	public static TransactionStatus forwardPaymentDetail(String paymentAsJson) {
		TransactionStatus signature = null;
		
		final HttpClient httpClient = HttpClientBuilder.create().build();
		try {
 			final HttpPost request = new HttpPost(LOCAL_HOST + BRIDGE_URI);
 			final StringEntity input  = new StringEntity(paymentAsJson);
 			input.setContentType("application/json");
 			request.setEntity(input);
 			
 			final HttpResponse response = httpClient.execute(request);
 					

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
				        + response.getStatusLine().getStatusCode());
			}

//			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
//			String output;
//			System.out.println("Output from Server .... \n");
//			while ((output = br.readLine()) != null) {
//				System.out.println(output);
//			}

			// JSon
			final ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.configure(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS, false);
			signature = mapper.readValue(response.getEntity().getContent(), TransactionStatus.class);
			
		} catch (Exception e) {
			System.out.println("Illegal argument " + e.toString());
		} finally {

		}
		return signature;

	}

}
