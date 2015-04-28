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
package global;

import java.util.Arrays;
import java.util.Objects;

import jobs.Jobs;
import model.Check;
import model.EmailToken;
import model.Subscription;
import play.Application;
import play.Configuration;
import play.GlobalSettings;
import play.Logger;
import play.Play;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.noorq.casser.core.Casser;

public class Global extends GlobalSettings {

	public static Check check = Casser.dsl(Check.class);
	public static Subscription subscription = Casser.dsl(Subscription.class);
	public static EmailToken emailToken = Casser.dsl(EmailToken.class);

	public static String APP_WEBSITE;
	public static String MAILGUN_ENDPOINT;
	public static String MAILGUN_KEY;
	public static String FROM_MAIL;
	
	public static Jobs jobs = new Jobs();
	
	private String getProperty(Configuration conf, String key) {
		return Objects.requireNonNull(conf.getString(key), "application.conf property is empty: " + key);
	}
	
	public void onStart(Application app) {
		Logger.info("Application has started");
		
		Configuration conf = Play.application().configuration();
		APP_WEBSITE = getProperty(conf, "application.website");
		MAILGUN_ENDPOINT = getProperty(conf, "mailgun.endpoint");
		MAILGUN_KEY = getProperty(conf, "mailgun.key");
		FROM_MAIL = getProperty(conf, "mail.from");
		
		Logger.info(Arrays.toString(new String[] {APP_WEBSITE, MAILGUN_ENDPOINT, MAILGUN_KEY, FROM_MAIL}));
		
		String host = getProperty(conf, "cassandra.host");
		
		Logger.info("Cassandra host " + host);
		
		Cluster cluster = Cluster.builder()
				.addContactPoint(host)
				.withReconnectionPolicy(new ConstantReconnectionPolicy(1000))
				.build();
		Casser.connect(cluster, "uscis")
		.add(check)
		.add(subscription)
		.add(emailToken)
		.autoUpdate()
		.showCql()
		.singleton();
		
		jobs.schedule();
	}

	public void onStop(Application app) {
		Logger.info("Application shutdown...");
		
		Casser.shutdown();
		jobs.close();
	}
	
}
