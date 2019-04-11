package io.github.jroy.cowbot.utils;

import io.github.jroy.cowbot.ProxiedCow;
import net.dv8tion.jda.core.JDA;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DatabaseFactory {

  private JDA jda;

  private Connection connection;

  private List<String> whitelist = new ArrayList<>();

  public DatabaseFactory(JDA jda, File pluginFolder) throws SQLException, ClassNotFoundException {
    Logger.log("Loading Database Factory...");
    this.jda = jda;
    if (!pluginFolder.exists()) {
      //noinspection ResultOfMethodCallIgnored
      pluginFolder.mkdir();
    }
    Class.forName("org.sqlite.JDBC");
    connection = DriverManager.getConnection("jdbc:sqlite:"+pluginFolder.getAbsolutePath()+"players.db");
    connection.createStatement().execute("CREATE TABLE IF NOT EXISTS players( id integer PRIMARY KEY AUTOINCREMENT, mc text NOT NULL, discordid text NOT NULL);");
    connection.createStatement().execute("CREATE TABLE IF NOT EXISTS bans( id integer PRIMARY KEY AUTOINCREMENT, discordid text NOT NULL, reason text NOT NULL);");
    Logger.log("Connected to the Database!");
    ProxiedCow.instance.getProxy().getScheduler().schedule(ProxiedCow.instance, () -> {
      Logger.log("Starting Whitelist Purge...");
      try {
        ResultSet set = getUsers();
        List<String> list = new ArrayList<>();
        int purgeCount = 0;
        while (set.next()) {
          if (this.jda.getGuildById("438337215584796692").getMemberById(set.getString("discordid")) == null) {
            deleteUser(set.getInt("id"));
            purgeCount++;
            continue;
          }
          list.add(set.getString("mc"));
        }
        whitelist = list;
        Logger.log("Purged " + purgeCount + " member(s)!");
      } catch (SQLException e) {
        Logger.log("Error while purging: " + e.getMessage());
      }
    }, 0, 1, TimeUnit.HOURS);
    Logger.log("Loaded Database Factory!");
  }

  public ResultSet getUsers() throws SQLException {
    return connection.createStatement().executeQuery("SELECT id, mc, discordid FROM players");
  }

  public boolean isWhitelisted(String mcName) {
    try {
      ResultSet set = getUsers();
      while (set.next()) {
        if (set.getString("mc").equalsIgnoreCase(mcName)) {
          return true;
        }
      }
    } catch (SQLException ignored) {}
    return false;
  }

  public boolean isLinked(String discordId) {
    try {
      PreparedStatement statement = connection.prepareStatement("SELECT id, mc, discordid FROM players WHERE discordid = ?");
      statement.setString(1, discordId);
      return statement.executeQuery().next();
    } catch (SQLException ignored) {}
    return false;
  }

  public void linkUser(String discordId, String mcName) throws SQLException {
    PreparedStatement statement = connection.prepareStatement("INSERT INTO players(mc, discordid) VALUES(?, ?)");
    statement.setString(1, mcName);
    statement.setString(2, discordId);
    statement.executeUpdate();
  }

  public void updateUser(String discordId, String mcName) throws SQLException {
    PreparedStatement statement = connection.prepareStatement("UPDATE players SET mc = ? WHERE discordid = ?");
    statement.setString(1, mcName);
    statement.setString(2, discordId);
    statement.executeUpdate();
  }

  public void deleteUser(int id) throws SQLException {
    PreparedStatement statement = connection.prepareStatement("DELETE FROM players WHERE id = ?");
    statement.setInt(1, id);
    statement.executeUpdate();
  }

  public void banUser(String discordId, String reason) throws SQLException {
    PreparedStatement statement = connection.prepareStatement("INSERT INTO bans(discordid, reason) VALUES(?, ?)");
    statement.setString(1, discordId);
    statement.setString(2, reason);
    statement.executeUpdate();
  }

  public void pardonUser(String discordId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement("DELETE FROM bans WHERE discordid = ?");
    statement.setString(1, discordId);
    statement.executeUpdate();
  }

  public boolean isBanned(String discordId) {
    try {
      PreparedStatement statement = connection.prepareStatement("SELECT id FROM bans WHERE discordid = ?");
      statement.setString(1, discordId);
      return statement.executeQuery().next();
    } catch (SQLException ignored) {}
    return false;
  }

  public String getBanReason(String discordId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement("SELECT reason FROM bans WHERE discordid = ?");
    statement.setString(1, discordId);
    ResultSet set = statement.executeQuery();
    set.next();
    return set.getString("reason");
  }
}