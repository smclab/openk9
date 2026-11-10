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

{{/*
Create OpenK9 Rag URL
*/}}
{{- define "helper.ragURL" -}}
{{- with .Values.openk9.rag -}}
{{- printf "http://%s:%s" .host .port }}
{{- end }}
{{- end }}

{{/*
Create OpenK9 Ingestion URL
*/}}
{{- define "helper.ingestionURL" -}}
{{- with .Values.openk9.ingestion -}}
{{- printf "http://%s:%s" .host .port }}
{{- end }}
{{- end }}

{{/*
extraCAs: command of the init container. It copies the default JVM trust store to
/truststore and imports every PEM certificate of the files in /extra-cas, one by one
because keytool imports only the first certificate of a file.
*/}}
{{- define "helper.extraCAs.command" -}}
- /bin/sh
- -ec
- |
  JAVA_HOME="${JAVA_HOME:-$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")}"
  cp "$JAVA_HOME/lib/security/cacerts" /truststore/cacerts
  chmod u+w /truststore/cacerts
  n=0
  for f in /extra-cas/*; do
    inside=0
    while IFS= read -r line || [ -n "$line" ]; do
      case "$line" in *"-BEGIN CERTIFICATE-"*) inside=1; : > /truststore/extra-ca.pem ;; esac
      if [ "$inside" = 1 ]; then printf '%s\n' "$line" >> /truststore/extra-ca.pem; fi
      case "$line" in *"-END CERTIFICATE-"*)
        if [ "$inside" = 1 ]; then
          n=$((n + 1))
          keytool -importcert -noprompt -keystore /truststore/cacerts -storepass changeit -alias "extra-ca-$n" -file /truststore/extra-ca.pem
        fi
        inside=0 ;;
      esac
    done < "$f"
  done
  rm -f /truststore/extra-ca.pem
  [ "$n" -gt 0 ] || { echo "extraCAs: no PEM certificate found in /extra-cas" >&2; exit 1; }
  echo "extraCAs: $n certificate(s) added to the JVM trust store"
{{- end }}

{{/*
extraCAs: volumes with the certificates (existing Secret or the one rendered from
extraCAs.pem) and with the trust store built by the init container.
*/}}
{{- define "helper.extraCAs.volumes" -}}
{{- if eq (empty .Values.extraCAs.existingSecret) (empty .Values.extraCAs.pem) }}
{{- fail "extraCAs.enabled requires exactly one of extraCAs.existingSecret and extraCAs.pem" }}
{{- end }}
- name: extra-cas
  secret:
    secretName: {{ .Values.extraCAs.existingSecret | default (printf "%s-extra-cas" (include "common.names.name" .)) }}
- name: extra-cas-truststore
  emptyDir: {}
{{- end }}

{{/*
extraCAs: JVM options that point to the trust store built by the init container.
changeit is the default JDK trust store password: the file holds only public certificates.
*/}}
{{- define "helper.extraCAs.javaToolOptions" -}}
-Djavax.net.ssl.trustStore=/truststore/cacerts -Djavax.net.ssl.trustStorePassword=changeit
{{- end }}