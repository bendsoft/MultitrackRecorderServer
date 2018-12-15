#!/bin/bash

#mongoimport --username=admin --password=admin --authenticationDatabase=admin --db mtr --drop --collection tracks --jsonArray --file data/sample_tracks.json

files=( "HP Ableton Gtr.wav" "HP Army.wav" "HP Bass.wav" "HP Drum.wav" "HP Lead Resolve Gtr.wav" "HP Morph Crowd.wav" "HP FX.wav" "HP Misery.wav" )

rm -rf tmp_sample_import
mkdir tmp_sample_import
cd tmp_sample_import

#mongo localhost/mtr --username=admin --password=admin --authenticationDatabase=admin --eval "db.tracks.drop()"

for trackNr in 1 2 3 4
do
    mkdir ${trackNr}
    cp -r ../sample-channel-files/. ${trackNr}

    channels=""
    channelCounter=0
    for file in "${files[@]}"
    do
       ((channelCounter++))
       simpleName="${file%.wav}"
       newFilename="$simpleName $trackNr.wav"
       uniqueFilename="20181201_Track #${trackNr}_${newFilename}"
       newFilenameWithPath="$trackNr/$newFilename"

       channels+="{ filename: '$uniqueFilename', name: '$simpleName', number: $channelCounter },"

       mv "$trackNr/$file" "${newFilenameWithPath}"
       mongofiles --username=admin --password=admin --authenticationDatabase=admin --db recordings --type "audio/x-wav" --replace --local "${newFilenameWithPath}" put "${uniqueFilename}"
    done

    mongo --username=admin --password=admin --authenticationDatabase=admin --eval "const trackName='Track #$trackNr', trackNumber=$trackNr, channels=[$channels];" ../insert-track.js
    echo "Inserted Track Nr $trackNr"
done
