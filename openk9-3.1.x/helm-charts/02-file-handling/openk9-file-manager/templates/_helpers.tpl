{{/*
Create OpenK9 Datasource URL
*/}}
{{- define "helper.datasourceHost" -}}
{{- with .Values.openk9.datasource -}}
{{- printf "%s" .host }}
{{- end }}
{{- end }}