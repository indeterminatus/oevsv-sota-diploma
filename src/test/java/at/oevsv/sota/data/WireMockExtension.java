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

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.okForContentType;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class WireMockExtension implements QuarkusTestResourceLifecycleManager {

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(wireMockConfig().gzipDisabled(true));
        wireMockServer.start();

        wireMockServer.stubFor(get(urlEqualTo("/admin/activator_roll?associationID=0")).willReturn(okJson(loadFrom("activators-2022-08-12.json"))));
        wireMockServer.stubFor(get(urlEqualTo("/admin/chaser_roll?associationID=0")).willReturn(okJson(loadFrom("chasers-2022-08-12.json"))));
        wireMockServer.stubFor(get(urlEqualTo("/admin/activator_log_by_id?id=11330&year=all")).willReturn(okJson(loadFrom("activator-log-oe5jfe-2022-08-12.json"))));
        wireMockServer.stubFor(get(urlEqualTo("/admin/chaser_log_by_id?id=11330&year=all")).willReturn(okJson(loadFrom("chaser-log-oe5jfe-2022-08-09.json"))));
        wireMockServer.stubFor(get(urlEqualTo("/admin/s2s_log_by_id?id=11330&year=all")).willReturn(okJson(loadFrom("s2s-log-oe5jfe-2022-08-14.json"))));
        wireMockServer.stubFor(get(urlEqualTo("/admin/activator_log_by_id?id=52194&year=all")).willReturn(okJson(loadFrom("activator-log-oe9nat-2022-08-28.json"))));
        wireMockServer.stubFor(get(urlEqualTo("/admin/chaser_log_by_id?id=52194&year=all")).willReturn(okJson("[]")));
        wireMockServer.stubFor(get(urlEqualTo("/admin/s2s_log_by_id?id=52194&year=all")).willReturn(okJson("[]")));

        wireMockServer.stubFor(get(urlEqualTo("/summitslist.csv")).withHeader("If-Modified-Since", matching(".+")).willReturn(aResponse().withStatus(Response.Status.NOT_MODIFIED.getStatusCode())));
        wireMockServer.stubFor(get(urlEqualTo("/summitslist.csv")).willReturn(okForContentType("text/csv", loadFrom("/summits-samples/summitslist-2022-09-30.csv"))));

        wireMockServer.checkForUnmatchedRequests();

        final Map<String, String> configuration = new HashMap<>();
        configuration.put("quarkus.resteasy.gzip.max-input", "50M");
        configuration.put("quarkus.rest-client.api-db.url", wireMockServer.baseUrl());
        configuration.put("quarkus.rest-client.summits.url", wireMockServer.baseUrl());

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
        if (StringUtils.startsWith(resource, "/")) {
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
