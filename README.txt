README.txt (24-Mar-2017)

restSQL Test Deployment Guide

Project website is at http://restsql.org. Distributions at http://restsql.org/dist. Release history at http://restsql.org/doc/ReleaseHistory.html. Source code hosted at http://github.com/restsql.

-------------------------------------------------------------------------------
Structure and Distributions

restSQL source code is contained in three eclipse projects:
    1. restsql                      (service and core framework)
    2. restsql-sdk                  (documentation, HTTP API explorer, javadoc, examples)
    3. restsql-test                 (test framework, test cases)

restSQL binary distributions contain three libraries:
    1. restsql-{version}.jar        (core framework only)
    2. restsql-{version}.war        (service and core framework, 3rd party dependencies)
    3. restsql-sdk-{version}.war    {sdk)

restSQL source distributions consist of one jar:
    1. restsql-{version}-src.jar    (service and core framework)

restSQL docker images:
	1. restsql/service - service and core framework
	2. restsql/service-sdk - service and core framework + sdk
	3. restsql/mysql-sakila - MySQL 5.7 + Sakila + restsql-test extensions

-------------------------------------------------------------------------------
Versions

The restsql and restsql-sdk versions are found in the jar and war's META-INF/MANIFEST.MF. It is also found in the the source tree in restsql/build.properties and restsql-sdk/build.properties in the property build.version. 


-------------------------------------------------------------------------------
Installing restSQL Test

Requirements: restSQL project or deployed restSQL, JDK, Ant

The test project contains component test code, artifacts and a harness that exercise the framework and the service using the Java and HTTP APIs, respectively. The Java API tests use straight JUnit tests. The HTTP API tests use an XML-driven test case harness built with JUnit. The Java API tests require the restsql project, since it relies on its build file and source code. The HTTP API tests only require a deployed restsql service. 

Database: If you have not deployed the SDK yet, you will need to deploy the extended sakila database. Bash and Windows batch scripts are provided to create the base and extended database for MySQL and PostgreSQL. The bash script is restsql-test/database/<database>/create-sakila.sh and the Windows batch script is restsql-test/database/<database>/create-sakila.bat, where database is mysql or postgresql. You will need to change the user and password variables in the beginning of the script to an account that has database and table creation privileges.

Execution: The tests are executed using the Ant build file (restsql-test/build.xml). Executing the default target, all, will run everything, but you can also run test-api (Java API) or test-service (HTTP API) to run one or either half. 

If the service is not running in the default location, http://localhost:8080/restsql/, then the System Property, org.restsql.baseUri, must be set. For example:

    ant -Dorg.restsql.baseUri=http://somehost:8080/restsql-0.8.7/ test-service-http

The trailing forward slash in the base URI is required.

By default, the tests will use the restsql properties file src/resources/properties/restsql-mysql.properties. If you are using PostgreSQL, you should use the methods that are postfixed with -pgsql, for example:

	ant test-service-java-pgsql

This will instruct the test harness to use the postgresql properties and exlude tests in the MySqlOnly folder. Note alternatively you can set the system property -Dorg.restsql.properties=/resources/properties/restsql-postgresql.properties. 

Test results will appear on the console. Test detail is available in restsql-test/obj/test.

Security tests are separately run in the api and http interface styles since the default profile of restsql is no security. (The java service interface does not support security tests). Before running the test-api-security or test-service-http-security, uncomment the security declarations in restsql/WebContent/WEB-INF/web.xml, uncomment the security.privileges property in restsql-test/src/resources/properties/restsql-xxx.properties and redeploy the web app.

If you are using a custom database, set the system property org.restsql.testDatabase, for example:

	ant -Dorg.restsql.testDatabase=mydbname test-service-http

-------------------------------------------------------------------------------
License

restSQL is licensed under the standard MIT license. Refer to the LICENSE.txt and CONTRIBUTORS.txt in the distribution.