package com.yurtsevich.wildfly.swarm.keycloak;

import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.datasources.DatasourcesFraction;

import java.net.URI;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws Exception {
        final Swarm swarm = new Swarm();

        final String driverModule = "org.postgresql";
        swarm.fraction(new DatasourcesFraction()
            .jdbcDriver(driverModule, (d) -> {
                d.driverClassName("org.postgresql.Driver");
                d.xaDatasourceClass("org.postgresql.xa.PGXADataSource");
                d.driverModuleName(driverModule);
            })
            .dataSource("KeycloakDS", (ds) -> {
                ds.driverName(driverModule);
                try {
                    // Heroku DB connection
                    final URI dbUri = new URI(System.getenv("DATABASE_URL"));
                    ds.connectionUrl("jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath());
                    final String[] userInfoParts = dbUri.getUserInfo().split(":");
                    ds.userName(userInfoParts[0]);
                    ds.password(userInfoParts[1]);

                    // Local DB connection
//                    ds.connectionUrl("jdbc:postgresql://localhost:5432/keycloak");
//                    ds.userName("username");
//                    ds.password("password");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            })
        );
        swarm.start();
    }

}
