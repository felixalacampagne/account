## Create dump file before any sort of system or image update

It seems that the MySQL container/database is extremely fragile and ridiculously sensitive to any
sort of change in the environment - typical Oracle ballshirt. Therefore it is required that a
database dump is created before any update of the NAS software or the docker image, preferably
any update whatsoever of the system.

Database dumps can be created via the Workbench Export command. It is reasonably self-explanatory:
- only dump the 'user' databases, not the sys ones
- choose single file, single transaction
- create schema
- save it somewhere on the NAS, not in the default Windows user directory

Database dumps can also be created via the container console 'mysqldump' command (see https://dev.mysql.com/doc/refman/8.4/en/mysqldump.html).
The command is something like:
```
mysqldump --databases accountmysql --result-file=/var/lib/mysql/accountmysql.sql -uroot -p"$MYSQL_ROOT_PASSWORD"
```
Warning: output file path needs to be visible outside of the container so must use a mapped volume of which 
there is only one by default, ie. the data directory. If this is the option used normally then a dedicated mapped
volume should be added to the container.

Haven't figured out yet how to replicate the criteria used for the Workbench export so probably better to use
the Workbench to ensure compatibility with the Workbench import.

## Recovery from corruption of MySQL docker container.
There is no way to diagnose why the MySQL container fails to start, eg. due to 'File exists' error.
The only solution is to create a new container and initialize a new data followed by an import of
the last backup file. Hopefully I wont need to do this often so having a set of instructions to follow
will make it easier.

Note that I have not figured out how to use Portainer to create a new container from a downloaded image so
use the NAS docker interface if this is required. Normally it is not necessary.

These instructions assume that Portainer is being used.
- Create a to a new, empty data directory in the docker/mysql shared directory. Since the Jan 2026 NAS update it
  appears that the data directory must be in the 'docker' shared directory otherwise the 'File exists' error occurs.
- Edit/duplicate the corrupt container definition
- Optionally change the name of the container to indicate it is different to the corrupted version.
- Change the volume mapping of /var/lib/mysql/ to new data directory.
- Set the value of environment variable MYSQL_ROOT_PASSWORD if desired. This avoids needing to use the docker shell
  to run mysql and set a new root password but is a security vulnerability if the password is not changed or the
  env.var. not removed after the database is initialized.
- Start the container. This should initialize the database structures for the first time. Check the log for issues.
- If MYSQL_ROOT_PASSWORD was set then stop the container, remove/change the value, restart the container. Check
  the log.
- If MYSQL_ROOT_PASSWORD was NOT set then a root password must be configured by the mysql command line:
  - Connect a console shell to the MySQL docker container.
  - From the docker shell:
```
$ mysql -u root -p
Enter password: [Enter]

mysql> ALTER USER 'root'@'%' IDENTIFIED BY '[password]';
mysql> select host, user from mysql.user;
+-----------+------------------+
| host      | user             |
+-----------+------------------+
| %         | root             |
| localhost | mysql.infoschema |
| localhost | mysql.session    |
| localhost | mysql.sys        |
+-----------+------------------+
```
- Connect to the new DB with MySQL workbench. If <password> is the same as before any old settings should work OK.
- Locate the account backup directory (\\\\[tomcathost]\\Development\\accountDB\\mysqlbackup).
  Unzip the most recent zip file. The content should be a single .sql file.
- Restore the account database from the backup file or from a dump file
  - Server > Data Import > Import from Disk > Import from self-contained file
    - Provide the path to the single SQL file from the zip
    - Create new default target schema: accountmysql
    - Switch to Import Progress tab and click Start/Import
- Create the account user, it is not part of the backup:
  - Administration (left side, next to Schemas)
  - Users and Privileges > Add Account (button)
    - User: account
    - Hosts: %
    - Password: (from \\\\[tomcathost]\\[tomcathome]\\conf\\accountmysql-local.properties)
    - Account Limits (tab)
        - Max.Queries: 0
        - Max.Updates: 0
        - Max.Connections: 0
        - Concurrent Connections: 0
        - Apply (button)
    - Schema Privileges (tab)
        - Add Entry...: %
        - Apply (button)

DB should now be ready to restart the tomcat - cross fingers, pray if you think it will help...


# History

## 07 Jan 2026 Attempt to resurrect the corrupt MySQL docker

Kinda worried that next time whatever killed the MySQL docker happens the backup might
be missing something crucial so would like to find out how to fix the original MySQL docker.
Since I don't know what the problem is it's not obvious where to start. Looking at
the start script it appears that it checks whether /var/lib/mysql is a directory and
performs the actions which likely result in the log messages if the directory does not exist.
Obviously it DOES exist so maybe it cannot see it due to a permission problem - but what problem?

### Try creating a mysql user
Perhaps a mysql NAS user is required so it can be granted permissions to the directory
when something changes in the way the NAS handles permissions. Not sure how this
user can be communicated to the MySQL running in the docker - maybe that is what UID, GID, MYSQL_USER
values are all about????

- Create NAS mysql user
- Determine UID, GID via NAS console (SSH)
  $ id mysql
  uid=1008(mysql) gid=100(users) groups=100(users),1000(smallcathome)
- Create MySQL docker image (using portainer) pointing to the corrupted DB volumn.
  NB. The doc (https://hub.docker.com/_/mysql/#environment-variables) says specifically that MYSQL_ROOT_PASSWORD should be omitted when starting
  with an existing DB
  Name: mysql8-4-CORRUPT
  Port: 3307 (default is 3306)
  Volumes: corrupt DB directory:/var/lib/mysql
  Env:
    UID: 1008
    USER_ID: 1008
    GID: 100
    GROUP_ID: 100
    MYSQL_USER: mysql (from docker-entrypoint.sh)
    MYSQL_PASSWORD:

Container still does not start.

Doc refers to 'Running as arbitrary user' which requires a command line option (so have
no clue what user MYSQL_USER is referring to).

Try to add the command line option as an 'Options' in the portainer 'Commands & Logging' section.
  --user 1008:100

Fork me - that is an option only for logging!
Only way to add the '--user' option is to add it after 'mysqld' in the command field. This is ineffective because the log then shows
```
2026-01-07T14:46:13.220932Z 0 [Warning] [MY-010143] [Server] Ignoring user change to '1008:100' because the user was set to 'mysql' earlier on the command line
```
So forced to conclude there is no way to use the '--user' option from portainer or the NAS UI.

### Try creating new container from scratch

This didn't work either. It wouldn't even work when trying to create a new database - the 'File exists' message still appeared in the log even though it really DID NOT exist. Tried pulling the
image again (and discovered that the image used for the currently working container is
marked as corrupted!!!! Yikes). Still same non-sensical error message.

Fork me! - I tried restarting the working second version of the database and it failed with
the same 'File exists' message.
Moved the data directory to the 'docker' share and it started again but it's giving errors
to like

2026-01-07T17:11:33.719853Z 10 [ERROR] [MY-011972] [InnoDB] Your database may be corrupt or you may have copied the InnoDB tablespace but not the InnoDB redo log files. Please refer to http://dev.mysql.com/doc/refman/8.4/en/forcing-innodb-recovery.html for information about forcing recovery.
2026-01-07T17:11:33.721393Z 10 [ERROR] [MY-011971] [InnoDB] Tablespace 'innodb_undo_001' Page [page id: space=4294967279, page number=260] log sequence number 33805135 is in the future! Current system log sequence number 29753816.
2026-01-07T17:11:33.721465Z 10 [ERROR] [MY-011972] [InnoDB] Your database may be corrupt or you may have copied the InnoDB tablespace but not the InnoDB redo log files. Please refer to http://dev.mysql.com/doc/refman/8.4/en/forcing-innodb-recovery.html for information about forcing recovery.

which is extrememly disconcerting!

### Try new container in different location

In desparation I decided to try creating the datadir in the 'docker' share. This is on the same 'volume' (ie. disk) as the previous directory so couldn't imagine that it would work but guess what?
Yup, the forking thing worked. It created an empty database. On a whim I decided to move the old datadir from it's previous location to the 'docker' share. The new container started successfully
while directed to the old datadir located in the 'docker' share. MySQL Workbench was able to
access the old database.

Moral of this sad story seems to be that if the container starts giving nonsense in the log and
fails to start then the best solution is to scrap the container, pull a new image, create a new
container and point it to somewhere completely different and allow it to initilialize an empty
database. Then move the old datadir directory to the same location as the new empty database,
update the image volumne mapping and restart. With a bit of luck it will work. If it doesn't then
try the restore from backup, assuming the image can be started with an empty database.

MYSQL_ALLOW_EMPTY_PASSWORD

## 06 Jan 2026 NAS upgrade killed MySQL docker

Account was running fine using the dockered MySQL db.
The MySQL docker container had been restarted multiple times without a problem.
Then the NAS said it needed to update and I unwisely allowed it to do so since
it wasn't the first time and previous updates went OK.

Not this time. The MySQL container keeps re-starting/crashing.

The log contains the following:

```
2026-01-06 16:48:12+00:00 [Note] [Entrypoint]: Switching to dedicated user 'mysql'
2026-01-06 16:48:12+00:00 [Note] [Entrypoint]: Entrypoint script for MySQL Server 8.4.6-1.el9 started.
2026-01-06 16:48:12+00:00 [Note] [Entrypoint]: Initializing database files
mysqld: Can't create directory '/var/lib/mysql/' (OS errno 17 - File exists)
2026-01-06T16:48:12.310901Z 0 [System] [MY-015017] [Server] MySQL Server Initialization - start.
2026-01-06T16:48:12.312601Z 0 [System] [MY-013169] [Server] /usr/sbin/mysqld (mysqld 8.4.7) initializing of server in progress as process 80
2026-01-06T16:48:12.314275Z 0 [ERROR] [MY-013236] [Server] The designated data directory /var/lib/mysql/ is unusable. You can remove all files that the server added to it.
2026-01-06T16:48:12.314299Z 0 [ERROR] [MY-010119] [Server] Aborting
2026-01-06T16:48:12.314723Z 0 [System] [MY-015018] [Server] MySQL Server Initialization - end.
```

I have no clue what to do about this. Of course the forking /var/lib/mysql/ directory exists - it's where
the database is!

Google suggests it's a permission thing. Tried SSHing to the NAS and making the mapped directory and
contents RW for everyone. No change.

Can't create a shell in the container to checkout the permissions as it stops immediately. Anyway, there is
no reason for the stuff in the container to be any different than it was before.

Tried updating the MySQL docker image. No change.

Tried changing the mapping of /var/lib/mysql/ to a new directory. The container starts up and creates
a bunch of files but obviously without the account db. The container can then be restarted without a problem.

So it looks like I'm going to have to start from scratch again... and hope the backup has been working correctly or that I can somehow import the original db files...


Obviously would be nice to know what went wrong with the original DB but I doubt I will ever know.
Some more googling suggests it is a permission issue related to the mapped drive being shared which
makes no sense since the original directory was working across many container restarts and the newly
created directory also works across container restarts. From an SSH shell into the NAS the external
permissions for both old and new directories appear to be the same. Of course with Unix nothing is ever
what it seems and there could be some hidden permission that it is impossible to see which is affecting the
behaviour. Google also suggests that using MariaDB might solve this kind of issue but if it really is a
permission problem then it is hard to see how it could avoid the same problem. Maybe need to move the
data directory to an non-shared directory, which is going to be hard as everything is shared! There is also
the question of what the MYSQL_USER/PASSWORD values are referring to - do I need to create a NAS user and
provide that, and maybe put the data dir in the users home, which I don't want since the homes are on the
wrong volumne. Aaaaghhhh! Maybe it would be better to change DB again to one less likely to suffer from
this nonesense...


### 17 Oct 2025 Initial MySQL setup

Ended up having to use a mysql docker image as that was the only recent version available.
Since MySQL requires a server, unlike Access or H2, then having it in docker on the NAS to start with makes sense.

Used portainer to run containers of image for mysql 8.4.

Required to set an environment variable for the container: MYSQL_ALLOW_EMPTY_PASSWORD = 1
Did this via portainer.

Connected to the docker server from within the container using an empty password at the prompt:

docker exec -it trusting_haibt mysql -u root -p

(trusting_haibt seems to be an automatically assigned name for the container)

Then used

mysql> ALTER USER 'root'@'localhost' IDENTIFIED BY 'Abcd1234';

and it appeared to work ok.

Tried to connect from windows using

mysql -u root -h <nashost> -p

and it cannot connect.


From portainer it is possible to open a console in the container. Here the 'mysql -u root -p' worked OK.

Maybe need to figure out how to expose the port?

Or maybe setting MYSQL_ROOT_HOST is required to connect as root from outside the container??
Nope (well maybe required but not enought on its own)

Map port 3306 and restart container

Now the Windows mysql gives: ERROR 2059 (HY000): Authentication plugin 'caching_sha2_password' cannot be loaded: The specified module could not be found.
This suggests that it is making a connection but something is not compatible.

Googles suggests doing:

mysql> ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'Abcd1234';

but this gives: ERROR 1524 (HY000): Plugin 'mysql_native_password' is not loaded
which means we are all stuck between a rock and a hard place.
Probably because the Windows mysql is too old to be useful.

Tried installing the 'mysql workbench' - It can't connect.

Try doing
mysql> ALTER USER 'root'@'%' IDENTIFIED BY 'Abcd1234';

YAYAYAYAYAYAYAY! This worked.
Fork me, what a hassle! You can tell that Oracle has become involved in this, no way a normal
human would ever make it so forking hard!

In theory the initial root password should be a one time password created when the data directory
is initialized which happens the first time the image is run. It seems that the image
does not come pre-configured for this to happen. It requires:

- MYSQL_RANDOM_ROOT_PASSWORD=1
- MYSQL_ONETIME_PASSWORD=1

Then the log of the container must be consulted for find the password and on first access it should
need changing. I used MYSQL_ALLOW_EMPTY_PASSWORD instead.

So
 - map port 3306
 - env.vars.
      MYSQL_ALLOW_EMPTY_PASSWORD=1
      MYSQL_ROOT_HOST=%
 - volumes: bind internal /var/lib/mysql to external, eg. /development/projects/mysql/datadir
 - start container
 - connect from portainer console
   mysql -u root
 - confirm there is a remote access entry for 'root'

```
   mysql> select host, user from mysql.user;
   +-----------+------------------+
   | host      | user             |
   +-----------+------------------+
   | %         | root             |
   | localhost | mysql.infoschema |
   | localhost | mysql.session    |
   | localhost | mysql.sys        |
   | localhost | root             |
   +-----------+------------------+
```

 - set root password like
   mysql> ALTER USER 'root'@'%' IDENTIFIED BY '<password>';

   Workbench should then be able to access the server from a remote machine

   Might also be required to change the 'other' root entry. Probably better to keep the passwords the same.
   mysql> ALTER USER 'root'@'localhost' IDENTIFIED BY '<password>';

Using the workbench create a user for the account DB
   I chose 'account'
   'Limits to Hosts Matching': %
   Account Limits must be provided - must use 0 for unlimited
   Administrative Roles can be left alone
   Schema Privileges must be set: %

Ran DatabaseConfigurationTest.databaseIsCreated and after an hour or so managed to
get the DB created and tables added. Needed to change column names for Prefs.
As always MUST ignore 'MySQLDialect does not need to be specified explicitly'.

Population of the tables might be possible in similar way to that used for H2
but the data.sql command is very different. It will also require a CSV export from the H2 database.

LOAD DATA LOCAL INFILE
'csv/account_h2cols.csv'
INTO TABLE account
FIELDS TERMINATED BY ';'
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
(id,code,description,address,contact,currency,currencyformat,statementref,ranking,swiftbic);


First attempt gives: Loading local data is disabled;

Can be fixed with

mysql> SET GLOBAL local_infile=1;
SHOW GLOBAL VARIABLES LIKE 'local_infile';

After much Googling and cursing I finally got the client side to allow the local infiles; allowLoadLocalInfile=true
https://dev.mysql.com/doc/connector-j/en/connector-j-connp-props-security.html
(Workbench requires 'OPT_LOCAL_INFILE=1' in the connection advanced tab - Why the fork can't it use the same
value as the JDBC connection!)

The LOAD command seemed to work well but the sequence update failed. MySql doesn't do sequences, it seems
to work like the Access DB does where one column is designated to AUTO_INCREMENT. Curiously Spring JPA (whatever)
has created tables for the sequences and the 'next_val' column can be set to the next value to be used.
Need to experiment with adding records without id to see if it really gets automatically applied.

In the end I decided to revert to the 'IDENTITY' ID generation type. This translates to the mysql AUTO_INCREMENT.
It appears the AUTO_INCREMENT is aware of the explicit IDs in the CSV files and automatically adjusts itself when the LOAD DATA is performed.

NB Workbench needs a configuration to allow updates and deletes without where clauses!
Edit > Preferences > SQL Editor > Safe updates

User 'account' has exceeded the 'max_questions' resource (current value: 99999)

Use workbench to set the limits for user 'account' to zero.
Then
FLUSH PRIVILEGES;
in a query window.

## Export from H2
CALL CSVWRITE('/development/tmp/account_h2.csv',
'SELECT * FROM account',
STRINGDECODE('charset=UTF-8 escape=\\\\ fieldSeparator=; lineSeparator=\n fieldDelimiter='));

CALL CSVWRITE('/development/tmp/transaction_h2.csv',
'SELECT * FROM transaction',
STRINGDECODE('charset=UTF-8 escape=\\\\ fieldSeparator=; lineSeparator=\n fieldDelimiter='));

CALL CSVWRITE('/development/tmp/standingorder_h2.csv',
'SELECT * FROM standingorder',
STRINGDECODE('charset=UTF-8 escape=\\\\ fieldSeparator=; lineSeparator=\n fieldDelimiter='));

CALL CSVWRITE('/development/tmp/phoneaccount_h2.csv',
'SELECT * FROM phoneaccount',
STRINGDECODE('charset=UTF-8 escape=\\\\ fieldSeparator=; lineSeparator=\n fieldDelimiter='));

CALL CSVWRITE('/development/tmp/phonetransaction_h2.csv',
'SELECT * FROM phonetransaction',
STRINGDECODE('charset=UTF-8 escape=\\\\ fieldSeparator=; lineSeparator=\n fieldDelimiter='));

The H2 CSV columns will need to be mapped to the right order in the LOAD DATA lines but they should be
match the order already in data.sql

## Import of H2 CSVs into MySQL

### NULLs
The H2 CSVs appear to have either (null) or ';;' (the latter is for the live DB) for NULL columns
which gets imported as the text "(null)" or the empty string "".
If column containing the NULL is numeric then the row is not imported, presumably as the text is not a valid
numeric value. There is no error reported for this. Thus numeric columns which can contain nulls, eg. phoneaccount.accountid, require special processing, eg.

set ACCOUNTID = NULLIF(@vaccid,'')

where ACCOUNTID in the field list is replaced by the variable '@vaccid'.

The TRUE/FALSE for boolean in the CSV is always interpreted as true. This can be worked around with special processing, eg. for (transaction.checked)

set CHECKED = (@vchk = 'TRUE')

where CHECKED in the field list is replace by the variable '@vchk'.

Empty numeric fields, ie. ';;', are interpreted as 0.00 instead of NULL.
For the transaction credit and debit columns this results in incorrect display although the balance appears to be
OK. Special processing will be required for this, I guess similar to that required for ACCOUNTID.

For backup to work need to update live DB with

update phonetransaction set paydate=null where paydate = 0;
update phonetransaction set sentdate=null where sentdate = 0;
update phonetransaction set transactiondate=null where transactiondate = 0;


## Accessing MySQL from Excel

Confirmed that it is possible to read from the mysql database in Excel. Requires use of
ADO instead of the DAO currently used by the reconciliation routines so this is going to
require quite a bit of work given that it is probably a decade since I last touched the VBA
database code! Will probably still be quicker than re-writing the functionality as Java code (I hope!!)
Needed an old 32bit ODBC driver described as 8.0 for compatibility with Excel. It didn't seem to
cause a problem but I guess it's a sign that I will need the Java version eventually - unless
there is an OpenOffice equivalent of Excel which can be programmed in a similar way to Excel
