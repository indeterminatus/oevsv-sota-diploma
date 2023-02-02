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

package at.oevsv.sota;

import javax.annotation.security.PermitAll;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.Locale;
import java.util.ResourceBundle;

@Path("/api/test")
public class TestResource {

    @GET
    @PermitAll
    @Path("/bundle")
    @Produces("text/plain")
    public String checkResourceBundle(@QueryParam("name") @DefaultValue("pdf.i18n.messages") String bundleName, @QueryParam("language") @DefaultValue("de") String language, @QueryParam("key") @DefaultValue("diploma.title") String key) {
        final var bundle = ResourceBundle.getBundle(bundleName, Locale.forLanguageTag(language), ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        return bundle.getString(key);
    }
}
