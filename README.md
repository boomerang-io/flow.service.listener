# Boomerang Flow Listener service <!-- omit in toc -->

## Table of Contents
- [Table of Contents](#table-of-contents)
- [Description](#description)
- [Dependencies](#dependencies)
- [Configuration](#configuration)
  - [Eventing](#eventing)
  - [Configuration Properties](#configuration-properties)
    - [Required Components](#required-components)
    - [Other Properties](#other-properties)
- [Usage](#usage)
- [Testing](#testing)
  - [NATS Integration](#nats-integration)
- [Known Issues](#known-issues)

## Description
Boomerang Flow Listener is a Spring Boot microservice designed to consume events based on the [CloudEvents](cloudevents.io) specification and industry standard to describe events in a common way.

It also has domain specific endpoints to consume events from specific sources that may not meet this specification such as DockerHub and Slack webhooks.

Events that are consumed will be translated to a CloudEvent and submitted to the Workflow Service via NATS or HTTP depending what has been configured.

## Dependencies

Maven dependencies:
1. Spring Boot Starter ([`spring-boot-starter`](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter))
2. Spring Boot Starter Web ([`spring-boot-starter-web`](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web))
3. Spring Boot Starter Log4j2 ([`spring-boot-starter-log4j2`](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-log4j2))
4. Spring Boot Starter Actuator ([`spring-boot-starter-actuator`](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-actuator))
5. Spring Boot DevTools ([`spring-boot-devtools`](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-devtools))
6. Jstan ([`java-nats-streaming`](https://mvnrepository.com/artifact/io.nats/java-nats-streaming))
7. Jnats ([`jnats`](https://mvnrepository.com/artifact/io.nats/jnats))
8. CloudEvents API ([`cloudevents-api`](https://mvnrepository.com/artifact/io.cloudevents/cloudevents-api))
9. Springfox Boot Starter ([`springfox-boot-starter`](https://mvnrepository.com/artifact/io.springfox/springfox-boot-starter))
10. Apache HttpClient ([`httpclient`](https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient))
11. Spring Boot Starter Test ([`spring-boot-starter-test`](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-test))

## Configuration

### Eventing

Boomerang Flow Listener integrates two types of event forwarding for both CloudEvents and Webhook notification events:
1. Forward the event NATS.
2. Forward the event directly to Workflow Service, which triggers a workflow if the payload is valid.

To configure the type of eventing, please see: [Configuration Properties](#other-properties).

### Configuration Properties

#### Required Components

There are two main components that require to be configured to ensure an end-to-end working condition of the service, NATS and Workflow Service:
1. NATS properties:
   - `eventing.nats.url` – the URL to connect to for NATS.
   - `eventing.nats.cluster` – the cluster ID where NATS is running.
   - `eventing.nats.channel` – the channel to which the message is to be published.

2. Workflow Service properties:
   - `workflow.service.host` – the host where Workflow Service is running.
   - `workflow.service.url.execute` – the URL to connect to for Workflow Service. 
   - `workflow.service.url.validateToken` – the URL used for token validation.

#### Other Properties
- `eventing.enabled` – defines the type of event forwarding – the events are forwarded to NATS if enabled, to Workflow Service otherwise.
- `eventing.auth.enabled` – enables authorization for exposed eventing APIs.

## Usage

To run the application, see the instructions at: [Spring Boot - Running Your Application](https://docs.spring.io/spring-boot/docs/current/reference/html/using-spring-boot.html#using-boot-running-your-application).

The application also exposes additional endpoints (via Springfox Swagger maven plugin) that provide information about all APIs, like request header, body, response type, response data, etc. These endpoints are:
* API Docs – http://localhost:7720/api-docs/
* Swagger API Documentation – http://localhost:7720/swagger-ui/

**NOTE**

Replace `<localhost:7720>` with the configured host and port on your computer.

## Testing

### NATS Integration

If [eventing is enabled](#other-properties) on this service, it will attempt to connect to, and subscribe to events via NATS. If you want to test NATS integration locally, create a NATS streaming server by running the following command ([Docker](https://www.docker.com) required):
```
docker run --entrypoint /nats-streaming-server -p 4222:4222 -p 8222:8222 nats-streaming
```

See [NATS Streaming on Docker Hub](https://hub.docker.com/_/nats-streaming) for more information.

## Known Issues

Need to kill NATS streaming pod after restarting minikube.
