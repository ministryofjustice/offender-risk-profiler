FROM openjdk:11-slim
MAINTAINER HMPPS Digital Studio <info@digital.justice.gov.uk>

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY build/libs/offender-risk-profiler*.jar /app/app.jar
COPY run.sh /app

ENTRYPOINT ["/bin/sh", "/app/run.sh"]
