package dendai.cps.tekihi.TravelInfoFetcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GooglePlacesInfo {

	private static final String GOOGLE_API_KEY = "xxxxx";

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		// ユーザーに緯度を入力させる
		System.out.print("緯度を入力してください: ");
		double latitude = scanner.nextDouble();

		// ユーザーに経度を入力させる
		System.out.print("経度を入力してください: ");
		double longitude = scanner.nextDouble();

		// 複数のキーワードをリストで管理
		List<String> keywords = Arrays.asList("美術館", "公園", "カフェ"); // 任意のキーワードを追加

		int radius = 1000;

		for (String keyword : keywords) {
			String apiUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=" + GOOGLE_API_KEY
					+ "&location=" + latitude + "," + longitude
					+ "&radius=" + radius
					+ "&language=ja"
					+ "&keyword=" + keyword;

			try {
				// HTTPリクエストを作成
				URL url = new URL(apiUrl);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");

				// レスポンスを読み取る
				int responseCode = connection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					StringBuilder response = new StringBuilder();
					String line;

					while ((line = reader.readLine()) != null) {
						response.append(line);
					}

					reader.close();

					// JSONレスポンスを解析
					ObjectMapper objectMapper = new ObjectMapper();
					JsonNode rootNode = objectMapper.readTree(response.toString());

					// JSONから情報を抽出
					JsonNode resultsNode = rootNode.path("results");
					System.out.println("Results for keyword: " + keyword);
					for (JsonNode result : resultsNode) {
						String placeName = result.path("name").asText();
						System.out.println("Place Name: " + placeName);
					}
					System.out.println("-------------------------");

				} else {
					System.out.println("HTTP request failed with response code: " + responseCode);
				}

				connection.disconnect();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Scannerを閉じる
		scanner.close();
	}
}
