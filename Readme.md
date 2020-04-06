# Functional Data Programming

There is a terraform file, so you can get all our infrastructure, and try our solution.

1. Please download [this file](https://www.kaggle.com/new-york-city/nyc-parking-tickets#Parking_Violations_Issued_-_Fiscal_Year_2017.csv), and add into the sender program.

```
/SenderProgram/src/main/scala/data/ 
```

2. To receive email for the alert, you will need to change the code in the lambda function and set your own email adress. Be sure that you add it in the [Simple Email Service](https://eu-west-1.console.aws.amazon.com/ses/home?region=eu-west-1#verified-senders-email:). 
> To compile the KinesisCSVDataDrone class, you need to add the argument: stream-name, endpoint-url and VM options: -Dcom.amazonaws.sdk.disableCbor

> To compile the kinesisJsonSimulDrone class, you need to add the argument: stream-name, endpoint-url, nb-simulation and VM options: -Dcom.amazonaws.sdk.disableCbor

3. To let Databricks access to S3, you'll need to create an [IAM user](https://console.aws.amazon.com/iam/home?region=eu-west-1#/users), and add the keys, to PJ.scala file.
