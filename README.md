# paymenthub-ee-notifications

Sends the notifications for Payment Hub EE: it turns a finished payment into a message
and delivers it to the customer over SMS or email.

[![License](https://img.shields.io/badge/License-MPL--2.0-blue.svg)](LICENSE)

## What it is

One application, built from what used to be two services:

- **the connector** (`org.mifos.connector.notification`, from `ph-ee-notifications`) takes
  part in the payment workflow. It does not expose a "send me a message" API: it listens
  to Zeebe, and the BPMN process decides when a customer should be told something. When a
  payment ends it fills in a message template with the transaction details.
- **the gateway** (`org.fineract.messagegateway`, from `message-gateway`) owns the sending.
  It stores each message, hands it to an SMS or email provider (Twilio, InfoBip, Telerivet,
  Jasmin, RapidPro), keeps the delivery status, and receives the provider callbacks.

They used to run as two containers that called each other over HTTP. They now run as one
process, so that call is a call inside the same JVM.

## Ports

| Port | What answers there |
| --- | --- |
| 9191 | The gateway HTTP API (`/sms`, `/smsbridges`, `/tenants`) and the actuator |
| 5000 | The Camel REST routes of the connector (`/sms/callback`, `/channel/...`) |

## Database

The gateway half owns the `messagegateway` schema and creates it with Flyway at startup,
so the application needs a MySQL database. Point it at one with `SPRING_DATASOURCE_URL`,
`MYSQL_USERNAME` and `MYSQL_PASSWORD`.

## Building

```
./gradlew build
```

That produces `build/libs/app.jar`, which is what the `Dockerfile` copies. Java 21 is
required. Every shared library version comes from `org.mifos:paymenthub-ee-bom`, imported
as an `enforcedPlatform` — do not pin managed versions in `build.gradle`.

The context test starts the whole application, gateway half included, so it needs a
database; H2 stands in for MySQL there.

## Branches

- `dev` is the active development branch — all PRs should target `dev`.
- `main` holds released versions.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md), our [Code of Conduct](CODE_OF_CONDUCT.md) and the
[security policy](security.md).
