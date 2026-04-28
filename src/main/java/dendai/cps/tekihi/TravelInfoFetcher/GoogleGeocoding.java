package dendai.cps.tekihi.TravelInfoFetcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleGeocoding {

	private static final String GOOGLE_API_KEY = "xxxxx";

	public static void main(String[] args) {
		try {
			// ユーザーにアドレスを入力させる
			Scanner scanner = new Scanner(System.in);
			System.out.print("目的地を入力してください: ");
			String address = scanner.nextLine();
			scanner.close();

			// URLを構築
			String apiUrl = "https://maps.googleapis.com/maps/api/geocode/json" +
					"?address=" + address + "&key=" + GOOGLE_API_KEY;

			// HTTPリクエストを送信し、結果を取得
			String jsonData = sendHttpRequest(apiUrl);

			// JSONデータから座標情報を抽出
			coordinates(jsonData);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String sendHttpRequest(String url) throws IOException {
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

		connection.disconnect();

		return result.toString();
	}

	private static void coordinates(String jsonData) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(jsonData);

		JsonNode resultsNode = rootNode.path("results");
		if (resultsNode.isArray() && resultsNode.size() > 0) {
			JsonNode locationNode = resultsNode.get(0).path("geometry").path("location");

			double latitude = locationNode.path("lat").asDouble();
			double longitude = locationNode.path("lng").asDouble();

			System.out.println("緯度: " + latitude);
			System.out.println("経度: " + longitude);
		} else {
			System.out.println("座標情報が見つかりませんでした。");
		}
	}
}
