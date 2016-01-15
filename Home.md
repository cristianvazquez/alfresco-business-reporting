

# Introduction #
Alfresco is a superb platform. However, reporting is not it's 'strongest feature'. Actually, there is close to none. One can build some [Freemarker](http://wiki.alfresco.com/wiki/FreeMarker_Template_Cookbook) reports or other custom coded dashlets, but it always involves too much too complex work, and reporting demand is often 'now'.

Alfresco Business Reporting is created to solve just that. Envision your possibilities if you could create your own reports in 'plain' SQL reporting. All your Alfresco Business Objects in plain tables dynamically populated with all their properties! Reports executed against them every hour/day/week/month/other... For example Reports specific for Sites or Persons, stored in that particular Site or `UserHome`. Adhering Alfresco's powerful permissions. A versioned stack of reports over time...

Mixing login data with Categories and metadata about your sites and documents, being it (older) versions, deleted items, most active users, Sites with people being `SiteManager` but having an external email address... Most active users, Sites inactive for over a year. Whatever you think is important. Do some exploration using Qlikview or other ad-hoc tool.

# Download #
Download the latest version (version 1.0.1.x), run the reports that come out of the box. Create your own reports, store them in a container in the Data Dictionary, and they are executed. Works in Alfresco Enterprise 4.0, 4.1 and 4.2 (and seen running nicely in Community up to version 4.2.f)(For those still at Alfresco version 3.4.x, see the latest release of the 0.8.x branche...)

# Installation #
Installation is easy;
  1. create a reporting database (just like when installing Alfresco)
  1. install the alfresco-business-reporting-explorer-1.0.amp **(use the -force parameter in apply\_amps!)**
  1. configure Tomcat/JBoss to use JNDI against your reporting database (now the same reports work in all your development, acceptance and production environments).
  1. ~~if you are running 3.4, install the `MyBatis.zip` too in tomcat/alfresco/WEB-INF/lib (next version, when we are cross database). Never tested this, but I guess it should work)~~ If you run version 3.4.x, please stick to version 0.8.x

Done.

After starting your server again, navigate to "Data Dictionary/Reporting". You will find 3 spaces called "Daily" appending the database vendor name. Remove the two you don't need. they will cause exceptions, because they are defiend in a different SQL Dialect. Really.

