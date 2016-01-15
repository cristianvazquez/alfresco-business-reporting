Since I use this add-on in my own projects too, there are some intermediate versions that have not made it into the public. Getting it documented and creating samples sometimes just takes too much time.

# 1.1.0.0  (april 14, 2015) #

## LastSuccesfulRun -> LastSuccesfulBatch ##
In the past, the module recorded the timestamp before it started ahrvesting a table, and put it in the lastsuccesfulrun table after it completed harvesting the table. However there are situations where the last event never happens. For example if the system goes down for a backup before that point in time. Next harvest run, the system will start in 1970 gain. This is a deadlock, the system will never proceed beyond that table. This release this has been fixed. The harvesting process will update the the timestamp after each batch. A batch is defined as the full processing of a search result set (usually 1000 result items). So even if the system goes down, this batch is preserved at all times.

In order to make this work, the behaviour of the lastsuccesfulrun table has changed slightly. NodeRef based tables have two rows in this table. The tablename is postfixed with _w or_a (first letter of the respective SpaceStore). The system forces the harvesting of the Workspace SpaceStore before the Archive SpaceStore is harvested.

As a feature, this behaviour can be switched of in the reportingRoot object (called "Reporting" directly in the DataDictionary). In there, also the max number of batches per table per SpaceStore can be configured (default=50) as well as the max number of results in a batch (of a search request) (default=1000)

The tablename column (in lastsuccesfulrun table) is increased in size to house the longer table name. There is no migration script. (VARCHAR(50) -> VARCHAR(100)

Related issues:
[|#43](https://code.google.com/p/alfresco-business-reporting/issues/detail?id=43)


## lndexes ##
The speed of harvesting degraded (lineair?) with the number of rows in the reporting-tables. This was caused by full table scans. The solution in this version is the use of Indexes. When new tables are created, these indexes are created by default. On existing  tables, use the JavascriptConsole and invoke: reporting.addindexesToTables(); or addindexesToTable("tablename"); This will add the indexes onto the known tables, or to the given table. There is no check if the indexes already exist!

Related Issues:
[|#43](https://code.google.com/p/alfresco-business-reporting/issues/detail?id=43)


## Force status=Done @ startup ##
In the past, if Alfresco went down when harvesting, the run-status of that particular table remained "Running". The admin had to manually change this into "Done" (for example using JS-Console Reporting.setAllStatusesDoneForTable();). Now the module will update all statuses into "Done" when booting Alfresco. Since Alfresco was down, there is no meaningfull 'resume', all bookkeeping was in memory. This way the system becomes more self-healing.

Related Issues:
-none

## JavaScript methods ##
JavaScript methods were added to start harvesting from the JS-Console. Use reporting.harvestMoreFrequent();,
reporting.harvestLessFrequent(); or reporting.harvestAll();

Related Issues:
- none


## What is not in here? ##
Related to the changes in this release, some migration path's are not provided:
  * There is NO migration of the definition of the lastsuccesfulrun table...
  * The content of the tablename column has changed into %tablename%_w and %tablename%_a. These rows will be added when the module starts harvesting. This means the old row became pointless, AND the module wil evaluate all content of the repo, to update the lastsuccesfulrun timestamp. This can be preventedd by manually renaming the tablename into %tablename%_w. This way only the archive is fully re-scanned._

# 1.0.1.4 (sept 2014) #
fix for actively opening/closing DB connections, was an issue with Oracle && increase robustness against broken nodes. Never released publicly. Behaviour tuning (noticed against version 4.2.2) because it blocked 'download as zip'. (most likely backward compatibility problem too...)

# 1.0.1.1 #
Fix for report execution that ignored other than hourly execution
# 1.0.1.0 (July 2014) #
Replaced PentahoReporting by version 5.1 instead of 5.0, because 5.0 contained an error displaying PDF in Adobe reader.
Also fixed issues with cron defintion.

# 1.0.0.0 (April/May 2014) #
Finalized iBatis, HarvestingProcessors, many bug fixes, introduced some tests. Fixed Admin table overiew again. Apart from the first few columns, all other columns are lower case. Some columns added (like type, versioned [0,1])

# 0.9.x #
Introduced iBatis/MyBatis to become 'database vendor independent'. Whaha. Not. But aiming at Oracle, Postgres and MySQL (Never publicly released) Upgraded (embedded) Pentaho Reporting.

# 0.8.1.0 #
Started migrating Harvesting into Java. (Never publicly released)

# Most recent: 0.8.0.3 (June 2013) #
  * Made the AMP install again. And includes the fixes from 0.8.0.2. Remind, we have to override faces-config-custom.xml, therefore the -force parameter is required when using apply\_amps!!

# 0.8.0.2 (May 2013) #
This version is NOT installable. However, it should contain:
  * fix for non-English languages. Data Dictionary appeared to be hard-coded in the web-client-config.xml. These were replaced by an ActionHandler, nicely working with the language independent xpath notation.
  * Integers (and all other non-text properties) with a non-default value (e.g. could be NULL values) fail to build decent SQL queries.

# 0.8.0.1 (April 2013) #
  * JavaScripts were configured incorrectly (switching from tomcat/shared/extension to AMP based packaging) <- apologies

# 0.8.0 (April 2013) #
  * AMP packaged
  * Harvesting configurable per UI
  * Harvesting can be split in moreFrequent and lessFrequent
  * Each reporting database table  has a last-run-date
  * Execution configurable per UI (Reporting Root | Reporting Container | Reporting Template)
  * Execution triggered each hour/day/week/month by cron job.
  * Extended support for site-based reports
  * Fixed object-based harvesting of Tags and Categories (they appeared to be broken for quite a while)
  * Share UI (starter)
  * (prepared for cross-vendor reporting database)


# 0.7.5 (September 2012) #
This version is shipped to a Happy Few only.

  * Added JNDI for all reporting database activity (actually: a must if you use some test, acceptance and production systems)
  * Added support for capturing multi-value properties
  * Improved support for versioned documents
  * Added support for relations (parent-child as well as source-target). The noderef(s) of 'the other side' are stored on the reporting row.
  * Added configuration for the default mapping of Alfresco types into SQL types
  * Added configuration for overruling the default mapping of individual properties (occupy more or less space in a SQL column/row; remember MySQL row length is limited in length!)
  * Included a 'summary' of all tables in the reporting database (webscript), displaying the number of active/deleted content, latest/non-latest versions in there (Intermediate step for Share Administrator page/control)
  * Moved timestamp of last successful run and current run-status into the reporting database (remove dependencies on content in the repository for functioning)

  * Moved report execution into a CustomAction
  * Added support for executing parameterized reports. Think of having 1 report definition, and generate separate PDF reports for each Share Site (or other recognizable repository structure) Sample provided.

# 0.6.5 (January 2012) #

  * Packaged all code and configuration into a single jar to be deployed in WEB-INF/lib
  * Extracted database and scheduled jobs configuration into alfresco-global.properties


## 0.6 (November 2011) ##

The initial public version