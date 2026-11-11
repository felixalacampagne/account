The changes for CORS for use with docker will probably screw up development using a local angular server.
This is googles solution for this:

To allow your Angular frontend at http://localhost:4200 during local development without opening CORS in production, use either an Angular dev proxy (recommended, zero Java changes required) or a Spring @Profile("dev") CORS configuration.

Option 1: Angular Dev Server Proxy (Recommended)

Using Angular's built-in proxy makes the Angular dev server (localhost:4200) forward API calls to your local Spring Boot backend (localhost:8080) behind the scenes. Because the browser only talks to http://localhost:4200, it treats every call as same-origin and never triggers CORS.
• Create proxy.conf.json in your Angular root folder. Point /app (or your API path) to your local Spring Boot port.
json
{
  "/app": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true
  }
}
Use code with caution.
• Register the proxy in angular.json. Add "proxyConfig": "proxy.conf.json" under architect.serve.options (or pass --proxy-config proxy.conf.json when running ng serve).
json
"serve": {
  "builder": "@angular-devkit/build-angular:dev-server",
  "options": {
    "proxyConfig": "proxy.conf.json"
  }
}
Use code with caution.
• Use relative API URLs in Angular. Ensure your Angular service calls /app/restapi rather than hardcoding http://localhost:8080/app/restapi. This works identically in both local development and your Docker production setup.

Option 2: Spring Boot @Profile("dev") CORS Bean

If your Angular dev app calls http://localhost:8080 directly instead of using a proxy, enable CORS exclusively when the dev profile is active so production remains locked down.
• Create a dev-only CORS configuration class. Add @Profile("dev") so Spring ignores this class in production.
java
package com.example.yourapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@Profile("dev")
public class DevCorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
Use code with caution.
• Activate the dev profile locally. Start your embedded Spring Boot server with -Dspring.profiles.active=dev (or set SPRING_PROFILES_ACTIVE=dev in your IDE run configuration).
• Note if not using Spring Security. If your project uses plain Spring MVC without spring-boot-starter-security, implement WebMvcConfigurer instead of CorsConfigurationSource and override addCorsMappings with .allowedOrigins("http://localhost:4200").
Let me know whether your project includes spring-boot-starter-security in your pom.xml or build.gradle, and I can confirm whether CorsConfigurationSource or WebMvcConfigurer applies to your build.




Handy commands:

