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

package at.oevsv.sota.security.jpa;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.transaction.Transactional;

/**
 * A helper class to set up administrative REST access the first time the application starts.
 */
@ApplicationScoped
class AdminInitializer {

    /**
     * The name of the administrative user.
     */
    private static final String USERNAME = "administrator";

    @ConfigProperty(name = "administrator.password", defaultValue = "")
    String password;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (StringUtils.isNotBlank(password)) {
            if (User.findById(USERNAME) == null) {
                Log.infof("Setting initial administrator password to '%s'", password);
                User.add(USERNAME, password, "admin");
            } else {
                Log.infof("Changing administrator password to '%s'", password);
                User.changePassword(USERNAME, password);
            }
        }
    }
}
