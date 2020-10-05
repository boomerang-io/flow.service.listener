package io.boomerang;
//package net.boomerangplatform;
//
//import java.io.IOException;
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.filter.OncePerRequestFilter;
//import net.boomerangplatform.client.WorkflowClient;
//
//public class SecurityFilter extends OncePerRequestFilter {
//
//  @Autowired
//  private WorkflowClient workflowClient;
//
//  @Override
//  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//      FilterChain chain) throws IOException, ServletException {
//    
//    String workflowId = "";
//    String trigger = "";
//    
//    if (!request.getHeader("Authorization").isEmpty()) {
//      workflowClient.validateTriggerToken(workflowId, trigger, request.getHeader("Authorization"));
//      SecurityContextHolder.clearContext();
//      chain.doFilter(request, response);
//      return;
//    } else if (!request.getParameter("access_token").isEmpty()) {
//      workflowClient.validateTriggerToken(workflowId, trigger, request.getParameter("access_token"));
//      SecurityContextHolder.clearContext();
//      chain.doFilter(request, response);
//      return;
//    } else {
//      response.reset();
//      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//      return;
//    }
//  }
//}