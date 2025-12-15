{{/*
Create R2DBC URL for PostgreSQL
*/}}
{{- define "helper.r2dbcURL" -}}
{{- with .Values.database.postgresql -}}
{{- printf "r2dbc:postgresql://%s/%s" .host .database }}
{{- end }}
{{- end }}

{{/*
Create JDBC URL for Liquibase
*/}}
{{- define "helper.liquibaseURL" -}}
{{- with .Values.database.postgresql -}}
{{- printf "jdbc:postgresql://%s/%s" .host .database }}
{{- end }}
{{- end }}

{{/*
Create RabbitMQ Host
*/}}
{{- define "helper.rabbitmqHost" -}}
{{- .Values.rabbitmq.host }}
{{- end }}

{{/*
Create OpenK9 Searcher URL
*/}}
{{- define "helper.searcherURL" -}}
{{- with .Values.openk9.searcher -}}
{{- printf "http://%s:%s" .host .port }}
{{- end }}
{{- end }}

{{/*
Create OpenK9 Datasource URL
*/}}
{{- define "helper.datasourceURL" -}}
{{- with .Values.openk9.datasource -}}
{{- printf "http://%s:%s" .host .port }}
{{- end }}
{{- end }}
