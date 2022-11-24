#!/bin/bash

check_changes() {
    for dir in $1
    do
        for change in $2
        do
            [[ "$dir" == *$(dirname $change) ]] && echo "true" && break
        done
    done
}

git_changes="$(git diff-tree --no-commit-id --name-only -r "$(git log --format="%H" -n 1)")"

for project_name in $OPENK9_PROJECT_NAMES
do
  project_dirs="$(cd core ; ./mvnw -q --also-make exec:exec -Dexec.executable="pwd" -pl $project_name)"
  check_changes $project_dirs $git_changes && export $project_name="true"
done