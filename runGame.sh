#!/bin/bash

javac Minskytron.java
javac Minskytron2.java
./halite -d "30 30" "java Minskytron" "java Minskytron2"
