// Databricks notebook source
dbutils.fs.unmount("/mnt/s3Data")

val AccessKey = "Place the AccessKey from the IAM user"
// Encode the Secret Key as that can contain "/"
val SecretKey = "Place the SecretKey from the IAM user"
val EncodedSecretKey = SecretKey.replace("/", "%2F")
val AwsBucketName = "alertdronebucket"
val MountName = "s3Data"

dbutils.fs.mount(s"s3a://$AccessKey:$EncodedSecretKey@$AwsBucketName", s"/mnt/$MountName")

// COMMAND ----------

display(dbutils.fs.ls(s"/mnt/$MountName/2020/04/04/"))

// COMMAND ----------

val file = spark.read.text("dbfs:/mnt/s3Data/2020/04/04/22/KinesisToS3-1-2020-04-04-22-17-32-9d9545c8-13c6-497f-8409-f1b18ca731cd")
file.show()

// COMMAND ----------

import org.apache.spark.sql.functions._
import spark.implicits._

val names = file.select("value").collectAsList()
var test = names.toString()

test = test.replace("[[", "[")
test = test.replace("]]", "]")
test = test.replace("}{", "},{")
test = test.replace("} {", "},{")
  
val df = spark.read.json(Seq(test).toDS)

// COMMAND ----------

df.write.format("com.databricks.spark.csv").option("header","true").save("dbfs:/ooo.csv")

// COMMAND ----------

val sparkDF = spark.read.format("csv")
.option("header", "true")
.option("inferSchema", "true")
.load("/ooo.csv")

// COMMAND ----------

display(sparkDF)

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
