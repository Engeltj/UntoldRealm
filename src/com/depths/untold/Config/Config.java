/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.depths.untold.Config;

import com.depths.untold.Untold;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.Bukkit;

/**
 *
 * @author Tim
 */
public class Config extends YamlConfiguration {
    private final Untold plugin;
    private File configFile;
    
    
    public Config(String filename){
        this.plugin = (Untold) Bukkit.getServer().getPluginManager().getPlugin("Untold");
        configFile = new File(plugin.getDataFolder() + File.separator + filename).getAbsoluteFile();
        
        if (!configFile.exists()){
            try {
                configFile.createNewFile();
            } catch (Exception e){
                plugin.sendConsole("Error creating "+filename);
            }
        } else
            try{
                load(configFile);
            }catch (Exception ignored){}
    }
    
    public boolean setConfigFile(File configFile){
        if (configFile != null){
            this.configFile = configFile;
            return true;
        }
        return false;
    }
    
    public File getConfigFile(){
        return this.configFile;
    }

    public void save()
    {
        try{
            save(configFile);
        }
        catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
