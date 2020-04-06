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

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.kinesis.model.PutRecordRequest

import ai.x.play.json.Jsonx
import play.api.libs.json.{Json, OFormat}

import scala.io.Source


/**
 * Usage: KinesisWordProducerASL <stream-name> <endpoint-url>
 *
 *   <stream-name> is the name of the Kinesis stream (ie. mySparkStream)
 *   <endpoint-url> is the endpoint of the Kinesis service
 *     (ie. https://kinesis.us-east-1.amazonaws.com)
 *
 * Example:
 *    $ SPARK_HOME/bin/run-example streaming.KinesisWordProducerASL mySparkStream \
 *         https://kinesis.us-east-1.amazonaws.com 10 5
 */
object KinesisCSVDataDrone {
  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      System.err.println(
        """
          |Usage: KinesisWordProducerASL <stream-name> <endpoint-url>
          |
          |    <stream-name> is the name of the Kinesis stream
          |    <endpoint-url> is the endpoint of the Kinesis service
          |                   (e.g. https://kinesis.us-east-1.amazonaws.com)
          |
        """.stripMargin)

      System.exit(1)
    }


    // Set default log4j logging level to WARN to hide Spark logs
    StreamingExamples.setStreamingLogLevels()

    // Populate the appropriate variables from the given args
    val Array(stream, endpoint) = args

    // Generate the records and return the totals
    val totals = generate(stream, endpoint)

  }

  def generate(stream: String,
               endpoint: String): Seq[(String, Int)] = {

    val droneId = scala.collection.mutable.Map[String, Int]()

    // Create the low-level Kinesis Client from the AWS Java SDK.
    val kinesisClient = new AmazonKinesisClient(new DefaultAWSCredentialsProviderChain())
    kinesisClient.setEndpoint(endpoint)

    val bufferedSource = Source.fromFile("src/main/scala/data/Parking_Violations_Issued_-_Fiscal_Year_2014__August_2013___June_2014_.csv")

    implicit val dataTransfer: OFormat[NYDataFull] = Jsonx.formatCaseClass[NYDataFull]
    var a = 0
    for (line <- bufferedSource.getLines){
      a += 1
      val cols = line.split(",").map(_.trim)
      if (cols(0) != "Summons Number"){
         val data = NYDataFull(
         IdDrone = "-1",
         NiveauBatterie = "-1",
         DroneAltitude= "-1",
         Speed= "-1",
         DiscSpace= "-1",
         RegistrationState = s"${cols(2)}",
         Temperature= "-1",
         ViolationTime = s"${cols(19)}",
         Identifiable= "-1",
         PlateID = s"${cols(1)}",
         ViolationCode = s"${cols(5)}",
         SummonsNumber = s"${cols(0)}",
         Latitude= "-1",
         Longitude= "-1",
         PlateType = s"${cols(3)}",
         IssueDate = s"${cols(5)}",
         ViolationLocation= s"${cols(13)}",
         VehicleBodyType = s"${cols(6)}",
         VehicleMake = s"${cols(7)}",
         IssuingAgency = s"${cols(8)}",
         StreetCode1 = s"${cols(9)}",
         StreetCode2 = s"${cols(10)}",
         StreetCode3 = s"${cols(11)}",
         VehicleExpirationDate= s"${cols(12)}",
         ViolationPrecinct= s"${cols(14)}",
         IssuerPrecinct= s"${cols(15)}",
         IssuerCode = s"${cols(16)}",
         IssuerCommand = s"${cols(17)}",
         IssuerSquad = s"${cols(18)}")

        val dataJSON = Json.toJson(data).toString()

        // Create a partitionKey based on recordNum
        val partitionKey = s"partitionKey-csv"

        // Create a PutRecordRequest with an Array[Byte] version of the data
        val putRecordRequest = new PutRecordRequest().withStreamName(stream)
          .withPartitionKey(partitionKey)
          .withData(ByteBuffer.wrap(dataJSON.getBytes()))

        // Put the record onto the stream and capture the PutRecordResult
        val putRecordResult = kinesisClient.putRecord(putRecordRequest)
      }

      // Sleep for a second
      //Thread.sleep(1000)
      println("Sent record number : " + a + " .")
    }
    // Convert the totals to (index, total) tuple
    droneId.toSeq.sortBy(_._1)
  }
}