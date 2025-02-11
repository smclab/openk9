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
{{- printf "https://%s" .host }}
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
Create OpenK9 Ingestion URL
*/}}
{{- define "helper.ingestionURL" -}}
{{- with .Values.openk9.ingestion -}}
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


{{/*
Create OpenK9 Tenant Manager URL
*/}}
{{- define "helper.tenantManagerHost" -}}
{{- with .Values.openk9.tenantManager -}}
{{- printf "%s" .host }}
{{- end }}
{{- end }}