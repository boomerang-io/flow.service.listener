package net.boomerangplatform.model;

import java.util.HashMap;
import java.util.Map;
import io.cloudevents.extensions.ExtensionFormat;
import io.cloudevents.extensions.InMemoryFormat;

public class CustomAttributeExtension implements ExtensionFormat {

  private final Map<String, String> transport = new HashMap<>();
  private final InMemoryFormat inmemory;
  
  public CustomAttributeExtension(final String name, final String value) {
      transport.put(name, value);
      
      this.inmemory = new InMemoryFormat() {
          
          @Override
          public Class<?> getValueType() {
              return String.class;
          }
          
          @Override
          public Object getValue() {
              return value;
          }
          
          @Override
          public String getKey() {
              return name;
          }
      };
  }
  
  @Override
  public InMemoryFormat memory() {
      return inmemory;
  }

  @Override
  public Map<String, String> transport() {
      return transport;
  }

}
