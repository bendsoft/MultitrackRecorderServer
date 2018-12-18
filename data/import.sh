#!/bin/bash

files=( "Ableton-Gtr.wav" "Army.wav" "Bass.wav" "Drum.wav" "Lead-Resolve-Gtr.wav" "Morph-Crowd.wav" "FX.wav" "Misery.wav" )

rm -rf tmp_sample_import
mkdir tmp_sample_import
cd tmp_sample_import

mongo localhost/mtr --username=admin --password=admin --authenticationDatabase=admin --eval "db.tracks.drop()"

for trackNr in 1 2 3 4
do
    mkdir ${trackNr}
    cp -r ../sample-channel-files/. ${trackNr}

    channels=""
    channelCounter=0
    for file in "${files[@]}"
    do
       ((channelCounter++))
       channelNameWoFileExt="${file%.wav}"
       newChannelFileName="$channelNameWoFileExt-$trackNr.wav"
       trackName="Track#${trackNr}"
       uniqueChannelFilename="20181201_${trackName}_${newChannelFileName}"
       newChannelFilenameWithPath="$trackNr/$newChannelFileName"

       channels+="{ filename: '$uniqueChannelFilename', name: '$channelNameWoFileExt', number: $channelCounter },"

       mv "$trackNr/$file" "${newChannelFilenameWithPath}"
       mongofiles --username=admin --password=admin --authenticationDatabase=admin --db recordings --type "audio/x-wav" --replace --local "${newChannelFilenameWithPath}" put "${uniqueChannelFilename}"
    done

    mongo --username=admin --password=admin --authenticationDatabase=admin --eval "const trackName='Track #$trackNr', trackNumber=$trackNr, channels=[$channels];" ../insert-track.js
    echo "Inserted Track Nr $trackNr"
done
