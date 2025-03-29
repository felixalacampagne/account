package com.felixalacampagne.account.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


// The tests don't find AccountService, even when package is included in TestApplication
// scanBasePackages. AccountService IS found by the real application, probably (who can tell
// how the magic works) because of the magic in SpringbootInTomcatApplicationConfig.
// It would make more sense to me have have the magic find AccountService in both tests
// and real app without having to put more magic in two different places...
// So, does having some magic here help?
@Configuration
@ComponentScan(basePackages = {"com.felixalacampagne.account.service"})
public class ServiceConfig
{
   // This is used by services to 'manually' create the JSON string from the Java object containing LocalDate
   // before returning a response. I don't think this is actually used anymore, the conversion from Java object
   // to JSON is being done by Spring.
//   @Bean
//   public ObjectMapper localdateJsonMapper()
//   {
//      return JsonMapper.builder()
//            .addModule(new JavaTimeModule())
//            .build();
//   }

// Added this in attempt at fixing the exception I had trying to save. Problem was actually due to
// missing 'default' constructor which appears to be required when using non-simple fields.
// Add,view and update appear to be working without needing this.
//   @Bean
//   public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
//
//       return builder -> {
//
//           // formatter
//           DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//           DateTimeFormatter dateTimeFormatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//           // deserializers
//           builder.deserializers(new LocalDateDeserializer(dateFormatter));
//           builder.deserializers(new LocalDateTimeDeserializer(dateTimeFormatter));
//
//           // serializers
//           builder.serializers(new LocalDateSerializer(dateFormatter));
//           builder.serializers(new LocalDateTimeSerializer(dateTimeFormatter));
//       };
//   }
}
