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
Create OpenK9 K8s Client URL
*/}}
{{- define "helper.k8sClientURL" -}}
{{- with .Values.openk9.k8sClient -}}
{{- $port := ( .port | toString ) -}}
{{- printf "http://%s:%s" .host $port }}
{{- end }}
{{- end }}

{{/*
Create Keycloak service URL
*/}}
{{- define "helper.keycloak_svcURL" -}}
{{- with .Values.keycloak -}}
{{- $port := ( .svcport | toString ) -}}
{{- printf "http://%s:%s" .service $port }}
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

