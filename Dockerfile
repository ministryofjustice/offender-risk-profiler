FROM openjdk:11-slim
MAINTAINER HMPPS Digital Studio <info@digital.justice.gov.uk>

RUN apt-get update && apt-get install -y curl

RUN addgroup --gid 2000 --system appgroup && \
    adduser --uid 2000 --system appuser --gid 2000

WORKDIR /app

COPY build/libs/offender-risk-profiler*.jar /app/app.jar
COPY run.sh /app

RUN chown -R appuser:appgroup /app
USER 2000

ENTRYPOINT ["/bin/sh", "/app/run.sh"]
