package com.depths.untold;


import com.depths.untold.UntoldPlayer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 *
 * @author tim
 */
public class UntoldPlayers {
    private Map<UUID, UntoldPlayer> players = new HashMap<UUID, UntoldPlayer>();
    
    public UntoldPlayers() {
        
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
    
    public void removeUntoldPlayer(Player p) {
        UUID uuid = p.getUniqueId();
        if (players.containsKey(uuid)) {
            UntoldPlayer up = players.get(p.getUniqueId());
            up.save();
            players.remove(uuid);
        }
    }
    
    public void savePlayers() {
        Iterator it = players.entrySet().iterator();
        while (it.hasNext()) {
            Entry pair = (Entry) it.next();
            UntoldPlayer up = (UntoldPlayer) pair.getValue();
            up.save();
        }
    }
    
    
}
