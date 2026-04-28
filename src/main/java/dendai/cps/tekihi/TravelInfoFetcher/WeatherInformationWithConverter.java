package dendai.cps.tekihi.TravelInfoFetcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeatherInformationWithConverter {

	private static final String WEATHER_API_KEY = "xxxxx";

	public static void main(String[] args) {
		try {
			// 日本語の都市名を英語に変換
			String englishCityName = getEnglishCityName();

			if (englishCityName != null) {
				// ユーザーに日付を入力させる
				Scanner scanner = new Scanner(System.in);
				System.out.print("出発日を入力してください (yyyy-MM-dd): ");
				String userInputDate = scanner.nextLine();

				String apiUrl = "http://api.openweathermap.org/data/2.5/forecast?q=" + englishCityName + "&appid="
						+ WEATHER_API_KEY + "&lang=ja&units=metric";
				String jsonData = getWeatherData(apiUrl);

				if (jsonData != null) {
					displayWeatherInfo(jsonData, userInputDate);
				} else {
					System.out.println("データ取得が失敗しました。もう一度入力してください。");
				}

				scanner.close();
			} else {
				System.out.println("対応する都市名が見つかりませんでした。");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getEnglishCityName() {
		// 日本語の都市名を英語に変換
		Map<String, String> cityMap = createCityMap();

		Scanner scanner = new Scanner(System.in);
		System.out.print("目的地(都道府県名)を入力してください: ");
		String japaneseCityName = scanner.nextLine();

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

	private static String getWeatherData(String apiUrl) throws IOException {
		try {
			URL url = new URL(apiUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				StringBuilder stringBuilder = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					stringBuilder.append(line);
				}
				return stringBuilder.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void displayWeatherInfo(String jsonData, String userInputDate) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(jsonData);

			JsonNode listNode = rootNode.get("list");
			for (JsonNode forecastNode : listNode) {
				long timestamp = forecastNode.get("dt").asLong() * 1000;
				Date date = new Date(timestamp);
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDate = dateFormat.format(date);

				if (formattedDate.startsWith(userInputDate)) {
					double temperature = forecastNode.get("main").get("temp").asDouble();
					String weatherDescription = forecastNode.get("weather").get(0).get("description").asText();
					SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
					String formattedTime = timeFormat.format(date);

					System.out.println("-----" + formattedTime + "-----");
					System.out.println("気温: " + temperature + " °C");
					System.out.println("天気: " + weatherDescription);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
