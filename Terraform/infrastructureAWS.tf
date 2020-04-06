provider "aws" {
  profile = "default"
  region = "eu-west-1"
}

### STREAM ###
resource "aws_kinesis_stream" "DroneStream" {
  name             = "DroneStream"
  shard_count      = 1
  retention_period = 24
}

### LAMBDA ###

resource "aws_iam_role" "AlertDrone_role" {
  name = "AlertDrone_role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_iam_role_policy" "AlertDrone_policy" {
  name = "AlertDrone_policy"
  role = aws_iam_role.AlertDrone_role.id

  policy = <<-EOF
  {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Action": [ 
                "ses:*",
                "kinesis:Get*",
                "kinesis:List*",
                "kinesis:Describe*"],
        "Effect": "Allow",
        "Resource": "*"
      }
    ]
  }
  EOF
}

resource "aws_lambda_function" "AlertDroneLambda" {
  filename      = "alertDrone.js.zip"
  function_name = "AlertDroneLambda"
  role          = "${aws_iam_role.AlertDrone_role.arn}"
  handler       = "alertDrone.handler"

  # The filebase64sha256() function is available in Terraform 0.11.12 and later
  # For Terraform 0.11.11 and earlier, use the base64sha256() function and the file() function:
  # source_code_hash = "${base64sha256(file("lambda_function_payload.zip"))}"
  # source_code_hash = "${filebase64sha256("lambda_function_payload.zip")}"

  depends_on = [aws_kinesis_stream.DroneStream]

  runtime = "nodejs12.x"

}

resource "aws_lambda_event_source_mapping" "kinesisToLambda" {
  event_source_arn  = "${aws_kinesis_stream.DroneStream.arn}"
  function_name     = "${aws_lambda_function.AlertDroneLambda.arn}"
  starting_position = "LATEST"
}

### STREAM TO S3 ###

resource "aws_s3_bucket" "AlertDroneBucket" {
  bucket = "alertdronebucket"
  acl    = "private"
}

resource "aws_iam_role" "firehose_role" {
  name = "firehose_role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "firehose.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_iam_role" "firehose_delivery_role" {
  name = "firehose_delivery_role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "firehose.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_iam_role_policy" "firehose_delivery_role_policy" {
  name = "firehose_delivery_role_policy"
  role = aws_iam_role.firehose_delivery_role.id

  policy = <<-EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "",
      "Effect": "Allow",
      "Action": [
          "kinesis:DescribeStream",
          "kinesis:GetShardIterator",
          "kinesis:GetRecords",
          "kinesis:ListShards"
      ],
      "Resource": "${aws_kinesis_stream.DroneStream.arn}"
    },
    {
      "Sid": "",
      "Effect": "Allow",
      "Action": [
        "s3:AbortMultipartUpload",
        "s3:GetBucketLocation",
        "s3:GetObject",
        "s3:ListBucket",
        "s3:ListBucketMultipartUploads",
        "s3:PutObject"
      ],
      "Resource": "*"
    },
    {
      "Sid": "",
      "Effect": "Allow",
      "Action": [
        "lambda:InvokeFunction",
        "lambda:GetFunctionConfiguration"
      ],
      "Resource": "*"
    },
    {
      "Sid": "",
      "Effect": "Allow",
      "Action": [
        "glue:GetTable",
        "glue:GetTableVersion",
        "glue:GetTableVersions"
      ],
      "Resource": "*"
    }
  ]
}
EOF
}

resource "aws_kinesis_firehose_delivery_stream" "StreamToS3" {
  name        = "StreamToS3"
  destination = "s3"

  s3_configuration {
    role_arn   = "${aws_iam_role.firehose_delivery_role.arn}"
    bucket_arn = "${aws_s3_bucket.AlertDroneBucket.arn}"
  }

  kinesis_source_configuration {
    kinesis_stream_arn = "${aws_kinesis_stream.DroneStream.arn}"
    role_arn = "${aws_iam_role.firehose_delivery_role.arn}"
  }


  depends_on = [aws_kinesis_stream.DroneStream]
}