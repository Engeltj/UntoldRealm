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
    private static String host = "localhost";
    private static String port = "3306";
    private static String database = "untold";
    private static String username = "untold";
    private static String password = "Fv3P5eCzwUeY8dtq";
    private static Connection con;

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
