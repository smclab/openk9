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

package io.openk9.searcher.resource;

public class SearchRequestExamples {

    public static final String AUTOCORRECTION_SEARCH_REQUEST =
            """
                {
                \t"range": [
                \t\t0,
                \t\t10
                \t],
                \t"language": "it_IT",
                \t"searchQuery": [
                \t\t{
                \t\t\t"tokenType": "TEXT",
                \t\t\t"values": [
                \t\t\t\t"smc"
                \t\t\t],
                \t\t\t"filter": false,
                \t\t\t"search": true
                \t\t}
                \t],
                \t"sortAfterKey": ""
                }""";
    public static final String TEXT_SEARCH_REQUEST =
            """
                {
                \t"range": [
                \t\t0,
                \t\t10
                \t],
                \t"language": "it_IT",
                \t"searchQuery": [
                \t\t{
                \t\t\t"tokenType": "TEXT",
                \t\t\t"values": [
                \t\t\t\t"smc"
                \t\t\t],
                \t\t\t"filter": false,
                \t\t\t"search": true
                \t\t}
                \t],
                \t"sortAfterKey": ""
                }""";
    public static final String KNN_SEARCH_REQUEST =
            """
                {
                \t"range": [
                \t\t0,
                \t\t10
                \t],
                \t"language": "it_IT",
                \t"searchQuery": [
                \t\t{
                \t\t\t"tokenType": "KNN",
                \t\t\t"values": [
                \t\t\t\t"smc"
                \t\t\t],
                \t\t\t"filter": false,
                \t\t\t"search": true
                \t\t}
                \t],
                \t"sortAfterKey": ""
                }""";
    public static final String HYBRID_SEARCH_REQUEST =
            """
                {
                \t"range": [
                \t\t0,
                \t\t10
                \t],
                \t"language": "it_IT",
                \t"searchQuery": [
                \t\t{
                \t\t\t"tokenType": "HYBRID",
                \t\t\t"values": [
                \t\t\t\t"smc"
                \t\t\t],
                \t\t\t"filter": false,
                \t\t\t"search": true
                \t\t}
                \t],
                \t"sortAfterKey": ""
                }""";

