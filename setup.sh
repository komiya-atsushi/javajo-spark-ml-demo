#!/bin/bash

rm smsspamcollection.zip SMSSpamCollection
rm bin/sbt-launch.jar

wget https://archive.ics.uci.edu/ml/machine-learning-databases/00228/smsspamcollection.zip
unzip smsspamcollection.zip SMSSpamCollection

mkdir bin
wget -P bin http://dl.bintray.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.8/sbt-launch.jar
