# Introduction #

Currently I think there are 2 ways of 'reporting',
  * reports that need to reflect the status of the repository NOW, think of progress of processes, tasks assigned to people, etc.
  * 'statistical' reports that could be run once a day or less

The first category should be implemented in Alfresco dashlets, or custom pages, because they need to be calculated runtime. If you want to tweak the report (or want a new one) usually you run into 'software development'

The second category can be specified up front of a project, but usually organizations get a need for specific reports after using the system for a while. How many processes ran out of schedule, how many products/projects were created/closed this fiscal year? This is the type of reporting I believe this tool will help you out. It is unlikely there is a squad of Alfresco programmers to create all these reports in Freemarker, Dashlets and custom pages time after time. There are 'SQL reporting' skills in the organization. You can easily create a new sql-based reports againstthe reporting database.


I wanted to know how our Alfresco repository is being used. The company I work for uses Alfresco Share as a collaboration/knowledge tool, and I suspect some people to use it more/better than others. But if I want to educate and convince my colleagues to use the system in a more clever way, how can I without knowing what they do with it?

_These considerations thought should struck all repository owners._

I was surprised:
  * How many single-user Sites there were
  * about the ratio Public/Private/Moderated sites
  * That there were more images stored than documents
  * Some users use the system as a backup (which is better than not storing at all)
  * Only a few users are familiar with datalists, discussions, links (all other than documents)
  * About the rate created versus modified.
  * About the number of users having an incomplete profile
  * Some unexpected 'champions', users that appear to use the repository a lot
  * How many Sites are not used for quite some time

_We should have this information before!_

# Business Cases #
@todo