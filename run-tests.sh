#!/bin/bash

set -ev

chmod +x gradlew

if [ "${TRAVIS_EVENT_TYPE}" == "cron" ]; then
  # scheduled external deploy & test

  ## insert commands to deploy & test here...
  echo true
  
else
  # default path

  ./gradlew clean test --stacktrace --info

  if [ "${TRAVIS_PULL_REQUEST}" != "false" ]; then
    # if its a PR then run sonar preview
    ./gradlew sonarqube --no-daemon -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.login=$SONAR_TOKEN -Dsonar.branch.name=$TRAVIS_BRANCH -Dsonar.organization=adyen -Dsonar.analysis.mode=preview -Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST -Dsonar.github.repository=$TRAVIS_REPO_SLUG -Dsonar.github.oauth=$SONAR_GITHUB_TOKEN;
  fi

  # run cucumber test framework
  ./gradlew cucumberTest --stacktrace --info

  # package output war
  ./gradlew bootRepackage -Pprod -x test

fi
