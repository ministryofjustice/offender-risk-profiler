# offender-risk-profiler
API for Providing Risk Profile Information on Offenders

##Intellij setup

- Install jdk 11
- Enable Gradle using jdk 11
- set jdk in project structure
- enable the lombok plugin and restart if necessary
- Enable Annotation Processors at "Settings > Build > Compiler > Annotation Processors"

#### Health

- `/ping`: will respond `pong` to all requests.  This should be used by dependent systems to check connectivity to offender risk profiler,
rather than calling the `/health` endpoint.
- `/health`: provides information about the application health and its dependencies.  This should only be used
by offender risk profiler health monitoring (e.g. pager duty) and not other systems who wish to find out the state of offender risk profiler.
- `/info`: provides information about the version of deployed application.