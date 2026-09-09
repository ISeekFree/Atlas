#!/usr/bin/env bash
if [[ -z "${BASH_VERSION:-}" ]]; then
  exec /usr/bin/env bash "$0" "$@"
fi
set -euo pipefail

usage() {
  cat <<'USAGE'
Usage: ./build.sh

Builds all modules with Java 17:
  mvn -Phero-rdc clean package -DskipTests
USAGE
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi
if [[ "$#" -gt 0 ]]; then
  echo "Unknown argument: $1" >&2
  usage >&2
  exit 2
fi

shopt -s expand_aliases
if [[ -f "$HOME/.bash_profile" ]]; then
  # Load local Java aliases such as `jdk17`.
  # shellcheck source=/dev/null
  source "$HOME/.bash_profile"
fi

java17_selected=false
if type jdk17 >/dev/null 2>&1; then
  eval jdk17
  java17_selected=true
elif command -v /usr/libexec/java_home >/dev/null 2>&1; then
  export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
  java17_selected=true
fi
if [[ "$java17_selected" != "true" ]]; then
  echo "Java 17 is required. Define jdk17 or install a Java 17 JDK." >&2
  exit 1
fi
export PATH="$JAVA_HOME/bin:$PATH"

mvn -Phero-rdc clean package -DskipTests
