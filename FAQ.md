In this page the FAQ's will be collected when they arrive



# Can the user adjust the report filtering before export to pdf/excel? #
No. The tool does the harvesting and report execution in an automated (scheduled) manner, without user interaction. If you want to execute reports with user interaction you need a different tool for the second part. The harvesting is fine, but you will need to put some interactive front-end on top of the reporting database. Perfectly doable, just not in scope for this project. (I bet Pentaho/Jaspersoft/... have something for that)

# Why do I see 3 Daily %Vendor name% folders? #
Alfresco Business Reporting as became multi-vendor with respect to where you store your reporting database. The SQL dialect for harvesting as well as report execution varies per SQL vendor. For Harvesting these different SQL snippits are hidden from you, for _report execution_ they cannot. Each of these 3 folders contains the specialized report definitions for that brand of vendor. You can remove the brands you don't need (otherwise report execution will result in SQL errors)


# Why do I see 3 Reports %Vendor name% folders? #
See  "Why do I see 3 Daily %Vendor name% folders?". These folders contain the vendor specific _report output_.

# The size of the Harvest Definition Less Frequent is 0 bytes #
That is by intention. The content element of the Harvest Definition is used to store the key/value pairs that define the 'Property Queries' type of harvesting. The tool is shipped with this feature active in the MoreFrequent defintion only. Therefore the size of the harvestingDefinitionMoreFrequent will be >0 bytes, and the size of the HarvestDefinintionLessFrequent will be 0 bytes.

If you have a business need why you want to include 'Property Queries' in your lessFrequent definition too, you are free to do so of course...

# Alfresco started nicely, there is no harvesting/report generation #
The reporting module  usually harvests at the hour. BUT, if you just booted your Alfresco repository, the Reporting module respects a time-out of 5 minutes (by default, you can change) for cron jobs like harvesting and report execution. Beware that you might hit this 5 minute time-out... (And enjoy the booting of your repository!)

# Alfresco started nicely, there is no harvesting #
Please validate if your report definition has any jobs enabled. It has happened that in the 'MoreFrequent definition' both Query Tables and Auditng Export are disabled... That doesn't work. Please enable something, and invoke harvesting again.

# Harvesting is done, but no reports generated #
If you notice your reporting tables do get filled with rows of data (check using [SQuirrel-SQL](http://squirrel-sql.sourceforge.net/)) but reports are not generated, best bet is your install is half-done. Check your %tomcat%/webapps/alfresco/WEB-INF/web.xml if you actually included the element `<resource-ref>`...This is needed for report execution (in Tomcat 7, otherwise you need it anyway for harvesting)


# You told us the tool would be delivered in LGPL? #
Yes. And I have some issues right now to repackage the module. Pentaho comes in [GPL](http://www.gnu.org/licenses/gpl.html), so I have to make some modifications and/or do some research to find a way to deal with that,and get my stuff packaged as [LGPL](http://www.gnu.org/licenses/lgpl.html).

# Can I control reporting from Javascript? #
Yes you can. Parts of it. There is a new root object in JavaScript that can control some administrative actions. The methods are described below:

`reporting.setAllStatusesDoneForTable();`

If a row in the latsuccessfulrun table remains status=Running, it will block all other harvesting from progress. It will not start. Sometimes a running process has died unexpectedly. Then you need to 'manually' set the status bback to 'done'. You can do so by this call. You can execute from the JavaScriptConsole or other ways to execute a JavaScript.

`reporting.dropTables("document,links,forum")`

This method can take a comma-separated list of table names, and will drop those table(s). REMIND: the lastsuccessfulrun-date in the lastsuccessfulrun table needs to be reset too, otherwise old objects will **not** get back into the reporting database.

`reporting.dropLastTimestampTable()`

This method will drop the entire lastsuccessfulrun-table. It will force the next harvesting run to create a new one, and evaluate all content that complies to the 'table queries' again. The reporting tables will be updated where needed.

`reporting.clearLastTimestampTable("document")`

This method will reset the lastsuccesfulrun date to null (okay, somewhere close to 1970). This means that the next harvesting run will start from objects with a modified date after 1970.

If you are on version 1.1.0.0 or above, you can start harvesting from javacript using:

  * `reporting.harvestMoreFrequent();`
  * `reporting.harvestLessFrequent();`
  * `reporting.harvestAll();`


If you are on version 1.1.0.0 or above, you can add indexes to yur reporting tables using:

`reporting.addindexesToTables();`

If you are on version 1.1.0.0 or above
This also means tat if you mnually craft a date (remind, stored as a String, sorry!!!) into this table/row, you can control from how long ago the reporting tables need to be re-inspected and updated... The tool will update rows that are not valid, and will add objects that is not yet in.