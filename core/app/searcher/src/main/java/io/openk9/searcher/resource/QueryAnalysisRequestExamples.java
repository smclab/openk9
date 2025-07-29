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

