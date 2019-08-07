
### Example deploy command
```
helm --namespace offender-categorisation-dev  --tiller-namespace offender-categorisation-dev upgrade offender-risk-profiler ./offender-risk-profiler/ --install --values=values-dev.yaml --values=example-secrets.yaml
```

### Rolling back a release
Find the revision number for the deployment you want to roll back:
```
helm --tiller-namespace offender-categorisation-dev history offender-risk-profiler -o yaml
```
(note, each revision has a description which has the app version and circleci build URL)

Rollback
```
helm --tiller-namespace offender-categorisation-dev rollback offender-risk-profiler [INSERT REVISION NUMBER HERE] --wait
```

### Helm init

```
helm init --tiller-namespace offender-categorisation-dev --service-account tiller --history-max 200
```

