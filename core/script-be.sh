#!/bin/bash

# Path to your list of file managers
LIST_FILE="list.txt"

# Check if the list file exists
if [[ ! -f "$LIST_FILE" ]]; then
    echo "-!- Error: $LIST_FILE not found! Please create it first."
    exit 1
fi

# Loop through each line in the list
while IFS= read -r NAME || [[ -n "$NAME" ]]; do
    # Clean up any carriage returns (common if file was saved on Windows)
    NAME=$(echo "$NAME" | tr -d '\r')
    
    # Skip empty lines
    [[ -z "$NAME" ]] && continue

    echo ">>> Executing commands for: $NAME"
    
    # --- PASTE YOUR THREE COMMANDS BELOW ---
    
    # Command 1 (Example: sudo apt update)
    mvn package $MAVEN_OPTS -pl app/$NAME -am -Dmaven.test.skip=true -Dquarkus.container-image.build=true -Dquarkus.container-image.push=false
    
    # Command 2 (Example: sudo apt install "$NAME")
    docker tag openk9/openk9-$NAME:3.0.2-SNAPSHOT registry.smc.it:49083/openk9/openk9-$NAME:3.0.2-SNAPSHOT
    
    # Command 3 (Example: "$NAME" --version)
    docker push registry.smc.it:49083/openk9/openk9-$NAME:3.0.2-SNAPSHOT

    echo ">>> Finished $NAME"
    echo "--------------------------------------"

done < "$LIST_FILE"

echo "All tasks complete!"
