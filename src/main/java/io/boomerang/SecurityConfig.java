package io.boomerang;
//package net.boomerangplatform;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
//import net.boomerangplatform.service.EventProcessorImpl;
//
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//  private static final String INFO = "/info";
//  private static final String API_DOCS = "/api-docs/**";
//  private static final String HEALTH = "/health";
//  private static final String INTERNAL = "/internal";
//  private static final String SWAGGER_UI="/swagger-ui.html";
//  private static final String SWAGGER_RESOURCES="/swagger-resources/**";
//  private static final String WEBJARS = "/webjars/**";
// 
//
//  private static final Logger logger = LogManager.getLogger(EventProcessorImpl.class);
//  
//  @Override
//  protected void configure(HttpSecurity http) throws Exception
//  {
//    logger.info("Token Authorization Enabled");
////    final SecurityFilter securityFilter = new SecurityFilter();
//     http.csrf().disable()
//        .authorizeRequests()
//        .antMatchers(HEALTH, API_DOCS, INFO, INTERNAL, SWAGGER_UI, SWAGGER_RESOURCES, WEBJARS).permitAll()
//        .anyRequest().authenticated();
//     http.addFilterBefore(new SecurityFilter(), BasicAuthenticationFilter.class);
//  }
//
//}
