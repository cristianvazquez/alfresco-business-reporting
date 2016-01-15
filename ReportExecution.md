

# Introduction #
There is at least 1 Reportin Root object. Underneath this Reporting Root There are some Reporting Containers. The Reporting Containers own the report execution frequency. Inside the reporting Containers there are the actual Reporting Templates, the Pentaho report definitions.

# Configuration #
Each of the three levels carries configuration properties.

## Reporting Root ##

The reporting root owns the switches to enable or disable all report execution containers underneath, and the harvesting definitions underneath. Sometimes it is handy to just disable the thing. Next to that it defines the actual extensions for Excel and pdf.

The thing that is important for site-based reports is the field 'Target Queries". These are named queries. if your Reporting Template is configured properly, and your Pentaho report is clever enough, you can execute your report template against each individual result from the resultset of this named query. Read the last part of this last sentence again: "you can execute your report template against each individual result from the resultset of this named query". A blog about his topic will be posted shortly. The language of this query you can define. I still love Lucene.

<img src='http://tpeelen.files.wordpress.com/2013/03/reportingroot1.png'>

<h2>Reporting Container</h2>

The reporting container mainly owns the execution frequency. Next to that you can enable/disable the entire container, but that is about all you can do with it.<br>
<br>
<img src='http://tpeelen.files.wordpress.com/2013/03/reportingroot1.png'>

<h2>Reporting Template</h2>

The reporting template contains the actual Pentaho report definition. It knows the output format (Excel or PDF) and if the resulting report is versioned or not. Sometimes you want to, sometimes it does not make sense.<br>
Parameter substitution is the actual parameterization of the Pentaho reports. You can drive the report from within Alfresco. For example insert hte email address extension of your company, so the report can determine all other email extensions are 'external' people. Could be handy in a generic report showing external Site Managers... You can also substitute a parameter with any property of the base object (the result from the resultset of the query in the Reporting Root). For example the parameter sitename with name of the current site (sitename=cm:name).<br>
<br>
The targetPath is the path where the resulting report wil be stored. This is a relative path against the base object (that very site for example), or against the targetNode (a Space assigned-by-noderef). You can make this path relative by using for example elements from Java's <a href='http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html'>SimpleDateFormat</a>

The targetNode, as mentioned a line ago, is the 'named' folder where the resulting reports are stored. (These can be extended with the targetPath)<br>
<br>
<img src='http://tpeelen.files.wordpress.com/2013/03/reportingtemplate.png'>

<h1>Report execution</h1>

<h2>Manual execution</h2>
When needed, there are several handles to trigger execution of 'something'. There are 3 levels of execution using the action (in the Explorer UI):<br>
<br>
<img src='http://tpeelen.files.wordpress.com/2013/04/explorerreportingrootactions.png'>

<i>Additional actions against the Space 'Reporting' in the Data Dictionary. Actions to execute all reports, and to harvest all harvest definitions</i>

<img src='http://tpeelen.files.wordpress.com/2013/04/explorerreportingcontaineractions.png'>

<i>Additional action against 'Reporting Containers'; execute all reports inside the Reporting Container</i>

<img src='http://tpeelen.files.wordpress.com/2013/04/explorerreporttemplateactions.png'>

<i>Additional action against single report definition; Execute this Report Template</i>

After executing (some or all) reports, the resulting reports can be found in Data Dictionary/Reporting/Reports. Site specifics can be found in the Reports folder of each Site's document library.<br>
<br>
<h2>Scheduled execution</h2>
Report execution is scheduled in 4 frequencies; hourly, daily, weekly and monthly. Some reports need to be more up-to-date than others. Drop your Pentaho report in the proper Space ('Reporting Container') and you are ready to go. Every automated execution is scheduled by a cron definition, and can be overridden using the alfresco-global-properties. The default settings are:<br>
<pre><code># hourly every 5 min past the whole hour, from 6h - 22h, on weekdays only<br>
reporting.execution.frequency.hourly= 0 5 6-22/1 ? * 1-5<br>
# daily at 00:10 on weekdays only<br>
reporting.execution.frequency.daily= 0 10 0 ? * 1-5 <br>
#every monday 0:15<br>
reporting.execution.frequency.weekly= 0 15 0 ? * 1<br>
#every 1st of the month @ 0:20<br>
reporting.execution.frequency.monthly= 0 20 0 1 * ?<br>
</code></pre>