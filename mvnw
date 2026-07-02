#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.3.2
# ----------------------------------------------------------------------------

set -e

findMavenBasedir() {
  if [ -z "$1" ]; then
    echo "Path not specified to findMavenBasedir"
    return 1
  fi

  basedir="$1"
  wrapperJar="$basedir/.mvn/wrapper/maven-wrapper.jar"
  wrapperProperties="$basedir/.mvn/wrapper/maven-wrapper.properties"

  while [ ! -f "$wrapperJar" ] && [ ! -f "$wrapperProperties" ]; do
    if [ "$basedir" = "/" ]; then
      echo "Could not find .mvn/wrapper/ directory from $1 upwards."
      return 1
    fi
    basedir=$(dirname "$basedir")
  done
  echo "$basedir"
}

MAVEN_PROJECTBASEDIR=$(findMavenBasedir "$(pwd)")
export MAVEN_PROJECTBASEDIR

MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ -f "$MAVEN_WRAPPER_PROPERTIES" ]; then
  DISTRIBUTION_URL=$(grep 'distributionUrl' "$MAVEN_WRAPPER_PROPERTIES" | sed 's/distributionUrl=//')
  WRAPPER_JAR_URL=$(grep 'wrapperUrl' "$MAVEN_WRAPPER_PROPERTIES" | sed 's/wrapperUrl=//')
fi

MAVEN_USER_HOME="${MAVEN_USER_HOME:-$HOME/.m2}"
MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"

# If wrapper jar not present, download it
if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
  if command -v wget > /dev/null 2>&1; then
    wget -q -O "$MAVEN_WRAPPER_JAR" "$WRAPPER_JAR_URL" || true
  elif command -v curl > /dev/null 2>&1; then
    curl -q -o "$MAVEN_WRAPPER_JAR" "$WRAPPER_JAR_URL" || true
  fi
fi

JAVA_HOME_TMP="${JAVA_HOME}"

exec "$JAVA_HOME_TMP/bin/java" \
  -classpath "$MAVEN_WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
