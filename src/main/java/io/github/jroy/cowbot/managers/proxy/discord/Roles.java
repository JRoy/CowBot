package io.github.jroy.cowbot.managers.proxy.discord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public enum Roles {

  MINECRAFT("667473176887820328"),
  CIVILIZATION("620410029698449412"),
  TF2("667473453502300200"),
  CSGO("642104537431408670"),
  STELLARIS("667472343416963103"),
  RAINBOW("611356436345389066"),
  TARKOV("673316710631079940");

  private String roleId;

  Roles(String roleId) {
    this.roleId = roleId;
  }

  public String getRoleId() {
    return roleId;
  }

  public Role getRole(Guild guild) {
    return guild.getRoleById(roleId);
  }
}
