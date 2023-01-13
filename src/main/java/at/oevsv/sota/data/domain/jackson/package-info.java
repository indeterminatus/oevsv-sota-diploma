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

/**
 * Contains helper classes and methods to support specific requirements in dealing with JSON objects; these are bound
 * to the implementation of Jackson. When switching to another JSON library, the components contained herein must be
 * mapped to the respective concepts. Unfortunately, there is no JAX-RS standard way of doing this as of this time of
 * writing.
 */
@ParametersAreNonnullByDefault
package at.oevsv.sota.data.domain.jackson;

import javax.annotation.ParametersAreNonnullByDefault;
