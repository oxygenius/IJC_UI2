package nl.amity.ijc_ui.io.util;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class HttpConnector {

	private final static Logger logger = Logger.getLogger(HttpConnector.class.getName());

		private static final String USER_AGENT = "Mozilla/5.0";

		public static String sendGET(String urlstring) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
			KeyStore clientStore = KeyStore.getInstance("PKCS12");
			clientStore.load(new FileInputStream("svdestelling.nl.cer"), "DeStelling".toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(clientStore, "DeStelling".toCharArray());
			KeyManager[] kms = kmf.getKeyManagers();
			KeyStore trustStore = KeyStore.getInstance("JKS");
			trustStore.load(new FileInputStream("svdestelling.nl.pfx"), "DeStelling".toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(trustStore);
			TrustManager[] tms = tmf.getTrustManagers();
			SSLContext sslContext = null;
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kms, tms, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
			URL url = new URL(urlstring);
			HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
			int responseCode = urlConn.getResponseCode();
			logger.log(Level.FINE, "GET Response Code :: " + responseCode);
			StringBuffer response = null;
			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String inputLine;
				response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				// print result
				logger.log(Level.FINEST, response.toString());
			} else {
				logger.log(Level.WARNING, "GET request did not work.");
			}
			return response.toString();

		}

		public String sendPOST(String url, String post) throws IOException {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);

			// For POST only - START
			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			os.write(post.getBytes());
			os.flush();
			os.close();
			// For POST only - END

			int responseCode = con.getResponseCode();
			logger.log(Level.FINE, "POST Response Code :: " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) { //success
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				// print result
				logger.log(Level.FINEST, () -> response.toString());
				return response.toString();
			} else {
				logger.log(Level.WARNING, "POST request did not work.");
				return "";
			}
		}
	}

