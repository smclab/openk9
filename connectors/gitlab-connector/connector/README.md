# Gitlab Connector

Gitlab connector is a service for extracting data from specific domains.\
Run container from built image and configure appropriate plugin to call it.

The container takes via environment variable INGESTION_URL, which must match the url of the Ingestion Api.

## Gitlab Api

Since issue #2125 extraction is split across two dedicated endpoints, each exposed
through its own FastAPI `APIRouter`:

- **`/users`** — extract Gitlab users
- **`/projects`** — extract Gitlab projects and their related items (issues,
  commits, branches, labels, milestones, merge requests)

Each endpoint exposes `/execute`, `/form`, `/sample` and `/health` under its own
prefix (e.g. `/users/execute`, `/projects/form`). In the OpenK9 admin UI two
connectors are created, one per endpoint, using the base urls:

- `http://docker-host-name:5000/users`
- `http://docker-host-name:5000/projects`

Splitting the endpoints allows setting Gitlab REST API filters specific to users
and projects, for a more detailed extraction.

The legacy single `/execute` endpoint (see [Legacy endpoint](#legacy-endpoint)) is
kept for backward compatibility with existing installations.

### Common request fields

Both `/users/execute` and `/projects/execute` share these fields in the JSON raw body:

- **domain**: Gitlab domain to extract from (required)
- **accessToken**: access token connecting to Gitlab domain (required)
- **itemsPerPage**: pagination items extracted per call (optional, default 100)
- **datasourceId**: id of datasource (provided by OpenK9)
- **tenantId**: id of tenant (provided by OpenK9)
- **scheduleId**: id of schedulation (provided by OpenK9)
- **timestamp**: timestamp to check data to be extracted (provided by OpenK9)

Boolean filters accept the string values `"True"`, `"False"` or `"NoFilter"`
(the default). `"NoFilter"` omits the filter from the Gitlab API call.

### Users endpoint

`POST /users/execute` — starts the extraction of Gitlab users.

In addition to the common fields it accepts the following filters (all optional,
default `"NoFilter"`), mapped to the [Gitlab Users API](https://docs.gitlab.com/api/users):

- **filterActive**: only active users
- **filterExternal**: only external users
- **filterBlocked**: only blocked users
- **filterHuman**: only regular users that are not bot or internal users
- **excludeActive**: only non active users
- **excludeExternal**: only non external users
- **excludeHumans**: only bot or internal users
- **excludeInternal**: only non internal users

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5000/users/execute' \
--header 'Content-Type: application/json' \
--data-raw '{
    "domain": "https://git.smc.it",
    "accessToken": "123abc",
    "itemsPerPage": 20,
    "filterActive": "True",
    "datasourceId": 1,
    "tenantId": "1",
    "scheduleId": "1",
    "timestamp": 0
}'
```

### Projects endpoint

`POST /projects/execute` — starts the extraction of Gitlab projects and, for each
project, the related items selected through the `doExtract*` flags.

In addition to the common fields it accepts:

- **projectList**: list of project ids to be extracted (optional, if not
  specified every accessible project is extracted)

Project list filters (mapped to the [Gitlab Projects API](https://docs.gitlab.com/api/projects)):

- **archived**: limit by archived status
- **membership**: limit by projects that the current user is a member of
- **owned**: limit by projects explicitly owned by the current user
- **starred**: limit by projects starred by the current user
- **withIssuesEnabled**: limit by enabled issues feature
- **withMergeRequestsEnabled**: limit by enabled merge requests feature
- **active**: limit by projects that are not archived and not marked for deletion
- **minAccessLevel**: minimum access level. One of `NoFilter`, `MinimalAccess`,
  `Guest`, `Planner`, `Reporter`, `SecurityManager`, `Developer`, `Maintainer`,
  `Owner`
- **visibility**: one of `NoFilter`, `public`, `internal`, `private`

Related items are enabled by boolean `doExtract*` flags (default `true`), each with
its own filters:

- **doExtractIssues** — [Issues API](https://docs.gitlab.com/api/issues/): `issuesConfidential`,
  `issueDueDate`, `issueType`, `issueScope`, `issueState`
- **doExtractCommits** — [Commits API](https://docs.gitlab.com/api/commits/):
  `commitFirstParent`, `commitWithStats`
- **doExtractBranches** — [Branches API](https://docs.gitlab.com/api/branches/)
- **doExtractLabels** — [Labels API](https://docs.gitlab.com/api/labels/):
  `labelWithCount`, `labelIncludeAncestorGroups`, `labelArchivedOnly`
- **doExtractMilestones** — [Milestones API](https://docs.gitlab.com/api/milestones/):
  `milestoneState`, `milestoneIncludeAncestors`
- **doExtractMergeRequests** — [Merge Requests API](https://docs.gitlab.com/api/merge_requests/):
  `mergeRequestEnvironment`, `mergeRequestScope`, `mergeRequestState`, `mergeRequestDraft`

The full field structure, with allowed values and defaults, is served by the
`/projects/form` endpoint.

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5000/projects/execute' \
--header 'Content-Type: application/json' \
--data-raw '{
    "domain": "https://git.smc.it",
    "accessToken": "123abc",
    "itemsPerPage": 20,
    "projectList": [1, 2],
    "doExtractIssues": true,
    "doExtractCommits": true,
    "doExtractBranches": false,
    "doExtractLabels": false,
    "doExtractMilestones": false,
    "doExtractMergeRequests": true,
    "datasourceId": 1,
    "tenantId": "1",
    "scheduleId": "1",
    "timestamp": 0
}'
```

### Form and sample endpoints

Each endpoint exposes its own form structure and result sample:

- `GET /users/form`, `GET /users/sample`
- `GET /projects/form`, `GET /projects/sample`

### Health check endpoint

Each endpoint exposes a health check used for container orchestration:

- `GET /users/health`
- `GET /projects/health`
- `GET /health` (legacy)

Follows an example of Curl call:

```
curl --location --request GET 'http://localhost:5000/users/health'
```

### Legacy endpoint

The original single `/execute` endpoint is kept for backward compatibility. It
takes a `types` list to select which data to extract in one call:

- **types**: list of data to extract (`User`, `Project`, `Project Issue`,
  `Project Commit`, `Project Branch`, `Project Labels`, `Project Milestone`,
  `Project Merge Request`)
- plus the common request fields and `projectList`

For existing installations this configuration is left in place until the new
`/users` and `/projects` endpoints are verified.

Follows an example of Curl call:

```
curl --location --request POST 'http://localhost:5000/execute' \
--header 'Content-Type: application/json' \
--data-raw '{
    "domain": "https://git.smc.it",
    "accessToken": "123abc",
    "types": ["User", "Project", "Project Issue", "Project Commit"],
    "itemsPerPage": 20,
    "datasourceId": 1,
    "tenantId": "1",
    "scheduleId": "1",
    "timestamp": 0
}'
```

# Quickstart

## How to run

## Docker

### Using Dockerfile

Build the Docker file:
```
docker build -t gitlab-parser .
```

**Command parameters**:
- **-t**: Set built image name
- **-f**: Specify the path to the Dockerfile**

Run the built Docker image:
```
docker run -p 5000:5000 --name gitlab-parser gitlab-parser 
```

**Command parameters**:
- **-p**: Exposed port to make api calls
- **-name**: Set docker container name

## Kubernetes/Openshift

To run Gitlab Connector in Kubernetes/Openshift Helm Chart is available under [chart folder](../chart).

# Docs and resources

To read more go on [official site connector section](https://staging-site.openk9.io/plugins/)

# Migration Guides

#### TO-DO: Add wiki links
