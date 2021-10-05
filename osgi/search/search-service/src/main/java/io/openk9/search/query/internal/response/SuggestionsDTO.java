package io.openk9.search.query.internal.response;


/*
{
    "entities": [
        {
            "name": <NOME_ENTITA>,
            "id": <IDENTIFICATIVO_ENTITA>,
            "type" <TIPOLOGIA_ENTITA>,
            ...<LISTA_CHIAVI_VALORE_DELLA_SPECIFICA_ENTITA>
        }
    ],
    "datasources": [
        {
            "name": <NOME_DATASOURCE>,
            "active": <true|false>,
            documentTypes: [
                {
                    name: <NAME>,
                    icon: <URL_ICON>
                }
            ]
        }
    ],
    "types": [
        "<LISTA_TYPES>": {
            "<FIELD>": [
                <VALORI_FIELD>
            ]
        }
    ]
}
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SuggestionsDTO {
	private List<Map<String, Object>> entities;
	private List<Map<String, Object>> datasources;
	private List<Map<String, Object>> types;
}
