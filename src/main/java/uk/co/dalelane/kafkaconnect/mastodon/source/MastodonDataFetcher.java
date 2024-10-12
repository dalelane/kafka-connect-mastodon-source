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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.errors.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import social.bigbone.MastodonClient;
import social.bigbone.api.entity.Status;
import social.bigbone.api.entity.streaming.MastodonApiEvent.GenericMessage;
import social.bigbone.api.entity.streaming.TechnicalEvent.Failure;
import social.bigbone.api.entity.streaming.WebSocketCallback;
import social.bigbone.api.entity.streaming.WebSocketEvent;


/**
 * Starts a streaming websocket to the Mastodon API. When new posts are
 *  received, these are added to a local List that the SourceTask can
 *  retrieve when it is ready.
 */
public class MastodonDataFetcher {

    private static Logger log = LoggerFactory.getLogger(MastodonDataFetcher.class);

    private final List<Status> fetchedRecords = Collections.synchronizedList(new ArrayList<Status>());

    private Closeable mastodonStream = null;
    private ConnectException connectionError = null;

    private final String accesstoken;
    private final String instancehostname;
    private final String searchterm;



    public MastodonDataFetcher(AbstractConfig config) {
        log.info("Creating a data fetcher");

        accesstoken = config.getPassword(MastodonConfig.ACCESS_TOKEN).value();
        instancehostname = config.getString(MastodonConfig.INSTANCE);
        searchterm = config.getString(MastodonConfig.SEARCH_TERM);
    }


    public synchronized List<Status> getStatuses() throws ConnectException {
        if (connectionError != null) {
            throw connectionError;
        }

        synchronized (fetchedRecords) {
            List<Status> copy = new ArrayList<>(fetchedRecords);
            fetchedRecords.clear();
            return copy;
        }
    }

    public void stop() {
        log.debug("Stopping Mastodon stream");

        if (mastodonStream != null) {
            try {
                mastodonStream.close();
            }
            catch (IOException e) {
                log.error("Error stopping mastodon stream", e);
            }
            mastodonStream = null;
            connectionError = null;
        }
    }

    public void start() {
        try {
            connectionError = null;

            MastodonClient mastodonClient = new MastodonClient
                .Builder(instancehostname)
                .accessToken(accesstoken)
                .build();

            mastodonStream = mastodonClient.streaming()
                .hashtag(searchterm, false, new WebSocketCallback() {
                    @Override
                    public void onEvent(WebSocketEvent event) {
                        if (event instanceof GenericMessage) {
                            GenericMessage message = (GenericMessage) event;

                            // ignore everything except new status events
                            Status status = MastodonDataParser.parseEvent(message.getText());
                            if (status != null) {
                                synchronized (fetchedRecords) {
                                    fetchedRecords.add(status);
                                }
                            }
                        }
                        else if (event instanceof Failure) {
                            log.error("streaming connection error {}", event.toString());
                            connectionError = new ConnectException("Streaming connection error " + event.toString());
                        }
                        else {
                            log.info("event type {}", event.getClass().getCanonicalName());
                            log.info("event contents {}", event.toString());
                        }
                    }
                });
        }
        catch (Throwable e) {
            log.error("client failure", e);
        }
    }
}
