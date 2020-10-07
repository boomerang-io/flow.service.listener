package net.boomerangplatform.client;

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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import io.cloudevents.v1.CloudEventImpl;
import net.boomerangplatform.client.model.ValidateTokenRequest;
//import net.boomerangplatform.security.service.ApiTokenService;

@Service
public class WorkflowClientImpl implements WorkflowClient {

	@Value("${workflow.service.url.execute}")
	private String executeWorkflowUrl;

    @Value("${workflow.service.url.validateToken}")
    private String validateTokenWorkflowUrl;

	private static final Logger LOGGER = LogManager.getLogger(WorkflowClientImpl.class);

//	@Autowired
//	private ApiTokenService apiTokenService;
	
	@Autowired
	@Qualifier("internalRestTemplate")
	private RestTemplate restTemplate;

//	TODO return a wfActivityId
	@Override
	public void executeWorkflowPut(String subject, CloudEventImpl<JsonNode> jsonPayload) {
		final HttpHeaders headers = new HttpHeaders();
//		headers.add("Authorization", "Bearer " + apiTokenService.createJWTToken());
		headers.add("Content-Type", "application/cloudevents+json");
		final HttpEntity<CloudEventImpl<JsonNode>> req = new HttpEntity<>(jsonPayload, headers);
		
		LOGGER.info("executeWorkflowPut() - Request: " + req.toString());
	
		ResponseEntity<String> responseEntity = restTemplate.exchange(executeWorkflowUrl, HttpMethod.PUT, req, String.class);
		
		LOGGER.info("postWebhookEvent() - Status Code: " + responseEntity.getStatusCode());
		
//		FlowActivity activity = responseEntity.getBody();
//		FlowWebhookResponse response = new FlowWebhookResponse();
//		if(activity != null) {
//		  response.setActivityId(activity.getId());
//		}
//		return response;
	}
	
    @Override
    public Boolean validateWorkflowToken(String workflowId, String token) {
      final HttpHeaders headers = new HttpHeaders();
//      headers.add("Authorization", "Bearer " + apiTokenService.createJWTToken());
      headers.add("Content-Type", "application/json");

      ValidateTokenRequest payload = new ValidateTokenRequest();
      payload.setToken(token);
      final HttpEntity<ValidateTokenRequest> req = new HttpEntity<>(payload, headers);

      ResponseEntity<String> responseEntity =
          restTemplate.exchange(validateTokenWorkflowUrl.replace("{workflowId}", workflowId), HttpMethod.POST, req, String.class);

      LOGGER.info("workflowTriggerTokenCheck() - Status Code: " + responseEntity.getStatusCode());

      if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
        return true;
      }

      return false;
    }

//
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
}
