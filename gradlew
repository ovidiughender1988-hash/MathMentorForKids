#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

APP_HOME=$( cd "$( dirname "$0" )" && pwd )
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  *)
    ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched.
if $cygwin ; then
    [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# Attempt to locate a Java executable.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
fi

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ "$cygwin" = "true" ] || [ "$msys" = "true" ] ; then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
    CLASSPATH=`cygpath --path --mixed "$CLASSPATH"`
fi

# Increase the maximum file descriptors if we can.
if [ "$darwin" = "true" ] ; then
    MAX_FD_LIMIT=$(ulimit -H -n)
    if [ "$MAX_FD_LIMIT" != "unlimited" ] ; then
        if [ "$MAX_FD_LIMIT" -lt 65536 ] ; then
            echo "Could not set maximum file descriptors to $MAX_FD_LIMIT on this system."
        fi
    fi
fi

if [ "$cygwin" = "true" ] || [ "$msys" = "true" ] ; then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
fi

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this build.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms256m"'
JAVA_OPTS="$JAVA_OPTS $DEFAULT_JVM_OPTS"
GRADLE_OPTS="$GRADLE_OPTS $DEFAULT_JVM_OPTS"

# Split up the JVM options into an array.
JVM_OPTS=()
for arg in "$@"; do
  case "$arg" in
    -D*|-X*|-XX*|-ea*|-da*|-en*|-dis*|-max*|-min*|-client|-server|-noverify) JVM_OPTS+=("$arg") ;;
    *) ;;
  esac
done

exec "$JAVACMD" "${JVM_OPTS[@]}" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
