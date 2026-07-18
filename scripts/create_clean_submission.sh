#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT="${1:-$ROOT/BluePathApp-clean.zip}"
STAGING="$(mktemp -d)"
trap 'rm -rf "$STAGING"' EXIT
DEST="$STAGING/BluePathApp"
mkdir -p "$DEST/app" "$DEST/backend" "$DEST/gradle"

copy_path() {
  local relative="$1"
  if [[ -e "$ROOT/$relative" ]]; then
    mkdir -p "$DEST/$(dirname "$relative")"
    rsync -a --exclude '__pycache__' --exclude '*.pyc' --exclude '.DS_Store' \
      "$ROOT/$relative" "$DEST/$(dirname "$relative")/"
  fi
}

for path in \
  app/src app/build.gradle \
  backend/app backend/data backend/migrations backend/static backend/tests \
  backend/.env.example backend/Dockerfile backend/__init__.py backend/requirements.txt backend/schema.sql \
  docs finetuning scripts gradle/wrapper \
  .gitignore README.md build.gradle settings.gradle gradle.properties.example \
  gradlew gradlew.bat docker-compose.yml; do
  copy_path "$path"
done

rm -f "$OUTPUT"
(
  cd "$STAGING"
  zip -qr "$OUTPUT" BluePathApp \
    -x '*/.DS_Store' '*/__MACOSX/*' '*/__pycache__/*' '*/.pytest_cache/*' \
       '*/.git/*' '*/.idea/*' '*/.gradle/*' '*/build/*' \
       '*/.env' '*/local.properties' '*/developer.properties'
)

echo "Created clean submission: $OUTPUT"
