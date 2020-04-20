package io.github.jroy.cowbot.commands.spigot.stonks;

import dev.tycho.stonks.command.base.ModularCommandSub;
import dev.tycho.stonks.command.base.autocompleters.CompanyNameAutocompleter;
import dev.tycho.stonks.command.base.autocompleters.MemberCompanyNameAutocompleter;
import dev.tycho.stonks.command.base.validators.CompanyValidator;
import dev.tycho.stonks.model.core.Company;
import dev.tycho.stonks.model.core.Member;
import io.github.jroy.cowbot.managers.spigot.HomeManager;
import io.github.jroy.cowbot.utils.CompanyHomePerk;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class SetHomeCommand extends ModularCommandSub {

  private final HomeManager homeManager;

  public SetHomeCommand(HomeManager homeManager) {
    super(new CompanyValidator("company"));
    addAutocompleter("company", new MemberCompanyNameAutocompleter());
    this.homeManager = homeManager;
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
      if (homeManager.hasHome(company)) {
        homeManager.updateHome(company, player.getLocation());
      } else {
        homeManager.addHome(company, player.getLocation());
      }
      sendMessage(player, "Successfully updated your companies' home!");
    } catch (SQLException e) {
      e.printStackTrace();
      sendMessage(player, "An error occurred while setting home! Please report this to Joshie!");
    }
  }
}
