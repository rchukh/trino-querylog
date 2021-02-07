[![Build Status](https://github.com/rchukh/trino-querylog/workflows/Unit%20Tests/badge.svg?branch=master)](https://github.com/rchukh/trino-querylog/workflows/Unit%20Tests/badge.svg?branch=master)
[![](https://jitpack.io/v/rchukh/trino-querylog.svg)](https://jitpack.io/#rchukh/trino-querylog)


# Overview

Trino QueryLog is a [Trino (formerly Presto SQL)](https://trino.io/) plugin for logging query events into separate log file.

Its main purpose is to gather queries metadata and statistics as one event per line, so it can be easily collected by external software (e.g. Elastic FileBeat which will send data to Logstash/ElasticSearch/Kibana for storage/analysis).


## Build

```
mvn clean package dependency:copy-dependencies -DincludeScope=runtime
```

## Deploy

### Copy artifacts

Copy the following artifacts (after successful build) to the Trino plugin folder (`<path_to_trino>/plugin/trino-querylog/`)
```
target/dependency/*.jar
target/trino-querylog-*.jar
```

### Prepare configuration file

Create `<path_to_trino>/etc/event-listener.properties` with the following required parameters, e.g.:

```
event-listener.name=trino-querylog
trino.querylog.log4j2.configLocation=<path_to_trino>/etc/querylog-log4j2.xml
```

#### Optional Parameters

| Configuration                            | Default  | Description                |
| ---------------------------------------- | -------- | -------------------------- |
| `trino.querylog.log.queryCreatedEvent`   | **true** | Log Query Create event.    |
| `trino.querylog.log.queryCompletedEvent` | **true** | Log Query Completed event. |
| `trino.querylog.log.splitCompletedEvent` | **true** | Log Split Completed event. |

* `trino.querylog.log.queryCompletedEvent` can be used for post-hoc analysis of completed queries, as it contains all of the statistics of the query.
* `trino.querylog.log.splitCompletedEvent` can be used to track query progress.
* `trino.querylog.log.queryCreatedEvent` can be used to track long-running queries that are stuck without progress. 

### Create log4j2 configuration file

Prepare configuration file for logging query events, e.g. `<path_to_trino>/etc/querylog-log4j2.xml`

```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="warn" name="TrinoQueryLog" packages="">
    <Appenders>
        <RollingFile name="JsonRollingFile">
            <FileName>/var/log/trino/trino-querylog.log</FileName>
            <FilePattern>/var/log/trino/%d{yyyy-MM-dd-hh}-%i.log</FilePattern>
            <JsonLayout charset="UTF-8" includeStacktrace="false"
                        compact="true" eventEol="true" objectMessageAsJsonObject="true"/>
            <Policies>
                <SizeBasedTriggeringPolicy size="10 MB"/>
            </Policies>
            <DefaultRolloverStrategy max="10"/>
        </RollingFile>
    </Appenders>

    <Loggers>
        <Root level="INFO">
            <AppenderRef ref="JsonRollingFile"/>
        </Root>
    </Loggers>
</Configuration>
```

Most of the configuration can be safely changed, but for easier consumption by FileBeat it is advised to leave at least JsonLayout and its parameters. 
