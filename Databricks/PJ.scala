// Databricks notebook source
dbutils.fs.unmount("/mnt/s3Data")

val AccessKey = "X"
// Encode the Secret Key as that can contain "/"
val SecretKey = "X"
val EncodedSecretKey = SecretKey.replace("/", "%2F")
val AwsBucketName = "X"
val MountName = "s3Data"

dbutils.fs.mount(s"s3a://$AccessKey:$EncodedSecretKey@$AwsBucketName", s"/mnt/$MountName")

// COMMAND ----------

display(dbutils.fs.ls(s"/mnt/$MountName/2020/04/"))

// COMMAND ----------

import java.util.Calendar

val date = Calendar.getInstance.getTime.toString().split(" ")
val day = date(2)
val date4 = date(3).split(":")
val hour = date4(0)

// COMMAND ----------

import org.apache.spark.sql.functions._
import spark.implicits._

val all_files : Seq[String] = dbutils.fs.ls(s"/mnt/$MountName/2020/04/"+day+"/"+hour+"/").map(_.path)
val file = spark.read.text(all_files(0))

val json = file.select("value").collectAsList().toString().replace("[[", "[").replace("]]", "]").replace("}{", "},{").replace("} {", "},{")
 
val df = spark.read.json(Seq(json).toDS)

// COMMAND ----------

df.groupBy("IdDrone").count().show()
df.groupBy("IdDrone").agg(avg("DroneAltitude")).show()
df.groupBy("IdDrone").agg(avg("Temperature"), avg("Speed")).show()
df.groupBy("IdDrone").agg(avg("Speed")).show()
df.groupBy("IdDrone").agg(avg("Temperature")).show()
df.groupBy("IdDrone").agg(avg("DroneAltitude"), avg("Speed")).show()

// COMMAND ----------

val all_files : Seq[String] = dbutils.fs.ls(s"/mnt/$MountName/2020/04/"+day+"/"+hour+"/").map(_.path)
val file = spark.read.text(all_files(0))

val json = file.select("value").collectAsList().toString().replace("[[", "[").replace("]]", "]").replace("}{", "},{").replace("} {", "},{")
 
val df = spark.read.json(Seq(json).toDS)
df.write.format("com.databricks.spark.csv").option("header","true").save("dbfs:/final.csv")

val sparkDF = spark.read.format("csv")
.option("header", "true")
.option("inferSchema", "true")
.load("/final.csv")

// COMMAND ----------

// Number of flight for each drone
display(sparkDF)

// COMMAND ----------

// Average altitude of each drone
display(sparkDF)

// COMMAND ----------

// Average comparison between the speed and temperature for each drone 
display(sparkDF)

// COMMAND ----------

// Average speed for each drone
display(sparkDF)

// COMMAND ----------

// Average temperature for each drone
display(sparkDF)

// COMMAND ----------

// Average comparison between the speed and altitude for each drone 
display(sparkDF)
