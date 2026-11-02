#!/bin/sh
cd "`dirname "$0"`"
dot_clean .
for file in *.sh; do mv "$file" "${file%.sh}.command"; done
chmod +x *.command
