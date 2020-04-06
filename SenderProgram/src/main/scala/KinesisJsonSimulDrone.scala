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
package org.apache.spark.examples.streaming

// scalastyle:off println
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
import play.api.libs.json.Json

import scala.util.Random



/**
 * Usage: KinesisWordProducerASL <stream-name> <endpoint-url> \
 *   <nb-simulation>
 *
 *   <stream-name> is the name of the Kinesis stream (ie. mySparkStream)
 *   <endpoint-url> is the endpoint of the Kinesis service
 *     (ie. https://kinesis.us-east-1.amazonaws.com)
 *   <nb-simulation> is the number of simulation
 *
 * Example:
 *    $ SPARK_HOME/bin/run-example streaming.KinesisWordProducerASL mySparkStream \
 *         https://kinesis.us-east-1.amazonaws.com 10 5
 */
object KinesisJsonSimulDrone {
  def main(args: Array[String]): Unit = {
    if (args.length != 3) {
      System.err.println(
        """
          |Usage: KinesisWordProducerASL <stream-name> <endpoint-url> <records-per-sec>
          |                              <words-per-record>
          |
          |    <stream-name> is the name of the Kinesis stream
          |    <endpoint-url> is the endpoint of the Kinesis service
          |                   (e.g. https://kinesis.us-east-1.amazonaws.com)
          |    <records-per-sec> is the rate of records per second to put onto the stream
          |    <words-per-record> is the number of words per record
          |
        """.stripMargin)

      System.exit(1)
    }


    // Set default log4j logging level to WARN to hide Spark logs
    StreamingExamples.setStreamingLogLevels()

    // Populate the appropriate variables from the given args
    val Array(stream, endpoint, nbSimulation) = args

    // Generate the records and return the totals
    val totals = generate(stream, endpoint,
      nbSimulation.toInt)
  }

  def generate(stream: String,
               endpoint: String,
               nbSimulation: Int): Seq[(String, Int)] = {

    val drone1 = new DroneSimul(1)
    val drone2 = new DroneSimul(2)
    val drone3 = new DroneSimul(3)
    val drone4 = new DroneSimul(4)
    val drone5 = new DroneSimul(5)
    val drone6 = new DroneSimul(6)
    val drone7 = new DroneSimul(7)
    val drone8 = new DroneSimul(8)
    val drone9 = new DroneSimul(9)
    val drone10 = new DroneSimul(10)

    val nypdDrones: List[DroneSimul] = List(drone1,drone2,drone3,drone4,drone5,drone6,drone7,drone8,drone9,drone10)

    val droneId = scala.collection.mutable.Map[String, Int]()

    // Create the low-level Kinesis Client from the AWS Java SDK.
    val kinesisClient = new AmazonKinesisClient(new DefaultAWSCredentialsProviderChain())
    kinesisClient.setEndpoint(endpoint)

    println(s"Putting records onto stream $stream and endpoint $endpoint for the number of simulation" +
      s" $nbSimulation ")

    // Iterate and put records onto the stream per the given drones
    for (i <- 0 to nypdDrones.size-1) {
      val data = (1 to nbSimulation.toInt).map(f = x => {
        val nypdDrone = nypdDrones(i).messageJSON()
        // Increment total count to compare to server counts later
        droneId(nypdDrone) = droneId.getOrElse(nypdDrone, i+1)
        nypdDrones(i).simulDrone()
        nypdDrone
      }).mkString(" ")

      // Create a partitionKey based on recordNum
      val partitionKey = s"partitionKey-simuldrone"

      // Create a PutRecordRequest with an Array[Byte] version of the data
      val putRecordRequest = new PutRecordRequest().withStreamName(stream)
        .withPartitionKey(partitionKey)
        .withData(ByteBuffer.wrap(data.getBytes()))

      // Put the record onto the stream and capture the PutRecordResult
      val putRecordResult = kinesisClient.putRecord(putRecordRequest)
      //}

      // Sleep for a second
      Thread.sleep(1000)
      println("Sent " + nbSimulation + " records from " + nypdDrones(i).infoDrone())
    }
    // Convert the totals to (index, total) tuple
    droneId.toSeq.sortBy(_._1)
  }
}

