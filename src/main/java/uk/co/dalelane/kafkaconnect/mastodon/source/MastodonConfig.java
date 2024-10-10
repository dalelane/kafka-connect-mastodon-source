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

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;


/**
 * Config that the source connector expects - mostly connection
 *  details to mastodon.
 */
public class MastodonConfig {

    public static final String ACCESS_TOKEN = "mastodon.accesstoken";
    public static final String INSTANCE = "mastodon.instance";
    public static final String SEARCH_TERM = "mastodon.searchterm";
    public static final String TOPIC = "mastodon.topic";

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
        .define(ACCESS_TOKEN,
                Type.PASSWORD,
                "0000000000000000000000000000000000000000000",
                Importance.HIGH,
                "Access token for the mastodon server")
        .define(INSTANCE,
                Type.STRING,
                "mastodon.social",
                new ConfigDef.NonEmptyString(),
                Importance.HIGH,
                "Host for the mastodon instance to get statuses from")
        .define(SEARCH_TERM,
                Type.STRING,
                "mastodon",
                new ConfigDef.NonEmptyString(),
                Importance.HIGH,
                "Hash tag to search for")
        .define(TOPIC,
                Type.STRING,
                "mastodon",
                new ConfigDef.NonEmptyString(),
                Importance.HIGH,
                "Topic to deliver messages to");
}
