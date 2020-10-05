package net.boomerangplatform;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import net.boomerangplatform.client.WorkflowClient;
import net.boomerangplatform.service.EventProcessorImpl;

public class SecurityFilter extends BasicAuthenticationFilter {
  
  protected static final String TOKEN_HEADER = "Authorization";
      
  protected static final String TOKEN_PARAM = "access_token";
 
  private static final Logger logger = LogManager.getLogger(SecurityFilter.class);

  private WorkflowClient workflowClient;
  
  protected final Boolean AUTHZ_ENABLED; 
  
  public SecurityFilter(AuthenticationManager authManager, WorkflowClient workflowClient, Boolean authzEnabled) {
    super(authManager);
    this.workflowClient = workflowClient;
    this.AUTHZ_ENABLED = authzEnabled;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    
    String token = getToken(request);
    String workflowId = "";
    String trigger = "";
    
    if (!checkAccess(workflowId, trigger, token)) {
      response.reset();
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    
    SecurityContextHolder.clearContext();
    chain.doFilter(request, response);
    return;
  }
  
  private String getToken(HttpServletRequest request) {
    if (request.getHeader(TOKEN_HEADER) != null && !request.getHeader(TOKEN_HEADER).isEmpty()) {
      return request.getHeader(TOKEN_HEADER).replace("Bearer ", "");
    } else if (request.getParameter(TOKEN_PARAM) != null && !request.getParameter(TOKEN_PARAM).isEmpty()) {
      return request.getParameter(TOKEN_PARAM);
    } else {
      return "";
    }
  }
  
  //@RequestHeader("Authorization") String token
  //@RequestParam("access_token") String token
  private Boolean checkAccess(String workflowId, String trigger, String token) {
    if (this.AUTHZ_ENABLED) {
      logger.info("checkAccess() - Token: " + token);
      if (token != null) {
        return this.workflowClient.validateTriggerToken(workflowId, trigger, token);
      } else {
        logger.error("checkAccess() - Error: no token provided.");
        return false;
      }
    } else {
      return true;
    }
  }
}