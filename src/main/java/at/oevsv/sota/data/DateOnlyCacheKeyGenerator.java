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

import io.quarkus.cache.CacheKeyGenerator;
import io.quarkus.cache.CompositeCacheKey;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Interprets an RFC 1123 String as timestamp, but strips away hours, minutes, seconds ... effectively allowing us to
 * coerce all requests on the same day into the same cache entry. This effectively allows to fetch data only once per
 * day.
 *
 * @implNote Note that this class may make assumptions about the number and constellation of method parameters; before blindly
 * wiring this up, make sure that it fits your needs!
 */
@RegisterForReflection
public final class DateOnlyCacheKeyGenerator implements CacheKeyGenerator {

    @Override
    public Object generate(Method method, Object... methodParams) {
        if (methodParams[0] instanceof String s) {
            try {
                LocalDate date = ZonedDateTime.parse(s, DateTimeFormatter.RFC_1123_DATE_TIME).toLocalDate();
                return new CompositeCacheKey(method.getName(), date);
            } catch (Exception ignored) {
                // Nothing to do here
            }
        }

        return new CompositeCacheKey(method.getName(), methodParams);
    }
}
