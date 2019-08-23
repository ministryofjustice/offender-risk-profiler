{{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: JAVA_OPTS
    value: "-Xmx512m" 

  - name: SPRING_PROFILES_ACTIVE
    value: "s3,postgres,sqs"

  - name: APPLICATION_INSIGHTS_IKEY
    valueFrom:
      secretKeyRef:
        name: {{ template "app.name" . }}
        key: APPINSIGHTS_INSTRUMENTATIONKEY

  - name: NOMIS_AUTH_URL
    value: {{ .Values.env.NOMIS_AUTH_URL | quote }}

  - name: JWT_PUBLIC_KEY 
    value: {{ .Values.env.JWT_PUBLIC_KEY | quote }}

  - name: ELITE2API_ENDPOINT_URL
    value: {{ .Values.env.ELITE2API_ENDPOINT_URL | quote }}

  - name: AWS_REGION
    value: "eu-west-2"

  - name: RISK_PROFILER_CLIENT_SECRET
    valueFrom:
      secretKeyRef:
        name: {{ template "app.name" . }}
        key: RISK_PROFILER_CLIENT_SECRET
  
  - name: RISK_PROFILER_CLIENT_ID
    valueFrom:
      secretKeyRef:
        name: {{ template "app.name" . }}
        key: RISK_PROFILER_CLIENT_ID

  - name: S3_BUCKET_DEFAULT
    valueFrom:
      secretKeyRef:
        name: risk-profiler-s3-bucket-output
        key: bucket_name

  - name: AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        name: risk-profiler-s3-bucket-output
        key: access_key_id

  - name: AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: risk-profiler-s3-bucket-output
        key: secret_access_key

  - name: DATABASE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output
        key: risk_profiler_password

  - name: SUPERUSER_USERNAME
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output
        key: database_username

  - name: SUPERUSER_PASSWORD
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output
        key: database_password

  - name: DATABASE_NAME
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output
        key: database_name

  - name: DATABASE_ENDPOINT
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output
        key: rds_instance_endpoint

  - name: SQS_AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        name: rp-sqs-instance-output
        key: access_key_id

  - name: SQS_AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: rp-sqs-instance-output
        key: secret_access_key

  - name: SQS_RPC_QUEUE_URL
    valueFrom:
      secretKeyRef:
        name: rp-sqs-instance-output
        key: sqs_rpc_url

{{- end -}}
