#!/bin/bash

git_changes="$(git diff-tree --no-commit-id --name-only -r "$(git log --format="%H" -n 1)")"

echo "Checking for changes in the following files:"
echo "$git_changes"

check_changes() {
  for dir in $@
  do
      for change in $git_changes
      do
        changeDirName=$(dirname $change)
        if [[ $dir == *$changeDirName* ]]
        then
          echo "Changes found in $dir"
          return 0
        fi
      done
  done
  return 1
}

for project_name in $OPENK9_PROJECT_NAMES
do
  project_dirs="$(cd core ; ./mvnw -q --also-make exec:exec -Dexec.executable="pwd" -pl $project_name)"
  if check_changes $project_dirs ; then
    echo "Project $project_name has changes"
    (cd core ; ./mvnw package --batch-mode -pl $project_name -am -Dquarkus.container-image.build=true -Dquarkus.container-image.push=true -Dquarkus.container-image.tag=$OPENK9_CONTAINER_IMAGE_TAG)
  else
    echo "Project $project_name has no changes"
  fi
done

exit 0