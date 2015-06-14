package max.hubbard.bettershops.Listeners;

import max.hubbard.bettershops.Configurations.Language;
import max.hubbard.bettershops.ShopManager;
import max.hubbard.bettershops.Shops.Shop;
import max.hubbard.bettershops.Shops.Types.NPC.NPCManager;
import max.hubbard.bettershops.Shops.Types.NPC.ShopsNPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * ***********************************************************************
 * Copyright Max Hubbard (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated documents with similar branding
 * are the sole property of Max. Distribution, reproduction, taking snippets, or
 * claiming any contents as your own will break the terms of the license, and void any
 * agreements with you, the third party.
 * ************************************************************************
 */
public class NPCOpen implements Listener {

    @EventHandler
    public void onOpen(final PlayerInteractEntityEvent e) {

        if (e.getRightClicked() instanceof LivingEntity) {
            final LivingEntity ent = (LivingEntity) e.getRightClicked();

            if (ent.getCustomName() != null && ent.getCustomName().contains("§a§l")) {

                final Shop shop = ShopManager.fromString(ent.getCustomName().substring(4));

                if (shop != null) {

                    e.setCancelled(true);

                    Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("BetterShops"), new Runnable() {
                        @Override
                        public void run() {

                            if (!shop.isNPCShop() || shop.getNPCShop() == null) {
                                shop.setObject("NPC", true);
                                ShopsNPC n = new ShopsNPC(ent, shop);

                                n.removeChest();
                                n.returnNPC();
                                NPCManager.addNPCShop(n);
                            }
                            e.setCancelled(true);

                            Player p = e.getPlayer();

                            if (!shop.getBlacklist().contains(p)) {
                                if (shop.isOpen()) {
                                    Opener.open(p, shop);
                                } else {
                                    if (!shop.getOwner().getUniqueId().equals(p.getUniqueId()) || !shop.getOwner().getUniqueId().toString().equals(p.getUniqueId().toString())) {
                                        p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "ShopClosed"));
                                    } else {
                                        Opener.open(p, shop);
                                    }
                                }
                            } else {
                                p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "NotAllowed"));
                            }
                        }
                    });
                }
            }
        }
    }
}
