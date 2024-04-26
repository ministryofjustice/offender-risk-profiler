#!/usr/bin/env bash

set -e
export TERM=ansi
export AWS_ACCESS_KEY_ID=foobar
export AWS_SECRET_ACCESS_KEY=foobar
export AWS_DEFAULT_REGION=eu-west-2

aws --endpoint-url=http://sqs.eu-west-2.localhost.localstack.cloud:4566/000000000000/ sqs create-queue --queue-name events-dl
aws --endpoint-url=http://sqs.eu-west-2.localhost.localstack.cloud:4566/000000000000/ sqs create-queue --queue-name events
aws --endpoint-url=http://sqs.eu-west-2.localhost.localstack.cloud:4566/000000000000/ sqs set-queue-attributes --queue-url "http://localhost:4566/000000000000/events" --attributes '{"RedrivePolicy":"{\"maxReceiveCount\":\"3\", \"deadLetterTargetArn\":\"arn:aws:sqs:eu-west-2:000000000000:events-dl\"}"}'

echo "queues..."
aws --endpoint-url=http://sqs.eu-west-2.localhost.localstack.cloud:4566/00000/ sqs list-queues
ls ./buckets
echo 'S3 bucket for risk profiler'
aws --endpoint-url=http://localhost:4566 s3 mb s3://testbucket

aws --endpoint-url=http://localhost:4566 s3 cp ./buckets/viper/VIPER-Dummy.csv s3://testbucket/viper/VIPER-Dummy.csv
aws --endpoint-url=http://localhost:4566 s3 cp ./buckets/ocgm/OCGM-Dummy.csv s3://testbucket/ocgm/OCGM-Dummy.csv
aws --endpoint-url=http://localhost:4566 s3 cp ./buckets/ocg/OCG-Dummy.csv s3://testbucket/ocg/OCG-Dummy.csv
aws --endpoint-url=http://localhost:4566 s3 cp ./buckets/pras/PRAS-Dummy.csv s3://testbucket/pras/PRAS-Dummy.csv

echo 'Finished localstack bucket setup'

echo All Ready