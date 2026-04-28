package dendai.cps.tekihi.TravelInfoFetcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class DestinationInfoRetrieval {

	private static final String GOOGLE_API_KEY = "xxxxx";
	private static final String RAKUTEN_API_KEY = "xxxxx";
	private static final String YOLP_API_KEY = "xxxxx";
	private static final String WEATHER_API_KEY = "xxxxx";

	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(System.in);
			System.out.print("目的地(都道府県名)を入力してください(例:東京都): ");
			String destinationForWeather = scanner.nextLine();

			System.out.print("出発日(5日先まで)を入力してください (yyyy-MM-dd)(例:2023-12-15): ");
			String inputDate = scanner.nextLine();

			System.out.print("出発地を入力してください(例:綾瀬駅): ");
			String departurePlace = scanner.nextLine();

			System.out.print("目的地を入力してください(例:北千住駅): ");
			String destination = scanner.nextLine();

			scanner.close();

			String englishCityName = convertToEnglishCity(destinationForWeather);
			if (englishCityName != null) {
				retrieveWeatherInfo(englishCityName, inputDate);
			} else {
				System.out.println("対応する都市名が見つかりませんでした。");
			}

			retrieveDirectionInfo(departurePlace, destination);

			double[] coordinates = getCoordinates(destination);

			if (coordinates != null) {
				retrieveHotelInfo(coordinates[0], coordinates[1]);
				retrievePlacesInfo(coordinates[0], coordinates[1]);
				retrieveGourmetInfo(coordinates[0], coordinates[1]);
			} else {
				System.out.println("座標情報が見つかりませんでした。");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String convertToEnglishCity(String japaneseCityName) {
		Map<String, String> cityMap = createCityMap();
		return cityMap.get(japaneseCityName);
	}

	private static Map<String, String> createCityMap() {
		Map<String, String> cityMap = new HashMap<>();
		cityMap.put("北海道", "Hokkaido");
		cityMap.put("青森県", "Aomori");
		cityMap.put("岩手県", "Iwate");
		cityMap.put("宮城県", "Miyagi");
		cityMap.put("秋田県", "Akita");
		cityMap.put("山形県", "Yamagata");
		cityMap.put("福島県", "Fukushima");
		cityMap.put("茨城県", "Ibaraki");
		cityMap.put("栃木県", "Tochigi");
		cityMap.put("群馬県", "Gunma");
		cityMap.put("埼玉県", "Saitama");
		cityMap.put("千葉県", "Chiba");
		cityMap.put("東京都", "Tokyo");
		cityMap.put("神奈川県", "Kanagawa");
		cityMap.put("新潟県", "Niigata");
		cityMap.put("富山県", "Toyama");
		cityMap.put("石川県", "Ishikawa");
		cityMap.put("福井県", "Fukui");
		cityMap.put("山梨県", "Yamanashi");
		cityMap.put("長野県", "Nagano");
		cityMap.put("岐阜県", "Gifu");
		cityMap.put("静岡県", "Shizuoka");
		cityMap.put("愛知県", "Aichi");
		cityMap.put("三重県", "Mie");
		cityMap.put("滋賀県", "Shiga");
		cityMap.put("京都府", "Kyoto");
		cityMap.put("大阪府", "Osaka");
		cityMap.put("兵庫県", "Hyogo");
		cityMap.put("奈良県", "Nara");
		cityMap.put("和歌山県", "Wakayama");
		cityMap.put("鳥取県", "Tottori");
		cityMap.put("島根県", "Shimane");
		cityMap.put("岡山県", "Okayama");
		cityMap.put("広島県", "Hiroshima");
		cityMap.put("山口県", "Yamaguchi");
		cityMap.put("徳島県", "Tokushima");
		cityMap.put("香川県", "Kagawa");
		cityMap.put("愛媛県", "Ehime");
		cityMap.put("高知県", "Kochi");
		cityMap.put("福岡県", "Fukuoka");
		cityMap.put("佐賀県", "Saga");
		cityMap.put("長崎県", "Nagasaki");
		cityMap.put("熊本県", "Kumamoto");
		cityMap.put("大分県", "Oita");
		cityMap.put("宮崎県", "Miyazaki");
		cityMap.put("鹿児島県", "Kagoshima");
		cityMap.put("沖縄県", "Okinawa");

		return cityMap;
	}

	private static void retrieveWeatherInfo(String englishCityName, String inputDate) {
		String weatherUrl = "http://api.openweathermap.org/data/2.5/forecast?q=" + englishCityName
				+ "&appid=" + WEATHER_API_KEY + "&lang=ja&units=metric";

		try {
			String weatherjsonData = sendHttpRequest(weatherUrl);
			extractWeatherInfo(weatherjsonData, inputDate);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void extractWeatherInfo(String weatherjsonData, String inputDate) {
		System.out.println("-------------------------");
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(weatherjsonData);
			JsonNode listNode = rootNode.get("list");
			System.out.println(inputDate + "の目的地の天気は: ");
			for (JsonNode forecastNode : listNode) {
				long timestamp = forecastNode.get("dt").asLong() * 1000;
				Date date = new Date(timestamp);
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDate = dateFormat.format(date);
				if (formattedDate.startsWith(inputDate)) {
					double temperature = forecastNode.get("main").get("temp").asDouble();
					String description = forecastNode.get("weather").get(0).get("description").asText();
					SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
					String formattedTime = timeFormat.format(date);

					System.out.println(formattedTime);
					System.out.println("気温: " + temperature + " °C");
					System.out.println("天気: " + description);
					System.out.println();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static double[] getCoordinates(String destination) throws IOException {
		String coordinatesApiUrl = "https://maps.googleapis.com/maps/api/geocode/json" +
				"?address=" + destination + "&key=" + GOOGLE_API_KEY;

		String coordinatesJsonData = sendHttpRequest(coordinatesApiUrl);

		return extractCoordinates(coordinatesJsonData);
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

	private static double[] extractCoordinates(String coordinatesData) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(coordinatesData);

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

	private static void retrieveHotelInfo(double latitude, double longitude) throws IOException {
		String hotelApiUrl = "https://app.rakuten.co.jp/services/api/Travel/SimpleHotelSearch/20170426?format=json&latitude="
				+ latitude + "&longitude=" + longitude
				+ "&searchRadius=1&datumType=1&applicationId=" + RAKUTEN_API_KEY;

		String hotelJsonData = sendHttpRequest(hotelApiUrl);

		extractHotelInfo(hotelJsonData);
	}

	private static void extractHotelInfo(String jsonData) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(jsonData);

		JsonNode pagingInfoNode = rootNode.path("pagingInfo");
		int recordCount = pagingInfoNode.path("recordCount").asInt();
		System.out.println("近くのホテル数: " + recordCount);
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
			System.out.println(hotelInfo.getHotelName());
			System.out.println("ホテル情報URL: " + hotelInfo.getHotelInformationUrl());
			System.out.println("最低宿泊料金: " + hotelInfo.getHotelMinCharge());
			System.out.println();
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

	private static void retrievePlacesInfo(double latitude, double longitude) {
		System.out.println("-------------------------");
		List<String> keywords = Arrays.asList("美術館", "博物館");
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
					System.out.println("付近の" + keyword + ": ");

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
						System.out.println();
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

	private static void retrieveGourmetInfo(double latitude, double longitude) {
		String apiUrl = "https://map.yahooapis.jp/search/local/V1/localSearch?appid=" +
				YOLP_API_KEY + "&gc=01&lat=" + latitude + "&lon=" + longitude + "&dist=1&output=json";

		try {
			String jsonResponse = sendHttpRequest(apiUrl);
			JsonNode featuresNode = new ObjectMapper().readTree(jsonResponse).path("Feature");

			System.out.println("近くのグルメ・レストラン: ");

			for (JsonNode feature : featuresNode) {
				String featureName = feature.path("Name").asText();
				System.out.println("店名: " + featureName);

				JsonNode categoryNode = feature.path("Category");
				for (JsonNode category : categoryNode) {
					String categoryName = category.asText();
					System.out.println("カテゴリー: " + categoryName);
				}
				System.out.println();
			}
			System.out.println("-------------------------");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void retrieveDirectionInfo(String departurePlace, String destination) {
		try {
			String directionsApiUrl = "https://maps.googleapis.com/maps/api/directions/json"
					+ "?origin=" + departurePlace
					+ "&destination=" + destination
					+ "&key=" + GOOGLE_API_KEY;

			String jsonData = sendHttpRequest(directionsApiUrl);

			System.out.println("-------------------------");
			System.out.println(departurePlace + "から " + destination + "までの");
			displayDirectionInfo(jsonData);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void displayDirectionInfo(String jsonData) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(jsonData);

			JsonNode routesNode = rootNode.path("routes");
			for (JsonNode routeNode : routesNode) {
				String distance = routeNode.path("legs").get(0).path("distance").path("text").asText();
				String duration = routeNode.path("legs").get(0).path("duration").path("text").asText();

				System.out.println("距離: " + distance);
				System.out.println("自動車での所要時間: " + duration);
				System.out.println("-------------------------");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
