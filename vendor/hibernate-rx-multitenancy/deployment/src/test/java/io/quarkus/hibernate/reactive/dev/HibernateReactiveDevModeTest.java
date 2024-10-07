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

package io.quarkus.hibernate.reactive.dev;

import io.quarkus.test.QuarkusDevModeTest;
import io.restassured.response.Response;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that public field access is correctly replaced with getter/setter calls,
 * regardless of the field type.
 */
public class HibernateReactiveDevModeTest {

    @RegisterExtension
    static QuarkusDevModeTest runner = new QuarkusDevModeTest()
        .withApplicationRoot((jar) -> jar
            .addClasses(Fruit.class, FruitMutinyResource.class)
            .addAsResource("application.properties")
            .addAsResource(
                new StringAsset(
                    "INSERT INTO known_fruits(id, name) VALUES (1, 'Cherry');\n" +
                    "INSERT INTO known_fruits(id, name) VALUES (2, 'Apple');\n" +
                    "INSERT INTO known_fruits(id, name) VALUES (3, 'Banana');\n"),
                "import.sql"
            ));

    @Test
    public void testListAllFruits() {
        Response response = given()
            .when()
            .get("/fruits")
            .then()
            .statusCode(200)
            .contentType("application/json")
            .extract().response();
        assertThat(response.jsonPath().getList("name")).isEqualTo(Arrays.asList(
            "Apple",
            "Banana",
            "Cherry"
        ));

        runner.modifySourceFile(
            Fruit.class,
            s -> s.replace("ORDER BY f.name", "ORDER BY f.name desk")
        );
        given()
            .when()
            .get("/fruits")
            .then()
            .statusCode(500);

        runner.modifySourceFile(Fruit.class, s -> s.replace("desk", "desc"));
        response = given()
            .when()
            .get("/fruits")
            .then()
            .statusCode(200)
            .contentType("application/json")
            .extract().response();
        assertThat(response.jsonPath().getList("name")).isEqualTo(Arrays.asList(
            "Cherry",
            "Banana",
            "Apple"
        ));
    }

}
