#!/bin/bash

# --------------------------------------------------------------------------
#
#   setup local s3 and sqs in localstack,called by docker-compose-local task
#
#  -------------------------------------------------------------------------

sleep 5 # we should have better way, srlsy it is not 90s ;[]

set -e
export TERM=ansi
export AWS_ACCESS_KEY_ID=foo
export AWS_SECRET_ACCESS_KEY=bar
export AWS_DEFAULT_REGION=eu-west-2

echo 'Starting localstack bucket setup'

aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name events-dl
aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name events
aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name risk_profiler_change

aws --endpoint-url=http://localhost:4566 s3 mb s3://testbucket

aws --endpoint-url=http://localhost:4566 s3 cp /etc/localstack/init/ready.d/buckets/VIPER-Dummy.csv s3://testbucket/viper/VIPER-Dummy.csv
aws --endpoint-url=http://localhost:4566 s3 cp /etc/localstack/init/ready.d/buckets/VIPER_2_2024_10_29.csv s3://testbucket/viper/VIPER_2_2024_10_29.csv
aws --endpoint-url=http://localhost:4566 s3 cp /etc/localstack/init/ready.d/buckets/OCGM-Dummy.csv s3://testbucket/ocgm/OCGM-Dummy.csv
aws --endpoint-url=http://localhost:4566 s3 cp /etc/localstack/init/ready.d/buckets/OCG-Dummy.csv s3://testbucket/ocg-data/OCG-Dummy.csv
aws --endpoint-url=http://localhost:4566 s3 cp /etc/localstack/init/ready.d/buckets/PRAS-Dummy.csv s3://testbucket/pras/PRAS-Dummy.csv

echo 'Finished localstack bucket setup'

aws --endpoint-url=http://localstack:4566 s3 ls

aws --endpoint-url=http://localstack:4566 s3 ls s3://testbucket --recursive

echo 'Listing all queues in Localstack'

aws --endpoint-url=http://localstack:4566 sqs list-queues

aws configure list

echo All Ready