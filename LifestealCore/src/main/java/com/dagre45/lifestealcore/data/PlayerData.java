package com.dagre45.lifestealcore.data;

import java.util.UUID;

public final class PlayerData {

    private final UUID uuid;
    private int hearts;
    private int kills;
    private int deaths;
    private boolean eliminated;
    private long eliminatedAt;

    public PlayerData(UUID uuid, int hearts) {
        this.uuid = uuid;
        this.hearts = hearts;
        this.kills = 0;
        this.deaths = 0;
        this.eliminated = false;
        this.eliminatedAt = 0L;
    }

    public UUID getUuid() { return uuid; }

    public int getHearts() { return hearts; }
    public void setHearts(int hearts) { this.hearts = hearts; }

    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }

    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }

    public boolean isEliminated() { return eliminated; }
    public void setEliminated(boolean eliminated) { this.eliminated = eliminated; }

    public long getEliminatedAt() { return eliminatedAt; }
    public void setEliminatedAt(long eliminatedAt) { this.eliminatedAt = eliminatedAt; }
}
