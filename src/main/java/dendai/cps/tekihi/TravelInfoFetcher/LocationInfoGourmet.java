package dendai.cps.tekihi.TravelInfoFetcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LocationInfoGourmet {

	private static final String YOLP_API_KEY = "xxxxx";

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		System.out.print("緯度を入力してください: ");
		double latitude = scanner.nextDouble();

		System.out.print("経度を入力してください: ");
		double longitude = scanner.nextDouble();

		scanner.close();

		String apiUrl = "https://map.yahooapis.jp/search/local/V1/localSearch?appid=" +
				YOLP_API_KEY + "&gc=01&lat=" + latitude + "&lon=" + longitude + "&dist=1&output=json";

		try {
			URL url = new URL(apiUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuilder response = new StringBuilder();
				String line;

				while ((line = reader.readLine()) != null) {
					response.append(line);
				}

				reader.close();

				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode rootNode = objectMapper.readTree(response.toString());

				JsonNode featuresNode = rootNode.path("Feature");
				for (JsonNode feature : featuresNode) {
					String featureName = feature.path("Name").asText();
					System.out.println("店名: " + featureName);

					JsonNode categoryNode = feature.path("Category");
					for (JsonNode category : categoryNode) {
						String categoryName = category.asText();
						System.out.println("カテゴリー: " + categoryName);
					}

					JsonNode genreNode = feature.path("Property").path("Genre");
					for (JsonNode genre : genreNode) {
						String genreName = genre.path("Name").asText();
						System.out.println("ジャンル名: " + genreName);
					}
					System.out.println("-------------------------");
				}

			} else {
				System.out.println("HTTPエラーコード: " + responseCode);
			}

			connection.disconnect();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
