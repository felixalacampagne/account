package com.felixalacampagne.account.application;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;

/**
 * To be honest I've no idea what this is doing. It was created at the suggestion of Google AI to solve
 * POST requests being blocked with '403 Invalid CORS request' when I switched from the Windows based
 * apache-tomcat configuration using jk_mod (AJP?) to a docker based config relying on http
 * communication between apache and tomcat. Apparently the error is caused by the presence
 * of an 'Origin' header in the request which refers to the external address, ie. https://host:portX/app,
 * which doesn't match the actual URL seen by tomcat, ie. http://docker-service:portZ/app. The problem does not
 * happen for GET requests as there is not usually an 'Origin' header. At least that is what I understood.
 * 
 * The problem was temporarily fixed by adding a 'Location' block to httpd.conf which strips the 'Origin' header
 * and a '<Valve className="org.apache.catalina.valves.RemoteIpValve" ... />' to the 'Host' block of the tomcat
 * server.xml. In theory both of these workarounds can be removed if this bit of magic does what Google
 * thinks it should.
 * 
 * 
 * The google explanation (replace 44443 with the actual external port):
 * How it works end-to-end. Apache sends Host: host:44443, X-Forwarded-Proto: https, and X-Forwarded-Port: 44443. 
 * Spring's ForwardedHeaderFilter runs first (HIGHEST_PRECEDENCE), updates the request URL to 
 * https://host:44443/app/restapi, and Spring's CORS check sees an exact match with 
 * Origin: https://host:44443
 * 
 * To be tested.....
 */

@Configuration
public class ForwardedHeaderConfig {

    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> bean = new FilterRegistrationBean<>(new ForwardedHeaderFilter());
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}