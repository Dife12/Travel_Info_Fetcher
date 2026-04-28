package dendai.cps.tekihi.TravelInfoFetcher.sub;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CityNameConverter {

	public static void main(String[] args) {
		// 日本語の都市名と英語の都市名の対応表を作成
		Map<String, String> cityMap = createCityMap();

		// Scannerを使用してユーザーから日本語の都市名を入力
		Scanner scanner = new Scanner(System.in);
		System.out.print("日本語の都市名を入力してください: ");
		String japaneseCityName = scanner.nextLine();

		// 入力された都市名を英語に変換
		String englishCityName = convertToEnglish(japaneseCityName, cityMap);

		// 結果を出力
		if (englishCityName != null) {
			System.out.println("英語の都市名: " + englishCityName);
		} else {
			System.out.println("対応する英語の都市名が見つかりませんでした。");
		}

		scanner.close();
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

	private static String convertToEnglish(String japaneseCityName, Map<String, String> cityMap) {
		return cityMap.get(japaneseCityName);
	}
}
