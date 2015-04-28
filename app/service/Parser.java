/*
 *      Copyright (C) 2015 Noorq, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.noorq.casser.support.Fun;

public final class Parser {

	private Parser() {
	}
	
	public static Fun.Tuple2<String, String> parse(String content) {
		
		String status = null;
		String text = null;
		
		Document doc = Jsoup.parse(content);

		Elements headers = doc.select("h1");
		
		for (Element h1 : headers) {
			
			status = h1.text();
			
			Element textEl = h1.nextElementSibling();
			if (textEl != null) {
				text = textEl.text();
			}

			break;
		}
		
		if (status == null) {
		
			Element error = doc.getElementById("formErrorMessages");
				
			if (error != null) {
				status = "Receipt Number Error";
				text = error.text();
			}
		}

		return Fun.Tuple2.of(status, text);

	}
	
}
