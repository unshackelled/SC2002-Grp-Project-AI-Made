#!/bin/sh
# Compiles and runs this stage. Extra arguments are passed to the game, e.g.
#   ./run.sh --echo < tests/victory.txt
cd "$(dirname "$0")" || exit 1
mkdir -p out
javac -d out -sourcepath src src/restaurantrush/Main.java || exit 1
java -cp out restaurantrush.Main "$@"
