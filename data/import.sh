#!/bin/bash

mongoimport --username=admin --password=admin --authenticationDatabase=admin --db local --drop --collection track --jsonArray --file data/track.json