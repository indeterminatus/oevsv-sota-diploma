# OE SOTA award generation

A simple web-service (with accompanying Web UI) to facilitate management, generation and sending of awards for
SOTA accomplishments specific to the Austrian SOTA division.

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Configuration options

In order for the application to work properly, a few configuration options need to be passed via environment:

* `QUARKUS_MAILER_HOST`
* `QUARKUS_MAILER_USERNAME`
* `QUARKUS_MAILER_PASSWORD`
* `QUARKUS_MAILER_FROM`
* `QUARKUS_MAILER_MOCK=false`

If you're not running the application in dev mode, you should specify the following:

* `QUARKUS_REDIS_HOSTS`
* `QUARKUS_DATASOURCE_JDBC_URL`

To configure the behaviour of the application, following environment variables are supported:

* `DIPLOMA_MAILING_RECIPIENTS`: Recipient mail addresses, separated by `,`
* `CHECK_AFTER_DATE`: a date in the format `YYYY-MM-DD`
* `PDF_PREVIEW_QUALITY`: an integer percentage between 1 and 100. This determines the quality (and file size) of the
  preview attachments. Default is `95`.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

## Building everything for Docker

This is the sequence to use to build the application for Docker:

```shell script
mvn clean package
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/oevsv-sota-diploma:1.0.0 .
```
