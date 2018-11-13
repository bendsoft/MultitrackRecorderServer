#!/bin/bash

mongoimport --db local --drop --collection track --jsonArray --file data/track.json