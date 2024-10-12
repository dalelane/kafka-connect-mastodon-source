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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import social.bigbone.api.entity.Status;

public class MastodonSourceTask extends SourceTask {

    private static Logger log = LoggerFactory.getLogger(MastodonSourceTask.class);

    private MastodonDataFetcher dataFetcher;
    private MastodonRecordFactory recordFactory;


    @Override
    public void start(Map<String, String> properties) {
        log.info("Starting task {}", properties);

        AbstractConfig config = new AbstractConfig(MastodonConfig.CONFIG_DEF, properties);
        log.debug("Config {}", config);

        recordFactory = new MastodonRecordFactory(config);

        dataFetcher = new MastodonDataFetcher(config);
        dataFetcher.start();
    }


    @Override
    public void stop() {
        log.info("Stopping task");

        if (dataFetcher != null) {
            dataFetcher.stop();
        }

        dataFetcher = null;
        recordFactory = null;
    }


    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        List<Status> statuses = dataFetcher.getStatuses();
        return statuses.stream()
            .map(r -> recordFactory.createSourceRecord(r))
            .collect(Collectors.toList());
    }


    @Override
    public String version() {
        return MastodonSourceConnector.VERSION;
    }
}
