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
import java.util.Date;
import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.connect.source.SourceRecord;

import social.bigbone.PrecisionDateTime;
import social.bigbone.api.entity.Account;
import social.bigbone.api.entity.Application;
import social.bigbone.api.entity.Status;


public class MastodonRecordFactory {

    private String topic;

    public MastodonRecordFactory(AbstractConfig config) {
        topic = config.getString(MastodonConfig.TOPIC);
    }

    private static final Schema ACCOUNT_SCHEMA = SchemaBuilder.struct()
            .field("username", Schema.STRING_SCHEMA)
            .field("displayName", Schema.STRING_SCHEMA)
            .field("url", Schema.STRING_SCHEMA)
            .field("note", Schema.STRING_SCHEMA)
            .field("avatar", Schema.STRING_SCHEMA)
            .field("avatarStatic", Schema.STRING_SCHEMA)
            .field("bot", Schema.BOOLEAN_SCHEMA)
        .build();

    private static final Schema APPLICATION_SCHEMA = SchemaBuilder.struct()
            .field("name", Schema.OPTIONAL_STRING_SCHEMA)
            .field("website", Schema.OPTIONAL_STRING_SCHEMA)
        .build();

    private static final Schema STATUS_SCHEMA = SchemaBuilder.struct()
        .name("status")
            .field("id", Schema.STRING_SCHEMA)
            .field("uri", Schema.STRING_SCHEMA)
            .field("content", Schema.STRING_SCHEMA)
            .field("isSensitive", Schema.BOOLEAN_SCHEMA)
            .field("language", Schema.STRING_SCHEMA)
            .field("createdAt", Timestamp.builder().optional().build())
            .field("account", ACCOUNT_SCHEMA)
            .field("application", APPLICATION_SCHEMA)
        .build();


    public SourceRecord createSourceRecord(Status data) {
        return new SourceRecord(createSourcePartition(),
                                createSourceOffset(),
                                topic,
                                STATUS_SCHEMA,
                                createStruct(data));
    }

    private Map<String, Object> createSourcePartition() {
        return null;
    }
    private Map<String, Object> createSourceOffset() {
        return null;
    }

    private Struct createStruct(Status data) {
        Account account = data.getAccount();
        String username = account.getUsername();
        String displayName = account.getDisplayName();
        String userUrl = account.getUrl();
        String userNote = account.getNote();
        String userAvatar = account.getAvatar();
        String userAvatarStatic = account.getAvatarStatic();
        boolean isBot = account.isBot();

        Struct accountStruct = new Struct(ACCOUNT_SCHEMA);
        accountStruct.put("username", username);
        accountStruct.put("displayName", displayName);
        accountStruct.put("url", userUrl);
        accountStruct.put("note", userNote);
        accountStruct.put("avatar", userAvatar);
        accountStruct.put("avatarStatic", userAvatarStatic);
        accountStruct.put("bot", isBot);

        Application application = data.getApplication();
        String appName = application == null ? null : application.getName();
        String appWebsite = application == null ? null : application.getWebsite();

        Struct applicationStruct = new Struct(APPLICATION_SCHEMA);
        applicationStruct.put("name", appName);
        applicationStruct.put("website", appWebsite);

        String id = data.getId();
        String uri = data.getUri();
        String htmlContent = data.getContent();
        boolean isSensitive = data.isSensitive();
        String language = data.getLanguage() == null ? "en" : data.getLanguage();
        PrecisionDateTime createdAt = data.getCreatedAt();
        Instant createdAtInstant = createdAt == null ? null : createdAt.mostPreciseInstantOrNull();

        Struct statusStruct = new Struct(STATUS_SCHEMA);
        statusStruct.put("id", id);
        statusStruct.put("uri", uri);
        statusStruct.put("content", htmlContent);
        statusStruct.put("isSensitive", isSensitive);
        statusStruct.put("language", language);
        if (createdAtInstant != null) {
            statusStruct.put("createdAt", Date.from(createdAtInstant));
        }
        statusStruct.put("account", accountStruct);
        statusStruct.put("application", applicationStruct);

        return statusStruct;
    }
}
