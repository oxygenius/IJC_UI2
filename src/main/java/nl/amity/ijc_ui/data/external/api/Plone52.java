/**
 * Copyright (C) 2016 - 2024 Lars Dam
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3.0
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * See: http://www.gnu.org/licenses/gpl-3.0.html
 *  
 * Problemen in deze code:
 */
 package nl.amity.ijc_ui.data.external.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.openxml4j.opc.internal.ContentType;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import nl.amity.ijc_ui.io.GroepenReader;
import nl.amity.ijc_ui.ui.util.Utils;
import nl.amity.ijc_ui.io.util.HttpConnector;

public class Plone52 {
	
	private final static Logger logger = Logger.getLogger(GroepenReader.class.getName());

	
	
	
	/*
	 * // method for retrieve homepage (Example, not actively used) public static
	 * String getrequest(String url) throws Exception { HttpClient send2client =
	 * HttpClient.newHttpClient(); HttpRequest Req2client =
	 * HttpRequest.newBuilder().uri(URI.create(url)).build(); HttpResponse<String>
	 * clientRes = send2client.send(Req2client,
	 * HttpResponse.BodyHandlers.ofString()); logger.log(Level.INFO, "Body is '" +
	 * clientRes.body().toString() + "'");
	 * 
	 * return clientRes.body().toString(); }
	 */

	// method for retrieve homepage (Example, not actively used)
	public static String getrequest2(String urlstring) throws Exception {
			String result = HttpConnector.sendGET(urlstring);
		return result;
	}
	

	
	// method for logon and retrieving Token
	public static Token login (String url, String username, String password, String loginpath) throws Exception {
		Token token = null;
		Gson gson = new Gson();
		String httpsbody = "";
		JSONObject jsonOb1 = new JSONObject(); 
		jsonOb1.put("login", username);
		jsonOb1.put("password", password);
		// logger.log(Level.WARNING, "Temporarily disbled!!!");
		
		  HttpClient send2client = HttpClient.newHttpClient(); HttpRequest Req2client =
		  HttpRequest.newBuilder() .uri(URI.create("https://" + url + "/" + loginpath))
		  .header("Accept", "application/json") .header("Content-Type",
		  "application/json")
		  .POST(HttpRequest.BodyPublishers.ofString(jsonOb1.toString())) .build();
		  HttpResponse<String> response = send2client.send(Req2client,
		  HttpResponse.BodyHandlers.ofString()); httpsbody += response.body();
		 
		logger.log(Level.INFO, "httpsbody is '" + httpsbody + "'");
		token = gson.fromJson(httpsbody, Token.class);
		return token;
	}
	
