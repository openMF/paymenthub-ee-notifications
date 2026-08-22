/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fineract.messagegateway.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Keeps URLs with a trailing slash working, as they did on Spring Boot 2.
 *
 * Spring Boot 3 no longer matches "/sms/" against the mapping "/sms", so every existing
 * caller that sends the trailing slash gets a 404. Callers exist inside and outside this
 * repository (the notifications connector posts to "/sms/" and "/sms/report/"), so the
 * compatibility switch is turned back on here instead of changing every caller.
 *
 * The switch is deprecated in Spring and will be removed in a future version. It is here
 * to keep the migration behaviour-neutral; the callers should drop the trailing slash and
 * then this class can go.
 */
@Configuration
@SuppressWarnings("deprecation")
public class WebMvcPathMatchConfiguration implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(final PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(true);
    }
}
