# Kafka Connect source connector for Mastodon

## Overview

Emits status updates from Mastodon (for a hashtag) to a Kafka topic.

## Config

|    | Notes |
| -- | ----- |
|  `mastodon.accesstoken` | Access token for the Mastodon API. The token needs to have at least the `read:search` and `read:statuses` scopes. |
| `mastodon.instance` |  The hostname for the instance of Mastodon to use - e.g. `mastodon.social`, `mastodon.org.uk`, etc. |
| `mastodon.searchterm` | The hashtag to stream status updates with. You do not need to include the `#` character - for example, to bring Mastodon posts containing #winter into Kafka, you would set this to `winter`. |
| `mastodon.topic` |  The name of the Kafka topic to deliver events to.

### Example output

With the JSON converter, events will look like:

```json
{
    "id": "111111111111111111",
    "uri": "https://mastodon.org.uk/users/theusername/statuses/111111111111111111",
    "content": "<p>This is a post that mentions <a href=\"https://mastodon.org.uk/tags/thehashtag\" class=\"mention hashtag\" rel=\"nofollow noopener noreferrer\" target=\"_blank\">#<span>thehashtag</span></a> somewhere in it</p>",
    "language": "en",
    "isSensitive": false,
    "account": {
        "username": "theusername",
        "displayName": "The User",
        "url": "https://mastodon.org.uk/@theusername",
        "note": "<p>Bio about the user who made this post</p>",
        "avatar": "https://files.mastodon.social/cache/accounts/avatars/108/293/995/954/830/376/original/b87b4123567191b2.jpg",
        "avatarStatic": "",
        "bot": false
    },
    "application": {
        "name": "Some application",
        "website": "https://social-application.com"
    }
}
```

This is a subset of data available from the Mastodon API. Refer to Mastodon API docs for an explanation of individual fields.

Note that the `content` field contains HTML data.
