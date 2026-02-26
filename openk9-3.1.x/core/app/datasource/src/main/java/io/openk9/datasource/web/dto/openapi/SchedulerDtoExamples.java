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


package io.openk9.datasource.web.dto.openapi;

public class SchedulerDtoExamples {

    public static final String GET_DELETED_CONTENT_IDS_RESPONSE =
            """
            ["121321321, "432342343"]""";
    public static final String STATUS_RESPONSE =
            """
            {
                "datasources": [
                    {
                        "id": 209,
                        "name": "test",
                        "status": "IDLE"
                    },
                    {
                        "id": 237,
                        "name": "test2",
                        "status": "ERROR"
                    }
                ],
                "total": 1,
                "errors": 1
             }""";
    public static final String TRIGGER_REQUEST =
            """
            {
                "datasourceId": 209,
                "reindex": false,
                "startIngestionDate": "2025-07-28T12:47:00+02:00"
              }""";
    public static final String TRIGGER_RESPONSE =
            """
            {
                "id":209,
                "oldSchedulerType":"NO_RUNNING_SCHEDULER",
                "triggerStatus":"STARTED"
            }""";
}
