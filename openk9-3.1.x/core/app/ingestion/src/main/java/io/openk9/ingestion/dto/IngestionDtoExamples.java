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


package io.openk9.ingestion.dto;

public final class IngestionDtoExamples {

    public static final String INGESTION_SIMPLE_DTO =
            """
            {
              "datasourceId": 12,
              "tenantId": "tenantId",
              "contentId": "12fs4e3cds3ew32",
              "parsingDate": 1743007437740,
              "rawContent": "string",
              "datasourcePayload": {
                "additionalProp1": "string",
                "additionalProp2": "string",
                "additionalProp3": "string"
              },
              "scheduleId": "bf988b9c-92a2-4a44-9558-5ebeb94c790e"
            }""";
    public static final String INGESTION_WITH_RESOURCES_DTO =
            """
            {
               "datasourceId": 12,
               "tenantId": "tenantId",
               "contentId": "12fs4e3cds3ew32",
               "parsingDate": 1743007437740,
               "rawContent": "string",
               "datasourcePayload": {
                 "additionalProp1": "string",
                 "additionalProp2": "string",
                 "additionalProp3": "string"
               },
               "resources": {
                 "binaries": [
                   {
                     "id": "123",
                     "name": "document.pdf",
                     "contentType": "application/pdf",
                     "data": "ZG9jdW1lbnQucGRm",
                     "resourceId": "string"
                   }
                 ]
               },
               "scheduleId": "0187185e-c370-4959-b3ee-7c1dfbce5e56"
             }""";

    public static final String INGESTION_WITH_ACL_DTO =
            """
            {
                "datasourceId": 12,
                "tenantId": "tenantId",
                "contentId": "12fs4e3cds3ew32",
                "parsingDate": 1743007437740,
                "rawContent": "string",
                "datasourcePayload": {
                  "additionalProp1": "string",
                  "additionalProp2": "string",
                  "additionalProp3": "string"
                },
                "acl": {
                  "rolesName": [
                    "admin", "viewer", "guest"
                  ]
                },
                "scheduleId": "string"
              }""";
    public static final String INGESTION_RESPONSE =
            """
                {}""";
}