    public static final String SEARCH_RESPONSE =
            """
                {
                \t"result": [
                \t\t{
                \t\t\t"highlight": {},
                \t\t\t"score": 0.0,
                \t\t\t"source": {
                \t\t\t\t"last": false,
                \t\t\t\t"web": {
                \t\t\t\t\t"favicon": "https://www.smc.it/o/smc-theme/images/favicon.ico",
                \t\t\t\t\t"title": "Il prossimo 6 ottobre saremo protagonisti al Liferay Vision 2022",
                \t\t\t\t\t"url": "https://www.smc.it/-/il-prossimo-6-ottobre-saremo-protagonisti-al-liferay-vision-2022",
                \t\t\t\t\t"content": "Content content content content content content content content "
                \t\t\t\t},
                \t\t\t\t"indexName": "hitmontop-2761-data-3a1558a2-45f1-4872-89d0-811b992d63c6",
                \t\t\t\t"documentTypes": [
                \t\t\t\t\t"web"
                \t\t\t\t],
                \t\t\t\t"contentId": "6450297360913884",
                \t\t\t\t"datasourceId": 2761,
                \t\t\t\t"tenantId": "hitmontop",
                \t\t\t\t"oldIndexName": "hitmontop-2761-data-d16661f8-f36e-470e-a632-6fc3be7de983",
                \t\t\t\t"ingestionId": "59700f29-5b7e-461f-9509-0cd8f9640a83",
                \t\t\t\t"acl": {
                \t\t\t\t\t"public": true
                \t\t\t\t},
                \t\t\t\t"id": "2pdCjpQBIQNzsDHHSGJ1",
                \t\t\t\t"scheduleId": "3a1558a2-45f1-4872-89d0-811b992d63c6",
                \t\t\t\t"parsingDate": 1737555901056,
                \t\t\t\t"rawContent": "Content content content content content content content content"
                \t\t\t}
                \t\t},
                \t\t{
                \t\t\t"highlight": {},
                \t\t\t"score": 0.0,
                \t\t\t"source": {
                \t\t\t\t"last": false,
                \t\t\t\t"web": {
                \t\t\t\t\t"favicon": "https://www.smc.it/o/smc-theme/images/favicon.ico",
                \t\t\t\t\t"title": "DGS, portfolio company di H.I.G. Capital, acquisisce SMC",
                \t\t\t\t\t"url": "https://www.smc.it/-/dgs-portfolio-company-di-h-i-g-capital-acquisisce-smc",
                \t\t\t\t\t"content": "Content content content content content content content content"
                \t\t\t\t},
                \t\t\t\t"indexName": "hitmontop-2761-data-3a1558a2-45f1-4872-89d0-811b992d63c6",
                \t\t\t\t"documentTypes": [
                \t\t\t\t\t"web"
                \t\t\t\t],
                \t\t\t\t"contentId": "4570164635913900",
                \t\t\t\t"datasourceId": 2761,
                \t\t\t\t"tenantId": "hitmontop",
                \t\t\t\t"oldIndexName": "hitmontop-2761-data-d16661f8-f36e-470e-a632-6fc3be7de983",
                \t\t\t\t"ingestionId": "777325a7-4147-4518-91d2-7896c9cc66d1",
                \t\t\t\t"acl": {
                \t\t\t\t\t"public": true
                \t\t\t\t},
                \t\t\t\t"id": "5pdCjpQBIQNzsDHHWWL1",
                \t\t\t\t"scheduleId": "3a1558a2-45f1-4872-89d0-811b992d63c6",
                \t\t\t\t"parsingDate": 1737555901056,
                \t\t\t\t"rawContent": "Content content content content content content content content"
                \t\t\t}
                \t\t},
                \t\t{
                \t\t\t"highlight": {},
                \t\t\t"score": 0.0,
                \t\t\t"source": {
                \t\t\t\t"last": false,
                \t\t\t\t"web": {
                \t\t\t\t\t"favicon": "https://www.smc.it/o/smc-theme/images/favicon.ico",
                \t\t\t\t\t"title": "Homepage | SMC DGS Company",
                \t\t\t\t\t"url": "https://www.smc.it/homepage",
                \t\t\t\t\t"content": "content content content content"
                \t\t\t\t},
                \t\t\t\t"indexName": "hitmontop-2761-data-3a1558a2-45f1-4872-89d0-811b992d63c6",
                \t\t\t\t"documentTypes": [
                \t\t\t\t\t"web"
                \t\t\t\t],
                \t\t\t\t"contentId": "5128501921869583",
                \t\t\t\t"datasourceId": 2761,
                \t\t\t\t"tenantId": "hitmontop",
                \t\t\t\t"oldIndexName": "hitmontop-2761-data-d16661f8-f36e-470e-a632-6fc3be7de983",
                \t\t\t\t"ingestionId": "ec67ae53-a523-4b4f-ad28-0c97317a0531",
                \t\t\t\t"acl": {
                \t\t\t\t\t"public": true
                \t\t\t\t},
                \t\t\t\t"id": "DJdCjpQBIQNzsDHHqGPZ",
                \t\t\t\t"scheduleId": "3a1558a2-45f1-4872-89d0-811b992d63c6",
                \t\t\t\t"parsingDate": 1737555901056,
                \t\t\t\t"rawContent": "content content content content"
                \t\t\t}
                \t\t}
                \t],
                \t"total": 3
                }""";
    public static final String SEARCH_REQUEST_FOR_SUGGESTIONS =
            """
            {
                "searchQuery": [
                  {
                    "tokenType": "TEXT",
                    "values": [
                      "smc"
                    ],
                    "filter": false,
                    "search": true
                  }
                ],
                "range": [
                  0,
                  9
                ],
                "suggestionCategoryId": 364,
                "suggestKeyword": "",
                "order": "asc",
                "language": "it_IT"
              }""";
    public static final String SEARCH_REQUEST_FOR_SUGGESTIONS_WITH_PREFIX_FILTER =
            """
            {
              "searchQuery": [
              {
                    "tokenType": "TEXT",
                    "values": [
                      "smc"
                    ],
                    "filter": false,
                    "search": true
                  }
              ],
              "range": [
                0,
                20
              ],
              "suggestionCategoryId": 364,
              "suggestKeyword": "poli",
              "order": "desc",
              "language": "it_IT"
            }""";
    public static final String SUGGESTIONS_RESPONSE =
            """
            {
                 "result": [
                     {
                         "tokenType": "FILTER",
                         "value": " Attività commerciale",
                         "suggestionCategoryId": 364,
                         "count": 46,
                         "keywordKey": "news.topic.i18n.it_IT.keyword"
                     },
                     {
                         "tokenType": "FILTER",
                         "value": " benessere",
                         "suggestionCategoryId": 364,
                         "count": 13,
                         "keywordKey": "news.topic.i18n.it_IT.keyword"
                     },
                     {
                         "tokenType": "FILTER",
                         "value": " divertimento",
                         "suggestionCategoryId": 364,
                         "count": 2,
                         "keywordKey": "news.topic.i18n.it_IT.keyword"
                     },
                     {
                         "tokenType": "FILTER",
                         "value": " politica",
                         "suggestionCategoryId": 364,
                         "count": 6,
                         "keywordKey": "news.topic.i18n.it_IT.keyword"
                     },
                     {
                         "tokenType": "FILTER",
                         "value": " religione",
                         "suggestionCategoryId": 364,
                         "count": 1,
                         "keywordKey": "news.topic.i18n.it_IT.keyword"
                     },
                     {
                         "tokenType": "FILTER",
                         "value": " scienza",
                         "suggestionCategoryId": 364,
                         "count": 4,
                         "keywordKey": "news.topic.i18n.it_IT.keyword"
                     },
                     {
                         "tokenType": "FILTER",
                         "value": " sport",
                         "suggestionCategoryId": 364,
                         "count": 9,
                         "keywordKey": "news.topic.i18n.it_IT.keyword"
                     },
                     {
                         "tokenType": "FILTER",
                         "value": " stile e bellezza",
                         "suggestionCategoryId": 364,
                         "count": 4,
                         "keywordKey": "news.topic.i18n.it_IT.keyword"
                     }
                 ],
                 "afterKey": "eyJuZXdzLnRvcGljLmkxOG4uaXRfSVQua2V5d29yZCI6IiBzdGlsZSBlIGJlbGxlenphIn0="
             }""";
    public static final String AUTOCORRECTION_QUERY_RESPONSE =
        """
            {
                "size": 0,
                "suggest": {
                  "autocorrection_suggestion": {
                    "term": {
                      "field": "web.title",
                      "size": 1,
                      "max_edits": 1,
                      "min_word_length": 4,
                      "prefix_length": 4,
                      "sort": "score",
                      "suggest_mode": "popular"
                    }
                  },
                  "text": "storria lifera"
                }
              }""";
}

