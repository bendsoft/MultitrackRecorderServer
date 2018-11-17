#!/bin/bash

mongoimport --username=admin --password=admin --authenticationDatabase=admin --db mtr --drop --collection track --jsonArray --file data/track.json