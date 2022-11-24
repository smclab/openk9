#!/bin/bash

check_changes() {
  for dir in $1
  do
      for change in $2
      do
         if [[ "$dir" == *$(dirname $change) ]]
         then
           echo "Changes detected in $dir for $change"
           return 0
         fi
      done
  done
  return 1
}

git_changes="$(git diff-tree --no-commit-id --name-only -r "$(git log --format="%H" -n 1)")"

for project_name in $OPENK9_PROJECT_NAMES
do
  project_dirs="$(cd core ; ./mvnw -q --also-make exec:exec -Dexec.executable="pwd" -pl $project_name)"
  check_changes $project_dirs $git_changes
  if [ $? -eq 0 ]
  then
    echo "Project $project_name has changes"
    case $project_name in
      "io.openk9.app:tenant-manager")
        export OPENK9_TENANT_MANAGER="true"
        ;;

      "io.openk9.app:searcher")
        export OPENK9_SEARCHER="true"
        ;;

      "io.openk9.app:ingestion")
        export OPENK9_INGESTION="true"
        ;;

      "io.openk9.app:datasource")
        export OPENK9_DATASOURCE="true"
        ;;
      "io.openk9.app:entity-manager")
        export OPENK9_ENTITY_MANAGER="true"
        ;;
      "io.openk9.app:file-manager")
        export OPENK9_FILE_MANAGER="true"
        ;;
      "io.openk9.app:resources-validator")
        export OPENK9_RESOURCES_VALIDATOR="true"
        ;;
    esac
  fi
done

exit 0