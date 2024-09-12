package com.felixalacampagne.account.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// @Component is needed to get @Value to work. The instance of Version must be autowired by spring for
// the object to have the @Value values. For spring to autowire the object into the AccountController 
// the Version class must be in the same package as AccountController. There may be a different way
// to do it by explicitly creating a Bean. This might be prefarble since Version really belongs in the model package
// because it gets send to the client
@Component 
public class Version
{

@Value("${falc.account.name}")
private String name;

@Value("${falc.account.version}")
private String version;

@Value("${falc.account.db}")
private String db;

   public Version()
   {
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getVersion()
   {
      return version;
   }

   public void setVersion(String version)
   {
      this.version = version;
   }

   public String getDb()
   {
      return db;
   }

   public void setDb(String db)
   {
      this.db = db;
   }

   @Override
   public String toString()
   {
      return "Version [name=" + name + ", version=" + version + ", db=" + db + "]";
   }
   
   
}
