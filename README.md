# Listener

The Listener is designed to consume events based on the [CloudEvents](cloudevents.io) specification and industry standard to describe events in a common way.

It also has domain specific endpoints to consume events from specific sources that may not meet this specification such as DockerHub and Slack webhooks.

Events that are consumed will be translated to a CloudEvent and submitted to the Workflow Service via NATS or HTTP depending what has been configured.

## Known Issues

Need to kill nats stremaing pod after restarting minikube 

