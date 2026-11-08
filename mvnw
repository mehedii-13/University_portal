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
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Maven Start Up Batch script
#
# Required ENV Vars:
# ------------------
#   JAVA_HOME - location of a JDK home dir
#
# Optional ENV Vars
# -----------------
#   MAVEN_OPTS - parameters to pass to the Java VM when running Maven
#     e.g. to debug Maven itself, use
#       set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
# ----------------------------------------------------------------------------

die() {
    echo "$*" >&2
    exit 1
}

BASE_DIR=$(pwd)
PRG="$0"
while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`/"$link"
    fi
done
BASE_DIR=`dirname "$PRG"`

# Locate Java
if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD="java"
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly." >&2
  echo "  We cannot execute $JAVACMD" >&2
  exit 1
fi

WRAPPER_JAR="$BASE_DIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -e "$WRAPPER_JAR" ]; then
    echo "Couldn't find $WRAPPER_JAR, attempting to download..."
    WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
    if [ -r "$WRAPPER_PROPERTIES" ]; then
        WRAPPER_URL_FROM_FILE=$(sed -n -e 's/^wrapperUrl=\(.*\)/\1/p' "$WRAPPER_PROPERTIES")
        if [ -n "$WRAPPER_URL_FROM_FILE" ]; then
            WRAPPER_URL="$WRAPPER_URL_FROM_FILE"
        fi
    fi
    mkdir -p "$BASE_DIR/.mvn/wrapper"
    if command -v curl >/dev/null 2>&1; then
        curl -fsSL -o "$WRAPPER_JAR" "$WRAPPER_URL"
    elif command -v wget >/dev/null 2>&1; then
        wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL"
    else
        echo "Error: Neither curl nor wget found to download $WRAPPER_JAR" >&2
        exit 1
    fi
fi

exec "$JAVACMD" \
  $MAVEN_OPTS \
  -jar "$WRAPPER_JAR" \
  "$@"

