const injectedTrackParams = {
    trackNr: trackNumber || 0,
    name: trackName || '',
    channels: channels || []
};

const channelCreator = channel => ({
    id: null,
    filename: channel.filename,
    name: channel.name,
    channelNumber: channel.number,
    data: null
});

const trackCreator = (tnumber, tname) => ({
    name: tname,
    trackNumber: tnumber,
    channelRecordingFiles: injectedTrackParams.channels.map(channelCreator)
});

db.tracks.insert(
    trackCreator(
        injectedTrackParams.trackNr,
        injectedTrackParams.name
    )
);
