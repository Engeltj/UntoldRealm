package com.depths.untold.Config;


import com.depths.untold.UntoldPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 *
 * @author tim
 */
public class ConfigPlayers extends Config {
    private Map<UUID, UntoldPlayer> players = new HashMap<UUID, UntoldPlayer>();
    
    public ConfigPlayers() {
        super("players.yml");
    }
    
    private void setPlayerExperience(Player p, int exp) {
        set(p.getUniqueId().toString() + '.' + "exp", exp);
    }

    private int getPlayerExperience(Player p) {
        return getInt(p.getUniqueId().toString() + '.' + "exp");
    }
    
    
    public UntoldPlayer getUntoldPlayer(Player p) {
        if (!players.containsKey(p.getUniqueId())) {
            return addUntoldPlayer(p);
        } else {
            return players.get(p.getUniqueId());
        }
    }
    
    public UntoldPlayer addUntoldPlayer(Player p) {
        if (!players.containsKey(p.getUniqueId())) {
            UntoldPlayer up = new UntoldPlayer(p);       
            players.put(p.getUniqueId(), up);
            return up;
        } else {
            return players.get(p.getUniqueId());
        }
    }
    
    
}
