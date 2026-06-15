#!/usr/bin/env bash
# Build (if needed) and run the app.
# JDK: prefers the bundled ./oracleJdk-26, falls back to system Java (needs a full JDK 17+, i.e. with javac).
set -euo pipefail
cd "$(dirname "$0")"

# 1. Pick a Java runtime + its javac. Bundled JDK first, then system.
if [ -x "./oracleJdk-26/bin/java" ]; then
    JAVA="./oracleJdk-26/bin/java"
    JAVAC="./oracleJdk-26/bin/javac"
elif command -v java >/dev/null 2>&1; then
    JAVA="$(command -v java)"
    JAVAC="$(command -v javac || true)"
else
    echo "ERROR: no Java found. Unpack a JDK 17+ as ./oracleJdk-26/ or install one." >&2
    exit 1
fi

# 2. Build if classes are missing.
if [ ! -f "target/classes/com/example/Main.class" ]; then
    echo ">> target/classes missing — compiling..."
    if [ -z "${JAVAC:-}" ] || [ ! -x "${JAVAC}" ]; then
        echo "ERROR: no javac found (system Java is a JRE-only). Use the bundled ./oracleJdk-26/ (full JDK)." >&2
        exit 1
    fi
    mkdir -p target/classes
    find src/main/java -name '*.java' > target/sources.txt
    "$JAVAC" --release 17 -d target/classes @target/sources.txt
    # put resources (config.properties.example etc.) on the classpath
    [ -d src/main/resources ] && cp -r src/main/resources/. target/classes/ 2>/dev/null || true
fi

# 3. Run.
echo ">> running with: $JAVA"
exec "$JAVA" -cp target/classes com.example.Main
