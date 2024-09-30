# Offender Risk Profiler

> API for Providing Risk Profile Information on Offenders

[![CircleCI](https://circleci.com/gh/ministryofjustice/offender-risk-profiler/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/offender-risk-profiler/tree/main)
[![Docker Repository on Quay](https://img.shields.io/badge/quay.io-repository-2496ED.svg?logo=docker)](https://quay.io/repository/hmpps/offender-risk-profiler)
[![Known Vulnerabilities](https://snyk.io/test/github/ministryofjustice/pathfinder-api/badge.svg)](https://snyk.io/test/github/ministryofjustice/offender-risk-profiler)
[![Repo Standards Badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.data%5B%3F%28%40.name%20%3D%3D%20%22offender-risk-profiler%22%29%5D.status&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fgithub_repositories)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/github_repositories#offender-risk-profiler "Link to report")

<!-- [![API docs](https://img.shields.io/badge/API_docs-view-85EA2D.svg?logo=swagger)](https://###.service.justice.gov.uk/swagger-ui/index.html?configUrl=/v3/api-docs) -->
<!-- [![License: ###](https://img.shields.io/badge/License-###-lightgrey.svg)](https://opensource.org/licenses/###) -->

[![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=flat&logo=Gradle&logoColor=white)](https://gradle.org/)
[![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=flat&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)

[![AWS](https://img.shields.io/badge/-Amazon%20AWS-232F3E?logo=Amazonaws&logoColor=amazonorange)](https://aws.amazon.com/)
[![Docker](https://img.shields.io/badge/-Docker-000?logo=docker)](https://www.docker.com)
[![Kubernetes](https://img.shields.io/badge/kubernetes-%23326ce5.svg?style=flat&logo=kubernetes&logoColor=white)](https://kubernetes.io/)
[![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=postgres&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=flat&logo=redis&logoColor=white)](https://redis.io/)

## Purpose

Risk Profiler API provides a Risk profile of each prisoner in a prison. Prisons need to be included in the configuration. The Risk profiler uses decision trees to determine the level of risk for the following categories - violence, escape, extremism, serious organised crime or life. It gathers this information from resources such as pathfinder, soc,. 

This risk profile information is used by the Offender Categorisation tool to allow users to help judge level of risk when defining a categorisation of a nominal.


## Local Setup

Setup risk profiler dependencies on localstack by running docker-compose-local:

`docker-compose -f docker-compose-local.yml up
`

This simulates the aws environment with sqs, s3, Redis, Postrgres.

 - It will create the sqs listeners required - events, events-dl
 - riskprofilerchangequeue producer queue 
 - s3 testbucket with the following folders: ocgm, viper, pras and ocg
 - populate ocgm, viper, pras and ocg with dummy .csv test data files


**Run the sql script to create tables at setup-local\setup_sql_tables.sql**

### LocalStack Desktop

Install Localstack Desktop. This will allow you to put messages on the queues and upload test data to the s3 buckets for manual testing. 

See https://docs.localstack.cloud/user-guide/tools/localstack-desktop/

### Run Risk Profiler

Set up a run configuration in Intellij and add the following:

- **Active profiles set** - stdout,localstack,postgres
- **Environment variables set** - DATABASE_ENDPOINT=localhost:5432;DATABASE_NAME=risk-profiler;DATABASE_PASSWORD=risk-profiler;DATABASE_USERNAME=risk-profiler;SUPERUSER_PASSWORD=risk-profiler;SUPERUSER_USERNAME=risk-profiler

### Risk Profiler Running

- If Risk Profiler is up and running successfully we would expect to see in the logs:

`
o.s.b.a.e.web.EndpointLinksResolver      : Exposing 3 endpoint(s) beneath base path '' |  
o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path '' |   
offenderRiskProfilerApplication$Companion : Started OffenderRiskProfilerApplication.Companion in 13.605 seconds (JVM running for 16.21) |

**The logs above tells us the following:**

- The three sqs endpoints have started and the application has binded with them. 
- The Risk Profiler application has started on port 8080.

### RiskProfiler Components

1. **Events Listener** - Listens to events queue  for an Alert or Incident and either clears the Redis cache or polls the Prisoner service. If the prisoners service is triggered The soc, violenceescape dcision trees are updated. The current profile stored in the Risk Profiler table will be queired to see if anything has changed, if so it is updated with the latest information. 
2. **SQS Service** - Sends Risk Profiler change messages to the sqs.rpc.queue.url=risk_profiler_change. These publishd messages are read by the CAT UI. To update the UI accordingly.
3. **Queue Scheduler** - Will check if any messages have ended up on the Events Dead Letter Queue. If they have it will release them for a retry. This is called periodically. You will the following in the log: `Starting: Event DLQ` and `Complete: Event DLQ` 
4. **Csv S3 Processor** - Will poll the S3 folders to check for any CSV files. IF they are it will process them. Importing data from the csv file in to the relevant repository.
5. **Rest API** - Provides developer support for manually triggering Risk Profiler Processes.
- `/startPollPrisoners` - Start a batch job run.
- `/prison/{prisonId}` - Add prison with prison id to the config. The overnight polling batch will then include this prison.
- `/transferEventMessages` - Manually trigger the transfer of any DLQ messages back to the event queue to be retried.
- `/soc/{nomsId}` - Return SOC Risk for offender.
- `/escape/{nomsId}` - Return Escape Risk for offender.
- `/violence/{nomsId}` - Return Violence Risk for offender.
- `/extremism/{nomsId}` - Return Extremism Risk for offender.
- `/life/{nomsId}` - Assess whether offender has a life sentence.

#### Health

- `/ping`: will respond `pong` to all requests.  This should be used by dependent systems to check connectivity to offender risk profiler,
  rather than calling the `/health` endpoint.
- `/health`: provides information about the application health and its dependencies.  This should only be used
  by offender risk profiler health monitoring (e.g. pager duty) and not other systems who wish to find out the state of offender risk profiler.
- `/info`: provides information about the version of deployed application.

#### Integration Testing
Integration tests uses the 'localstack-embedded' profile is which runs localstack in a thread and configures it with the necessary queues.

You can also run the tests against local docker images constructed from docker-compose. To do this change the following properties in test/application-localstack.properties:

sqs.provider=localstack
s3.provider=localstack

#### Tests

Note that **Redis** needs to be running for the unit / integration tests.



