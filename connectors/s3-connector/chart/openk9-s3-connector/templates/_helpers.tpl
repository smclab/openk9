{{/*
Create PostgreSQL Reactive URL
*/}}
{{- define "helper.postgresReactiveURL" -}}
{{- with .Values.postgresql -}}
{{- $port := ( .port | toString ) -}}
{{- printf "postgresql://%s:%s/%s" .host $port .database }}
{{- end -}}
{{- end }}

{{/*
Create Keycloak URL
*/}}
{{- define "helper.keycloakURL" -}}
{{- with .Values.keycloak -}}
{{- $port := ( .port | toString ) -}}
{{- printf "http://%s:%s" .host $port }}
{{- end }}
{{- end }}
         
{{/*
Create OpenK9 Datasource URL
*/}}
{{- define "helper.datasourceURL" -}}
{{- with .Values.openk9.datasource -}}
{{- $port := ( .port | toString ) -}}
{{- printf "http://%s:%s" .host $port }}
{{- end }}
{{- end }}

{{/*
Create OpenK9 Index Writer URL
*/}}
{{- define "helper.indexWriterURL" -}}
{{- with .Values.openk9.indexWriter -}}
{{- $port := ( .port | toString ) -}}
{{- printf "http://%s:%s" .host $port }}
{{- end }}
{{- end }}

{{/*
Create OpenK9 Ingestion URL
*/}}
{{- define "helper.ingestionURL" -}}
{{- with .Values.openk9.ingestion -}}
{{- $port := ( .port | toString ) -}}
{{- printf "http://%s:%s" .host $port }}
{{- end }}
{{- end }}

{{/*
Create OpenK9 Plugin Driver Manager URL
*/}}
{{- define "helper.pluginDriverURL" -}}
{{- with .Values.openk9.pluginDriverManager -}}
{{- $port := ( .port | toString ) -}}
{{- printf "http://%s:%s" .host $port }}
{{- end }}
{{- end }}

{{/*
Create OpenK9 Searcher URL
*/}}
{{- define "helper.searcherURL" -}}
{{- with .Values.openk9.searcher -}}
{{- $port := ( .port | toString ) -}}
{{- printf "http://%s:%s" .host $port }}
{{- end }}
{{- end }}

