# offender-risk-profiler
API for Providing Risk Profile Information on Offenders

##Intellij setup

- Install jdk 16
- Enable Gradle using jdk 16
- set jdk in project structure
- Enable Annotation Processors at "Settings > Build > Compiler > Annotation Processors"

#### Health

- `/ping`: will respond `pong` to all requests.  This should be used by dependent systems to check connectivity to offender risk profiler,
rather than calling the `/health` endpoint.
- `/health`: provides information about the application health and its dependencies.  This should only be used
by offender risk profiler health monitoring (e.g. pager duty) and not other systems who wish to find out the state of offender risk profiler.
- `/info`: provides information about the version of deployed application.

#### SQS
Use `sqs` profile to run against an aws sqs resource. See required env variables in application-sqs.properties
There is a `localstack` profile for running locally with the sqs resource provided by
localstack, which can be run as a docker container. See https://github.com/localstack/localstack. In the case it is up to you to create the required queues.

Also for tests the 'localstack-embedded' profile is used which runs localstack in a thread and configures it with the necessary queues.

#### Tests

Note that **Redis** needs to be running for the unit / integration tests.
