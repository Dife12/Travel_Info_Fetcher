package dendai.cps.tekihi.TravelInfoFetcher.sub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

public class CoordinatesWithDOMAndXPath {

	private static final String YOLP_API_KEY = "xxxxx";

	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(System.in);

			System.out.print("目的地を入力してください: ");
			String address = scanner.nextLine();
			String encodedAddress = URLEncoder.encode(address, "UTF-8");
			String apiUrl = "https://map.yahooapis.jp/geocode/cont/V1/contentsGeoCoder?appid=" + YOLP_API_KEY
					+ "&query=" + encodedAddress + "&category=address";
			String xmlData = xmlUrl(apiUrl);
			CoordinatesInfo coordinatesInfo = extractCoordinatesWithDOMAndXPath(xmlData);

			if (coordinatesInfo != null) {
				System.out.println("緯度: " + coordinatesInfo.getLatitude());
				System.out.println("経度: " + coordinatesInfo.getLongitude());
			} else {
				System.out.println("位置情報が見つかりませんでした。");
			}

			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String xmlUrl(String url) throws IOException {
		StringBuilder result = new StringBuilder();

		URL apiUrl = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
		connection.setRequestMethod("GET");

		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					result.append(line);
				}
			}
		} else {
			System.out.println("HTTPエラーコード: " + connection.getResponseCode());
		}

		return result.toString();
	}

	private static CoordinatesInfo extractCoordinatesWithDOMAndXPath(String xmlData) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(xmlData)));

			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xPath = xPathFactory.newXPath();

			XPathExpression latitudeExpr = xPath.compile("//Coordinates/latitude/text()");
			XPathExpression longitudeExpr = xPath.compile("//Coordinates/longitude/text()");

			String latitude = latitudeExpr.evaluate(document);
			String longitude = longitudeExpr.evaluate(document);

			if (latitude != null && !latitude.isEmpty() && longitude != null && !longitude.isEmpty()) {
				return new CoordinatesInfo(Double.parseDouble(longitude), Double.parseDouble(latitude));
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static class CoordinatesInfo {
		private final double latitude;
		private final double longitude;

		public CoordinatesInfo(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}

		public double getLatitude() {
			return latitude;
		}

		public double getLongitude() {
			return longitude;
		}
	}
}
