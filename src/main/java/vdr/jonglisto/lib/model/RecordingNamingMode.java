package vdr.jonglisto.lib.model;

public enum RecordingNamingMode {
    Auto(1), // autodetect if 'constabel', 'serie' or 'normal movie'
    Constabel(2), // naming in constabel series style with season, number, ..
    Serie(3), // series style, like Title/Subtitle
    Categorized(4), // sorted in sub folders which are auto-named by category
    User1(5), // for future use. If someone wants to add a new file name
              // generation
    User2(6); // for future use. If someone wants to add a new file name
              // generation

    private int id; // Could be other data type besides int

    private RecordingNamingMode(int id) {
        this.id = id;
    }

    public static RecordingNamingMode fromId(int id) {
        for (RecordingNamingMode type : RecordingNamingMode.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }

    public int getId() {
        return this.id;
    }
}
