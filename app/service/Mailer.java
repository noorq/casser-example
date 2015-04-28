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

import global.Global;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

public final class Mailer {

	private Mailer() {
	}

	public static Promise<WSResponse> sendMail(String to, String message,
			String text) {

		return WS.url(Global.MAILGUN_ENDPOINT)
				.setQueryParameter("from", Global.FROM_MAIL)
				.setQueryParameter("to", to)
				.setQueryParameter("subject", message)
				.setQueryParameter("text", text)
				.setAuth("api", Global.MAILGUN_KEY).post("content");

	}

}
