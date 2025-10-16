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

public class PluginDriverDtoExamples {

    public static final String TEMPLATES_RESPONSE =
            """
            [
                {
                    "name": "test",
                    "id": 751
                }
             ]""";
    public static final String TABS_RESPONSE =
            """
            [
                {
                    "label": "All",
                    "tokens": [],
                    "sortings": [],
                    "translationMap": null
                },
                {
                    "label": "web",
                    "tokens": [
                        {
                            "tokenType": "DOCTYPE",
                            "keywordKey": "documentTypes.keyword",
                            "filter": true,
                            "values": [
                                "web"
                            ],
                            "extra": null
                        }
                    ],
                    "sortings": [],
                    "translationMap": null
                }
             ]""";
    public static final String SUGGESTION_CATEGORIES_RESPONSE =
            """
            [
                {
                    "translationMap": null,
                    "id": 752,
                    "createDate": "2025-08-04T10:49:05.81171Z",
                    "modifiedDate": "2025-08-04T10:49:05.832618Z",
                    "name": "test",
                    "description": "",
                    "priority": 0.0,
                    "multiSelect": false
                }
             ]""";
    public static final String SORTABLE_DOCTYPE_FIELDS_RESPONSE =
            """
            [
                {
                    "field": "web.title",
                    "id": 221,
                    "label": "web.title",
                    "translationMap": null
                }
            ]""";
    public static final String SORTING_RESPONSE =
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
    public static final String DEFAULT_LANGUAGE_RESPONSE =
            """
            {
                "id": 7,
                "createDate": "2025-07-28T12:13:08.898766Z",
                "modifiedDate": "2025-07-28T12:13:08.898778Z",
                "name": "English",
                "value": "en_US"
             }""";
    public static final String AVAILABLE_LANGUAGES_RESPONSE =
            """
            [
                {
                    "id": 3,
                    "createDate": "2025-07-28T12:13:08.884263Z",
                    "modifiedDate": "2025-07-28T12:13:08.884309Z",
                    "name": "German",
                    "value": "de_DE"
                },
                {
                    "id": 7,
                    "createDate": "2025-07-28T12:13:08.898766Z",
                    "modifiedDate": "2025-07-28T12:13:08.898778Z",
                    "name": "English",
                    "value": "en_US"
                },
                {
                    "id": 8,
                    "createDate": "2025-07-28T12:13:08.90197Z",
                    "modifiedDate": "2025-07-28T12:13:08.901989Z",
                    "name": "French",
                    "value": "fr_FR"
                },
                {
                    "id": 9,
                    "createDate": "2025-07-28T12:13:08.913523Z",
                    "modifiedDate": "2025-07-28T12:13:08.913536Z",
                    "name": "Italian",
                    "value": "it_IT"
                },
                {
                    "id": 12,
                    "createDate": "2025-07-28T12:13:08.940398Z",
                    "modifiedDate": "2025-07-28T12:13:08.940428Z",
                    "name": "Spanish",
                    "value": "es_ES"
                }
             ]""";
    public static final String CURRENT_BUCKET_RESPONSE =
            """
            {
                "refreshOnSuggestionCategory": false,
                "refreshOnTab": false,
                "refreshOnDate": false,
                "refreshOnQuery": false,
                "retrieveType": "HYBRID"
             }""";
    public static final String HEALTH_STATUS =
        """
		{
			"status":"UP"
		}""";
}
