


Ensure to export `DD_API_KEY`:
```shell
export DD_API_KEY=<secret_api_key>
```

Open the project in advance and collapsed all regions


Run a docker compose file and rebuild the apps:
```shell
docker compose -f docker-compose-step1.yaml up --build
```

Test the service endpoints using the `request.http` file and check the trace using [the Datadog backend](https://app.datadoghq.eu/account/login).
