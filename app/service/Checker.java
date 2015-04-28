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

import static com.noorq.casser.core.Query.desc;
import static com.noorq.casser.core.Query.eq;
import static global.Global.check;
import static global.Global.emailToken;
import static global.Global.subscription;
import global.Global;
import global.PlayAsync;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import model.Check;
import model.Subscription;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import com.noorq.casser.core.Casser;
import com.noorq.casser.mapping.convert.TimeUUIDUtil;
import com.noorq.casser.support.Fun;
import com.noorq.casser.support.Fun.Tuple1;

public final class Checker {
	
	private static final String RECEIPT_PARAM = "appReceiptNum";
	private static final String USCIS_ENDPOINT = "https://egov.uscis.gov/casestatus/mycasestatus.do";

	private Checker() {
	}
	
	public static Promise<String> load(String receipt) {

		return WS.url(USCIS_ENDPOINT)
				.setQueryParameter(RECEIPT_PARAM, receipt)
				.post("content")
				.map(r -> r.getBody());

	}

	public static Promise<WSResponse> check(String applicationId, String email)  {
		
		return load(applicationId)
			.map(c -> new CheckResult(applicationId, c))
			.flatMap(cr -> PlayAsync.asPromise(Casser.session().upsert(cr).showValues(false).async(), cr))
			.flatMap(t -> notifyChanges(email, t._2));
	}
	
	
	public static void checkAll() {

		Map<String, List<Subscription>> map = Casser.session()
				.select(Subscription.class)
				.where(subscription::active, eq(true))
				.sync()
				.collect(Collectors.groupingBy(s -> s.applicationId()));
		
		map.entrySet().forEach(e -> {
			
			String applicationId = e.getKey();
			String content = load(applicationId).get(1000, TimeUnit.SECONDS);

			Check checkResult = new CheckResult(applicationId, content);
			
			Casser.session().upsert(checkResult).showValues(false).sync();
			
			e.getValue().forEach(s -> {
				notifyChanges(s.email(), checkResult);
			});
			
		});


	}
	
	public static Promise<WSResponse> notifyChanges(String email, Check checkResult) {
		
		return PlayAsync.asPromise(Casser.session().select(check::status)
		.where(check::applicationId, eq(checkResult.applicationId()))
		.orderBy(desc(check::lastAt))
		.limit(1).async())
		.map(r -> r.findFirst().orElse(Tuple1.of("_"))._1)
		.flatMap(s -> notifyChanges(email, s, checkResult));
		
	}
	
	public static Promise<WSResponse> notifyChanges(String email, String oldStatus, Check checkResult) {
		
		String unsubscribeText = getUnsubscribeText(email, checkResult.applicationId());
		
		if ("_".equals(oldStatus) || checkResult.status().equals(oldStatus)) {
			return Mailer.sendMail(email, "No Changes, Status Checked for " + checkResult.applicationId(), 
					"Current status: " + checkResult.status() + "\n" + 
			        checkResult.text() + "\n" + unsubscribeText);
		}
		else {
			return Mailer.sendMail(email, "Attention! Status Was Changed for " + checkResult.applicationId(),
					"New status: " + checkResult.status() + "\n" +
					"Previous status: " + oldStatus + "\n" + 
					checkResult.text() + "\n" + unsubscribeText);
		}
		
	}
	
	public static String getUnsubscribeText(String email, String applicationId) {
		
		UUID tokenId = UUID.randomUUID();
		
		Casser.session().insert()
		.value(emailToken::tokenId, tokenId)
		.value(emailToken::applicationId, applicationId)
		.value(emailToken::email, email)
		.usingTtl(60 * 60 * 24)
		.async();
		
		String unsubscribeLink = Global.APP_WEBSITE + "/unsubscribe?token=" + tokenId.toString();
		
		return "\nYou have got this email, because you are subscribed on this service before.\nIf you want to unsubscribe click this link " + unsubscribeLink;
		
	}
	
	public static class CheckResult implements Check {

		final String applicationId;
		final UUID checkAt = TimeUUIDUtil.createTimeUUID(new Date());
		final String content;
		final String status;
		final String text;
		
		public CheckResult(String applicationId, String content) {
			this.applicationId = applicationId;
			this.content = content;
			Fun.Tuple2<String, String> t = Parser.parse(content);
			this.status = t._1;
			this.text = t._2;
		}
		
		@Override
		public String applicationId() {
			return applicationId;
		}

		@Override
		public UUID lastAt() {
			return checkAt;
		}

		@Override
		public String content() {
			return content;
		}

		@Override
		public String status() {
			return status;
		}

		@Override
		public String text() {
			return text;
		}
		
	}
	

}
