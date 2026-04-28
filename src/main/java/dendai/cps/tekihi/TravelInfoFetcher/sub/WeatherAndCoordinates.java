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

public class WeatherAndCoordinates {

	private static final String OPENWEATHERMAP_API_KEY = "xxxxx";
	private static final String YAHOO_API_KEY = "xxxxx";

	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(System.in);

			System.out.print("都市名を入力してください (in English): ");
			String city = scanner.nextLine();

			System.out.print("日付を入力してください (yyyy-MM-dd): ");
			String userInputDate = scanner.nextLine();

			// 天気情報取得
			String weatherApiUrl = "http://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid="
					+ OPENWEATHERMAP_API_KEY + "&lang=ja&units=metric";
			String weatherJsonData = fetchDataFromUrl(weatherApiUrl);

			if (weatherJsonData != null) {
				displayWeatherInfo(weatherJsonData, userInputDate);
			} else {
				System.out.println("OpenWeatherMapのAPIからのデータ取得に失敗しました。もう一度入力してください。");
			}

			// 位置情報取得
			System.out.print("住所を入力してください: ");
			String address = scanner.nextLine();
			String encodedAddress = URLEncoder.encode(address, "UTF-8");
			String coordinatesApiUrl = "https://map.yahooapis.jp/geocode/cont/V1/contentsGeoCoder?appid="
					+ YAHOO_API_KEY + "&query=" + encodedAddress + "&category=address";
			String xmlData = fetchDataFromUrl(coordinatesApiUrl);
			String coordinates = extractCoordinates(xmlData);

			System.out.println("Coordinates: " + coordinates);

			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
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
			System.out.println("HTTPエラーコード: " + connection.getResponseCode());
		}

		return result.toString();
	}

	private static String extractCoordinates(String xmlData) {
		Pattern pattern = Pattern.compile("<Coordinates>(.*?)</Coordinates>");
		Matcher matcher = pattern.matcher(xmlData);

		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return "Coordinates not found";
		}
	}

	private static void displayWeatherInfo(String jsonData, String userInputDate) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(jsonData);

			JsonNode listNode = rootNode.get("list");
			for (JsonNode forecastNode : listNode) {
				long timestamp = forecastNode.get("dt").asLong() * 1000;
				Date date = new Date(timestamp);
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDate = dateFormat.format(date);

				if (formattedDate.startsWith(userInputDate)) {
					double temperature = forecastNode.get("main").get("temp").asDouble();
					String weatherDescription = forecastNode.get("weather").get(0).get("description").asText();

					System.out.println("Date: " + formattedDate);
					System.out.println("Temperature: " + temperature + " °C");
					System.out.println("Weather: " + weatherDescription);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