	// 
    /**
     * method for creating a new page , with token for periode and ronde
     *
     * @param token - logon token for the external API 
     * @param periode - periode for page to be created
     * @param ronde - ronde for page to be created
     * @return String - OK or HTTP Status Code
     */
	public static String createpage(Token token, String url, String path, String template, int periode, int ronde) throws URISyntaxException {

		Gson gson = new Gson();
		String httpsbody = "";
		int httpsresponse = 0;
		//String token = "";
		HttpClient httpClient = HttpClient.newHttpClient();
		String bestandsnaam = "R" + periode + "-" + ronde + "Uitslag.txt";
		String dirName = "R" + periode + "-" + ronde;
		String txtUitslag[] = Utils.leesBestand(dirName + File.separator + bestandsnaam);
		String txtUitslagparagraaf = "";
		for (String r : txtUitslag) {
			txtUitslagparagraaf += r + "\r\n";
		}
		//
		String s;
		try {
			TimeZone zone = TimeZone.getDefault();
			FileTime ft = Utils.getCreationDateBestand(dirName + File.separator + bestandsnaam);
			ZonedDateTime zdt = ft.toInstant().atZone(zone.toZoneId());
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM yyyy");
			s = zdt.format(dtf);
			//logger.log(Level.INFO, "Datum is " + s.toString());

		} catch (IOException e1) {
			s = new SimpleDateFormat("dd MMMM yyyy").format(Calendar.getInstance().getTime());
			//logger.log(Level.INFO, "Datum is " + s.toString());
		}
		// temporary
		String documentTitle = "Clubavond " + s;
		//Old:String txtles = "Het was weer een leuke les met hoge opkomst.\nTrainer Lodewijk had weer een mooie les voorbereid.\n";

		bestandsnaam = "R" + periode + "-" + ronde + "Stand.txt";
		String txtStand[] = Utils.leesBestand(dirName + File.separator + bestandsnaam);
		String txtStandparagraaf = "";
		for (String r : txtStand) {
			txtStandparagraaf += r + "\n";
		}
		JSONObject jsonOb1 = new JSONObject(); 
		jsonOb1.put("@type", "Document");
		jsonOb1.put("title", documentTitle);
		jsonOb1.put("description", "Clubavond met uitslagen en stand");
		JSONObject jsonOb2 = new JSONObject(); 
		jsonOb2.put("content-type","text/html");
		//String data = template.replaceAll("\\b%Uitslag%\\b", txtUitslagparagraaf);
		//data = data.replaceAll("\\b%Stand%\\b", txtStandparagraaf);
		String data = template.replace("%Uitslag%", txtUitslagparagraaf);
		data = data.replace("%Stand%", txtStandparagraaf);
		jsonOb2.put("data",data);
		jsonOb2.put("encoding","utf-8");
		jsonOb1.put("text", jsonOb2);		
		//
		System.out.println("JSON : " + jsonOb1.toString(2));
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://" + url + "/" + path))
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + token.getToken())
				.POST(HttpRequest.BodyPublishers.ofString(jsonOb1.toString()))
				.build();			  
		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			httpsbody += response.body();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		/*
		 * if (httpsresponse / 100 != 2 ) {
		 * System.out.println("StatusCode :" + httpsresponse);
		 * System.out.println("URI :" + httpsbody); return
		 * Integer.toString(httpsresponse); }
		 */
		Map<String, Object> map = gson.fromJson(httpsbody, new TypeToken<Map<String, Object>>() {}.getType());
		Object id = map.get("id");
	        
		System.out.println("Document created with title : " + id.toString());
		httpsbody = "";
		request = HttpRequest.newBuilder()
				.uri(URI.create("https://" + url + "/" + path + "/" + id.toString() + "/@workflow/publish"))
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + token.getToken())
				.POST(HttpRequest.BodyPublishers.ofString("{}"))
				.build();			  
		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			httpsresponse = response.statusCode();
			httpsbody += response.body();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (httpsresponse / 100 != 2 ) {
//			System.out.println("StatusCode :" + httpsresponse);
//			System.out.println("URI :" + httpsbody);
			logger.log(Level.INFO, "StatusCode :" + httpsresponse);
			logger.log(Level.INFO, "URI :" + httpsbody);
			return Integer.toString(httpsresponse);
		} else {
//			System.out.println("Document published!");
		logger.log(Level.INFO, "Document published!");
			return "OK";
		}
//		return "TEMP";
	}
	
	// Delete user from Plone52!!!
	public static int delete(Token token, String username) throws Exception {
		if (token == null) {	
			throw new NullPointerException();
		}
		int httpsresponse = 0;
		HttpClient httpClient = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://www.svdestelling.nl/@users/" + username))
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + token.getToken())
				.DELETE()
				.build();			  
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		httpsresponse = response.statusCode();
		return httpsresponse;
	}

	// Get userslist from Plone52!!!
	public static String userList(Token token) throws Exception {
		if (token == null) {	
			throw new NullPointerException();
		}
		String httpsbody = "";
		HttpClient httpClient = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://www.svdestelling.nl/@users"))
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + token.getToken())
				.GET()
				.build();			  
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		httpsbody = response.body();
		return httpsbody;
	}


}
