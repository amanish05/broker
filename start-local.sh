#!/bin/zsh
# Load environment variables from .env if it exists, then start the Spring Boot app
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi
./gradlew bootRun
