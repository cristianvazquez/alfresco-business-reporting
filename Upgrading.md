# Upgrade from 0.8.x.y to 1.0.x #

## Upgrade steps ##
  * Put the new AMP in the alfresco/amps folder
  * Remove the old AMP from the alfresco/amps folder
  * Execute apply-amps
  * copy the share jar file into %tomcat%/webapps/share/WEB-INF/lib (and remove the old existing one)
  * Open each of your report definitions in Alfresco (the .prpt files) in the 5.0.x Report Designer, and save those back again. The 'old' 3.7.x file format appears to be 'almost forward compatible', but not fully against this 5.0.x Pentaho Report Designer release...

I think/believe your existing reporting database can be used apart fro the changes in column names. It is best to drop all tables and recreate new ones (and validate/upgrade (Pentaho Reporting 3.7 -> 5.0) and update your report definitions (for column names) when possible.

Alfresco 4.0, 4.1, 4,2 only (no more 3.4.x, please stay at version 0.8)

## What's different then? ##
  * Pentaho Reporting is upgraded, so you ca create Excel output again!! (actually in the 0.9 range already, but hardly anybody will know)
  * Columns are added for the object type, if a conent-type object is versioned.
  * MyBatis was introduced (actually in the 0.9 range already, but hardly anybody will know)
  * You can store your reporting database in MySQL, Oracle and/or Postgresql! Yeah!
  * There are 3 Spaces containing Penatho report definitions. SQL != SQL, so I had to tweak report definitions into the dialect of the database. They output into Data Dictionary/Reporting/Reports/MySQL (or other vendor name like Oracle or Postgres). Or into Sites of course.

# Upgrade from 0.8.0.x to 0.9.0.x #
## Upgrade steps ##
  * Put the new AMP in the alfresco/amps folder
  * Remove the old AMP from the alfresco/amps folder
  * Execute apply-amps
  * Open each of your report definitions in Alfresco (the .prpt files) in the 5.0.x Report Designer, and save those back again. The 'old' 3.7.x file format appears to be 'almost forward compatible', but not fully against this 5.0.x Pentaho Report Designer release...

## What's different then? ##
  * Pentaho Report Designer is upgraded to the newest 5.0.3 version. This enables Excel output again.
  * The MyBatis framework is used for all Database access (just like Alfresco does). This means MySQL and Postgreql databases are supported by now ;-) If one needs Oracle or SQLServer, contact me and I will be happy to assist you.
  * You will not notice, but Harvesting has been rewritten big-time. This makes it a more robust reporting framework, and reduces my load on testing.

# Upgrade from 0.6.5 to 0.8.0.x #
The only meaningful upgrade is from the most recent version 0.6.5. The reporting database has the same structure, so I guess your reports are still valid. Most of the code has changed, and the tool is upgraded from a Javascript API into a UI based module.

## Upgrade steps ##
You need to take the following steps:
  1. copy the folder 'reporting' away from `Data Dictionary/Scripts` to somewhere out-of-the-way. You can delete these scripts if everything works fine.
  1. The folder `alfresco-business-reporting` with the (sample) report definitions, report output and log files is not used anymore. You can move the report definitions into the new structure if you want to keep them.
  1. Get rid of the `alfresco-business-reporting-0.6.5.jar` from the folder `%tomcat%/webapps/alfresco/WEB-INF/lib` (or any other version of that jar hanging out there)
  1. Get rid of the folders underneath `%tomcat%/shared/classes/alfresco/*  `You definately want to preserve your alfresco-global.properties...

## What's different then? ##
  * No need anymore to create the tables in the reporting database 'manually' (using the 'old' script in the `Data Dictionary/scripts/reporting` folder). The tool will create any table it thinks is required according your configuration.
  * the current status (done or running) will be maintained on a per-table basis, as wel as the last-run timestamp. This enables running Harvest definitions at different frequencies. Some queries can be executed more frequent, other queries less frequent. Usually you can use this to execute the queries that drop and recreate tables daily instead of hourly (or more often).
  * You can drop report definitions in one of the Execution Containers that are executed in a given frequency.
  * You can generate reports using parameters, and by this feature execute a single report definition against for example each site. And store the resulting reports in each particular site.
  * You can use any fragment of a timestamp in your folder path or filename. This can be used to store all reports by day-of-the-week, or more likely per year or month.
  * [Pentaho](http://reporting.pentaho.com/) is steady in. [JasperReports](http://www.jaspersoft.com/reporting) is on its way out. The zip-like feature of sub-reports in Pentaho is a great and welcome feature. JasperSoft lacks this feature. Since time is limited I stopped testing against JasperSoft.
  * Way more enhancements you might not even be aware of or in need of. Yet.