## Create your Reporting database ##
Creating your database is as easy as it was creating your Alfresco database. This sample creates a database named 'alfrescoreporting' and uses a user with username=alfresco and password=alfresco. (It probably will do on your dev. environment, but won't make it to production)
MySQL:
```
create database alfrescoreporting default character set utf8 collate utf8_bin;
grant all on alfrescoreporting.* to 'alfresco'@'localhost' identified by 'alfresco' with grant option;
grant all on alfrescoreporting.* to 'alfresco'@'localhost.localdomain' identified by 'alfresco' with grant option;
```

Postgresql:
In Linux, run something like:
```
sudo -u postgres createuser -D -A -P myuser
sudo -u postgres createdb -O myuser mydb
```
**substitute myuser by alfresco** substitute mydb by alfrescoreporting


Oracle:
```
$ sqlplus sys as sysdba
SQL> create user username identified by password;
User created.

SQL> grant connect,resource to username;
Grant succeeded. 
```

Remind, if you expect to use reporting heavily using all kind of explorative tooling, consider putting the reporting database on a different server than your Alfresco is using. (You can of course always change this later on.) (Remind: Works on MySQL only for now...
If you run Alfresco from an Installer version (thus Postgresql database) you need download MySQL  [Connector/J](http://dev.mysql.com/downloads/connector/j/) and place mysql-connector-java-%version%-bin.jar to </opt/alfresco>/tomcat/lib folder)

## Install the amp ##
See [Alfresco's documentation](http://www.google.com/url?q=http%3A%2F%2Fdocs.alfresco.com%2F4.0%2Findex.jsp%3Ftopic%3D%252Fcom.alfresco.enterprise.doc%252Ftasks%252Famp-install.html&sa=D&sntz=1&usg=AFQjCNE5UjY5SVQHEbjLOVrgUxwcDpYZgQ). **Remind: use the -force parameter when executing apply\_amps!** (reason: faces-config-custom.xml needs to be overriden, and that will succeed only if -foce is used...) It is as easy as that.

## Configure Tomcat for JNDI ##
This is the part to pay attention. Depending on your versions, you need to edit 1 or 2 files:
tomcat/conf/Catalina/localhost/alfresco.xml (any version) and tomcat/webapps/alfresco/META-INF/context.xml (appears to be Java 1.6 specfic; you might want to actually update this file inside the war, so you benefit from the change every time the war is exploded). Use the same credentials and database name as used when creating the database in the first step.

Add the following code as child element of `<Context>`. I expect it is common sense to rename user names, passwords, database names, hostnames etc.)

MySQL:
```
<Resource defaultTransactionIsolation="-1"
           	defaultAutoCommit="true" 
		validationQuery="select 1"
		testOnBorrow="true"
		maxActive="5" maxIdle="3" initialSize="1"
           	username="alfresco" password="alfresco"
           	url="jdbc:mysql://localhost:3306/alfrescoreporting"
           	driverClassName="org.gjt.mm.mysql.Driver"
           	type="javax.sql.DataSource" auth="Container"
           	auth="Container" 
           	name="jdbc/alfrescoReporting"/>
```

PostgreSQL:
```
<Resource defaultTransactionIsolation="-1"  
		defaultAutoCommit="true" 
		validationQuery="select 1"
		testOnBorrow="true"
		maxActive="5" maxIdle="3" initialSize="1"
		username="alfresco" password="alfresco" 
		url="jdbc:postgresql://localhost:5432/alfrescoreporting"
		driverClassName="org.postgresql.Driver" 
		type="javax.sql.DataSource" 
		auth="Container" 
		name="jdbc/alfrescoreporting"/>
```


Oracle:
```
<Resource defaultTransactionIsolation="-1"

		defaultAutoCommit="true" 
		validationQuery="select 1 from dual"
		testOnBorrow="true"
		removeAbandoned="true"
                removeAbandonedTimeout="60000"
                testWhileIdle="true"
                timeBetweenEvictionRunsMillis="60000"
                minEvictableIdleTimeMillis="120000"
		maxActive="5" maxIdle="3" initialSize="1"
		username="alfresco" password="alfresco" 
		url="jdbc:oracle:thin:@localhost:1521/XE" 
		driverClassName="oracle.jdbc.OracleDriver"
		type="javax.sql.DataSource"
		auth="Container"
		name="jdbc/alfrescoReporting"
	/>
```

For MySQL and Oracle the Alfresco provided Tomcat has no drivers. Please download them manually.

# Configure web.xml for report execution #
Executing the Pentaho report defintion also use JNDI. Actually, it is the reason to use JNDI in the first place. (It makes reports portable over Alfresco instances and your Pentaho reporting desktop environment to create report defintions.) In order for these to function well, edit your %tomcat%/webapps/alfresco/WEB-INF/web.xml:
```
<resource-ref>
            <description>Alfresco Reporting JNDI definitition </description>
            <res-ref-name>jdbc/alfrescoReporting</res-ref-name>
            <res-type>javax.sql.DataSource</res-type>
            <res-auth>Container</res-auth>
</resource-ref>
```
Remind to update this file in your master alfresco.war, so it does not get lost when re-applying amps. And remind to tweak the alfresco.war of the next release of Alfresco...

# Fix import issue #
Alfresco does not like to import placeholders like ${site} and ${yyyy}. They get stripped. So, go th Data Dictionary/Reporting/Daily and add the properties of the usersPerSite.prpt. Change /documentLibrary/Reports/ into ${site}/documentLibrary/Reports/ and potentially add ${yyyy}-${MM} to the end of that value. The ${site} is mandatory though. See:

<a href='http://www.youtube.com/watch?feature=player_embedded&v=_foQZz_iGbs' target='_blank'><img src='http://img.youtube.com/vi/_foQZz_iGbs/0.jpg' width='500' height=344 /></a>


# Next steps #
The next step is to burn some rubber; fill the reporting database and generate some reports. For now the good old Explorer is my UI of choice. Some development time needs to be spend to make Share look good too (in all its versions), and to provide some Actions. All your actions and features are all available in the Explorer... (Metadata works in Share too, but hasn't been polished.)

**REMIND**: The Cron jobs taking care of harvesting and report execution wait for 5 minutes after startup before they become active not to hurt the startup of your repo)!

If you navigate to /Data Dictionary. You see the space called Reporting. Here you have 2 additional buttons:

<img src='http://tpeelen.files.wordpress.com/2013/03/img_fill-reporting-database.gif'> Fill reporting database.<br>
This manually triggers filling the reporting database with the most recent data. Remind, if you have an Alfresco system running for years, you potentially have quite some data. Harvesting can take quite some time for an initial run... Per reporting table, the tool marks the timestamp when last successful run had started, so next run, only the newly changed content will be harvested.<br>
<img src='http://tpeelen.files.wordpress.com/2013/04/explorerharvestingactions.png'>

<i>Additional action against a Harvest Definition. Execute this harvest definition</i>

<img src='http://tpeelen.files.wordpress.com/2013/03/img_execute-reports.gif'> Execute report definitions.<br>
This manually triggers execution of all reports below. Remind that there is a master switch, and a switch per container to actually execute report definitions within containers. At the level of Reporting 'all' reports will be executed, but one can execute the daily reports only, or even a single report definition. Remind, login-related reports will fail if there is no login audit trail available yet!<br>
<br>
<img src='http://tpeelen.files.wordpress.com/2013/04/explorerreportingrootactions.png'>

<i>Additional actions against the Space 'Reporting' in the Data Dictionary. Actions to execute all reports, and to harvest all harvest definitions</i>

<img src='http://tpeelen.files.wordpress.com/2013/04/explorerreportingcontaineractions.png'>

<i>Additional action against 'Reporting Containers'; execute all reports inside the Reporting Container</i>

<img src='http://tpeelen.files.wordpress.com/2013/04/explorerreporttemplateactions.png'>

<i>Additional action against single report definition; Execute this Report Template</i>

After executing (some or all) reports, the resulting reports can be found in Data Dictionary/Reporting/Reports. Site specifics can be found in the Reports folder of each Site's document library.