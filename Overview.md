This tool works in two phases. One is harvesting the objects and metadata from Alfresco into a reporting database, the second one is executing Pentaho reports to generate pdf or Excel reports against this reporting database.
<img src='http://tpeelen.files.wordpress.com/2013/03/mechanism.png'>

The main switches to enable or disable harvesting as well as report execution are located in the metadata of the Space: /Data Dictionary/Reporting. This space is called Reporting Root. (Technically, you can have multiple of these. Practically I doubt if it helps you. Each child-Reporting Container or Report will reference it's own Reporting Root)<br>
<img src='http://tpeelen.files.wordpress.com/2013/03/reportingroot1.png'>

<i>Edit Space properties of space /Data Dictionary/Reporting.<br>
Explained in Executing Reports: Notice you can set the extension for pdf and Excel (you pick xls or xlsx).</i>

<h1>Harvesting data</h1>
Alfresco Business Reporting allows for 4 different ways of harvesting information from Alfresco:<br>
<ol><li>Using queries to fill/update tables with the superset of properties from the resulting Alfresco objects. These properties include tags, multi-valued properties, associations etc. This category of data is versioned. For each object found, the validFrom and validTo timestamps are recorded (also for folders). Since each table is populated by a query (think Lucene, FTS or CMIS), you can define tables for your specific business needs.<br>
</li><li>Select existing Alfresco Categories. Each Category label, description, and the 'path' structure will be stored in the reporting database. The path structure is the concatenation of each category to its parent(s), just as how a folder structure is displayed.<br>
</li><li>Users and groups, but actually this option creates 3 tables;<br>
<ul><li>Persons: all known properties of all users known to the system (including their Alfresco zone)<br>
</li><li>Groups: All groups naming their Persons as members (including their Alfresco zone)<br>
</li><li>SiteGroups: a table containing the Site name, the SiteRole (e.g. SiteManagers), and the members of these role(s)<br>
</li></ul></li><li>Audit queries. Think of login information (successful but also failed logins), but actually, all defined audit applications can be added to the Reporting tool, with the flexible superset of properties. You get the logins and failed logins for free.</li></ol>

All these features can be configured in /Data Dictionary/Reporting/HarvestDefinition (but actually you are free to rename this Document)<br>
<br>
The harvesting process will add the Site name to each object that it harvests within a site. This enables you to associate objects (Documents, DataListItems, Folders etc.) to sites, persons to objects to sites etc. Also objects will be stored with the full displayPath as a property or your convenience.<br>
<br>
To users and groups it will add the zone information, so you will be able to tell if an authority is from Alfresco or LDAP or AD.<br>
<br>
<br>
The tool will create database columns on the fly, only if it detects a new column not there before. This behaviour is needed to deal with the ad-hoc nature of Aspects. Therefore expect the schema of your reporting database to grow. If some properties have never passed this tool, the column will not be there (example: failed logins at the moment of installation of the tool, since there have not been failed logins yet, and the name of this property gets known only if there is at least 1 failed login...)<br>
<br>
The Alfresco properties appear in the database as columns of a table. The column names are the sort-form names of the Alfresco properties. cm:name translates to cm_name. Actually, all ':' and '-' are changed into '<i>' since SQL does not like these characters in column names.</i>

<h2>More information</h2>
See <a href='Harvesting.md'>Harvesting</a> and <a href='AdvancedHarvesting.md'>AdvancedHarvesting</a> for more detailed information.<br>
<br>
<h1>Executing Reports</h1>
In /Data Dictionary/Reporting/ a few pre-defined spaces exist. If you create Spaces manually, they are automatically forced into a different type having more reasonable properties (from a reporting context). These 'containers' have an execution frequency (default: hourly, daily, weekly, monthly). Each container can contain report definitions, that are executed in that particular frequency (using a cron definition). You are free to name these containers. It helps to include the frequency in the name. You are free to include multiple containers having the same frequency (e.g. one container for sysadmin related reports, and a container for business-minded reports?)<br>
<br>
Reports can be executed against 'all', or within the context of a query-result. Most obvious examples are Sites and Persons. This allows you to define a parameter to parameterize Pentaho reports. One report definition can be executed against different contexts, resulting in different reports. A report definition containing some metrics about the number of files per site, can be executed in the context of that site, resulting in a pdf/xls report containing metrics for that particular site, stored in a folder within that particular site. And that for all sites (complying your query, so this can be limited to specific visibility, sitePresets etc.).<br>
If reports are generated against a specific context (e.g. Sites or Persons) the resulting reports can be stored relative to each resulting object. So Site specific reports can be stored in a particular folder within a Site, and Person related reports can be stored into some path inside the UserHome.<br>
<br>
<h2>More information</h2>
See <a href='ReportExecution.md'>ReportExecution</a> and <a href='AdvancedReportExecution.md'>AdvancedReportExecution</a> for more detailed information.<br>
<br>
<h1>Design-for-reporting</h1>
Having worked with this Reporting tool, it appears useful to design-for-reporting. Some sort of reporting need is known when starting a project. It helps to create such a data model that makes reporting easy. From a reporting perspective it could help if you accept some data redundancy. This can mean to define additional (actually redundant) properties in your datamodel, and use some behaviours to get the property values in sync with the parent carying the master value. For example, I often include the username as a property too if I use a Person association. (This appears a valuable choice if you happen to use this additional property to include that content in personal Dashlets too!)