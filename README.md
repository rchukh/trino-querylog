[![Build Status](https://travis-ci.org/rchukh/presto-querylog.svg?branch=master)](https://travis-ci.org/rchukh/presto-querylog)

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
<Configuration status="info" name="PrestoQueryLog" packages="">
    <Appenders>
        <RollingFile name="RollingFile">
            <FileName>/mnt0/var/log/presto/presto_querylog.log</FileName>
            <FilePattern>/mnt0/var/log/presto/presto_querylog-%d{yyyy-MM-dd-hh-mm-ss}.log</FilePattern>
            <PatternLayout pattern="%d{yyy-MM-dd HH:mm:ss.SSS}|%msg%n"/>
            <Policies>
                <SizeBasedTriggeringPolicy size="2 MB"/>
                <CronTriggeringPolicy schedule="58 59 23 ? * * *"/>
            </Policies>
            <DefaultRolloverStrategy max="20"/>
        </RollingFile>
    </Appenders>

    <Loggers>
        <Root level="INFO">
            <AppenderRef ref="RollingFile"/>
        </Root>
    </Loggers>
</Configuration>
```

Most of the configuration can be safely changed, but for easier consumption by FileBeat it is advised to leave at least JsonLayout and its parameters. 
