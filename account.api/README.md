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

20-Aug-2024

NB. The source of ucanaccess is apparently now https://github.com/spannm/ucanaccess. Not sure if this is what I used
or the one from sourceforge. Actually I'm thinking of the 'dialect', the actual driver I get from maven so it should be
the right one.

I finally have a Java based backend which can read the Account Access database. This results in different API URLs and some
differences in the format of the data returned so a new version of the Account UI is needed. The backend is a Spring web application
which runs in my tomcat server. It may be possible to package the frontend in the same WAR as the backend code so I'm combining
the UI project with the API project. Needless to say getting Eclipse, Spring, Angular and Maven to work together is not going to
be straight forward but ho! hum! isn't it always the case with this continuous improvement shirt.

First thing to do is get the UI code into the project. No idea what the required structure is for this - Google was surprisingly
quiet on the matter. Will need to figure out how a Spring web application that includes a frontend is supposed to work. I have a feeling
that the UI project will need to do it's build thing and then copy the result somewhere into the API file structure so it gets
included in the WAR and somehow served by the tomcat (unlike the current version which is served by the Apache HTTP server.

... seem to have lost some history here... 

07-Sep-2024 Decided to make JAccount live, ie. use the live DB on minnie. Only way to set the DB path is currently via the 
application properties so I needed to do a rebuild and redeploy. You guessed it.. that is obviously that is way to complex to 
actually work and the forking spring framework shirt now fails to start with some ludricous error about some XML class not being
found which is required by some incomprehensible class that I certainly am not knowingly using for anything and for which
there is not a shred of documentation anywhere. Why the fork did I bother with this shirt - I could have had something working
in a few hours using 'normal' java, instead the 'framework' has wasted weeks of my incredibly rare spare time, and it still isn't
actually working!

The error is:

BeanInstantiationException: Failed to instantiate [org.springframework.boot.web.servlet.filter.OrderedFormContentFilter]: Factory method 'formContentFilter' threw exception with message: Provider org.apache.xalan.processor.TransformerFactoryImpl not found

In the absence of any real idea how to fix this error I restarted the tomcat server. JAccount started OK. So once again the
magic shirt is forking around with me. Since random shirt is usually due to some sort of version mismatch I guess spring is
using something which doesn't work with something else - way to go Java committee, you managed to invent something that makes
Windows DLL issues seem totally inconsequential and trivial!
Google is not much help, the only reference to 'formContentFilter' suggests an incompatibility in Jackson, but I have no direct
references in the pom.xml, I only use the spring dependencies. I suppose it could be something related to the other applications
running on the Tomcat, eg. pagebuilderrest uses a later version of Jackson. There is no 'xalan' reference in the spring dependencies, according to the Eclipse dependency list so why the fork is it trying to use anything to do with xalan??

The application is unusable currently since it appears the database connection is being automatically closed after a short period
of time, ca. 10mins. The super wonderful connection manager is too stupid to figure out the connection is no good 
and reconnect before providing it for use.

The DB connection closed message seems to be

UcanaccessSQLException: UCAExc:::5.0.1 connection exception: closed

I don't know why the connection closes, maybe it is something that needs to be configured in the Ucanaccess stack of
software, although why the fork they would write the code to randomly close the DB is anybodies guess.

I did discover that refreshing the page 10 times caused a valid connection to be made and the list of accounts reappeared in the dropdown list. This is not suitable for normal use though. Thus many hours wasted trying to figure out what could be 
causing the connection to close and why the connection manager does not detect it and re-open the connection before accessing the DB.

There are parameters to tweak but the docs say the defaults are recommended. That's obviously a load of rowlocks
since it is providing dead connections! Only choice is to play around with the values and see what happens.

In the log there is a messages about 'idleTimeout' being ignored, so I guess that isn't going to cure the connection loss,
which is a pity since it seemd the most logical value to affect whether a connection is closed.

08-Sep-2024 Discovered there is a newer version of UCanaccess with different maven ids so will give it a try. Maybe it does
not need the extra 'dialect' stuff which would be good. What would be excellent is that the DB connection doesn't close itself
so I don't need to waste days trying to figure out how to get Hikari to open a connection when it is closed (why the fork 
doesn't it just do it?!?!?).

After looking at docs for Ucanaccess, Jackcess, Hsql, Hikari I came to the conclusion that trying to figure out why
the DB connections are randomly closing is a futile waste of my time. Since making a number of queries eventually resulted
in success I concluded that best option is to simply try to repeatedly make a dummy query until it does not result in an
exception before making the real query - maybe not the most performant way but that is not really an issue here. Thus I added the
connection resurrector. The current version is more of a test to see if it helps, eventually it might be worth investigating if
there is a better to way to detect the closed connection, and a way to automatically add the call before all 'real' db actions.

Since deploying the version with new version of Ucanaccess, the tweaked Hikari settings and the connection resurrector there
have been no problems while using the application. The logging from the resurrector suggests that it has not encountered
any closed connections. So maybe the tweaked settings have worked around the problem or the new driver contained a fix.
So maybe the resurrector can be disabled - for now I'll keep it.

## Links
HSQLDB: https://hsqldb.org/
Jackcess: https://jackcess.sourceforge.io/cookbook.html
Ucanaccess: https://github.com/spannm/ucanaccess?tab=readme-ov-file
Hikari config: https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#gear-configuration-knobs-baby
QR Reader: http://github.com/mebjas/html5-qrcode
