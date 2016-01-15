

# Introduction #

Now we have a reporting tool, what to do with it... I thought of the following reports. Suggestions are welcome! I included a quite few of the reports on this page in the reporting tool. remind that there are som reports actually being a collection of subreports.

If you share your (Alfresco Share compatible) reports with this project, I will publish them in the download section too. (Please inform if there are some settings to be tweaked before the report actually can be executed.)

# Meaningful reports #
## Per User ##
(To be stored for example in sub folder in each userhome.)
  * List of Sites + Role(s) per user. Order by Site (for the given user)
  * List of number of documents, number of MB per Site (for the given user)
  * ...

## Per Site ##
(To be stored for example in sub folder in each Site.)
  * List of members (username, full name, email, phone) (Role?)
  * Number of documents created per user
  * ...

## For Administrators ##
(To be stored in a designated folder somewhere not accessible for everyone)
  * Per user, timestamp of last login
  * Failed logins, timestamp, username (per calendar year?)
  * Number of failed logins, group by username
  * Ratio of MB's in Workspace/Archive Space
  * Ratio of number of documents in Workspace/Archive Space
  * Size of Workspace over time (cumulative per month?)
  * Size of Archive space over time (cumulative per month?)
  * Sites, order by number of days since last modified data of most recent document it contains (indication if a site is used at all)
  * Sites, order by percentage of documents that have a modified date != created date (indicator if site is backup, or actually in use)
  * Sites, order on #MB (sum of size of all content inside, including versions)
  * Sites, ordered by number of users
  * Sites, per site, per role the users (username, full name, phone, email)
  * Users, ordered by number of created documents
  * Users, ordered by MB in use
  * Users, order by number of modified (!=created) documents
  * List of mimetypes in use (and the number of instances?)
  * List of disabled users
  * List of Locked users
  * List of Expirable users ordered by Expiry date
  * ...