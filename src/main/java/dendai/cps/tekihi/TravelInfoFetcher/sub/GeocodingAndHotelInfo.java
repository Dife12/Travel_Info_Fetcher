package dendai.cps.tekihi.TravelInfoFetcher.sub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GeocodingAndHotelInfo {

	private static final String GOOGLE_API_KEY = "xxxxx";
	private static final String RAKUTEN_API_KEY = "xxxxx";

	public static void main(String[] args) {
		try {
			// ユーザーにアドレスを入力させる
			Scanner scanner = new Scanner(System.in);
			System.out.print("目的地を入力してください: ");
			String address = scanner.nextLine();
			scanner.close();

			// Google Geocoding APIを使用して座標を取得
			double[] coordinates = getCoordinates(address);

			// 座標が正常に取得された場合
			if (coordinates != null) {
				// Rakuten Travel APIを使用してホテル情報を取得してソート
				retrieveAndSortHotelInfo(coordinates[0], coordinates[1]);
			} else {
				System.out.println("座標情報が見つかりませんでした。");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static double[] getCoordinates(String address) throws IOException {
		// Google Geocoding APIのURLを構築
		String geocodingApiUrl = "https://maps.googleapis.com/maps/api/geocode/json" +
				"?address=" + address + "&key=" + GOOGLE_API_KEY;

		// HTTPリクエストを送信し、JSONデータを取得
		String geocodingJsonData = sendHttpRequest(geocodingApiUrl);

		// JSONデータから座標を抽出
		return extractCoordinates(geocodingJsonData);
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

	private static double[] extractCoordinates(String jsonData) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(jsonData);

		JsonNode resultsNode = rootNode.path("results");
		if (resultsNode.isArray() && resultsNode.size() > 0) {
			JsonNode locationNode = resultsNode.get(0).path("geometry").path("location");

			double latitude = locationNode.path("lat").asDouble();
			double longitude = locationNode.path("lng").asDouble();

			return new double[] { latitude, longitude };
		} else {
			return null;
		}
	}

	private static void retrieveAndSortHotelInfo(double latitude, double longitude) throws IOException {
		// Rakuten Travel APIのURLを構築
		String hotelApiUrl = "https://app.rakuten.co.jp/services/api/Travel/SimpleHotelSearch/20170426?format=json&latitude="
				+ latitude + "&longitude=" + longitude
				+ "&searchRadius=1&datumType=1&applicationId=" + RAKUTEN_API_KEY;

		// HTTPリクエストを送信し、JSONデータを取得
		String hotelJsonData = sendHttpRequest(hotelApiUrl);

		// JSONデータからホテル情報を抽出してソート
		extractAndSortHotelInfo(hotelJsonData);
	}

	private static void extractAndSortHotelInfo(String jsonData) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(jsonData);

		// ページング情報
		JsonNode pagingInfoNode = rootNode.path("pagingInfo");
		int recordCount = pagingInfoNode.path("recordCount").asInt();
		System.out.println("ホテル数: " + recordCount);

		// ホテル情報
		JsonNode hotelsNode = rootNode.path("hotels");

		// ホテル情報を格納するリスト
		List<HotelInfo> hotelList = new ArrayList<>();

		for (JsonNode hotelNode : hotelsNode) {
			JsonNode hotelBasicInfoNode = hotelNode.path("hotel").path(0).path("hotelBasicInfo");
			String hotelName = hotelBasicInfoNode.path("hotelName").asText();
			String hotelInformationUrl = hotelBasicInfoNode.path("hotelInformationUrl").asText();
			int hotelMinCharge = hotelBasicInfoNode.path("hotelMinCharge").asInt();

			// HotelInfoオブジェクトを作成してリストに追加
			HotelInfo hotelInfo = new HotelInfo(hotelName, hotelInformationUrl, hotelMinCharge);
			hotelList.add(hotelInfo);
		}

		// 最低宿泊料金の小さい順にソート
		Collections.sort(hotelList, new Comparator<HotelInfo>() {
			@Override
			public int compare(HotelInfo hotel1, HotelInfo hotel2) {
				return Integer.compare(hotel1.getHotelMinCharge(), hotel2.getHotelMinCharge());
			}
		});

		// ソートされたホテル情報を表示
		for (HotelInfo hotelInfo : hotelList) {
			System.out.println("ホテル名: " + hotelInfo.getHotelName());
			System.out.println("ホテル情報URL: " + hotelInfo.getHotelInformationUrl());
			System.out.println("最低宿泊料金: " + hotelInfo.getHotelMinCharge());
			System.out.println("-------------------------");
		}
	}

	// ホテル情報を格納するクラス
	private static class HotelInfo {
		private String hotelName;
		private String hotelInformationUrl;
		private int hotelMinCharge;

		public HotelInfo(String hotelName, String hotelInformationUrl, int hotelMinCharge) {
			this.hotelName = hotelName;
			this.hotelInformationUrl = hotelInformationUrl;
			this.hotelMinCharge = hotelMinCharge;
		}

		public String getHotelName() {
			return hotelName;
		}

		public String getHotelInformationUrl() {
			return hotelInformationUrl;
		}

		public int getHotelMinCharge() {
			return hotelMinCharge;
		}
	}
}
