#!/bin/bash

set -euo pipefail

#
# Quickly rebuild changed ComitasBotJ modules and run comitas-core.
# All arguments are passed to comitas-core.
#

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." >/dev/null 2>&1 && pwd)"
cd "$SCRIPT_DIR"

MODULES=(
    comitas-api
    comitas-core
    example-plugin
)

echo "Checking..."

changed_api=false
changed_core=false
changed_example=false

for module in "${MODULES[@]}"; do
    hash_file="./$module/.hash"
    new_hash="./$module/.new_hash"

    # Hash tracked files belonging to this module + its pom.xml.
    {
        git ls-files -z "$module/src" "$module/pom.xml" |
            sort -z |
            xargs -0 sha512sum
    } | sha512sum > "$new_hash"

    if [[ ! -f "$hash_file" ]] || ! cmp -s "$hash_file" "$new_hash"; then
        case "$module" in
            comitas-api)
                changed_api=true
                ;;
            comitas-core)
                changed_core=true
                ;;
            example-plugin)
                changed_example=true
                ;;
        esac
    fi
done

echo "Check done."

# Determine what actually needs rebuilding.
if $changed_api; then
    echo "API changed"
fi

if $changed_core; then
    echo "Core changed"
fi

if $changed_example; then
    echo "Example plugin changed"
fi

# Build everything affected with ONE Maven invocation.
if $changed_api; then
    echo "Building API, core and example plugin..."
    mvn -pl comitas-api,comitas-core,example-plugin -am install -DskipTests

elif $changed_core; then
    echo "Building core..."
    mvn -pl comitas-core -am package -DskipTests

elif $changed_example; then
    echo "Building example plugin..."
    mvn -pl example-plugin -am package -DskipTests
fi

# Only commit hashes after successful builds.
for module in "${MODULES[@]}"; do
    mv "./$module/.new_hash" "./$module/.hash"
done

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

echo Copying updated files...
cp "$JAR" ./comitas-core/run/comitas.jar
cp ./example-plugin/target/example-plugin-1.0.jar ./comitas-core/run/plugins/

cd ./comitas-core/run
exec java -jar comitas.jar "$@"
