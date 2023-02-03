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

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.net.URL;
import java.util.Calendar;

/**
 * This class exists only to register external classes for use with reflection.
 */
@SuppressWarnings("unused") // justification: required to register external classes for reflective access
@RegisterForReflection(targets = {Calendar.class, Calendar[].class, URL.class, URL[].class})
public final class NativeRuntimeReflectionRegistry {

    // NB: java.util.Calendar/Calendar[] is required by BeanUtils (which is required by OpenCSV)
    // NB: java.net.URL/URL[] is required by BeanUtils (which is required by OpenCSV)
}
