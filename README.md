[![Build Status](https://travis-ci.org/rchukh/presto-querylog.svg?branch=master)](https://travis-ci.org/rchukh/presto-querylog)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Frchukh%2Fpresto-querylog.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Frchukh%2Fpresto-querylog?ref=badge_shield)

# Overview

Presto QueryLog is a Presto plugin for logging query events into separate log file.

Its main purpose is to gather queries metadata and statistics as one event per line, so it can be easily collected by external software (e.g. Elastic FileBeat which will send data to Logstash/ElasticSearch/Kibana for storage/analysis).


## Build

```
mvn clean package dependency:copy-dependencies -DincludeScope=runtime
```

## Deploy

### Copy artifacts

Copy the following artifacts (after successful build) to the Presto plugin folder (`<path_to_presto>/plugin/presto-querylog/`)
```
target/dependency/*.jar
target/presto-querylog-*.jar
```

### Prepare configuration file

Create `<path_to_presto>/etc/event-listener.properties` with the following required parameters, e.g.:

```
event-listener.name=presto-querylog
presto.querylog.log4j2.configLocation=<path_to_presto>/etc/querylog-log4j2.xml
```

#### Optional Parameters

| Configuration                             | Default  | Description                | 
| ----------------------------------------- | -------- | -------------------------- |
| `presto.querylog.log.queryCreatedEvent`   | **true** | Log Query Create event.    |
| `presto.querylog.log.queryCompletedEvent` | **true** | Log Query Completed event. |
| `presto.querylog.log.splitCompletedEvent` | **true** | Log Split Completed event. |

* `presto.querylog.log.queryCompletedEvent` can be used for post-hoc analysis of completed queries, as it contains all of the statistics of the query.
* `presto.querylog.log.splitCompletedEvent` can be used to track query progress.
* `presto.querylog.log.queryCreatedEvent` can be used to track long-running queries that are stuck without progress. 

### Create log4j2 configuration file

Prepare configuration file for logging query events, e.g. `<path_to_presto>/etc/querylog-log4j2.xml`

```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="warn" name="PrestoQueryLog" packages="">
    <Appenders>
        <RollingFile name="JsonRollingFile">
            <FileName>/var/log/presto/presto-querylog.log</FileName>
            <FilePattern>/var/log/presto/%d{yyyy-MM-dd-hh}-%i.log</FilePattern>
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

## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Frchukh%2Fpresto-querylog.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Frchukh%2Fpresto-querylog?ref=badge_large)