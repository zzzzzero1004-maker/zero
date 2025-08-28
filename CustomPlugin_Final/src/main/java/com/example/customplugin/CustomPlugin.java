package com.example.customplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomPlugin extends JavaPlugin implements Listener {

    private ItemStack baseClock;

    @Override
    public void onEnable() {
        getLogger().info("CustomPlugin Enabled!");
        Bukkit.getPluginManager().registerEvents(this, this);
        createBaseClock();
        createRecipes();
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomPlugin Disabled!");
    }

    // 기본템 명령어
    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("기본템")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                giveStarterKit(player);
                player.sendMessage(ChatColor.GREEN + "기본템이 지급되었습니다!");
            }
            return true;
        }
        return false;
    }

    private void giveStarterKit(Player player) {
        // 철 갑옷 (보호 II, 내구성 X)
        ItemStack chest = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta chestMeta = chest.getItemMeta();
        chestMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true);
        chestMeta.addEnchant(Enchantment.DURABILITY, 10, true);
        chest.setItemMeta(chestMeta);

        // 철 검 (날카로움 II, 내구성 X)
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        ItemMeta swordMeta = sword.getItemMeta();
        swordMeta.addEnchant(Enchantment.DAMAGE_ALL, 2, true);
        swordMeta.addEnchant(Enchantment.DURABILITY, 10, true);
        sword.setItemMeta(swordMeta);

        // 시계 (베이스 시계)
        ItemStack clock = baseClock.clone();

        player.getInventory().addItem(chest, sword, clock);
    }

    // 베이스 시계 생성
    private void createBaseClock() {
        baseClock = new ItemStack(Material.CLOCK);
        ItemMeta meta = baseClock.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "베이스 시계");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        baseClock.setItemMeta(meta);
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().isSimilar(baseClock)) {
            Player player = event.getPlayer();
            Inventory gui = Bukkit.createInventory(null, 9, ChatColor.DARK_BLUE + "베이스 설정");
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "베이스 설정");
            paper.setItemMeta(meta);
            gui.addItem(paper);
            player.openInventory(gui);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.DARK_BLUE + "베이스 설정")) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PAPER) {
                Player player = (Player) event.getWhoClicked();
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "베이스가 설정되었습니다!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

                // 순간이동 아이템 추가
                ItemStack tpPaper = new ItemStack(Material.PAPER);
                ItemMeta tpMeta = tpPaper.getItemMeta();
                tpMeta.setDisplayName(ChatColor.AQUA + "베이스 순간이동");
                tpPaper.setItemMeta(tpMeta);
                player.getInventory().addItem(tpPaper);
            }
        } else if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PAPER &&
                   event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.AQUA + "베이스 순간이동")) {
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();
            new BukkitRunnable() {
                int countdown = 5;
                @Override
                public void run() {
                    if (countdown == 0) {
                        player.teleport(player.getWorld().getSpawnLocation());
                        player.sendMessage(ChatColor.AQUA + "베이스로 순간이동 되었습니다!");
                        cancel();
                    } else {
                        player.sendMessage(ChatColor.YELLOW + countdown + "초 후 순간이동!");
                        countdown--;
                    }
                }
            }.runTaskTimer(this, 0, 20);
        }
    }

    // 낙사 무효 (플레이어만)
    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    // 좀비 강화
    @EventHandler
    public void onZombieSpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.ZOMBIE) {
            Zombie zombie = (Zombie) event.getEntity();
            zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(200.0); // 체력 10배
            zombie.setHealth(200.0);
        }
    }

    // 커스텀 레시피 추가
    private void createRecipes() {
        // 네더라이트 검
        ItemStack opSword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta swordMeta = opSword.getItemMeta();
        swordMeta.addEnchant(Enchantment.DAMAGE_ALL, 20, true);
        swordMeta.addEnchant(Enchantment.FIRE_ASPECT, 10, true);
        swordMeta.addEnchant(Enchantment.DAMAGE_UNDEAD, 20, true);
        swordMeta.addEnchant(Enchantment.DAMAGE_ARTHROPODS, 20, true);
        swordMeta.addEnchant(Enchantment.SWEEPING_EDGE, 20, true);
        swordMeta.addEnchant(Enchantment.DURABILITY, 100, true);
        swordMeta.addEnchant(Enchantment.MENDING, 1, true);
        opSword.setItemMeta(swordMeta);

        NamespacedKey swordKey = new NamespacedKey(this, "op_sword");
        ShapedRecipe swordRecipe = new ShapedRecipe(swordKey, opSword);
        swordRecipe.shape(" N ", " N ", " S ");
        swordRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        swordRecipe.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(swordRecipe);

        // 불사의 토템
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        NamespacedKey totemKey = new NamespacedKey(this, "custom_totem");
        ShapedRecipe totemRecipe = new ShapedRecipe(totemKey, totem);
        totemRecipe.shape("G G", " E ", "G G");
        totemRecipe.setIngredient('G', Material.GOLD_INGOT);
        totemRecipe.setIngredient('E', Material.EMERALD);
        Bukkit.addRecipe(totemRecipe);
    }
}
