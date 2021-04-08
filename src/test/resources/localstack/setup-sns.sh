#!/usr/bin/env bash

set -e
export TERM=ansi
export AWS_ACCESS_KEY_ID=foobar
export AWS_SECRET_ACCESS_KEY=foobar
export AWS_DEFAULT_REGION=eu-west-2

aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name events-dl
aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name events
aws --endpoint-url=http://localhost:4576 sqs set-queue-attributes --queue-url "http://localhost:4576/queue/events" --attributes '{"RedrivePolicy":"{\"maxReceiveCount\":\"3\", \"deadLetterTargetArn\":\"arn:aws:sqs:eu-west-2:000000000000:events-dl\"}"}'

echo All Ready
