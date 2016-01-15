There are some considerations and some must-knows for the current release that need to be understood in order to understand the value of the reports generated.

## ToDo ##
  * ~~Multi Database - is Work-In-Progress already. Is a feature to be expected soon.~~ done in 0.9
  * ~~Cron jobs Cluster Aware~~ done in 0.9
  * Workflow tasks & process into the reporting database  (I have _all tasks_ working in the 0.9 codebase, incremental will be a 9.1 release)
  * Share admin UI for the overview of the current status (text-based webscript already). Actions for Harvest-all and Execute-all
  * ~~Upgrade the Report Designer support to version Pentaho version 4.x~~ done in 0.9

## Considerations ##
I still have to figure out how/when to deal with these.

  * ~~JaspeReporting has no 'zip-like' report format that supports sub-reports and embedded images. Instead, it expects all files positioned relative to eachother. At this point in time I think of _not_ supporting sub-reports and images. (Have to check against newest release of Jasperreporting.)~~
  * Delegate the JasperReporting and Pentaho dependencies  download to their native sites.
  * ~~Minimize the dependencies on Pentaho. I guess we can do with less Pentaho jar's.~~ Version 0.9 is shipped with the Pentaho SDK libraries.

## Must knows ##
With these in mind, one can interpret the results and the possibilities better.

  * This reporting tool only works against MySQL for the reporting database (for now, multi-vendor-db is work-in-progress).
  * It enables auditing in order to facilitate last-login-like reports
  * It sets the lastModified-date and created-date to the datetime version (instead of date only). This helps a lot to Harvest those objects that were updated last hour only. Otherwise the smallest period of time will be a day... (override of `alfresco/model/dataTypeAnalyzers.properties`)