_Potentially replaced by blogs_

SELECT some
FROM table
WHERE isLatest=1
AND spaceStore='workspace'

The tool only adds columns if it detects content having a value for that property in Alfresco. That is the way it can support the flexibility of Alfresco's Aspects and properties. There is a downside however. That is:
**In Auditing reports, remind not all columns might be existing at initial startup. E.g. the (failed) login-related reports don�t have the column of failed logins, if there is no failed login in the audit trail...** there can be reports showing number of deleted items (or users keen on deleting stuff). These reports will fail if there are no objects found deleted for that table. The report will crash...

**The solution**
SQL lacks the feature of exception handling (column-not-exists). The best solution would be to get it into the standard and convince the database manufacturers to include it in the product. On the other hand, we might better deal with it.