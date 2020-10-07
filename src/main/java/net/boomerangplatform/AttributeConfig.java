package net.boomerangplatform;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import net.boomerangplatform.attributes.TokenAttributeArgumentResolver;

@Configuration
public class AttributeConfig implements WebMvcConfigurer {

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(new TokenAttributeArgumentResolver());
  }
}
