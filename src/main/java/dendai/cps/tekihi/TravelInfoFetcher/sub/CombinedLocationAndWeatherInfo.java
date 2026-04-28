package dendai.cps.tekihi.TravelInfoFetcher.sub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CombinedLocationAndWeatherInfo {

	private static final String GEOCODE_API_KEY = "xxxxx";
	private static final String WEATHER_API_KEY = "xxxxx";

	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(System.in);

			// User inputs address
			System.out.print("Enter the address: ");
			String address = scanner.nextLine();
			String encodedAddress = URLEncoder.encode(address, "UTF-8");

			// Get coordinates from the address
			CoordinatesInfo coordinatesInfo = getCoordinates(encodedAddress);

			if (coordinatesInfo != null) {
				// Get weather information for the coordinates
				getWeatherInformation(coordinatesInfo.getLatitude(), coordinatesInfo.getLongitude());
			} else {
				System.out.println("Location information not found.");
			}

			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static CoordinatesInfo getCoordinates(String encodedAddress) throws IOException {
		String apiUrl = "https://map.yahooapis.jp/geocode/cont/V1/contentsGeoCoder?appid=" + GEOCODE_API_KEY
				+ "&query=" + encodedAddress + "&category=address";
		String xmlData = fetchDataFromUrl(apiUrl);

		// Extract latitude and longitude from XML data
		Pattern pattern = Pattern.compile("<Coordinates>(.*?),(.*)</Coordinates>");
		Matcher matcher = pattern.matcher(xmlData);

		if (matcher.find()) {
			double latitude = Double.parseDouble(matcher.group(1));
			double longitude = Double.parseDouble(matcher.group(2));
			return new CoordinatesInfo(longitude, latitude);
		} else {
			return null;
		}
	}

	private static void getWeatherInformation(double latitude, double longitude) throws IOException {
		String apiUrl = "http://api.openweathermap.org/data/2.5/forecast?lat=" + latitude + "&lon=" + longitude
				+ "&appid=" + WEATHER_API_KEY + "&lang=ja&units=metric";
		String jsonData = fetchDataFromUrl(apiUrl);

		if (jsonData != null) {
			displayWeatherInfo(jsonData);
		} else {
			System.out.println("Weather information not available.");
		}
	}

	private static String fetchDataFromUrl(String url) throws IOException {
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
			System.out.println("HTTP Error Code: " + connection.getResponseCode());
		}

		return result.toString();
	}

	private static void displayWeatherInfo(String jsonData) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(jsonData);

			JsonNode listNode = rootNode.get("list");
			for (JsonNode forecastNode : listNode) {
				long timestamp = forecastNode.get("dt").asLong() * 1000;
				Date date = new Date(timestamp);
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDate = dateFormat.format(date);

				double temperature = forecastNode.get("main").get("temp").asDouble();
				String weatherDescription = forecastNode.get("weather").get(0).get("description").asText();
				SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
				String formattedTime = timeFormat.format(date);

				System.out.println("-----" + formattedTime + " on " + formattedDate + "-----");
				System.out.println("Temperature: " + temperature + " °C");
				System.out.println("Weather: " + weatherDescription);
				System.out.println("------------------------");
			}
		} catch (IOException e) {
			e.printStackTrace();
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
