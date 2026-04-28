package dendai.cps.tekihi.TravelInfoFetcher.sub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class YahooPlaceInfo {

	private static final String API_KEY = "xxxxx";

	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(System.in);

			System.out.print("緯度を入力してください: ");
			double latitude = scanner.nextDouble();

			System.out.print("経度を入力してください: ");
			double longitude = scanner.nextDouble();

			String apiUrl = "https://map.yahooapis.jp/placeinfo/V1/get?lat=" + latitude + "&lon=" + longitude
					+ "&appid=" + API_KEY + "&output=json";

			String jsonData = fetchDataFromUrl(apiUrl);
			extractInformation(jsonData);

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

	private static void extractInformation(String jsonData) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(jsonData);

		JsonNode resultSetNode = rootNode.path("ResultSet");
		JsonNode resultsNode = resultSetNode.path("Result");

		for (JsonNode resultNode : resultsNode) {
			String name = resultNode.path("Name").asText();
			String category = resultNode.path("Category").asText();
			String label = resultNode.path("Label").asText();
			String where = resultNode.path("Where").asText();
			double score = resultNode.path("Score").asDouble();

			System.out.println("Name: " + name);
			System.out.println("Category: " + category);
			System.out.println("Label: " + label);
			System.out.println("Where: " + where);
			System.out.println("Score: " + score);
			System.out.println("------------------------");
		}
	}
}
