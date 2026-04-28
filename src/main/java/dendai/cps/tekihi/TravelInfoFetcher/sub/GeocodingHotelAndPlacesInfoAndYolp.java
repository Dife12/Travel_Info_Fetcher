package dendai.cps.tekihi.TravelInfoFetcher.sub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GeocodingHotelAndPlacesInfoAndYolp {

	private static final String GOOGLE_API_KEY = "xxxxx";
	private static final String RAKUTEN_API_KEY = "xxxxx";
	private static final String YOLP_API_KEY = "xxxxx";

	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(System.in);
			System.out.println("目的地を入力してください: ");
			String address = scanner.nextLine();
			scanner.close();

			double[] coordinates = getCoordinates(address);

			if (coordinates != null) {
				retrieveAndSortHotelInfo(coordinates[0], coordinates[1]);
				retrieveAndSortPlacesInfo(coordinates[0], coordinates[1]);
				retrieveAndSortGourmetInfo(coordinates[0], coordinates[1]);
			} else {
				System.out.println("座標情報が見つかりませんでした。");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static double[] getCoordinates(String address) throws IOException {
		String geocodingApiUrl = "https://maps.googleapis.com/maps/api/geocode/json" +
				"?address=" + address + "&key=" + GOOGLE_API_KEY;

		String geocodingJsonData = sendHttpRequest(geocodingApiUrl);

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
		String hotelApiUrl = "https://app.rakuten.co.jp/services/api/Travel/SimpleHotelSearch/20170426?format=json&latitude="
				+ latitude + "&longitude=" + longitude
				+ "&searchRadius=1&datumType=1&applicationId=" + RAKUTEN_API_KEY;

		String hotelJsonData = sendHttpRequest(hotelApiUrl);

		extractAndSortHotelInfo(hotelJsonData);
	}

	private static void extractAndSortHotelInfo(String jsonData) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(jsonData);

		JsonNode pagingInfoNode = rootNode.path("pagingInfo");
		int recordCount = pagingInfoNode.path("recordCount").asInt();
		System.out.println("-------------------------");
		System.out.println("ホテル数: " + recordCount);
		System.out.println("-------------------------");

		JsonNode hotelsNode = rootNode.path("hotels");
		List<HotelInfo> hotelList = new ArrayList<>();

		for (JsonNode hotelNode : hotelsNode) {
			JsonNode hotelBasicInfoNode = hotelNode.path("hotel").path(0).path("hotelBasicInfo");
			String hotelName = hotelBasicInfoNode.path("hotelName").asText();
			String hotelInformationUrl = hotelBasicInfoNode.path("hotelInformationUrl").asText();
			int hotelMinCharge = hotelBasicInfoNode.path("hotelMinCharge").asInt();

			HotelInfo hotelInfo = new HotelInfo(hotelName, hotelInformationUrl, hotelMinCharge);
			hotelList.add(hotelInfo);
		}

		Collections.sort(hotelList, new Comparator<HotelInfo>() {
			@Override
			public int compare(HotelInfo hotel1, HotelInfo hotel2) {
				return Integer.compare(hotel1.getHotelMinCharge(), hotel2.getHotelMinCharge());
			}
		});

		for (HotelInfo hotelInfo : hotelList) {
			System.out.println("ホテル名: " + hotelInfo.getHotelName());
			System.out.println("ホテル情報URL: " + hotelInfo.getHotelInformationUrl());
			System.out.println("最低宿泊料金: " + hotelInfo.getHotelMinCharge());
			System.out.println("-------------------------");
		}
	}

	private static void retrieveAndSortPlacesInfo(double latitude, double longitude) {
		List<String> keywords = Arrays.asList("美術館", "博物館", "カフェ");
		int radius = 1000;

		for (String keyword : keywords) {
			String apiUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=" + GOOGLE_API_KEY
					+ "&location=" + latitude + "," + longitude
					+ "&radius=" + radius
					+ "&language=ja"
					+ "&keyword=" + keyword;

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

					JsonNode resultsNode = rootNode.path("results");
					System.out.println(keyword + ": ");

					// ソート用のComparatorを定義
					Comparator<JsonNode> ratingComparator = new Comparator<JsonNode>() {
						@Override
						public int compare(JsonNode node1, JsonNode node2) {
							double rating1 = node1.path("rating").asDouble();
							double rating2 = node2.path("rating").asDouble();
							return Double.compare(rating2, rating1);
						}
					};

					List<JsonNode> sortedResults = new ArrayList<>();
					for (JsonNode result : resultsNode) {
						sortedResults.add(result);
					}
					Collections.sort(sortedResults, ratingComparator);

					for (JsonNode result : sortedResults) {
						String placeName = result.path("name").asText();
						System.out.println(placeName);
						String rating = result.path("rating").asText();
						System.out.println("評価:" + rating);
					}
					System.out.println("-------------------------");

				} else {
					System.out.println("HTTPエラーコード: " + responseCode);
				}

				connection.disconnect();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void retrieveAndSortGourmetInfo(double latitude, double longitude) {
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
					
					System.out.println("ジャンル名: ");
					JsonNode genreNode = feature.path("Property").path("Genre");
					for (JsonNode genre : genreNode) {
						String genreName = genre.path("Name").asText();
						System.out.println(genreName);
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