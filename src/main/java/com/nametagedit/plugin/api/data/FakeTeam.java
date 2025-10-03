package com.nametagedit.plugin.api.data;

import java.util.ArrayList;
import java.util.List;

public class FakeTeam {
    private final List<String> members = new ArrayList<>();
    private String name;
    private String prefix;
    private String suffix;
    private boolean visible = true;

    public FakeTeam(String name, String prefix, String suffix) {
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public void addMember(String player) {
        if (!members.contains(player)) {
            members.add(player);
        }
    }

    public List<String> getMembers() {
        return members;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}