package io.github.jroy.cowbot.commands.spigot.stonks;

import com.earth2me.essentials.Essentials;
import dev.tycho.stonks.command.base.ModularCommandSub;
import dev.tycho.stonks.command.base.autocompleters.MemberCompanyNameAutocompleter;
import dev.tycho.stonks.command.base.validators.CompanyValidator;
import dev.tycho.stonks.model.core.Company;
import dev.tycho.stonks.model.core.Member;
import io.github.jroy.cowbot.managers.spigot.HomeManager;
import io.github.jroy.cowbot.utils.CompanyHomePerk;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class HomeCommand extends ModularCommandSub {

  private final HomeManager homeManager;
  private final Essentials essentials;

  public HomeCommand(HomeManager homeManager) {
    super(new CompanyValidator("company"));
    addAutocompleter("company", new MemberCompanyNameAutocompleter());
    this.homeManager = homeManager;
    this.essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
  }

  @Override
  public void execute(Player player) {
    Company company = getArgument("company");
    if (!company.ownsPerk(CompanyHomePerk.class)) {
      sendMessage(player, "This company doesn't own the Company Home Perk! Purchase it in the perk menu!");
      return;
    }

    Member member = company.getMember(player);
    if (member == null || !member.hasManagamentPermission()) {
      sendMessage(player, "You have insufficient permission to execute this perk action! Please either ask to be promoted " +
          "or have a higher ranking member execute it");
      return;
    }

    try {
      if (!homeManager.hasHome(company)) {
        sendMessage(player, "This company is yet to setup a company home. Please ask a manager to set the company's home.");
        return;
      }
      essentials.getUser(player).getTeleport().now(homeManager.getHome(company), false, PlayerTeleportEvent.TeleportCause.COMMAND);
      sendMessage(player, "Teleported!");
    } catch (Exception e) {
      e.printStackTrace();
      sendMessage(player, "There was an issue while teleporting you to your companies' home. Please report this to Joshie!");
    }
  }
}
