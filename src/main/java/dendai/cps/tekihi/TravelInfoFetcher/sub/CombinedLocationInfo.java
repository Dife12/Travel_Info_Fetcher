package dendai.cps.tekihi.TravelInfoFetcher.sub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * ユーザーが住所を入力すると、その住所に対応する緯度と経度を取得し、
 * それを用いて場所情報を取得します。(例:東京都東京電機大学)
 */
public class CombinedLocationInfo {

	private static final String GEOCODE_API_KEY = "xxxxx";
	private static final String PLACEINFO_API_KEY = "xxxxx";

	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(System.in);

			// ユーザーに住所を入力してもらう
			System.out.print("目的地を入力してください: ");
			String address = scanner.nextLine();
			String encodedAddress = URLEncoder.encode(address, "UTF-8");

			CoordinatesInfo coordinatesInfo = getCoordinates(encodedAddress);

			if (coordinatesInfo != null) {
				getPlaceInformation(coordinatesInfo.getLatitude(), coordinatesInfo.getLongitude());
			} else {
				System.out.println("位置情報が見つかりませんでした。");
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

		// XML データから緯度経度を抽出
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

	private static void getPlaceInformation(double latitude, double longitude) throws IOException {
		String apiUrl = "https://map.yahooapis.jp/placeinfo/V1/get?lat=" + latitude + "&lon=" + longitude
				+ "&appid=" + PLACEINFO_API_KEY + "&output=json";
		String jsonData = fetchDataFromUrl(apiUrl);
		// JSON データから場所情報を抽出して表示
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(jsonData);

		JsonNode resultNode = rootNode.path("ResultSet").path("Result");

		if (resultNode.isArray() && resultNode.size() > 0) {
			for (JsonNode placeNode : resultNode) {
				String name = placeNode.path("Name").asText();
				String category = placeNode.path("Category").asText();

				System.out.println("名前: " + name);
				System.out.println("カテゴリ: " + category);
				System.out.println("------------------------");
			}
		} else {
			System.out.println("場所情報が見つかりませんでした。");
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
			System.out.println("HTTP エラーコード: " + connection.getResponseCode());
		}

		return result.toString();
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
