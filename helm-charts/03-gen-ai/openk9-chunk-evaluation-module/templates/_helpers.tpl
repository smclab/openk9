{{/*
Create RabbitMQ URL
*/}}
{{- define "helper.rabbitmqURL" -}}
{{- with .Values.rabbitmq -}}
{{- $port := ( .port | toString ) -}}
{{- printf "amqp://%s:%s@%s:%s/" .user .password .host $port }}
{{- end -}}
{{- end }}
