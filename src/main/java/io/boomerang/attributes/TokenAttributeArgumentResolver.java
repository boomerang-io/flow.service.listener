package io.boomerang.attributes;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class TokenAttributeArgumentResolver implements HandlerMethodArgumentResolver {

  private static final String TOKEN_HEADER_NAME = "Authorization";

  private static final String TOKEN_URL_PARAM_NAME = "access_token";

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterAnnotation(TokenAttribute.class) != null;
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

    TokenAttribute requestAttributeAnnotation = parameter.getParameterAnnotation(TokenAttribute.class);

    // Sanity check
    if (requestAttributeAnnotation == null)
      return null;

    // Get the request and extract the token
    HttpServletRequest httpRequest = (HttpServletRequest) webRequest.getNativeRequest();
    String tokenHeaderValue = httpRequest.getHeader(TOKEN_HEADER_NAME);
    String tokenUrlParamValue = httpRequest.getParameter(TOKEN_URL_PARAM_NAME);

    // Extract token from header if possible
    if (tokenHeaderValue != null && !tokenHeaderValue.isEmpty()) {
      return Arrays.asList(tokenHeaderValue.split(" ", 2)).stream().reduce((first, last) -> last).orElse("");
    }

    // Extract token from URL parameters if possible
    if (tokenUrlParamValue != null) {
      return tokenUrlParamValue;
    }

    // Empty token otherwise
    return "";
  }
}
