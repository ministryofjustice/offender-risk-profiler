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


## Intellij setup

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
localstack, which can be run as a docker container. See https://github.com/localstack/localstack. In this case it is up to you to create the required queues.

Also, for tests the 'localstack-embedded' profile is used which runs localstack in a thread and configures it with the necessary queues.

#### Tests

Note that **Redis** needs to be running for the unit / integration tests.

