package io.boomerang.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import io.boomerang.model.ValidateTokenRequest;
import io.cloudevents.v1.CloudEventImpl;

@Service
public class WorkflowClientImpl implements WorkflowClient {

  private static final Logger LOGGER = LogManager.getLogger(WorkflowClientImpl.class);

  @Autowired
  @Qualifier("internalRestTemplate")
  private RestTemplate restTemplate;

  @Value("${workflow.service.url.execute}")
  private String executeWorkflowUrl;

  @Value("${workflow.service.url.validateToken}")
  private String validateTokenWorkflowUrl;

  // TODO return a wfActivityId
  @Override
  @Async
  public void executeWorkflowPut(CloudEventImpl<JsonNode> jsonPayload) {
    final HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/cloudevents+json");

    final HttpEntity<CloudEventImpl<JsonNode>> req = new HttpEntity<>(jsonPayload, headers);

    LOGGER.debug("executeWorkflowPut() - Request Body Attributes: " + req.getBody().getAttributes().toString());
    LOGGER.debug("executeWorkflowPut() - Request Body Data: " + req.getBody().getData().get().toString());

    ResponseEntity<String> responseEntity = restTemplate.exchange(executeWorkflowUrl, HttpMethod.PUT, req,
        String.class);

    LOGGER.debug("executeWorkflowPut() - Status Code: " + responseEntity.getStatusCode());
  }

  @Override
  public HttpStatus validateWorkflowToken(String workflowId, String token) {
    if (token != null && !token.isBlank()) {
      final HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Type", "application/json");

      ValidateTokenRequest payload = new ValidateTokenRequest();
      payload.setToken(token);
      LOGGER.debug("validateWorkflowToken() - URL: " + validateTokenWorkflowUrl.replace("{workflowId}", workflowId));

      final HttpEntity<ValidateTokenRequest> req = new HttpEntity<>(payload, headers);
      try {
        ResponseEntity<String> responseEntity =
            restTemplate.exchange(validateTokenWorkflowUrl.replace("{workflowId}", workflowId),
                HttpMethod.POST, req, String.class);
  
        LOGGER.debug("validateWorkflowToken() - Status Code: " + responseEntity.getStatusCode());
        LOGGER.debug("validateWorkflowToken() - Body: " + responseEntity.getBody().toString());
  
        return responseEntity.getStatusCode();
      } catch (HttpClientErrorException e) {
        LOGGER.error("validateWorkflowToken() - Status Code: " + e.getStatusCode());
        return e.getStatusCode();
      } catch (Exception e) {
        throw e;
      }
    }
    return HttpStatus.BAD_REQUEST;
  }

// @formatter:off
//	@Override
//	public FlowActivity getFlowActivity(String token, String activityId) {
//		String tokenId = token;
//		FlowWorkflowEntity entity = flowWorkflowService.findByTokenString(tokenId);
//		
//		if (entity == null) {
//			return null;
//		}
//		
//		return getActivityById(activityId);
//	}
//
//  private FlowActivity getActivityById(String activityId) {
//    String url = activityStatus.replace("{activity.id}", activityId);
//		FlowExecutionRequest executionRequest = new FlowExecutionRequest();
//		
//		final HttpHeaders headers = new HttpHeaders();
//		headers.add("Authorization", "Bearer " + apiTokenService.createJWTToken());
//		final HttpEntity<FlowExecutionRequest> req = new HttpEntity<>(executionRequest, headers);
//		ResponseEntity<FlowActivity> responseEntity = restTemplate.exchange(url, HttpMethod.GET, req,
//				FlowActivity.class);
//		FlowActivity activity = responseEntity.getBody();
//		return activity;
//  }
//
//  @Override
//  public FlowActivity getFlowActivityViaProperty(String token, String key, String value) {
//    String tokenId = token;
//    FlowWorkflowEntity entity = flowWorkflowService.findByTokenString(tokenId);
//    
//    if (entity == null) {
//        return null;
//    }
//    
//    FlowWorkflowActivityEntity activity = flowActivtyService.findByWorkflowAndProperty(entity.getId(), key, value);
//    
//    if (activity == null) 
//    {
//      return null;
//    }
//    return getActivityById(activity.getId());
//  }
// @formatter:on
}
