conn = new Mongo();
db = conn.getDB("admin");
db.auth("admin", "admin");
db = db.getSiblingDB('mtr');

const injectedTrackParams = {
    trackNr: trackNumber || 0,
    name: trackName || '',
    channels: channels || []
};

const channelCreator = channel => ({
    filename: channel.filename,
    name: channel.name,
    channelNumber: channel.number,
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
