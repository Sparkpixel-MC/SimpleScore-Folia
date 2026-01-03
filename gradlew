#!/bin/sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME

# Resolve links: $0 may be a link
app_path=$0

# Need this for daisy-chained symlinks.
while
    APP_HOME="${app_path%/*}"
    [ -h "$app_path" ]
do
    ls=$( ls -ld "$app_path" )
    link="${ls#*' -> '}"
    case $link in             #(
      /*)   app_path="$link" ;; #(
      *)    app_path="$APP_HOME/$link" ;;
    esac
done

# Change from the installation directory to the base directory
APP_BASE=$( cd "${APP_HOME}/../.." && pwd )

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "$( uname )" in                #(
  CYGWIN* )         cygwin=true  ;; #(
  Darwin* )         darwin=true  ;; #(
  MSYS* | MINGW* )  msys=true    ;; #(
  NONSTOP* )        nonstop=true ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    case $MAX_FD in #(
      max*)
        # In POSIX sh, ulimit -H is undefined: we rely on the interactive
        # shell behavior of 'ulimit -n' to assign the high limit to the low limit.
        # Intentionally not using ulimit -S -n (wouldn't work in Zsh).
        MAX_FD=$( ulimit -n )
        if [ $? -eq 0 ] ; then
            if [ "$MAX_FD" = "unlimited" ] ; then
                MAX_FD=100000
            fi
        else
            warn "Could not query maximum file descriptor limit: $MAX_FD"
            MAX_FD=8192
        fi
        ;;
    esac
    ulimit -n $MAX_FD
fi

# For Darwin, add options to specify how the application appears in the dock
if "$darwin"; then
    GRADLE_OPTS="$GRADLE_OPTS -Xdock:name=Gradle -Xdock:icon=$APP_HOME/media/gradle.icns"
fi

# For Cygwin or MSYS, switch paths to Windows format before running Java
if "$cygwin" || "$msys" ; then
    APP_HOME=$( cygpath --path --mixed "$APP_HOME" )
    CLASSPATH=$( cygpath --path --mixed "$CLASSPATH" )

    JAVACMD=$( cygpath --unix "$JAVACMD" )

    # Now convert the arguments - kludge to limit ourselves to /bin/sh
    for arg do
        if
            case $arg in                                #(
              -*)   false ;;                            # don't mess with options #(
              /?*)  t=${arg#/} t=/${t%%/*}              # looks like a POSIX filepath
                    [ -e "$t" ] ;;                      #(
              *)    false ;;
            esac
        then
            arg=$( cygpath --path --mixed "$arg" )
        fi
        # Roll the args list around by copying all the arguments after
        # arg into arguments preceding arg.
        # We can't use shift because that loses the arguments.
        # We can't use "$@" because that doesn't work with special characters.
        roll_args=
        for roll_arg do
            if [ "$roll_arg" = "$arg" ] ; then
                roll_arg=
            fi
            if [ -n "$roll_arg" ] ; then
                roll_args="$roll_args \"$roll_arg\""
            fi
        done
        eval "set -- $roll_args \"$arg\""
    done
fi

# Collect all arguments for the java command, stacking in reverse order:
#   * args from the command line
#   * the main class name
#   * -classpath
#   * -D...appname settings
#   * --module-path (only if needed)
#   * DEFAULT_JVM_OPTS
#   * JAVA_OPTS, and GRADLE_OPTS environment variables.

# For Cygwin or MSYS, switch bin directory to Windows format
if "$cygwin" || "$msys" ; then
    APP_HOME=$( cygpath --path --mixed "$APP_HOME" )
fi

# Collect all arguments for the java command:
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, JAVA_OPTS, and extra var
#   * -D...appname settings
#   * --module-path (only if needed)
#   * DEFAULT_JVM_OPTS
args="\"-Dorg.gradle.appname=$APP_BASE\""
args="$args $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS $GRADLE_ARGS"

# Run the java command
eval "$JAVACMD $args -classpath '$CLASSPATH' org.gradle.wrapper.GradleWrapperMain \"$@\""