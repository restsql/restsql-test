README.txt (14-Jul-2011)

restSQL Test Deployment Guide

Project website is at http://restsql.org. Distributions at http://restsql.org/dist. Source code hosted at http://github.com/restsql.

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
    

-------------------------------------------------------------------------------
Installing restSQL Test

Requirements: restSQL project or deployed restSQL, JDK, Ant

The test project contains component test code, artifacts and a harness that exercise the framework and the service using the Java and HTTP APIs, respectively. The Java API tests use straight JUnit tests. The HTTP API tests use an XML-driven test case harness built with JUnit. The Java API tests require the restsql project, since it relies on its build file and source code. The HTTP API tests only require a deployed restsql service. 

Database: If you have not deployed the SDK yet, you will need to deploy the extended sakila database. Bash and Windows batch scripts are provided to create the base and extended database for MySQL and PostgreSQL. The bash script is restsql-test/database/<database>/create-sakila.sh and the Windows batch script is restsql-test/database/<database>/create-sakila.bat, where database is mysql or postgresql. You will need to change the user and password variables in the beginning of the script to an account that has database and table creation privileges.

Execution: The tests are executed using the Ant build file (restsql-test/build.xml). Executing the default target, all, will run everything, but you can also run test-api (Java API) or test-service (HTTP API) to run one or either half. If the service is not running in the default location, http://localhost:8080/restsql/, then the System Property, org.restsql.baseUri, must be set. For example:

    ant -Dorg.restsql.baseUri=http://somehost:8080/restsql-0.5/ test-service-http

By default, the tests will use the restsql properties file src/resources/properties/restsql-mysql.properties. If you are using PostgreSQL, you must explicitly instruct the test harness to use the postgresql properties. Set the System property org.restsql.properties as follows at ant execution:
	
	ant -Dorg.restsql.properties=/resources/properties/restsql-postgresql.properties 

Test results will appear on the screen. Test detail is available in restsql-test/obj/test.

PostgreSQL Notes: Five test cases that pass with MySQL are expected to fail with PostgreSQL. The first one returns a database-specific error:
	Negative/TestSelect_ErrorInQuery.xml
The other four return the correct data but in a different order than MySQL.
	FlatManyToOne/TestSelect_Limited.xml
	HierOneToMany/TestUpdate_MultiRowParentAndChild_ByBody.xml
	HierManyToManyExt/TestUpdate_MultiRowParentAndChild_ByBody.xml
	HierManyToMany/TestInsert_MultiRowParentAndChild_ByBody.xml

-------------------------------------------------------------------------------
License

restSQL is licensed under the standard MIT license. Refer to the LICENSE.txt and CONTRIBUTORS.txt in the distribution.