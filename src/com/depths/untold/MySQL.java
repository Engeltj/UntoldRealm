package com.depths.untold;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

/**
 *
 * @author Tim
 */
public class MySQL {
    public static String host = "localhost";
    public static String port = "3306";
    public static String database = "untold";
    public static String username = "untold";
    public static String password = "7ce8jE3KnXewuZtS";
    public static Connection con;

    static ConsoleCommandSender console = Bukkit.getConsoleSender();

    // connect
    public static boolean connect() {
        if (!isConnected()) {
            try {
                con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
                console.sendMessage("MySQL connection initialized!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return isConnected();
    }

    // disconnect
    public static void disconnect() {
        if (isConnected()) {
            try {
                con.close();
                console.sendMessage("MySQL connection closed successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // isConnected
    public static boolean isConnected() {
        return (con == null ? false : true);
    }

    // getConnection
    public static Connection getConnection() {
        return con;
    }
}
