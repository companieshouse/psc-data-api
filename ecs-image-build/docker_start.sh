#!/bin/bash

PORT=8080

exec java -jar -Dserver.port="${PORT}" "psc-data-api.jar"