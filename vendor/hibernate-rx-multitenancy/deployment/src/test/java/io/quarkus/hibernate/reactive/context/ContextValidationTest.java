/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.quarkus.hibernate.reactive.context;

import static io.restassured.RestAssured.given;

import io.quarkus.test.QuarkusDevModeTest;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Checks that Hibernate Reactive will refuse to store a contextual Session into
 * a Vert.x session which has been explicitly disabled by using
 * {@link io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle#setCurrentContextSafe(boolean)}.
 */
public class ContextValidationTest {

    @RegisterExtension
    static QuarkusDevModeTest runner = new QuarkusDevModeTest()
            .withApplicationRoot((jar) -> jar
                    .addClasses(Fruit.class, ContextFruitResource.class)
                    .addAsResource("application.properties")
                    .addAsResource(
                            new StringAsset("INSERT INTO context_fruits(id, name) VALUES (1, 'Mango');\n"),
                            "import.sql"));

    @Test
    public void testListAllFruits() {
        //This should work fine:
        given()
                .when()
                .get("/contextTest/valid")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().response();

        //This should throw an exception (status code 500):
        given()
                .when()
                .get("/contextTest/invalid")
                .then()
                .statusCode(500)
                .contentType("application/json")
                .extract().response();
    }
}
