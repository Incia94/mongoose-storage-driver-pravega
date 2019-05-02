[![Gitter chat](https://badges.gitter.im/emc-mongoose.png)](https://gitter.im/emc-mongoose)
[![Issue Tracker](https://img.shields.io/badge/Issue-Tracker-red.svg)](https://mongoose-issues.atlassian.net/projects/GOOSE)
[![CI status](https://travis-ci.org/emc-mongoose/mongoose-storage-driver-pravega.svg?branch=master)](https://travis-ci.org/emc-mongoose/mongoose-storage-driver-pravega/builds)
[![Maven metadata URL](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/github/emc-mongoose/mongoose-storage-driver-pravega/maven-metadata.xml.svg)](http://central.maven.org/maven2/com/github/emc-mongoose/mongoose-storage-driver-pravega)
[![Docker Pulls](https://img.shields.io/docker/pulls/emcmongoose/mongoose-storage-driver-pravega.svg)](https://hub.docker.com/r/emcmongoose/mongoose-storage-driver-pravega/)

# Content

1. [Introduction](#1-introduction)<br/>
2. [Features](#2-features)<br/>
3. [Usage](#3-usage)<br/>
&nbsp;&nbsp;3.1. [Basic](#31-basic)<br/>
&nbsp;&nbsp;3.2. [Docker](#32-docker)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;3.2.1. [Standalone](#321-standalone)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;3.2.2. [Distributed](#322-distributed)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;3.2.2.1. [Additional Node](#3221-additional-node)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;3.2.2.2. [Entry Node](#3222-entry-node)<br/>
&nbsp;&nbsp;3.3. [Specific Configuration Options](#33-specific-configuration-options)<br/>
&nbsp;&nbsp;3.4. [Specific Cases](#33-specific-cases)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;3.4.1. [Manual Scaling](#341-manual-scaling)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;3.4.2. [Multiple Destination Streams](#342-multiple-destination-streams)<br/>
4. [Design](#4-design)<br/>
&nbsp;&nbsp;4.1. [Event Stream Operations](#41-event-stream-operations)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;4.1.1. [Create](#411-create)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;4.1.1.1. [Transactional](#4111-transactional)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;4.1.2. [Read](#412-read)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;4.1.3. [Update](#413-update)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;4.1.4. [Delete](#414-delete)<br/>
&nbsp;&nbsp;4.2. [Byte Stream Operations](#42-byte-stream-operations)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;4.2.1. [Create](#421-create)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;4.2.2. [Read](#422-read)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;4.2.3. [Update](#423-update)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;4.2.4. [Delete](#424-delete)<br/>
&nbsp;&nbsp;4.3. [Open Issues](#43-open-issues)<br/>
5. [Development](#5-development)<br/>
&nbsp;&nbsp;5.1. [Build](#51-build)<br/>
&nbsp;&nbsp;5.2. [Test](#52-test)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;5.2.1. [Automated](#521-automated)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;5.2.1.1. [Unit](#5211-unit)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;5.2.1.2. [Integration](#5212-integration)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;5.2.1.3. [Functional](#5213-functional)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;5.2.2. [Manual](#522-manual)<br/>

# 1. Introduction

The storage driver extends the Mongoose's Abstract Coop Storage Driver. It's being built against the specified Pravega
source commit number.

# 2. Features

* Authentication: not implemented yet
* SSL/TLS: not implemented yet
* Item Types:
    * `data`: corresponds to an ***event*** either ***byte stream*** depending on the configuration 
    * `path`: not supported
    * `token`: not supported
* Supported load operations:
    * `create` (events)
    * `read` (streams)
    * `delete` (streams)
* Storage-specific:
    * Scaling policies
    * Stream sealing
    * Routing keys
    * Byte streams

# 3. Usage

Java 11+ is required to build/run.

## 3.1. Basic

1. Get the latest `mongoose-base` jar from the 
[maven repo](http://repo.maven.apache.org/maven2/com/github/emc-mongoose/mongoose-base/)
and put it to your working directory. Note the particular version, which is referred as *BASE_VERSION* below.

2. Get the latest `mongoose-storage-driver-coop` jar from the
[maven repo](http://repo.maven.apache.org/maven2/com/github/emc-mongoose/mongoose-storage-driver-coop/)
and put it to the `~/.mongoose/<BASE_VERSION>/ext` directory.

3. Get the latest `mongoose-storage-driver-pravega` jar from the
[maven repo](http://repo.maven.apache.org/maven2/com/github/emc-mongoose/mongoose-storage-driver-pravega/)
and put it to the `~/.mongoose/<BASE_VERSION>/ext` directory.

```bash
java -jar mongoose-base-<BASE_VERSION>.jar \
    --storage-driver-type=pravega \
    --storage-namespace=scope1 \
    --storage-net-node-addrs=<NODE_IP_ADDRS> \
    --storage-net-node-port=9090 \
    ...
```

## 3.2. Docker

### 3.2.1. Standalone

```bash
docker run \
    --network host \
    emcmongoose/mongoose-storage-driver-pravega \
    --storage-namespace=scope1 \
    --storage-net-node-addrs=<NODE_IP_ADDRS> \
    ...
```

### 3.2.2. Distributed

#### 3.2.2.1. Additional Node

```bash
docker run \
    --network host \
    --expose 1099 \
    emcmongoose/mongoose-storage-driver-pravega \
    --run-node
```

#### 3.2.2.2. Entry Node

```bash
docker run \
    --network host \
    emcmongoose/mongoose-storage-driver-pravega \
    --load-step-node-addrs=<ADDR1,ADDR2,...> \
    --storage-net-node-addrs=<NODE_IP_ADDRS> \
    ...
```

## 3.3. Specific Configuration Options

| Name                              | Type            | Default Value | Description                                      |
|:----------------------------------|:----------------|:--------------|:-------------------------------------------------|
| storage-driver-control-timeoutMillis | integer      | 30000         | The timeout for any Pravega Controller API call
| storage-driver-event-key-enabled | boolean         | false         | Specifies if Mongoose should generate its own routing key during the events creation
| storage-driver-event-key-count   | integer         | 0             | Specifies a max count of unique routing keys to use during the events creation (may be considered as a routing key period). 0 value means to use unique routing key for each new event
| storage-driver-event-timeoutMillis | integer         | 100           | The event read timeout in milliseconds
| storage-driver-scaling-type       | one of: "fixed", "event_rate", "byte_rate" | fixed | The scaling policy type to use. [See the Pravega documentation](http://pravega.io/docs/latest/terminology/) for details
| storage-driver-scaling-rate       | integer         | 0             | The scaling policy target rate. May be meausred in events per second either kilobytes per second depending on the scaling policy type
| storage-driver-scaling-factor     | integer         | 0             | The scaling policy factor. From the Pravega javadoc: *the maximum number of splits of a segment for a scale-up event.*
| storage-driver-scaling-segments   | integer         | 1             | From the Pravega javadoc: *the minimum number of segments that a stream can have independent of the number of scale down events.*
| storage-driver-stream-data        | enum            | "events"      | Work on events or byte streams (if `bytes` is set)
| storage-net-node-addrs            | list of strings | 127.0.0.1     | The list of the Pravega storage nodes to use for the load
| storage-net-node-port             | integer         | 9090          | The default port of the Pravega storage nodes, should be explicitly set to 9090 (the value used by Pravega by default)

## 3.4. Specific Cases

### 3.4.1. Manual Scaling

It's required to make a manual destination stream scaling while the event writing load is in progress in order to see
if the rate changes. The additional load step may be used to perform such scaling. In order to not perform any
additional load it should be explicitly configured to do a minimal work:
* load operations count limit: 1
* concurrency limit: 1
* payload size: 1 bytes

For more details see the corresponding [scenario content](https://github.com/emc-mongoose/mongoose-storage-driver-pravega/blob/master/src/main/resources/example/scenario/js/pravega/manual_scaling.js).

### 3.4.2. Multiple Destination Streams

The configuration [expression language](https://github.com/emc-mongoose/mongoose-base/tree/master/src/main/java/com/emc/mongoose/base/config/el#52-variable-items-output-path)
feature may be used to specify multiple destination streams to write the events. The example of the command to write
the events into 1000 destination streams (in the random order):

```bash
java -jar mongoose-<MONGOOSE_VERSION>.jar \
    --storage-driver-type=pravega \
    --storage-net-node-port=9090 \
    --item-data-size=1000 \
    --item-output-path=stream-%p\{1000\;1\}
```

# 4. Design

Mongoose and Pravega are using quite different concepts. So it's necessary to determine how
[Pravega-specific terms](http://pravega.io/docs/latest/terminology/) are mapped to the
[Mongoose abstractions]((https://gitlab.com/emcmongoose/mongoose/tree/master/doc/design/architecture#1-basic-terms)).

| Pravega | Mongoose |
|---------|----------|
| [Stream](http://pravega.io/docs/latest/pravega-concepts/#streams) | *Item Path* or *Byte Stream* |
| Scope | Storage Namespace
| [Event](http://pravega.io/docs/latest/pravega-concepts/#events) | *Data Item* |
| Stream Segment | N/A |

## 4.1. Event Stream Operations

Mongoose should perform the load operations on the *events* when the configuration option `item-type` is set to `data`.

### 4.1.1. Create

Steps:
1. Get the endpoint URI from the cache.
2. Check if the corresponding `StreamManager` exists using the cache, create a new one if it doesn't.
3. Check if the destination scope exists using the cache, create a new one if it doesn't.
4. Check if the destination stream exists using the cache, create a new one if it doesn't.
5. Check if the corresponding `ClientFactory` exists using the cache, create a new one if it doesn't.
6. Check if the `EventStreamWriter` exists using the cache, create new one if it doesn't.
7. Submit the event writing, use a routing key if configured.
8. Submit the load operation completion handler.

#### 4.1.1.1. Transactional

Using the [transactions](http://pravega.io/docs/latest/transactions/#pravega-transactions) to create the events allows 
to write the events in the batch mode. The maximum count of the events per transaction is defined by the 
`load-batch-size` configuration option value.

Example:
```bash
docker run \
    --network host \
    emcmongoose/mongoose-storage-driver-pravega \
    --storage-namespace=scope1 \
    --storage-driver-event-batch \
    --load-step-limit-count=100000 \
    --load-batch-size=1024 \
    --item-output-path=eventsStream1 \
    --item-data-size=10KB
```

**Note**:
> The transactional events create concurrency is limited currently by 1 due to the [issue #1](#43-open-issues).

### 4.1.2. Read

**Notes**:
> * The Pravega storage doesn't support reading the stream events in the random order.
> * Works synchronously

There is a specific option (config parameter) for reading called `storage-driver-event-timeoutMillis`. Pravega documentation says it only works when
there is no available event in the stream. `readNextEvent()` will block for the specified time in ms. So, in theory 0
and 1 should work just fine. They do not. In practice, this value should be somewhere between 100 and 2000 ms (2000 is
Pravega default value).

As Pravega is a streaming storage, it does not provide any information on a stream size. 
So, we allocate memory for reading one event only and then repeat it until we meet the end of a stream.
In terms of Mongoose it is called recycle-mode and activated via `--load-op-recycle` option.
That makes this option obligatory for doing read operations on events.
Learn more about recycle-mode here: https://github.com/emc-mongoose/mongoose-base/tree/master/doc/design/recycle_mode.

There is one important fact considering the end of a stream. As we do not know the size of a stream,
we only find out that it is the end, when `readNextEvent()` returns false, so 1 failed operation is not
actually a fail, it's a sign of stream's end.

Steps:
1. Get the endpoint URI from the cache.
2. Check if the corresponding `StreamManager` exists using the cache, create a new one if it doesn't.
3. Check if the corresponding `ClientFactory` exists using the cache, create a new one if it doesn't.
4. Check if the corresponding `EventStreamReader<ByteBuffer>` exists using the cache, create a new one if it doesn't.
5. Read the next event, verify the returned byte buffer content if configured so, discard it otherwise.
6. Invoke the load operation completion handler.

### 4.1.3. Update

Not supported. A stream append may be performed using the `create` load operation type and a same stream previously used
to write the events.

### 4.1.4. Delete

Not supported.

## 4.2. Byte Stream Operations

Mongoose should perform the load operations on the *streams* when the configuration option `storage-driver-stream-data` 
is set to `bytes`. This means that the whole streams are being accounted as *items*.

### 4.2.1. Create

Creates the [byte streams](https://github.com/pravega/pravega/wiki/PDP-30-ByteStream-API). The created byte stream is 
filled with content up to the size determined by the `item-data-size` option. The create operation will fail with the 
[status code](https://github.com/emc-mongoose/mongoose-base/tree/master/doc/interfaces/output#232-files) #7 if the 
stream existed before. 

**Example**:
```bash
docker run \
    --network host \
    emcmongoose/mongoose-storage-driver-pravega \
    --storage-driver-stream-data=bytes \
    --storage-namespace=scope1 \
    --storage-driver-limit-concurrency=100
```

### 4.2.2. Read

Reads the [byte streams](https://github.com/pravega/pravega/wiki/PDP-30-ByteStream-API).

**Example**:
```bash
docker run \
    --network host \
    emcmongoose/mongoose-storage-driver-pravega \
    --item-input-file=streams.csv \
    --read \
    --storage-driver-stream-data=bytes \
    --storage-driver-limit-concurrency=10 \
    --storage-namespace=scope1
```

It's also possible to perform the byte streams read w/o the input stream items file:
```bash
docker run \
    --network host \
    emcmongoose/mongoose-storage-driver-pravega \
    --item-input-path=scope1 \
    --read \
    --storage-driver-stream-data=bytes \
    --storage-driver-limit-concurrency=10 \
    --storage-namespace=scope1
```
All streams in the specified scope are listed and analyzed for the current size before the reading.

### 4.2.3. Update

Not implemented yet

### 4.2.4. Delete

Before the deletion, the stream must be sealed because of Pravega concepts. So the sealing of the stream is done during
the deletion too.

## 4.3. Open Issues

| Issue | Description |
|-------|-------------|
| 1 | [Pravega #3697](https://github.com/pravega/pravega/issues/3697): Missing asynchronous read event and byte stream write methods

# 5. Development

## 5.1. Build

```bash
./gradlew clean jar
```

## 5.2. Test

### 5.2.1. Automated

#### 5.2.1.1. Unit

```bash
./gradlew clean test
```

#### 5.2.1.2. Integration
```bash
docker run -d --name=storage --network=host pravega/pravega:<PRAVEGA_VERSION> standalone
./gradlew integrationTest
```

#### 5.2.1.3. Functional
```bash
./gradlew jar
export SUITE=api.storage
TEST=create_events ./gradlew robotest
TEST=create_stream ./gradlew robotest
```

### 5.2.1. Manual

1. Build the storage driver
2. Copy the storage driver's jar file into the mongoose's `ext` directory:
```bash
cp -f build/libs/mongoose-storage-driver-pravega-*.jar ~/.mongoose/<MONGOOSE_BASE_VERSION>/ext/
```
Note that the Pravega storage driver depends on the 
[Coop Storage Driver](http://repo.maven.apache.org/maven2/com/github/emc-mongoose/mongoose-storage-driver-coop/) 
extension so it should be also put into the `ext` directory
3. Run the Pravega standalone node:
```bash
docker run --network host pravega/pravega:<PRAVEGA_VERSION> standalone
```
4. Run Mongoose's default scenario with some specific command-line arguments:
```bash
java -jar mongoose-<MONGOOSE_BASE_VERSION>.jar \
    --storage-driver-type=pravega \
    --storage-net-node-port=9090 \
    --storage-driver-limit-concurrency=10 \
    --item-output-path=goose-events-stream-0
```
