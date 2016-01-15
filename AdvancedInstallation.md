

# Configure JNDI in Tomcat #
Have the JNDI connection configured for reporting:

1. if you're on Java 1.6 (it appears) edit Alfresco's web.xml ('%tomcat%/webapps/alfresco/WEB-INF/web.xml`) (not the one of Share! Reporting is a back-end feature)
```
<resource-ref>
	    <description>Alfresco Reporting JNDI definitition </description>
	    <res-ref-name>jdbc/alfrescoReporting</res-ref-name>
	    <res-type>javax.sql.DataSource</res-type>
	    <res-auth>Container</res-auth>
</resource-ref>
```
Changing web.xml is not needed for Java 1.7 as it appears...

2. Edit tomcat/conf/Catalina/localhost/alfresco.xml and tell it there is a new resource, your reporting database.
```
<?xml version="1.0" encoding="UTF-8"?>
<Context>
	<Resource name="jdbc/alfrescoReporting" auth="Container"
			defaultTransactionIsolation="-1" defaultAutoCommit="false" 
			maxActive="15" maxIdle="3" initialSize="1" 
			username="alfresco" password="alfresco" 
			url="jdbc:mysql://localhost:3306/alfrescoreporting" 
			driverClassName="org.gjt.mm.mysql.Driver" 
			type="javax.sql.DataSource"
			/>
</Context>
```

Changing the JNDI name means that all your reports have to be configured having the new name (!). The JNDI name has to be changed in the Tomcat settings in the 2 files mentioned above. In the alfresco-global.properties is an entry to specify the (new) name the connection is given. The default is 'alfrescoReporting'. (It is far more easy (and makes more sense) to change the database name in the context.xml!!)


# Move the folders #
The amp installs one folderstructure within the Data Dictionary. This way, the association between the ReportDefinition and the report output folder (called 'Reports') is preserved. You are actually free to move this folrer 'Reports' to wherever you like. I usually move it to Company Home, because I don't want 'content' in the Data Dictionary.

# Different JNDI entry #
If you use a different name for your jndi connection than alfrescoReporting, add to your alfresco-global.props
```
# jndi name of the connection. This name is also used in all reports in Pentaho!
reporting.db.jndiName=alfrescoReporting
```

# Enable/disable login auditing #
The reporting tool enables Alfresco's auditing framework by default. It adds  an 'application' to record login attempts. From the failed attempts it records the username used.  The switch to enable/disable the auditing framework is:
```
# Enable harvesting of login data
audit.enabled=true
```

# Change JNDI connection in your ReportDefinition/Report Designer #
In Tomcat, your app-server knows how to resole the JNDI uri against the database. Using Report Designer on your desktop has no app-server. Therefore simple-jndi is in place. Your OS needs to tell Report Designer about JNDI.

Navigate to your user home and then .pentaho/simple-jndi. On my laptop (Windows) it is:
C:\Users\tpeelen\.pentaho\simple-jndi
Open default.properties and add to this list of JNDI entries:
```
alfrescoReporting/type=javax.sql.DataSource
alfrescoReporting/driver=org.gjt.mm.mysql.Driver
alfrescoReporting/user=alfresco
alfrescoReporting/password=d=alfresco
alfrescoReporting/url=jdbc:mysql://localhost:3306/alfrescoreporting
```
You can change the name of the database independent of the name of the JNDI connection!!

Next configure Reporting Designer with the name of the JNDI connection:
Open a report definition, and select Add Resources | JDBC
In the pop-up you can defne the JNDI name and test your connection:

Now your Report Designer should be able to execute reports aganst your JNDI source.

# Exclude properties from appearing in the reporting database #
The row-length in MySQL is limited. One point in time, you run into this limit. One option is to define properties that will NOT appear in your reporting database. (The other one is to tune the column types of specific named attributes in the next section.) Specify them in alfresco-global.properties like this:

# list of properties to tweak for your project. cm:name is rather stupid to exclude though!
reporting.harvest.blacklist=cm:name

# The default mapping of alfresco property types versus SQL column types #
The Harvesting process maps Alfresco's property types against SQL column types. These property types are what is defined in Alfresco's (and your custom) model files The default mapping is:
```
text=VARCHAR(500)
mltext=VARCHAR(500)
datetime=DATETIME
date=DATETIME
int=INTEGER
boolean=BOOLEAN
noderef=VARCHAR(100)
noderefs=VARCHAR(700)
category=-
version=-
double=DOUBLE PRECISION
float=FLOAT
long=BIGINT
id=BIGINT
path=VARCHAR(250)
taggable=VARCHAR(200)
category=VARCHAR(500)
site=VARCHAR(80)
locale=VARCHAR(100)
any=VARCHAR(500)
html=VARCHAR(500)
content=VARCHAR(500)
version=BIGINT
size=BIGINT
mimetype=VARCHAR(100)
# name = username, groupname
name=VARCHAR(150)
# zone = set off user/group zones, e.g. APP.DEFAULT, AUTH.ALF
zone=VARCHAR(100)
# label = category label
label=VARCHAR(100)
```

## Define exceptions to the mapping ##
Create a file tomcat/shared/classes/alfresco/extension/reporting-custom-model.properties In there you can list your named properties, and define a custom mapping. This is quite handy, since MySQL has a limited row length. The sum of the length of the columns is limited. The default settings are rather generic. If you have many additional properties in your documents/folders/other, you run out of this row-limit. One way to solve this is to fine tune your mapping. (The other is to blacklist properties you don't need. See [AdvancedHarvesting](AdvancedHarvesting.md)) The content of the file in transformed into a Properties object. You can use the #-sign to comment a line out. The lines below are what I use (among some custom prop's)
```
sys_store_identifier=VARCHAR(50)
sys_store_protocol=VARCHAR(50)
sys_node_uuid=VARCHAR(100)
sys_archivedOriginalOwner=VARCHAR(100)
sys_archivedBy=VARCHAR(100)
cm_name=VARCHAR(100)
cm_creator=VARCHAR(100)
cm_modifier=VARCHAR(100)
cm_versionLabel=VARCHAR(50)
cm_versionType=VARCHAR(50)
cm_owner=VARCHAR(100)
cm_author=VARCHAR(100)
cm_workingCopyOwner=VARCHAR(100)
cm_workingCopyMode=VARCHAR(100)
```

# Make the tool stop indexing the deleted items into the reporting database #
By default the tool goes through the Workspace SpacesStore, but also through the Archive. In here Alfresco stores the deleted items. Sometimes this can be handy, sometimes not. If you want to stop adding deleted stuff into your database, change the property in alfresco-global.properties and strip off the `,archive://SpacesStore` part.
```
# List of Stores to crawl for reporting
reporting.harvest.stores=workspace://SpacesStore
```
# Change the timing of Report Execution or Harvesting #
Both are managed by Alfresco's scheduled jobs, thus by CRON definitions. These can be modified in the alfresco-global.properties. See the [Alfresco Wiki](http://wiki.alfresco.com/wiki/Scheduled_Actions#Cron_Explained) or [Cronmaker](http://cronmaker.com/), another tool that can come in handy.
```
# the default cron definition to harvest
#reporting.cron.filldatabase=0 0 0/1 * * ?
reporting.cron.harvest.moreFrequent=0 0 0/1 * * ?
# daily at 23:50
reporting.cron.harvest.lessFrequent=0 50 23 * * ? 

# run on weekdays only #
# hourly every 5 min past the whole hour, from 6h - 22h, on weekdays only
reporting.execution.frequency.hourly=0 5 6-22/1 ? * 1-5 
# daily at 00:10 on weekdays only 
reporting.execution.frequency.daily=0 10 0 ? * 1-5  
#every monday 0:15
reporting.execution.frequency.weekly=0 15 0 ? * 1 
#every 1st of the month @ 0:20
reporting.execution.frequency.monthly=0 20 0 1 * ?
```

# Timestamps and Timezone #
From 0.8.0.4 onwards I added the timzone awareness to the dates/timestamps in the reporting database. That means that you might run into strange behaviour if you misconfigure the timezone definition of your Alfresco server...

# Oracle and Timezone #
Oracle is timezone eager. Startup your Tomcat adding to the JAVA\_OPTS "-Duser.timezone=CET" or any other timezone more relevant for you.