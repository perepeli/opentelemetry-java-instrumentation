/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.mongo.v3_1.internal;

import static io.opentelemetry.instrumentation.mongo.v3_1.internal.MongoInstrumenterFactory.DEFAULT_MAX_NORMALIZED_QUERY_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.event.CommandStartedEvent;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MongoSpanNameExtractorTest {

  @Test
  @DisplayName("test span name with no dbName")
  void testSpanNameWithNoDbName() {
    MongoSpanNameExtractor nameExtractor =
        new MongoSpanNameExtractor(
            new MongoDbAttributesGetter(true, DEFAULT_MAX_NORMALIZED_QUERY_LENGTH));

    String command = "listDatabases";
    CommandStartedEvent event =
        new CommandStartedEvent(
            0, null, null, command, new BsonDocument(command, new BsonInt32(1)));

    String spanName = nameExtractor.extract(event);

    assertThat(spanName).isEqualTo(command);
  }

  @Test
  @DisplayName("test span name with no operation falls back to dbName")
  void testSpanNameWithNoOperationFallsBackToDbName() {
    MongoSpanNameExtractor nameExtractor =
        new MongoSpanNameExtractor(
            new MongoDbAttributesGetter(true, DEFAULT_MAX_NORMALIZED_QUERY_LENGTH));

    String dbName = "myDb";
    CommandStartedEvent event = new CommandStartedEvent(0, null, dbName, null, new BsonDocument());

    String spanName = nameExtractor.extract(event);

    assertThat(spanName).isEqualTo(dbName);
  }

  @Test
  @DisplayName("test span name with no operation and no dbName falls back to default")
  void testSpanNameWithNoOperationAndNoDbNameFallsBackToDefault() {
    MongoSpanNameExtractor nameExtractor =
        new MongoSpanNameExtractor(
            new MongoDbAttributesGetter(true, DEFAULT_MAX_NORMALIZED_QUERY_LENGTH));

    CommandStartedEvent event = new CommandStartedEvent(0, null, null, null, new BsonDocument());

    String spanName = nameExtractor.extract(event);

    assertThat(spanName).isEqualTo("DB Query");
  }

  @Test
  @DisplayName("test span name does not duplicate dbName when collectionName contains a dot")
  void testSpanNameWithDottedCollectionNameDoesNotDuplicateDbName() {
    MongoSpanNameExtractor nameExtractor =
        new MongoSpanNameExtractor(
            new MongoDbAttributesGetter(true, DEFAULT_MAX_NORMALIZED_QUERY_LENGTH));

    String command = "find";
    String dbName = "myDb";
    String collectionName = "myCollection.sub";
    CommandStartedEvent event =
        new CommandStartedEvent(
            0, null, dbName, command, new BsonDocument(command, new BsonString(collectionName)));

    String spanName = nameExtractor.extract(event);

    assertThat(spanName).isEqualTo(command + " " + collectionName);
  }
}
