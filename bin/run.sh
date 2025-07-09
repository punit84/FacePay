#!/bin/bash
set -e

mvn compile exec:java -Dexec.mainClass=com.punit.sts.NovaSonicVoipGateway

