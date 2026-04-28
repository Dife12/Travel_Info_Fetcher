package dendai.cps.tekihi.TravelInfoFetcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleMapsDirectionInfo {

	private static final String GOOGLE_API_KEY = "xxxxx";

	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(System.in);

			System.out.print("出発地を入力してください: ");
			String origin = scanner.nextLine();

			System.out.print("目的地を入力してください: ");
			String destination = scanner.nextLine();

			String apiUrl = "https://maps.googleapis.com/maps/api/directions/json?origin="
					+ origin + "&destination=" + destination + "&key=" + GOOGLE_API_KEY;
			String jsonData = fetchDataFromUrl(apiUrl);

			extractDirectionsInfo(jsonData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String fetchDataFromUrl(String apiUrl) throws IOException {
		StringBuilder result = new StringBuilder();
		URL url = new URL(apiUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		try {
			// Set request method to GET
			connection.setRequestMethod("GET");

			// Get the response code
			int responseCode = connection.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				// Read the response
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					String line;
					while ((line = reader.readLine()) != null) {
						result.append(line);
					}
				}
			} else {
				throw new IOException("Failed to fetch data from URL. Response Code: " + responseCode);
			}
		} finally {
			connection.disconnect();
		}

		return result.toString();
	}

	private static void extractDirectionsInfo(String jsonData) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(jsonData);

		JsonNode routesNode = rootNode.path("routes");
		for (JsonNode routeNode : routesNode) {
			String distanceText = routeNode.path("legs").get(0).path("distance").path("text").asText();
			String durationText = routeNode.path("legs").get(0).path("duration").path("text").asText();

			System.out.println("距離: " + distanceText);
			System.out.println("所要時間: " + durationText);
		}
	}
}
