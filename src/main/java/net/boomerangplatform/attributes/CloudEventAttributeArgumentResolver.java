package net.boomerangplatform.attributes;

import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import io.cloudevents.CloudEvent;
import io.cloudevents.v1.AttributesImpl;
import io.cloudevents.v1.http.Unmarshallers;

public class CloudEventAttributeArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterAnnotation(CloudEventAttribute.class) != null;
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

    CloudEventAttribute requestAttributeAnnotation = parameter.getParameterAnnotation(CloudEventAttribute.class);

    // Sanity check
    if (requestAttributeAnnotation == null)
      return null;

    // Get the native request and body as a string
    HttpServletRequest httpRequest = (HttpServletRequest) webRequest.getNativeRequest();
    String stringBody = httpRequest.getReader().lines().collect(Collectors.joining());

    // Unmarshals the request body and headers to a `CloudEvent` instance
    CloudEvent<AttributesImpl, JsonNode> event = Unmarshallers.structured(JsonNode.class).withHeaders(() -> {
      return Collections.list(httpRequest.getHeaderNames()).stream().collect(
          Collectors.toMap(Function.identity(), headerName -> Collections.list(httpRequest.getHeaders(headerName))));
    }).withPayload(() -> stringBody).unmarshal();

    return event;
  }
}
