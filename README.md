# KeyCloak on Swarm on Heroku
Here's an example of how to deploy standalone [WildFly KeyCloak](http://www.keycloak.org/ "KeyCloak official page") (Single-Sign-On, authentication and authorization server) on [Heroku](https://www.heroku.org/ "Heroku official page"). Though the task can seem trivial, in fact there are some difficulties. I'll describe them below and explain how to overcome.

## KeyCloak on Swarm
The first tough point is that KeyCloak can't be installed on Heroku as standalone server or WildFly adapter as you should have an access to a file system in such a case, which is not an option for Heroku - you don't have such access.

KeyCloak WildFly Swarm server to the rescue:

```
    <dependency>
      <groupId>org.wildfly.swarm.servers</groupId>
      <artifactId>keycloak</artifactId>
      <version>${version.wildfly.swarm}</version>
      <classifier>swarm</classifier>
    </dependency>
```

In such a case we can run the server with simply:

```
java -jar target/keycloak-heroku-1.0.0-swarm.jar
```

## Heroku slug size
Next tough point is Heroku's limitation on built slug size. It's limited by 300 MB and this example with last version of WildFly Swarm server (at the moment of this wrinting `2017.2.0`) is getting more. A solution for this point is to use the single one version which is built with less slug size - `2016.8`. Version `1.0.0.Final` and lower didn't have yet KeyCloak server enclosed.
 
## Initial DB data
And the last tough point is that KeyCloak server doesn't have prebuilt admin user. The only 2 options WildFly developers gave us are:
* to open KeyCloak page locally ([localhost:8080/auth](http://localhost:8080/auth))
* or to run CLI-command in local or remote console
And we are again stuck as Heroku doesn't provide filesystem access. Thankfully there's solution for this as well. We can run server locally `heroku local:start` on local empty DB, create first admin and then export this DB into a JSON-file. For this we should add following Java-options when starting Swarm:

```
-Dkeycloak.migration.action=export \
  -Dkeycloak.migration.provider=singleFile \
  -Dkeycloak.migration.file=data.json
```

Then change this for import:

```
-Dkeycloak.migration.action=import \
  -Dkeycloak.migration.provider=singleFile \
  -Dkeycloak.migration.file=data.json \
  -Dkeycloak.migration.strategy=OVERWRITE_EXISTING
```

... and push this to Heroku:

```
git push heroku master
```

Next step is to remove `data.json` file from repository and migration-options from starting process and push to Heroku once again.
 
## PostgreSQL connection
How to configure KeyCloak to connect to PostgreSQL is more or less straightforward and we can see it in `pom.xml`, in `Main.java` and in `module.xml`