/*
 * Copyright (C) 2023 David Schwingenschlögl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.oevsv.sota.data;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okForContentType;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Strings;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import jakarta.ws.rs.core.Response;

public class WireMockExtension implements QuarkusTestResourceLifecycleManager {

	private WireMockServer wireMockServer;

	@Override
	public Map<String, String> start() {
		wireMockServer = new WireMockServer(wireMockConfig().gzipDisabled(true).notifier(new ConsoleNotifier(false)));
		wireMockServer.start();

		wireMockServer.stubFor(get(urlEqualTo("/admin/activator_roll?associationID=0")).willReturn(notFound()));
		wireMockServer.stubFor(get(urlEqualTo("/admin/chaser_roll?associationID=0")).willReturn(notFound()));

		wireMockServer.stubFor(get(urlMatching("/rolls/activator/-1/[0-9]+/all/all$")).willReturn(
				okJson(loadFrom("/api-db2-samples/activators-all-2026-07-05.json"))));
		wireMockServer.stubFor(get(urlMatching("/rolls/chaser/-1/[0-9]+/all/all$")).willReturn(
				okJson(loadFrom("/api-db2-samples/chasers-2026-03-03.json"))));
		wireMockServer.stubFor(get(urlMatching("/rolls/swl/-1/[0-9]+/all/all$")).willReturn(
				okJson(loadFrom("/api-db2-samples/swl-2026-03-03.json"))));

		wireMockServer.stubFor(get(urlMatching("/logs/activator/11330/([0-9]+)/1$")).willReturn(
				okJson(loadFrom("/api-db2-samples/activator-log-oe5jfe-2026-10-16.json"))));
		wireMockServer.stubFor(get(urlMatching("/logs/chaser/11330/([0-9]+)/1$")).willReturn(
				okJson(loadFrom("/api-db2-samples/chaser-log-oe5jfe-2026-10-16.json"))));
		wireMockServer.stubFor(get(urlMatching("/logs/s2s/11330/([0-9]+)/1$")).willReturn(
				okJson(loadFrom("/api-db2-samples/s2s-log-oe5jfe-2026-10-16.json"))));

		wireMockServer.stubFor(get(urlMatching("/logs/activator/11608/([0-9]+)/1$")).willReturn(
				okJson(loadFrom("/api-db2-samples/activator-log-ik2ley-2026-10-16.json"))));
		wireMockServer.stubFor(get(urlMatching("/logs/chaser/11608/([0-9]+)/1$")).willReturn(
				okJson(loadFrom("/api-db2-samples/chaser-log-ik2ley-2026-10-16.json"))));
		wireMockServer.stubFor(get(urlMatching("/logs/s2s/11608/([0-9]+)/1$")).willReturn(
				okJson(loadFrom("/api-db2-samples/s2s-log-ik2ley-2026-10-16.json"))));

		wireMockServer.stubFor(get(urlMatching("/logs/chaser/79437/([0-9]+)/1$")).willReturn(
				okJson(loadFrom("/api-db2-samples/chaser-log-oe5hkt-2026-10-16.json"))));
		wireMockServer.stubFor(get(urlMatching("/logs/activator/79437/([0-9]+)/1$")).willReturn(okJson("[]")));
		wireMockServer.stubFor(get(urlMatching("/logs/s2s/79437/([0-9]+)/1$")).willReturn(okJson("[]")));

		wireMockServer.stubFor(get(urlMatching("/logs/activator/52194/([0-9]+)/1$")).willReturn(
				okJson(loadFrom("/api-db2-samples/activator-log-oe9nat-2026-10-16.json"))));
		wireMockServer.stubFor(get(urlMatching("/logs/chaser/52194/([0-9]+)/1$")).willReturn(okJson("[]")));
		wireMockServer.stubFor(get(urlMatching("/logs/s2s/52194/([0-9]+)/1$")).willReturn(okJson("[]")));

		wireMockServer.stubFor(get(urlMatching("/logs/chaser/46145/([0-9]+)/1$")).willReturn(
				okJson(loadFrom("/api-db2-samples/chaser-log-w0tz-2026-10-16.json"))));
		wireMockServer.stubFor(get(urlMatching("/logs/activator/46145/([0-9]+)/1$")).willReturn(okJson("[]")));
		wireMockServer.stubFor(get(urlMatching("/logs/s2s/46145/([0-9]+)/1$")).willReturn(okJson("[]")));

		wireMockServer.stubFor(get(urlEqualTo("/api/activations/OE/OO-006")).willReturn(
				okJson(loadFrom("/api2-db-samples/summit-activation-log-oe-oo-006-2023-05-09.json"))));
		wireMockServer.stubFor(get(urlMatching("/api/activations/.*")).willReturn(okJson("""
				[{"activationDate":"2023-04-01","qsos":5},\
				{"activationDate":"2023-04-02","qsos":10}]""")));
		wireMockServer.stubFor(get(urlEqualTo("/admin/swl_roll?associationID=-1")).willReturn(
				okJson(loadFrom("swl-2023-03-22.json"))));
		wireMockServer.stubFor(
				get(urlEqualTo("/admin/swl_roll?associationID=0")).willReturn(okJson(loadFrom("swl-2023-03-22.json"))));

		wireMockServer.stubFor(get(urlEqualTo("/summitslist.csv")).willReturn(
				okForContentType("text/csv", loadFrom("/summits-samples/summitslist-2022-09-30.csv"))));
		wireMockServer.stubFor(get(urlEqualTo("/summitslist.csv")).withHeader("If-Modified-Since", matching(".+"))
				.willReturn(aResponse().withStatus(Response.Status.NOT_MODIFIED.getStatusCode())));

		wireMockServer.checkForUnmatchedRequests();

		final Map<String, String> configuration = new HashMap<>();
		configuration.put("quarkus.http.compression-level", "6");
		configuration.put("quarkus.rest-client.api-db.url", wireMockServer.baseUrl());
		configuration.put("quarkus.rest-client.api-db2.url", wireMockServer.baseUrl());
		configuration.put("quarkus.rest-client.summits.url", wireMockServer.baseUrl());
		configuration.put("quarkus.rest-client.api2-db.url", wireMockServer.baseUrl());

		return configuration;
	}

	private static String loadFrom(String resource) {
		try {
			return IOUtils.resourceToString(testingResourceName(resource), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String testingResourceName(String resource) {
		if (Strings.CS.startsWith(resource, "/")) {
			return resource;
		} else {
			return "/api-db-samples/" + resource;
		}
	}

	@Override
	public void stop() {
		if (null != wireMockServer) {
			wireMockServer.stop();
		}
	}
}
