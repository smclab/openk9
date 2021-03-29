#!/usr/bin/env bash
# 
# Wait for Scrapyd to startup and execute command
#

CMDNAME=${0##*/}
SLEEP=1

echoerr() { if [[ $QUIET -ne 1 ]]; then echo "$@" 1>&2; fi }

usage() {
  exitcode="$1"
  cat << USAGE >&2
Usage:
  $CMDNAME host:port [-- command args]
  -s | --sleep=SLEEP                  Time in seconds to wait between tests (default=1)
  -q | --quiet                        Don't output any status messages
  -- COMMAND ARGS                     Execute command with args after the test finishes
USAGE
  exit "$exitcode"
}

wait_for_scrapyd() {
  until curl -fsS "$HOST:$PORT" &> /dev/null; do
    echoerr "Scrapyd is unavailable on $HOST:$PORT - sleeping"
    sleep $SLEEP
  done
  echoerr "Scrapyd is up on $HOST:$PORT - executing command"
  exec "$@"
}

while [ $# -gt 0 ]
do
  case "$1" in
    *:* )
    HOSTPORT=(${1//:/ })
    HOST=${HOSTPORT[0]}
    PORT=${HOSTPORT[1]}
    shift 1
    ;;
    -s)
    SLEEP="$2"
    if [[ $SLEEP == "" ]]; then break; fi
    shift 2
    ;;
    --sleep=*)
    SLEEP="${1#*=}"
    shift 1
    ;;
    -q | --quiet)
    QUIET=1
    shift 1
    ;;
    --)
    shift
    break
    ;;
    *)
    echoerr "Unknown argument: $1"
    usage 1
    ;;
  esac
done

if [ "$HOST" = "" -o "$PORT" = "" ]; then
  echoerr "Error: you need to provide a host and port to test."
  usage 2
fi

wait_for_scrapyd "$@"