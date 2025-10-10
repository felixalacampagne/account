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

mysql -u root -h berv8362 -p

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
   mysql> ALTER USER 'root'@'%' IDENTIFIED BY 'Abcd1234';  
   
   Workbench should then be able to access the server from a remote machine
   
   Might also be required to change the 'other' root entry. Probably better to keep the passwords the same.
   mysql> ALTER USER 'root'@'localhost' IDENTIFIED BY 'Abcd1234';

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
SHOW GLOBAaccountL VARIABLES LIKE 'local_infile';

After much Googling and cursing I finally got the client side to allow the local infiles; allowLoadLocalInfile=true
https://dev.mysql.com/doc/connector-j/en/connector-j-connp-props-security.html

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

where ACCOUNTID the field list is replaced by the variable '@vaccid'.

The TRUE/FALSE for boolean in the CSV is always interpreted as true. This can be worked around with special processing, eg. for (transaction.checked)

set CHECKED = (@vchk = 'TRUE')

where CHECKED in the field list is replace by the variable '@vchk'.


