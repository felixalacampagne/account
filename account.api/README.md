18-Aug-2024 The proof of concept for accessing the Access database using Spring JPA seems to have
all the features I need for now working so I figured now was the time to start doing it for real. 
I decided to start from scratch since there had been a large number of wild goose chases on the way
to getting something working. 

This was probably a mistake.

The code was migrated to the new structure easily enough and compiled without a problem.
Running a test was another matter. For no explicable reason Spring emits

'does not declare any static, non-private, non-final, nested classes annotated with @Configuration.'

and dies. WTF? The code is the same as the proof of concept with a couple of name changes here and there.

After a couple of hours beating my head against a brick wall I randomly tried to do a
maven build. This was lucky since it threw out a different error - something to do
with duplicate magic being used, I think it was @SpringBootApplication, and it names both places
with the duplicate, which is something of a miracle. It appeared that having main/AccountApplication and
test/TestApplication both containing the configuration magic and @SpringBootApplication was causing a
problem. So I deleted the magic from TestApplication and suddenly my test was working (again).

I think the problem arose because I moved the 'Application' classes to the top level account package so
both were being discovered by Spring. In the proof of concept the main Application class was in a sub-package
so only the test Application is discovered magically. Not sure how the main Application is invoked but I guess
it requires a specific class to be referenced somewhere which guarantees that the configuration is picked up. This 
theory appears to have been confirmed by moving the main Application class to a sub-package again and
now the tests run with the test Application as before.

Curiously the thread name for the test is 'springboot-in-tomcat' which is the name of the proof of concept project
but should not be appearing in the accountapi project - I guess some more magic has gone rogue and is going to waste
hours an hours trying to fix.... didn't take quite that long... the culprit was spring.application.name in 
application.properties which eclipse does not search in by default.

So next thing to do is see if works with from the war file with my tomcat server.

19 Aug 2024 Added a redirect in httpd.conf for jaccountapi and updated pom to produce jaccountapi.war.
Copy the war to tomcat and it was expanded. First attempt failed with the white label error page saying 404
because I forgot I changed the default path to 'greeting'. 'greeting' failed with 500 and a log message. The
path to the DB was wrong. Rebuilt the war with a fixed path (must figure out a way to read the db location
from a local installation file) and redeployed it - all sorts of weird errors about something to do with forms
and a missing xalan transformerimpl - basically WTF? Stop the servers, removed the war and expanded directory,
restarted the servers and deployed the war to the webapps directory. It appears to start but with a worrying sounding
message in localhost.log

ApplicationContext.log 2 Spring WebApplicationInitializers detected on classpath

I only have one Spring application and have no idea where 2 of these things can be comeing from.

However, the greeting URL gives me my message, and... tada... the API URLs list the DB content! Yay!
