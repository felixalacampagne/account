### 06 Jan 2026 NAS upgrade killed MySQL docker

Account was running fine using the dockered MySQL db.
The MySQL docker container had been restarted multiple times without a problem.
Then the NAS said it needed to update and I unwisely allowed it to do so,
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

### Recovery from corruption of MySQL docker container.

Change the volume mapping of /var/lib/mysql/ to a non-existing directory and start the mysql the container.
This will initialize the data structures for the first time.

Connect a shell to the  docker container

From the docker shell:

```
$ mysql -u root -p
Enter password: [Enter]

mysql> ALTER USER 'root'@'localhost' IDENTIFIED BY '<password>';
mysql> ALTER USER 'root'@'%' IDENTIFIED BY '<password>';
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

Connect with MySQL workbench

Server > Data Import > Import from Disk > Import from self-contained file

   Locate the account backup directory (\\<tomcathost>\Development\accountDB\mysqlbackup)
   Unzip the most recent backup zip
   Provide the path to the single SQL file from the zip

   Create new default target schema: accountmysql
   Switch to Import Progress tab and click Start/Import

Create the account user, it is not part of the backup:

Administration (left side, next to Schemas)

   Users and Privileges > Add Account (button)
   User: account
   Hosts: %
   Password: Abcd1234 (from \\<tomcathost>\<tomcathome>\conf\accountmysql-local.properties)
   Account Limits

      Max.Queries: 0
      Max.Updates: 0
      Max.Connections: 0
      Concurrent Connections: 0

   Schema Privileges

      Add Entry...: % Apply

DB should now be ready to restart the tomcat - cross fingers, pray if you think it will help...

Obviously would be nice to know what went wrong with the original DB but I doubt I will ever know until
it happens again and I have the time/inclination to try to figure it out...

