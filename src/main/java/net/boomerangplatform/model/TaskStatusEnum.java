package net.boomerangplatform.model;

public enum TaskStatusEnum {
  SUCCESS("success"),
  FAIL("fail");
  
  public final String status;

  private TaskStatusEnum(String status) {
      this.status = status;
  }
}
