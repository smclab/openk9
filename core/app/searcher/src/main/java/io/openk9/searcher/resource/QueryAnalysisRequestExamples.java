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

public class QueryAnalysisRequestExamples {

    public static final String SIMPLE_QUERY_ANALYSIS_REQUEST =
            """
            {
               "searchText": "acco",
               "tokens": []
             }
            """;
    public static final String QUERY_ANALYSIS_REQUEST_WITH_TOKENS =
            """
            {
               "searchText": "acco",
               "tokens": []
             }
            """;
    public static final String QUERY_ANALYSIS_RESPONSE =
            """
            {
                 "searchText": "acco",
                 "analysis": [
                     {
                         "text": "acco",
                         "start": 0,
                         "end": 4,
                         "tokens": [
                             {
                                 "score": 0.1,
                                 "label": "Topic",
                                 "tokenType": "TEXT",
                                 "value": "Accordi tavoli"
                             },
                             {
                                 "score": 0.1,
                                 "label": "Topic",
                                 "tokenType": "TEXT",
                                 "value": "Accordo Consob - Garante privacy"
                             },
                             {
                                 "score": 0.1,
                                 "extra": {
                                     "globalQueryType": "MUST",
                                     "boost": "50",
                                     "valuesQueryType": "MUST"
                                 },
                                 "label": "Topic",
                                 "tokenType": "TEXT",
                                 "value": "Accordo Consob PCAOB"
                             }
                         ],
                         "pos": [
                             0
                         ]
                     }
                 ]
             }""";

}

