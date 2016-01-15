
Harvesting is the term to find relevant metadata from Alfresco, and feed it into the reporting database. This configuration can be changed in the document called HarvestDefinition (in /Data Dictionary/Reporting).

# Frequency #
Some components only harvest the increment of data changed since last harvesting run has successfully closed. Samples are the query tables and audit logs. On the other hand there are query types that cannot determine changes only since last harvestng run. Samples are Categories and user/group information. It is not (easy enough) detectable what part of these structures have changed, replaced or removed. Therefore these set of reporting tables are emptied and regenerated from scratch. Depending on the type and size of content this can be time consuming. Therefore there is a cron definition for queries you want to execute more frequent, and queries you want to execute less frequent (because of impact on the Alfresco server).

<img src='http://tpeelen.files.wordpress.com/2013/03/harvestinglessfrequent.png'>

<i>Default settings for the lessFrequent harvesting</i>



<img src='http://tpeelen.files.wordpress.com/2013/03/harvestingmorefrequent.png'>

<i>Default settings for the moreFrequent harvesting</i>



<h1>Reporting components</h1>
The properties of this document allow to enable/disable harvesting features;<br>
<ul><li>Query tables<br>
</li><li>Audit queries<br>
</li><li>User/Group/SiteGroups<br>
</li><li>Categories</li></ul>

<h2>Query tables</h2>
This is the original starter of the Alfresco Business Reporting project. The content element of this HarvestDefinition document contains the named queries that result is a set of Alfresco objects that will populate this table's rows. Each object found will be versioned. This means that the obvious document versions will be available in Alfresco Business Reporting. But also objects like Folders will be versioned. The lastModifiedDate will be used to determine if the metadata represents a newer version than the one stored. Each entry will have additional columns like isLatest (true/false), validFrom and validTo (timestamp).<br>
<br>
In the content property of the HarvestDefinition document will contain the named queries. See the default set of queries below. Actually, it is processed in such a way that lines starting with a hash (#) are ignored. The first word (before the '=' sign) will become the table name. In the defaults below this means that the tables folder, document, site, datalist, and datalistitem will be created and populated, assuming the query (right hand side of the '=' sign) results in at least 1 result.<br>
<br>
For each result, the tool will find out all properties that can be extracted into the reporting database. (Some properties are irrelevant and are black-listed, see <a href='Advanced.md'>Harvesting</a>.). These also include Categorie values, Tag values, the Site name if the object is part of a Site, multi value properties, associations (source-target) and structural relations (parent-chid). The tool is able to gather all versions of documents, as well as the archive store, so processing deleted items. (it is good to know your top contributors, but who actually removes stuff 'overdue'?)<br>
<br>
<h3>Default queries</h3>
<pre><code># usage:  key = tablename, value=Lucene query<br>
folder=TYPE:"cm:folder" AND NOT TYPE:"st:site" AND NOT TYPE:"dl:dataList" AND NOT TYPE:"bpm:package" AND NOT TYPE:"cm:systemfolder" AND NOT TYPE:"fm:forum" <br>
document=TYPE:"cm:content" AND NOT TYPE:"bpm:task" AND NOT TYPE:"dl:dataListItem" AND NOT TYPE:"ia:calendarEvent" AND NOT TYPE:"lnk:link" AND NOT TYPE:"cm:dictionaryModel" AND NOT ASPECT:"reporting:executionResult"<br>
calendar=TYPE:"ia:calendarEvent"<br>
forum=TYPE:"fm:forum"<br>
link=TYPE:"lnk:link"<br>
site=TYPE:"st:site"<br>
#datalist=TYPE:"dl:dataList"<br>
datalistitem=TYPE:"dl:dataListItem"<br>
<br>
</code></pre>

I like Lucene queries, but feel free to use another way of querying. The system allows you to choose from Lucene, FTS and CMIS. (Solr based queries will follow in a later release. This module works with Solr based installs though!)<br>
<br>
<h2>Audit queries</h2>
Within Alfresco auditing is off by default. Installing the amp however, enables the auditing framework, and introduces capturing login attempts. To disable it, add to your alfresco-global.properties (override the setting from the amp):<br>
<pre><code>audit.enabled=false<br>
</code></pre>

You can define all kind of 'auditing applications'. See the <a href='http://wiki.alfresco.com/wiki/Auditing_(from_V3.4)'>Alfresco wiki</a> for some nice samples. The auditing framework is configured using 'Applications'. These are named configurations resulting in the registration of system events. Each event always captures a username and timestamp. Next to that the particular application records some custom properties based on the action audited. Alfresco Business Reporting captures the mandatory fields like username and timestamp as well as the full set of additional properties.<br>
The tool will use the application name to generate a table name. Each of the properties captured will end up being columns in this reporting table. For now you can put all your Applications in the configuration UI, comma separated.<br>
<br>
<br>
<h2>User/Group/SiteGroups</h2>
This is a query resulting in 3 tables:<br>
<br>
<h3>Person</h3>
All known metadata for each user will be registered. First of all it will get all 'general' Alfresco properties. Next to that it will add the zone, if the account is locked, enabled, if the account expires and the expiry date. Remind that the additional properties like skype name, mobile number and some more are for some reason not there by default. You need at least 1 person having one of these details filled in in order for Alfresco/this tool to recognize these properties. (Otherwise the reports based on these properties will fail because the columns don't exist.)<br>
<br>
<h3>Groups</h3>
This is the full set of groups. For each group the list of members (users only, non-recursive) will be listed. Additional the zone of the group will be recorded.<br>
<br>
<h3>SiteGroups</h3>
For each Site, there are by default 4 groups; GROUP_site<i>${sitename}</i>SiteManager, GROUP_site<i>${sitename}</i>SiteCollaborator etc. For each Site, for each group, all members (users only, recursively down) will be listed. This way you know who has access, given what role. This fits the default, tweak your reports if you go wild in changing access control.<br>
<br>
<h2>Categories</h2>
Currently, a rather limited way of showing the Categories is in the reporting tool. For each category, the category will be stored, as well as the category path. The list of categories, starting with the given category, back to the root level category is stored. The limit is that it will be stored to up to 3 levels deep. Full-recursive will follow in a later release.<br>
<br>
<h1>Manual execution</h1>
The Harvesting process can be started manually if needed. On the 'Reporting Root' (the Reporting folder in the Data Dictionary; actually each reporting root, if you happen to create multiple) there is an action (in Explorer) to invoke all harvesting. Next to that, each Harvesting Definition object in Alfresco (in Explorer) has this action  to be executed just that Harvesting Definition.<br>
<br>
<img src='http://tpeelen.files.wordpress.com/2013/04/explorerharvestingactions.png'>

<i>The additional action against a Harvesting Definition to execute this definition</i>

<h1>Scheduled execution</h1>
Harvesting is scheduled in 2 batches; moreFrequent and lessFrequent. As said before, some queries look for the changes since last run. Some queries drop the table and recreate&refill from scratch. The latter ones are usually candidate to execute 'lessFreqent'. Both schedules are cron definitions and can be overridden using the alfresco-global-properties. The default settings are:<br>
<br>
<pre><code># the moreFrequent default cron definition to harvest; every hour<br>
reporting.cron.harvest.moreFrequent=0 0 0/1 * * ?<br>
# less Frequent is daily at 23:50. Ideal for queries that rebuild entire tables<br>
reporting.cron.harvest.lessFrequent=0 50 23 * * ? <br>
</code></pre>