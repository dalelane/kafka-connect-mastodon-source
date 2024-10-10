/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package uk.co.dalelane.kafkaconnect.mastodon.source;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import social.bigbone.PrecisionDateTime;
import social.bigbone.api.entity.Status;


public class MastodonDataParser {

    private static Logger log = LoggerFactory.getLogger(MastodonDataParser.class);

    private static final Gson gson;
    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(PrecisionDateTime.class, (JsonDeserializer<PrecisionDateTime>) (json, typeOfT, context) -> {
            String dateTimeString = json.getAsString();
            if (dateTimeString == null || dateTimeString.isEmpty()) {
                return null;
            }
            try {
                Instant dateTime = Instant.parse(dateTimeString);
                return new PrecisionDateTime() {
                    @Override
                    public String asJsonString() {
                        return dateTimeString;
                    }
                    @Override
                    public boolean isValid() {
                        return true;
                    }
                    @Override
                    public Instant mostPreciseInstantOrNull() {
                        return dateTime;
                    }
                    @Override
                    public Instant mostPreciseOrFallback(Instant arg0) {
                        return dateTime;
                    }
                };
            }
            catch (Exception e) {
                log.error("Failed to parse " + dateTimeString, e);
                return null;
            }
        });
        gson = gsonBuilder.create();
    }


    public static Status parseEvent (String inputEventStr) {
        try {
            MastodonMessage wrapperEvent = gson.fromJson(inputEventStr, MastodonMessage.class);
            if ("update".equals(wrapperEvent.getEvent())) {
                Status status = gson.fromJson(wrapperEvent.getPayload(), Status.class);
                log.debug("received message from user: {}", status.getAccount().getUsername());
                log.debug("message html {}", status.getContent());

                return status;
            }
        }
        catch (Throwable th) {
            log.error("parsing failure", th);
        }

        return null;
    }
}
