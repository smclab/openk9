#!/bin/bash

function main {

	if [ -d ${KARAF_MOUNT_DIR}/deploy ]
	then
		if [[ $(ls -A /opt/apache-karaf/deploy) ]]
		then
			cp /opt/apache-karaf/deploy/* ${KARAF_MOUNT_DIR}/deploy
		fi

		rm -fr /opt/apache-karaf/deploy

		ln -s ${KARAF_MOUNT_DIR}/deploy /opt/apache-karaf/deploy

		echo "[KARAF] The directory /mnt/KARAF/deploy is ready. Copy files to \$(pwd)/xyz123/deploy on the host operating system to deploy modules to ${KARAF_PRODUCT_NAME} at runtime."
	else
		echo "[KARAF] The directory /mnt/KARAF/deploy does not exist. Create the directory \$(pwd)/xyz123/deploy on the host operating system to create the directory ${KARAF_MOUNT_DIR}/deploy on the container. Copy files to \$(pwd)/xyz123/deploy to deploy modules to ${KARAF_PRODUCT_NAME} at runtime."
	fi
}

main