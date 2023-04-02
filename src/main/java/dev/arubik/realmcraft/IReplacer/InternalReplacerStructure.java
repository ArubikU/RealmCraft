package dev.arubik.realmcraft.IReplacer;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import dev.arubik.realmcraft.FileManagement.InteractiveSection;
import lombok.Getter;

public class InternalReplacerStructure {
    @Getter
    public ReplacementTypes type;
    @Getter
    public OutputTypes outputType;
    @Getter
    public JsonObject TypeConfig;
    @Getter
    public JsonObject outputConfig;
    @Getter
    public List<String> IgnoreNBT;
    @Getter
    public String key;

    @Getter
    public InteractiveSection section;

    public InternalReplacerStructure() {
        TypeConfig = new JsonObject();
        outputConfig = new JsonObject();
        IgnoreNBT = new ArrayList<String>();
    }
}
