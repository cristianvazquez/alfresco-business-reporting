

# Harvesting #
## Lucene queries ##
For each query-key, a table will be created if it doesn't exist yet. The query is executed and results ordered by sys:node-uuid. he system will return a limited set of nodes. Remember the last node found, and execute the next query where the sys:node-uuid is in the range [one+1..MAX](last.md)
For each ResultSet found, the superset of properties in that ResultSet will be retrieved. For each property, validate if there is a Column of the right type. If not, alter the table, and make it exist.
Next, construct an SQL statement for each node, and if this entry does not exist yet, insert it into the table. If it does exist (noderef + lastModifiedDate is the unique key), update the existing row.
For your convenience, this tool adds a site property to all objects belonging to a site. Also multi-value properties are concatenated with a comma, Categories are treated as a property (the end nodes of the property) and tags are resolved. Also associations are processed, the source/target noderef's are stored as well as the parent/child NodeRefs.

All queries are executed and noderefs will be found with a modified date more recent than the last successful run. Each table/query will have a notion of its own timestamp when it started, given the requirement that it finished gently.

## Categories ##
You are able to define a set of root categories to be added to the reporting DB. Remind, each run the content of these tables will be dropped, and all Category trees will be rebuild from scratch. This is because it is near impossible to determine if a category node has changed position in the category hierarchy. Categories are stored as a path structure, similar to a folder path. Remind, at this point in time the tool will display Categories up to 3 levels deep (candidate to be re-factored...)


## Users, Groups, SiteGroups ##
You can enable the extraction of users, and groups to the reporting database. Users wil expose all user properties into the reporting database. Groups will display all group and their members. Groups having groups are not in the repository, since I have no clue about a decent SQL query to recursively retrieve the number of groups (in path-form) a person has access to...
SiteGroups is a table showing Site, Role, Member. So for each site you will be able to retrieve the SiteManagers etc., or find out to what sites a user has access.

## Auditing information ##
Initially I started using the JSON auditing feed. However, I ran into trouble having to authenticate. I switched to the AuditingService. But this service uses a call-back mechanism, and little documentation. In the end, it appears that the auditing framework uses a few fixed properties (username, timestamp and an id), and the remaining properties are stored in a Map. I use each key to create a (text based) column in the reporting database (VARCHAR100), and the value to insert the value into the database.

# Report Execution #
yet empty

# Data Dictionary & Behaviours #
  * Files underneath a ReportingContainer will be specialized into type ReportingDefiniton
  * Files underneath the ReportingRoot are specialized into HarvestDefinition
  * Folders underneath the ReportingRoot are specialized into ReportingContainers

# History #
This tool intially started as a sort of Javascript API. The idea back then was that you usually 'want something else', and the matter is too complex for configuration. A year later I decided that complexity can be made (more) simple. And configurable. Another year later this version is released.

But underneath there is a transition from a Javascript toolbox into a Action Executer based, configurable tool. And during this transition I had to serve my customers too, so there was no big bang to recreate everything from the ground up. Report execution has been made into Alfresco's Action Executers (driven by Javascript).

The harvesting of the queries still is mostly Javascript based. This is candidate for a quick transition too. Being Javascript the way it is has all kind of downsides. One of them is that you don't want to run 2 harvesting jobs in parallel, you will run into reporting database stuff. The tool now checks if there is any other job running to prevent disaster.