```
git diff --ignore-cr-at-eol --ignore-space-at-eol >patch.diff
git apply --whitespace=fix --ignore-whitespace --reject patch.diff
```
NB. The '.rej' files contain just the part that has failed. If the reason for the failure can be identified (it's usually space related) then modifying the '.rej' file and applying it should be easier than reverting all the changes
made when the '.diff' file was applied. Of course it's not that simple because, in a typical Unix-think way where the obvious is unthinkable, the '.rej' file is not valid for input to 'git apply' - I mean, that would be far too obvious and easy. The problem is that the lines in the .diff file appearing after the 'diff' line and before the '@@' line which indicated the relationship between the +/- and the a/b files are missing, eg. --- a/file, +++ b/file. Thus the
workaround is to insert the lines from the original .diff file into the .rej file and then, assuming the fixes are correct, the .rej file can be used as input to git apply. Why the fork didn't they do that when creating the .rej file
in the first place?

Removing trailing space, includes lines containing only spaces, might avoid some of these problems. VSCode can only remove
trailing space on save (there is no command to remove the trailing spaces). This is OK except when the VSCode editor is the
only one available for saving the ".diff" file -empty context lines must start with a space which gets removed when VSCode
saves the diff. It might be possible to prevent this whilst keeping the space removal for "normal" files. Something
like

```
"files.trimTrailingWhitespace": true,
    "[diff]": {"files.trimTrailingWhitespace": false},
```

in the settings.json file appears to allow the context line padding space to be retained for .diff files.
settings.json is located in the ".vscode" directory.


Account features implemented:
   - transaction display with paging, goto page, last page, device dependent pagesize (no custom page size yet),
   - transaction update
   - standing order application and editing/deleting,
   - transfer between accounts,
   - checked balances with manual trigger
   - show qr code for transfer
   - transfer account details update and delete
   - account creation and details update
   - permanent pay date sorted balance display
   - converted from MS Access database to MySQL database

Still to come:
  - transaction search
  - reconcile using CSV statement files (currently done via Excel for CBC and a couple of others) with some way to record exceptions encountered during the reconciliation as happens in the Excel sheet. The Excel script works well and provides useful options for handling the exceptions with the final option to abort the entire reconciliation. The script works with ODBC so can support different databases (with some modifications) providing there is a viable ODBC driver. Since the Excel sheets are working well it is unlikely that they will be replaced unless the ancient 32bit version of Excel that I have ceases to function (luckily I don't use a Mac so there should be no problem there).

0.6.0 converted to MySQL database since H2 does not have a viable ODBC driver. MySQL is more complex to administer than H2
but on the plus side it runs in a Docker on the NAS and the Workbench is somewhat better than the h2console for admin tasks.

0.5.1 converted to H2 database as this seemed a reasonable choice given the requirements for Account. The choice
was also based on the H2 documentation that claims support for ODBC which would allow Excel to continue to be used for reconciliation.
Turns out ODBC support is non-existant - the PostgreSQL driver is not compatible with the H2 database created by H2 - it
gives error 'PUBLIC' schema not found while apparently trying to create a sequence. The public schema is the default one and can be
used to address the tables and the sequence already exists so I have no idea what the fork is going on there. There isn't a fat lot of
info available for this and the documentation appears to be years out of date so I will just have to forget about using H2 with ODBC.
This means I must either implement some means of doing reconciliation with H2, manually perform the reconciliation or switch to a database
which supports ODBC. None of these options is appealing but I guess it will be option one that I try first although I have no clue
how reading a CSV file from the browser and in a tomcat/spring boot environment is going to work!!

0.4.25 standing order end of month processing - when a monthly standing order date is at the end of the month then it will stick to the end of the month for subsequent entries. This means that entries on the 28th or later will eventually move to the end of the month. Thought about making this configurable but decided not worth the effort since any date after the 28th is going to move around due to the way Java does 'add month' processing, ie. 31st will become 30th and eventually 28th. Getting the UI to display a checkbox as part of the datepicker field also proved to be impossible which is another reason I abandoned the configurable EOM processing. Maybe in the future it might be interesting to add a last day X of the month, eg. last Wednesday of the month, but so far I don't have a need for anything like that.

0.4.24 various changes including standing order delete, display balances on phone in landscape mode, abort QR scan, manual trigger for balance recalculation

0.4.23 converted lineendings to LF and hopefully configured repo for LF endings. Requires some
local commands I think:

git config --global core.autocrlf false

git config --global core.eol lf

git config core.eol lf

git add --update --renormalize

0.4.20 Account management. To get this done as quickly as I could it is more or less a direct copy of the
transfer account management with no attempt to make any of the code shared - partly because passing data between components
in Angular is so convoluted and partly because the objects to be displayed are unrelated. Still, things like the delete
confirmation could be shared but in the end not worth the time it would take - until I refactor to display the confirmation
dialog before the edit dialog is removed so cancel goes back to the edit instead of the list.

0.4.19 01-Feb-2025 Transfer account management. Was so fed up with constantly being directed to the documentation for
Angular Material 19 and reading about something which might work and then realising it was the wrong documentation and then
finding that once again the CIAs (continuous improvement grassholes) have made the new version incompatible with the
previous version that I migrated to Angular 19. Needless to say this was yet another painful Angular experience.
Never did figure out how to apply the standard Material colour variants to a button since they removed the 'color' attribute.

0.4.12 25-Jan-2025 QR code display. Creating the QR code image was no problem since I already
had all the code needed to for that. Displaying the bytes sent from the server was another matter - everything image related
revolves around a URL to a file. Google, and hours of wasted time, to the rescue once again.

0.4.11 11-Jan-2025 improved the transaction pageination to make it easier to use especially when reconciling. Reconciling
is a good case for search but just displaying a list of matches with no reference to the full transaction list as is done
in the VB version. Need to find a way to goto search hits in the actual transaction list while keeping the paging.
While reconciling the credit card statement concluded that the QR code would be handy to have after all so that will be
the next thing.

v0.4.10 04-Jan-2025 The first release of 2025 - Happy New Year! The Christmas break allowed some time to play around trying
to display a dynamic list of counterparties based on what is typed into the counterparty name field. Initially I though this
would be simply done by populating a dropdown as it is a common scenario which must be well supported by now. Well, I did manage to
cobble togehter something which worked with a dropdown by resorting to 'Angular Bootstrap' and some jiggery-pokery I discovered
using signals (which I don't fully understand) - in other words this now basic UI behaviour is not really supported at all. Then I
stumbled across the 'typeahead' seciton of the 'Angular Bootstrap' documentation and realised that it was probably what I needed to
use - the name is immensly misleading as it appears to have nothing to do with searching or dynamic list display. After a while I
managed to get it to work just about how I expect it to even though I don't understand all the things that are done,
especially when it comes to all the prefix symbols that are sprinkled around. The final revelation was that the 'string' variable
used for the counterparty name was actually being populated with a transfer account object! Knowing that the variable type
is completely meaningless made life a lot easier.
