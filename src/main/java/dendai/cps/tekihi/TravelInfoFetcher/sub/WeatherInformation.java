package dendai.cps.tekihi.TravelInfoFetcher.sub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// 5日先までの天気予報について抽出することができる。
public class WeatherInformation {

	private static final String API_KEY = "xxxxx";

	public static void main(String[] args) {
		try {
			// ユーザーに都市名と日付を入力させる
			Scanner scanner = new Scanner(System.in);
			System.out.print("目的地(都市名)を入力してください (in English): ");
			String city = scanner.nextLine();

			System.out.print("日付を入力してください (yyyy-MM-dd): ");
			String userInputDate = scanner.nextLine();

			String apiUrl = "http://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + API_KEY
					+ "&lang=ja&units=metric";
			String jsonData = getWeatherData(apiUrl);

			if (jsonData != null) {
				getUserInputAndDisplayInfo(jsonData, userInputDate);
			} else {
				System.out.println("APIからのデータ取得に失敗しました。もう一度入力してください。");
			}

			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getWeatherData(String apiUrl) throws IOException {
		try {
			URL url = new URL(apiUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			}

			reader.close();
			connection.disconnect();

			return stringBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void getUserInputAndDisplayInfo(String jsonData, String userInputDate) {
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
