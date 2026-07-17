{{- /*
  Markdown report template for Trivy (Issue #2004).
  Renders one table of vulnerabilities per scanned target. Used with
  `trivy ... --format template --template @.gitlab/trivy-templates/md.tpl`.
  GitLab CE has no Security Dashboard, so this .md is the human-readable
  artifact next to the raw JSON.
*/ -}}
# Trivy report

{{- if . }}
{{- range . }}
{{- if .Vulnerabilities }}

## {{ .Target }}

| CVE | Severity | Package | Installed | Fixed |
|-----|----------|---------|-----------|-------|
{{- range .Vulnerabilities }}
| {{ .VulnerabilityID }} | {{ .Severity }} | {{ .PkgName }} | {{ .InstalledVersion }} | {{ if .FixedVersion }}{{ .FixedVersion }}{{ else }}-{{ end }} |
{{- end }}
{{- else }}

## {{ .Target }}

No vulnerabilities found.
{{- end }}
{{- end }}
{{- else }}

No results.
{{- end }}
