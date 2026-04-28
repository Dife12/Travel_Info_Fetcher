package dendai.cps.tekihi.TravelInfoFetcher.sub;

import com.deepl.api.TextResult;
import com.deepl.api.Translator;

class TranslatorEx {
	Translator translator;

	public TranslatorEx() throws Exception {
		String authKey = "xxxxx"; // Replace with your key
		translator = new Translator(authKey);
		TextResult result = translator.translateText("東京都", null, "en-US");
		System.out.println(result.getText());
	}

	public static void main(String[] args) {
		try {
			new TranslatorEx();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
