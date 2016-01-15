

# Follow-up on harvesting #
If the module is harvesting, there used to be not a good way to follow-up on what's happening. Now there is an Administration panel showing the tables, if the harvesting process is running against that table, and some more details:
http://tpeelen.files.wordpress.com/2013/04/business-reporting-admin-console.png?w=500&h=255

<a href='http://www.youtube.com/watch?feature=player_embedded&v=KqKLYXG6nIk' target='_blank'><img src='http://img.youtube.com/vi/KqKLYXG6nIk/0.jpg' width='500' height=344 /></a>


# Reset the dates last-run #
The tool records the last timestamp the harvesting of that particular table went successful. This enables the tool to only harvest content that has been changed since the start of the last succesful run. There are circumstances that you want to ignore this date, and want to do a full harvesting run. There is a script in the Data Dictionary/scripts/reporting called resetLastTimestamp.js If you execute this script, the table maintaining all these last-run detials, is flushed. If you need more precise munition, get your SQL tool of choice and modify the timestamp of the table of interest:
[image 'database-view-lastruntable.png'](insert.md)

Alternative; use the JavaScript console (or a less handy alternative) and execute
```
reporting.resetLastTimestampTable(tablename);
```

Where you need to substitute `tablename` with the String value of your table name of course (like "documents")

# Many association nodes #
The tool captures the noderefs of the parent/child associations as well as the source/target associations. However, if you have a lot of them, you might run into a null-pointer exception. Think of having a folder User Homes stuffed with over 50.000 home folders of users. You don't want to traverse all of them to know it won't work. (Report the other way around, compare the parent noderef on the home folder of your user...).

For your convenience I added the following properties to use in your alfresco-global.properties:
```
reporting.harvest.treshold.child.assocs=10
reporting.harvest.treshold.sourcetarget.assocs=10
```
10 is the default value. If there are more than 10 associations found, _none_ will be recorded. (No, not even the first xx, because this does not make reporting sense.)

# Limit the mix of sub types #
Lucene is greedy in finding stuff. If you do a search for TYPE:documents, it will also find all subtypes.

Depending on the needs, I usually comment out the default, and (though using it as a template for the next) to create queries like:
```
document=TYPE:document AND NOT TYPE:document-a AND NOT TYPE:document-b
document-a=TYPE:document-a
document-b=TYPE:document-b
```
Alternatively, if you have less types but more aspects you can do something like:
```
document=TYPE:document AND NOT ASPECT:aspect-a AND NOT ASPECT:aspect-b
document-a-TYPE:document AND ASPECT:aspect-a AND NOT aspect-b
document-b:TYPE document AND ASPECT:aspect-b AND NOT ASPECT:aspect-a
```
Alternatively, you can do something similar with property values. Be sure to include the one, and exclude it in the other...

# Query languages #
Currently you can use Lucene, ~~FTS and XPath~~ only. ~~In the future CMIS and Solr will be added too.~~ Not having Solr in now does not mean this tool does not work with a Solr-based Alfresco (without the 'old-fashioned' Lucene). The query will be executed anyway, Alfresco will cater for that.

# Blacklist #
In alfresco-global.properties (in tomcat/shared/classes) you can define the blacklist of properties you want to exclude from putting into the reporting database. There are some properties that don't make sense, and eat up valuable (and limited) row length, you just don't want in your reporting database. Exclude these properties by adding to your alfresco-global.properties the key:
```
reporting.harvest.blacklist=cm_prop1,cm_prop2,my_whatever,your_thingy
```

In the [AdvancedInstallation](AdvancedInstallation.md) section you can find how the mapping from Alfresco Property types to SQL Column types is defined. This can be changed (preferably not), but more likely you can override the mapping for specific named properties. Sometimes you want to have the 10 noderefs of a folder that point to the child documents. Then increase the size of that particular association. If you don't care, block the entire property, or reduce the size so it is sufficient for ordinary use.