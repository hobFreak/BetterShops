package max.hubbard.bettershops.Menus.ShopMenus;

import max.hubbard.bettershops.Configurations.Config;
import max.hubbard.bettershops.Configurations.Language;
import max.hubbard.bettershops.Configurations.Permissions;
import max.hubbard.bettershops.Menus.MenuType;
import max.hubbard.bettershops.Menus.ShopMenu;
import max.hubbard.bettershops.Shops.Items.Actions.ClickableItem;
import max.hubbard.bettershops.Shops.Items.Actions.LeftClickAction;
import max.hubbard.bettershops.Shops.Items.Actions.ShopItemStack;
import max.hubbard.bettershops.Shops.Items.ShopItem;
import max.hubbard.bettershops.Shops.Shop;
import max.hubbard.bettershops.Shops.Types.Holo.DeleteHoloShop;
import max.hubbard.bettershops.Shops.Types.Holo.ShopHologram;
import max.hubbard.bettershops.Utils.AnvilManager;
import max.hubbard.bettershops.Utils.Stocks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * ***********************************************************************
 * Copyright Max Hubbard (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated documents with similar branding
 * are the sole property of Max. Distribution, reproduction, taking snippets, or
 * claiming any contents as your own will break the terms of the license, and void any
 * agreements with you, the third party.
 * ************************************************************************
 */
public class ItemManagerBuying implements ShopMenu {

    Shop shop;
    Inventory inv;

    public ItemManagerBuying(Shop shop) {
        this.shop = shop;
        inv = Bukkit.createInventory(null, 54, Language.getString("MainGUI", "ShopHeader") + shop.getName());

    }

    @Override
    public MenuType getType() {
        return MenuType.ITEM_MANAGER_BUYING;
    }

    @Override
    public Shop getAttachedShop() {
        return shop;
    }

    @Override
    public void draw(final Player p, final int page, final Object... obj) {
        inv.clear();

        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, shop.getFrameColor());
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(" ");
        item.setItemMeta(m);
        for (int i = 0; i < 18; i++) {
            inv.setItem(i, item);
        }

        final ShopItem it = (ShopItem) obj[0];

        ItemStack nam = new ItemStack(Material.STAINED_CLAY, 1, (byte) 4);
        ItemMeta namMeta = nam.getItemMeta();
        namMeta.setDisplayName(Language.getString("ItemTexts", "PriceDisplayName"));
        namMeta.setLore(Arrays.asList(Language.getString("ItemTexts", "PriceLore")));
        nam.setItemMeta(namMeta);
        ClickableItem namClick = new ClickableItem(new ShopItemStack(nam), inv, p);
        namClick.addLeftClickAction(new LeftClickAction() {
            @Override
            public void onAction(InventoryClickEvent e) {
                final AnvilManager man = new AnvilManager(p);
                Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("BetterShops"), new Runnable() {
                    @Override
                    public void run() {
                        String name = man.call();
                        boolean can;
                        double amt = 0.0;
                        try {
                            amt = Double.parseDouble(name);

                            can = true;
                        } catch (Exception ex) {
                            p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "InvalidNumber"));
                            can = false;
                        }

                        BigDecimal bd = new BigDecimal(amt);
                        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
                        amt = bd.doubleValue();

                        if (can) {
                            if (amt >= 0) {
                                if (amt <= Config.getMaxPrice()) {
                                    it.setPrice(amt);
                                    if (shop.isHoloShop()) {
                                        ShopHologram h = shop.getHolographicShop();
                                        h.updateItemLines(h.getItemLine(), false);
                                    }
                                    p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "ChangePrice"));
                                } else {
                                    if (String.valueOf(Config.getMaxPrice()).contains("E")) {
                                        p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "HighPrice") + " §7(Max: " + Config.getMaxPriceAsString() + ")");
                                    } else {
                                        p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "HighPrice") + " §7(Max: " + Config.getMaxPrice() + ")");
                                    }
                                }
                            } else {
                                p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "Zero"));
                            }
                        }

                        draw(p, page, obj);
                    }
                });
            }
        });

        ItemStack desc = new ItemStack(Material.STAINED_CLAY, 1, (byte) 15);
        ItemMeta descMeta = desc.getItemMeta();
        descMeta.setDisplayName(Language.getString("ItemTexts", "RemoveItemDisplayName"));
        descMeta.setLore(Arrays.asList(Language.getString("ItemTexts", "RemoveItemLore")));
        desc.setItemMeta(descMeta);
        ClickableItem descClick = new ClickableItem(new ShopItemStack(desc), inv, p);
        descClick.addLeftClickAction(new LeftClickAction() {
            @Override
            public void onAction(InventoryClickEvent e) {
                if (it != null) {
                    Stocks.removeAllOfDeletedItem(it, shop, p, false);

                    int page = it.getPage();
                    boolean cal = false;

                    if (shop.isHoloShop()) {
                        ShopHologram h = shop.getHolographicShop();
                        if (h.getItemLine().getItemStack().equals(it.getItem())) {
                            cal = true;
                        }
                    }

                    shop.deleteShopItem(it);

                    if (cal) {
                        ShopHologram h = shop.getHolographicShop();
                        if (shop.getShopItems(false).size() > 0) {
                            h.getItemLine().setItemStack(shop.getShopItems(false).get(0).getItem());
                        } else {
                            if (shop.getShopItems(true).size() > 0) {
                                h.getItemLine().setItemStack(shop.getShopItems(true).get(0).getItem());
                                h.getShopLine().setText(Language.getString("MainGUI", "Selling"));
                            } else {
                                DeleteHoloShop.deleteHologramShop(h);
                                shop.setObject("Holo", false);
                            }
                        }
                    }

                    if (!it.isSelling()) {
                        shop.getMenu(MenuType.OWNER_BUYING).draw(p, page);
                    } else {
                        shop.getMenu(MenuType.OWNER_SELLING).draw(p, page);
                    }
                    p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "RemoveItem"));

                } else {
                    p.closeInventory();
                    p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "NonExistingItem"));
                }
            }
        });

        ItemStack addStock = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 5);
        ItemMeta addStockMeta = addStock.getItemMeta();
        addStockMeta.setDisplayName(Language.getString("ItemTexts", "AddStockDisplayName"));
        addStockMeta.setLore(Arrays.asList(Language.getString("ItemTexts", "AddStockLore")));
        addStock.setItemMeta(addStockMeta);
        ClickableItem addClick = new ClickableItem(new ShopItemStack(addStock), inv, p);
        addClick.addLeftClickAction(new LeftClickAction() {
                                        @Override
                                        public void onAction(InventoryClickEvent e) {
                                            final AnvilManager man = new AnvilManager(p);
                                            Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("BetterShops"), new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            String name = man.call();
                                                            int amt;
                                                            int limit = (int) Config.getObject("StockLimit");
                                                            if (limit != 0 && it.getStock() >= limit) {
                                                                p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "HighStock"));
                                                                return;
                                                            }
                                                            try {
                                                                amt = Integer.parseInt(name);
                                                            } catch (Exception ex) {
                                                                if (name.equalsIgnoreCase("all")) {


                                                                    if (limit != 0 && Stocks.getNumberInInventory(it, p, shop) + it.getStock() > limit) {
                                                                        Stocks.addStock(it, limit - it.getStock(), p, shop);
                                                                        p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "StopStock"));
                                                                    } else {
                                                                        Stocks.addAll(it, shop, p);
                                                                    }
                                                                    if (shop.isHoloShop()) {
                                                                        ShopHologram h = shop.getHolographicShop();
                                                                        h.updateItemLines(h.getItemLine(), false);
                                                                    }
                                                                } else {
                                                                    p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "InvalidNumber"));
                                                                    draw(p, page, obj);
                                                                    return;
                                                                }
                                                                draw(p, page, obj);
                                                                return;

                                                            }


                                                            if (limit != 0 && amt + it.getStock() > limit) {
                                                                Stocks.addStock(it, limit - it.getStock(), p, shop);
                                                                p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "StopStock"));
                                                            } else {
                                                                Stocks.addStock(it, amt, p, shop);
                                                            }

                                                            draw(p, page, obj);
                                                        }
                                                    }

                                            );
                                        }
                                    }

        );

        ItemStack removeStock = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 14);
        ItemMeta removeStockMeta = removeStock.getItemMeta();
        removeStockMeta.setDisplayName(Language.getString("ItemTexts", "RemoveStockDisplayName"));
        removeStockMeta.setLore(Arrays.asList(Language.getString("ItemTexts", "RemoveStockLore")));
        removeStock.setItemMeta(removeStockMeta);
        ClickableItem remClick = new ClickableItem(new ShopItemStack(removeStock), inv, p);
        remClick.addLeftClickAction(new LeftClickAction() {
                                        @Override
                                        public void onAction(InventoryClickEvent e) {
                                            final AnvilManager man = new AnvilManager(p);
                                            Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("BetterShops"), new Runnable() {
                                                @Override
                                                public void run() {
                                                    String name = man.call();
                                                    int amt;
                                                    try {
                                                        amt = Integer.parseInt(name);
                                                    } catch (Exception ex) {
                                                        if (name.equalsIgnoreCase("all")) {
                                                            Stocks.removeAll(it, shop, p);
                                                            if (shop.isHoloShop()) {
                                                                ShopHologram h = shop.getHolographicShop();
                                                                h.updateItemLines(h.getItemLine(), false);
                                                            }
                                                            draw(p, page, obj);
                                                            return;
                                                        } else {
                                                            p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "InvalidNumber"));
                                                            draw(p, page, obj);
                                                            return;
                                                        }
                                                    }


                                                    Stocks.removeStock(it, amt, p, shop);
                                                    draw(p, page, obj);
                                                }
                                            });
                                        }
                                    }

        );

        ItemStack amount = new ItemStack(Material.STAINED_CLAY, 1, (byte) 2);
        ItemMeta amountMeta = amount.getItemMeta();
        amountMeta.setDisplayName(Language.getString("ItemTexts", "AmountDisplayName"));
        amountMeta.setLore(Arrays.asList(Language.getString("ItemTexts", "AmountLore")));
        amount.setItemMeta(amountMeta);
        ClickableItem amtClick = new ClickableItem(new ShopItemStack(amount), inv, p);
        amtClick.addLeftClickAction(new LeftClickAction() {
                                        @Override
                                        public void onAction(InventoryClickEvent e) {
                                            final AnvilManager man = new AnvilManager(p);
                                            Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("BetterShops"), new Runnable() {
                                                @Override
                                                public void run() {
                                                    String name = man.call();

                                                    boolean can;
                                                    int amt = 0;
                                                    try {
                                                        amt = Integer.parseInt(name);
                                                        can = true;
                                                    } catch (Exception ex) {
                                                        p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "InvalidNumber"));
                                                        can = false;
                                                    }

                                                    if (can) {

                                                        if (amt > 0 && amt <= 2304) {
                                                            it.setObject("Amount", amt);

                                                            if (it.getLiveEco()) {
                                                                it.getSister().setObject("Amount", amt);
                                                            }

                                                            if (shop.isHoloShop()) {
                                                                ShopHologram h = shop.getHolographicShop();
                                                                h.updateItemLines(h.getItemLine(), false);
                                                            }

                                                            p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "ChangeAmount"));
                                                            draw(p, page, obj);
                                                        } else {
                                                            draw(p, page, obj);
                                                            p.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "HighAmount"));
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }

        );

        ItemStack infinite = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 14);
        ItemMeta infiniteMeta = infinite.getItemMeta();
        if (it.isInfinite())

        {
            infiniteMeta.setDisplayName(Language.getString("ItemTexts", "InfiniteDisplayNameOn"));
            infinite = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 5);
        } else

        {
            infiniteMeta.setDisplayName(Language.getString("ItemTexts", "InfiniteDisplayNameOff"));
        }

        infiniteMeta.setLore(Arrays.asList(Language.getString("ItemTexts", "InfiniteLore")));
        infinite.setItemMeta(infiniteMeta);
        ClickableItem infClick = new ClickableItem(new ShopItemStack(infinite), inv, p);
        infClick.addLeftClickAction(new LeftClickAction() {
                                        @Override
                                        public void onAction(InventoryClickEvent e) {
                                            it.setObject("Infinite", !it.isInfinite());
                                            draw(p, page, obj);
                                        }
                                    }

        );

        ItemStack eco = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta ecoMeta = eco.getItemMeta();
        ecoMeta.setDisplayName(Language.getString("ItemTexts", "LiveEco"));
        ecoMeta.setLore(Arrays.asList(Language.getString("ItemTexts", "LiveEcoLore")));
        eco.setItemMeta(ecoMeta);
        ClickableItem ecoClick = new ClickableItem(new ShopItemStack(eco), inv, p);
        ecoClick.addLeftClickAction(new LeftClickAction() {
                                        @Override
                                        public void onAction(InventoryClickEvent e) {
                                            shop.getMenu(MenuType.LIVE_ECONOMY).draw(p, page, it);
                                        }
                                    }

        );

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(Language.getString("MainGUI", "BackArrow"));
        back.setItemMeta(backMeta);
        ClickableItem backClick = new ClickableItem(new ShopItemStack(back), inv, p);
        backClick.addLeftClickAction(new LeftClickAction() {
                                         @Override
                                         public void onAction(InventoryClickEvent e) {
                                             shop.getMenu(MenuType.OWNER_BUYING).draw(p, page);
                                         }
                                     }

        );

        inv.setItem(0, back);

        inv.setItem(4, it.getItem());

        if ((Boolean) Config.getObject("UseLiveEco") && Permissions.hasLiveEcoPerm(p))

        {
            inv.setItem(8, eco);
        }

        inv.setItem(inv.firstEmpty(), nam);
        inv.setItem(inv.firstEmpty(), desc);

        inv.setItem(inv.firstEmpty(), amount);
        if (p.isOp() || (Boolean) Config.getObject("Permissions") && Permissions.hasInfinitePerm(p)) {
            inv.setItem(inv.firstEmpty() + 1, infinite);
        }


        inv.setItem(32, removeStock);
        inv.setItem(33, removeStock);
        inv.setItem(34, removeStock);
        inv.setItem(35, removeStock);


        inv.setItem(27, addStock);
        inv.setItem(28, addStock);
        inv.setItem(29, addStock);
        inv.setItem(30, addStock);

       new BukkitRunnable() {

            @Override
            public void run() {
                p.openInventory(inv);
            }

        }.runTask(Bukkit.getPluginManager().getPlugin("BetterShops"));

    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}
