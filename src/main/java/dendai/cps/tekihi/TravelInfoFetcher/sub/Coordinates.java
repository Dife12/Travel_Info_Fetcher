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

/* 
 * 場所を表すキーワード(東京都新宿、東京都葛飾区北千住駅、東京都東京電機大学など)を入力し、
 * 位置情報（緯度、経度）を出力します。
 */

public class Coordinates {

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
			CoordinatesInfo coordinatesInfo = extractCoordinates(xmlData);

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

	private static CoordinatesInfo extractCoordinates(String xmlData) {
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
