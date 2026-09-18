#!/bin/bash

set -euo pipefail

#
# Quickly rebuild changed ComitasBotJ modules and run comitas-core.
# All arguments are passed to comitas-core.
#

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." >/dev/null 2>&1 && pwd)"
cd "$SCRIPT_DIR"

mvn -pl comitas-api,comitas-core,example-plugin -am clean install -DskipTests

mvn -pl comitas-core -am clean package -DskipTests

mvn -pl example-plugin -am clean package -DskipTests

JAR=$(find ./comitas-core/target \
    -maxdepth 1 \
    -type f \
    -name 'comitas-core-*.jar' \
    ! -name '*-sources.jar' \
    ! -name '*-javadoc.jar' \
    -print -quit)

if [[ -z "$JAR" ]]; then
    echo "Core JAR not found"
    exit 1
fi

mkdir -p ./comitas-core/run/plugins

cp "$JAR" ./comitas-core/run/comitas.jar
cp ./example-plugin/target/example-plugin-1.0.jar ./comitas-core/run/plugins/

cd ./comitas-core/run
exec java -jar comitas.jar "$@"