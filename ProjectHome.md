# Business Reporting is for the Business #

Business Reporting should be handled by the Business. Therefore reporting should use an existing tool and plain and simple SQL. Currently, reporting in Alfresco is not that easy, and many alternatives are tech-driven. This project allows the business to use their reporting suite of choice against 'plain tables' containing _your_ Alfresco business objects. The second feature is to generate reports against these reporting database and store the reports back into Alfresco. Reports can be designed using  [Pentaho](http://www.pentaho.com/) [Reporting](http://reporting.pentaho.com/report_designer.php) (Possibly [JasperSoft](http://www.jaspersoft.com/) ([iReport](http://jasperforge.org/projects/ireport)) works too, but these are not tested anymore).  But feel free to use any BusinessObjects, Cognos, Clickview.

<a href='http://www.youtube.com/watch?feature=player_embedded&v=nj_hVYOzISc' target='_blank'><img src='http://img.youtube.com/vi/nj_hVYOzISc/0.jpg' width='800' height=344 /></a>


This project is build on 2 parts.

<img src='http://tpeelen.files.wordpress.com/2013/03/mechanism.png'>

1. Generate a reporting database based on business entities like Document, Case, Folder, Site etc. Each of these entities should contain all possible attribute values (even with Alfresco's Aspect feature providing flexible, runtime extensions). next to that information from the auditing framework can be used as well as some user/group information and Categories. This reporting database can be filled from scratch, or incrementally since last update. Any Lucene query can define the content of the tables. (Think Aspects, Types, property values.)<br>
<br>
2. Given a reporting database, report execution can be scheduled (thing hourly, daily, weekly, monthly). The result can be a pdf or xls files that will be stored in the Alfresco repository. Reports can have the scope of a Site only (or a user, or another container-like concept), executed against that site, and stored within that site. Currently Pentaho reporting is the supported reporting engine.<br>
<br>
<b>UPDATE</b> Please find most recent code and issues @ <a href='https://github.com/Alfresco-Business-Reporting/alfresco-business-reporting'>GitHub</a>