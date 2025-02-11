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
{{- define "helper.datasourceHost" -}}
{{- with .Values.openk9.datasource -}}
{{- printf "%s" .host }}
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