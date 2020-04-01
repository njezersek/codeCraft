#!/bin/bash
mvn package -f "/home/coronacraft/spigot_plugin/codeCraft/pom.xml"
#docker stop mc2
mv /home/coronacraft/spigot_plugin/codeCraft/target/codeCraft-1.0-SNAPSHOT.jar /home/coronacraft/mc_test/plugins/codeCraft-1.0-SNAPSHOT.jar
#docker start mc2
echo "---------------------------------------------------"
echo "                RELOADING SERVER"
echo "---------------------------------------------------"

docker exec mc2 rcon-cli reload

echo "Plugin instaled!"