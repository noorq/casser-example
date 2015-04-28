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
package controllers;

import static com.noorq.casser.core.Query.eq;
import static global.Global.emailToken;
import static global.Global.subscription;
import global.PlayAsync;

import java.util.Optional;
import java.util.UUID;

import model.EmailToken;
import play.Logger;
import play.Routes;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import scala.Option;
import service.Checker;
import service.Parser;
import views.html.index;
import views.html.unsubscribe;

import com.datastax.driver.core.ResultSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.noorq.casser.core.Casser;

public class Application extends Controller {

	public static Result javascriptRoutes() {
	    response().setContentType("text/javascript");
	    return ok(
	        Routes.javascriptRouter("JsRoutes",
	            routes.javascript.Application.doSubscribe(),
	            routes.javascript.Application.doUnsubscribe()
	        )
	    );
	}
	
	public static Result index() {
		return ok(index.render("Your new application is ready."));
	}

	public static Promise<Result> doSubscribe(String receipt, String email) {
		
		if (receipt == null || receipt.isEmpty()) {
			return Promise.pure(badRequest("Missing parameter [receipt]"));
		}
		if (email == null || email.isEmpty()) {
			return Promise.pure(badRequest("Missing parameter [email]"));
		}
		
		Logger.info("DoSubscribe " + receipt + ", email " + email);
		
		ListenableFuture<ResultSet> future = Casser.session().upsert()
		.value(subscription::applicationId, receipt)
		.value(subscription::email, email)
		.value(subscription::active, true)
		.async();

		return PlayAsync.asPromise(future)
		.flatMap(t -> Checker.check(receipt, email))
		.map(t -> ok());

	}

	public static Promise<Result> unsubscribe(Option<String> token) {
		
		UUID tokenId = parseToken(token);
		
		if (tokenId == null) {
			return Promise.pure(renderUnsubscribe(Optional.empty()));
		}
		
		Logger.info("Unsubscribe Token = " + token);
	
		return PlayAsync.asPromise(Casser.session().select(EmailToken.class)
				.where(emailToken::tokenId, eq(tokenId))
				.async())
				.map(t -> t.findFirst())
				.map(et -> renderUnsubscribe(et));

	}
	
	private static Result renderUnsubscribe(Optional<EmailToken> et) {
		if (et.isPresent()) {
			return ok(unsubscribe.render(et.get().applicationId(), et.get().email()));
		}
		else {
			return ok(unsubscribe.render("", ""));
		}
	}
	
	private static UUID parseToken(Option<String> token) {
		if (token.isEmpty()) {
			return null;
		}
		try {
			return UUID.fromString(token.get());
		}
		catch(IllegalArgumentException e) {
			return null;
		}
	}
	
	public static Promise<Result> doUnsubscribe(String receipt, String email) {

		if (receipt == null || receipt.isEmpty()) {
			return Promise.pure(badRequest("Missing parameter [receipt]"));
		}
		if (email == null || email.isEmpty()) {
			return Promise.pure(badRequest("Missing parameter [email]"));
		}
		
		Logger.info("DoUnsubscribe " + receipt + ", email " + email);

		return PlayAsync.asPromise(
				Casser.session().update().set(subscription::active, false)
				.where(subscription::applicationId, eq(receipt))
				.and(subscription::email, eq(email))
				.async()).map(r -> ok());

	}
	
}
