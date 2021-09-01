# Boomerang Flow Listener Service <!-- omit in toc -->

## Table of Contents

- [Table of Contents](#table-of-contents)
- [Description](#description)
- [Dependencies](#dependencies)
  - [Compile Maven Dependencies](#compile-maven-dependencies)
  - [Test Maven Dependencies](#test-maven-dependencies)
- [Configuration](#configuration)
  - [Eventing](#eventing)
  - [Configuration Properties](#configuration-properties)
    - [Required Components](#required-components)
    - [Other Properties](#other-properties)
- [Usage](#usage)
  - [Notes](#notes)
- [Testing](#testing)
  - [NATS Jetstream Integration](#nats-jetstream-integration)
- [Known Issues](#known-issues)
- [Contributing](#contributing)
- [License](#license)

## Description

Boomerang Flow Listener is a Spring Boot microservice designed to consume events based on the [CloudEvents][1] specification and industry standard to describe events in a common way.

It also has domain specific endpoints to consume events from specific sources that may not meet this specification such as DockerHub and Slack webhooks.

Events that are consumed will be translated to a CloudEvent and submitted to the Workflow Service via [NATS Jetstream][13] or HTTP depending what has been configured.

## Dependencies

### Compile Maven Dependencies

1. Spring Boot Starter ([`spring-boot-starter`][2])
2. Spring Boot Starter Web ([`spring-boot-starter-web`][3])
3. Spring Boot Starter Log4j2 ([`spring-boot-starter-log4j2`][4])
4. Spring Boot Starter Actuator ([`spring-boot-starter-actuator`][5])
5. Spring Boot DevTools ([`spring-boot-devtools`][6])
6. Boomerang NATS Jetstream Library ([`lib-jetstream`][7])
7. CloudEvents API ([`cloudevents-api`][8])
8. Springfox Boot Starter ([`springfox-boot-starter`][9])
9. Apache HttpClient ([`httpclient`][10])

### Test Maven Dependencies

1. Spring Boot Starter Test ([`spring-boot-starter-test`][11])
2. JUnit Jupiter Engine ([`junit-jupiter-engine`][12])

## Configuration

### Eventing

Boomerang Flow Listener integrates two types of event forwarding for both [CloudEvents][1] and Webhook notification events:

1. Forward the event to [NATS Jetstream][13].
2. Forward the event directly to Workflow Service, which triggers a workflow if the payload is valid.

To configure the type of eventing, please see: [Configuration Properties](#other-properties).

### Configuration Properties

#### Required Components

There are two main components that require to be configured to ensure an end-to-end working condition of the service, [NATS Jetstream][13] and Workflow Service:

1. [NATS Jetstream][13] properties<sup id="ref-footnote-1">[1](#footnote-1)</sup>:
   - `eventing.jetstream.server.url` - the URL for connecting to [NATS Jetstream][13].
   - `eventing.jetstream.stream.name` - the name of the Jetstream stream to connect to. If the stream doesn't exists, it will be automatically created by `lib-jetstream`.
   - `eventing.jetstream.stream.subject` - the subject name for the stream if this is created. Visit <https://docs.nats.io/jetstream/concepts/streams> for more information.
   - `eventing.jetstream.consumer.push.name` - the name of the Jetstream push-based consumer to subscribe to. If the consumer doesn't exists, it will be automatically created by `lib-jetstream`.
   - `eventing.jetstream.consumer.push.delivery-subject` - the subject to deliver observed messages from Jetstream. Visit <https://docs.nats.io/jetstream/concepts/consumers#deliversubject> for more information.
   - `eventing.jetstream.consumer.pull.name` - the name of the Jetstream pull-based consumer to subscribe to. If the consumer doesn't exists, it will be automatically created by `lib-jetstream`.

2. Workflow Service properties:
   - `workflow.service.host` - the host where Workflow Service is running.
   - `workflow.service.url.execute` - the URL to connect to for Workflow Service.
   - `workflow.service.url.validateToken` - the URL used for token validation.

#### Other Properties

- `eventing.jetstream.enabled` - defines the type of event forwarding - the events are forwarded to [NATS Jetstream][13] if enabled, to Workflow Service otherwise.
- `eventing.auth.enabled` - enables authorization for exposed eventing APIs.

## Usage

To run the application, see the instructions at: [Spring Boot - Running Your Application](https://docs.spring.io/spring-boot/docs/current/reference/html/using-spring-boot.html#using-boot-running-your-application).

The application also exposes additional endpoints (via [Springfox Swagger][9] Maven plugin) that provide information about all APIs, like request header, body, response type, response data, etc. These endpoints are:

- OpenAPI v2 Docs - <http://localhost:7720/api-docs/>
- OpenAPI v3 Docs - <http://localhost:7720/v3/api-docs/>
- Swagger API Documentation - <http://localhost:7720/swagger-ui/>

### Notes

Replace `<localhost:7720>` with the configured host and port on your computer.

## Testing

### [NATS Jetstream][13] Integration

If [eventing is enabled](#other-properties) on this service, it will attempt to connect to, and publish messages to [NATS Jetstream][13]. If you want to test the integration locally, create a server by running the following command ([Docker][14] required):

```bash
docker run --detach --network host -p 4222:4222 --name nats-jetstream nats -js
```

Visit <https://docs.nats.io/jetstream/getting_started/using_docker> for more information.

## Known Issues

No known issues.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License

All of our work is licenses under the [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0) unless specified otherwise due to constraints by dependencies.

---

<span id="footnote-1">1.</span> For more information on how to configure `lib-jetstream`, please visit: [https://github.com/boomerang-io/flow.lib.streaming/tree/feature/jetstream][7]. [↩](#ref-footnote-1)

[1]: https://cloudevents.io "CloudEvents"
[2]: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter "Spring Boot Starter"
[3]: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web "Spring Boot Starter Web"
[4]: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-log4j2 "Spring Boot Starter Log4j2"
[5]: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-actuator "Spring Boot Starter Actuator"
[6]: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-devtools "Spring Boot DevTools"
[7]: https://github.com/boomerang-io/flow.lib.streaming "Boomerang NATS Jetstream Library"
[8]: https://mvnrepository.com/artifact/io.cloudevents/cloudevents-api "CloudEvents API"
[9]: https://mvnrepository.com/artifact/io.springfox/springfox-boot-starter "SpringFox Boot Starter"
[10]: https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient "Apache HttpClient"
[11]: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-test "Spring Boot Starter Test"
[12]: https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-engine "JUnit Jupiter Engine"
[13]: https://docs.nats.io/jetstream/jetstream "About NATS Jetstream"
[14]: https://www.docker.com "Docker"
