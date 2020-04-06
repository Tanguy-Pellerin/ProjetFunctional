/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// scalastyle:off println
package org.apache.spark.examples.streaming


import java.nio.ByteBuffer
import java.util.Properties
import java.util.Date

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.kinesis.model.PutRecordRequest
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.apache.spark.streaming.dstream.DStream.toPairDStreamFunctions
import org.apache.spark.streaming.kinesis.KinesisInitialPositions.Latest
import org.apache.spark.streaming.kinesis.KinesisInputDStream
import org.joda.time.{DateTime, DateTimeZone}
import scala.util.Random


/**
 * Consumes messages from a Amazon Kinesis streams and does wordcount.
 *
 * This example spins up 1 Kinesis Receiver per shard for the given stream.
 * It then starts pulling from the last checkpointed sequence number of the given stream.
 *
 * Usage: KinesisWordCountASL <app-name> <stream-name> <endpoint-url> <region-name>
 *   <app-name> is the name of the consumer app, used to track the read data in DynamoDB
 *   <stream-name> name of the Kinesis stream (ie. mySparkStream)
 *   <endpoint-url> endpoint of the Kinesis service
 *     (e.g. https://kinesis.us-east-1.amazonaws.com)
 *
 *
 * Example:
 *      # export AWS keys if necessary
 *      $ export AWS_ACCESS_KEY_ID=<your-access-key>
 *      $ export AWS_SECRET_KEY=<your-secret-key>
 *
 *      # run the example
 *      $ SPARK_HOME/bin/run-example  streaming.KinesisWordCountASL myAppName  mySparkStream \
 *              https://kinesis.us-east-1.amazonaws.com
 *
 * There is a companion helper class called KinesisWordProducerASL which puts dummy data
 * onto the Kinesis stream.
 *
 * This code uses the DefaultAWSCredentialsProviderChain to find credentials
 * in the following order:
 *    Environment Variables - AWS_ACCESS_KEY_ID and AWS_SECRET_KEY
 *    Java System Properties - aws.accessKeyId and aws.secretKey
 *    Credential profiles file - default location (~/.aws/credentials) shared by all AWS SDKs
 *    Instance profile credentials - delivered through the Amazon EC2 metadata service
 * For more information, see
 * http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html
 *
 * See http://spark.apache.org/docs/latest/streaming-kinesis-integration.html for more details on
 * the Kinesis Spark Streaming integration.
 */

private[streaming] object StreamingExamples extends Logging {
  // Set reasonable logging levels for streaming if the user has not configured log4j.
  def setStreamingLogLevels(): Unit = {
    val log4jInitialized = Logger.getRootLogger.getAllAppenders.hasMoreElements
    if (!log4jInitialized) {
      // We first log something to initialize Spark's default logging, then we override the
      // logging level.
      logInfo("Setting log level to [WARN] for streaming example." +
        " To override add a custom log4j.properties to the classpath.")
      Logger.getRootLogger.setLevel(Level.WARN)
    }
  }
}
// scalastyle:on println
