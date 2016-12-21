package vdr.jonglisto.lib;

import java.util.List;
import java.util.Optional;

import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.Device;
import vdr.jonglisto.lib.model.Plugin;
import vdr.jonglisto.lib.model.RecPathSummary;
import vdr.jonglisto.lib.model.RecordingInfo;
import vdr.jonglisto.lib.model.Timer;
import vdr.jonglisto.lib.model.osd.TextOsd;

public interface VdrDataService {

    /*
     * Information
     */
    public List<Plugin> getPlugins(String vdrUuid);

    public List<Device> getDevices(String vdrUuid);

    /*
     * Channels
     */
    public Optional<Channel> getChannel(String vdrUuid, String channelId);

    public Optional<List<Channel>> getChannels(String vdrUuid, boolean includeRadio);

    public Optional<List<String>> getGroups(String vdrUuid);

    public Optional<List<Channel>> getChannelsInGroup(String vdrUuid, String group, boolean includeRadio);

    public Optional<List<Channel>> getChannelsMap(String vdrUuid, boolean includeRadio);

    public Optional<List<Channel>> getChannelsInGroupMap(String vdrUuid, String group, boolean includeRadio);

    /*
     * Timer
     */
    public Optional<List<Timer>> getTimer(String vdrUuid);

    public Optional<Timer> getTimerById(String vdrUuid, String timerId);

    public void createTimer(String vdrUuid, Timer timer);

    public void updateTimer(String vdrUuid, Timer oldTimer, Timer newTimer);

    public void deleteTimer(String vdrUuid, Timer timer);

    public void deleteTimer(String vdrUuid, String timerId);

    public void bulkDeleteTimer(String vdrUuid, List<String> timerIds);

    public void activateTimer(String vdrUuid, String timerId);

    public void deactivateTimer(String vdrUuid, String timerId);

    /*
     * Recordings
     */
    public void fullRecSync(String vdrUuid);

    public void recSync(String vdrUuid);

    public void recUpdate(String vdrUuid);

    public List<String> getRecDirectories(String vdrUuid);

    public List<RecordingInfo> getRecordingsInPath(String vdrUuid, String path);

    public RecordingInfo getRecording(String vdruuid, String file);

    public RecPathSummary getRecSummary(String vdrUuid, String path);

    public void deleteRecording(String vdrUuid, String fileName);

    public void deleteRecordings(String vdrUuuid, List<String> recordingsToChange);

    public void renameRecording(String vdrUuid, String fileName, String newName);

    public void moveRecordings(String vdrUuid, List<String> recordingsToChange, String destination);

    public void moveRecording(String vdrUuid, String sourceFilename, String destination);

    /*
     * OSD and keyboard
     */
    public TextOsd getOsd(String vdrUuid);

    public void processKey(String vdrUuid, String key);

    public void processString(String vdrUuid, String string);

    // TODO: keyboard sequence

}
