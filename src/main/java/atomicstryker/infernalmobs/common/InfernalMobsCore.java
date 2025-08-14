package atomicstryker.infernalmobs.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.WorldEvent;
import atomicstryker.infernalmobs.common.mods.MM_1UP;
import atomicstryker.infernalmobs.common.mods.MM_Alchemist;
import atomicstryker.infernalmobs.common.mods.MM_Arsonist;
import atomicstryker.infernalmobs.common.mods.MM_Berserk;
import atomicstryker.infernalmobs.common.mods.MM_Blastoff;
import atomicstryker.infernalmobs.common.mods.MM_Bomber;
import atomicstryker.infernalmobs.common.mods.MM_Bulwark;
import atomicstryker.infernalmobs.common.mods.MM_Choke;
import atomicstryker.infernalmobs.common.mods.MM_Cloaking;
import atomicstryker.infernalmobs.common.mods.MM_Darkness;
import atomicstryker.infernalmobs.common.mods.MM_Ender;
import atomicstryker.infernalmobs.common.mods.MM_Exhaust;
import atomicstryker.infernalmobs.common.mods.MM_Fiery;
import atomicstryker.infernalmobs.common.mods.MM_Ghastly;
import atomicstryker.infernalmobs.common.mods.MM_Gravity;
import atomicstryker.infernalmobs.common.mods.MM_Lifesteal;
import atomicstryker.infernalmobs.common.mods.MM_Ninja;
import atomicstryker.infernalmobs.common.mods.MM_Poisonous;
import atomicstryker.infernalmobs.common.mods.MM_Quicksand;
import atomicstryker.infernalmobs.common.mods.MM_Regen;
import atomicstryker.infernalmobs.common.mods.MM_Rust;
import atomicstryker.infernalmobs.common.mods.MM_Sapper;
import atomicstryker.infernalmobs.common.mods.MM_Sprint;
import atomicstryker.infernalmobs.common.mods.MM_Sticky;
import atomicstryker.infernalmobs.common.mods.MM_Storm;
import atomicstryker.infernalmobs.common.mods.MM_Vengeance;
import atomicstryker.infernalmobs.common.mods.MM_Weakness;
import atomicstryker.infernalmobs.common.mods.MM_Webber;
import atomicstryker.infernalmobs.common.mods.MM_Wither;
import atomicstryker.infernalmobs.common.mods.MM_Webber.TrackInfo;
import atomicstryker.infernalmobs.common.network.AirPacket;
import atomicstryker.infernalmobs.common.network.HealthPacket;
import atomicstryker.infernalmobs.common.network.KnockBackPacket;
import atomicstryker.infernalmobs.common.network.MobModsPacket;
import atomicstryker.infernalmobs.common.network.NetworkHelper;
import atomicstryker.infernalmobs.common.network.VelocityPacket;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.registry.GameData;
import lotr.common.LOTRConfig;
import lotr.common.LOTRLevelData;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.item.LOTRItemModifierTemplate;

@Mod(modid = InfernalMobsCore.MODID, name = InfernalMobsCore.MODNAME, version = InfernalMobsCore.VERSION, dependencies = "after:lotr")
public class InfernalMobsCore
{
	public static final String MODID = "infernalmobslotr";
	public static final String MODNAME = "Infernal Mobs LOTR";
	public static final String VERSION = "1.7.3";
	
    private final long existCheckDelay = 500L;

    private long nextExistCheckTimeServer;
    private long nextExistCheckTimeClient;

    /**
     * Array of ItemStacks
     */
    private ArrayList<ItemStack> dropIdListElite;
    private ArrayList<ItemStack> dropIdListUltra;
    private ArrayList<ItemStack> dropIdListInfernal;

    private HashMap<String, Boolean> classesAllowedMap;
    private HashMap<String, Boolean> classesForcedMap;
    private HashMap<String, Float> classesHealthMap;
    private boolean useSimpleEntityClassNames;
    private boolean disableHealthBar;
    private double modHealthFactor;
    
    private Entity infCheckA;
    private Entity infCheckB;

    @Instance("infernalmobslotr")
    private static InfernalMobsCore instance;

    public static InfernalMobsCore instance()
    {
        return instance;
    }

    public String getNBTTag()
    {
        return "InfernalMobsMod";
    }

    private ArrayList<Class<? extends MobModifier>> mobMods;

    private int eliteRarity;
    private int ultraRarity;
    private int infernoRarity;
    
    public Configuration config;

    @SidedProxy(clientSide = "atomicstryker.infernalmobs.client.InfernalMobsClient", serverSide = "atomicstryker.infernalmobs.common.InfernalMobsServer")
    public static ISidedProxy proxy;

    public NetworkHelper networkHelper;

    private double maxDamage;
    
    public static int rarityNpcAlly = 1;
    public static int rarityNpcEnemy = 1;
    
    public static double lootMaxDamagePercent;
    public static double lootEnchantChance;
    public static int modifiersPerDrop;
    public static int lootEnchantPowerChange;
    public static double lootChance;
    public static boolean lootRequireDirectPlayerKill;
    
    public static float abilityRewardChance;
    public static boolean allowDestructiveAbilityRewards;
    
    public static boolean lotr;

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
    	lotr = Loader.isModLoaded("lotr");
    	
        dropIdListElite = new ArrayList<ItemStack>();
        dropIdListUltra = new ArrayList<ItemStack>();
        dropIdListInfernal = new ArrayList<ItemStack>();
        //nextExistCheckTimeServer = System.currentTimeMillis();
        classesAllowedMap = new HashMap<String, Boolean>();
        classesForcedMap = new HashMap<String, Boolean>();
        classesHealthMap = new HashMap<String, Float>();

        config = new Configuration(evt.getSuggestedConfigurationFile());
        config.load();
        loadMods();

        proxy.preInit();
        FMLCommonHandler.instance().bus().register(this);
        networkHelper = new NetworkHelper("AS_IF", MobModsPacket.class, HealthPacket.class, VelocityPacket.class, KnockBackPacket.class, AirPacket.class);
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(new EntityEventHandler());
        MinecraftForge.EVENT_BUS.register(new SaveEventHandler());

        proxy.load();

        System.out.println("InfernalMobsCore load() completed! Modifiers ready: " + mobMods.size());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt)
    {
        // lets use postInit so mod Blocks and Items are present
        loadConfig();
    }
    
    /* // temp fix for water bug
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedInEvent event) {
    	InfernalMobsCore.instance().sendAirPacket((EntityPlayerMP) event.player, -999);
	}*/
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent evt)
    {
    	evt.registerServerCommand(new InfernalCommandFindEntityClass());
        evt.registerServerCommand(new InfernalCommandSpawnInfernal());
    }
    
    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent evt)
    {
    	proxy.getRareMobs(false).clear();
    }

    /**
     * Registers the MobModifier classes for consideration
     * 
     * @param config
     */
    private void loadMods()
    {
        mobMods = new ArrayList<Class<? extends MobModifier>>();

        mobMods.add(MM_1UP.class);
        mobMods.add(MM_Alchemist.class);
        mobMods.add(MM_Berserk.class);
        mobMods.add(MM_Blastoff.class);
        mobMods.add(MM_Bulwark.class);
        mobMods.add(MM_Choke.class);
        mobMods.add(MM_Cloaking.class);
        mobMods.add(MM_Darkness.class);
        mobMods.add(MM_Ender.class);
        mobMods.add(MM_Exhaust.class);
        mobMods.add(MM_Fiery.class);
        mobMods.add(MM_Ghastly.class);
        mobMods.add(MM_Gravity.class);
        mobMods.add(MM_Lifesteal.class);
        mobMods.add(MM_Ninja.class);
        mobMods.add(MM_Poisonous.class);
        mobMods.add(MM_Quicksand.class);
        mobMods.add(MM_Regen.class);
        mobMods.add(MM_Rust.class);
        mobMods.add(MM_Sapper.class);
        mobMods.add(MM_Sprint.class);
        mobMods.add(MM_Sticky.class);
        mobMods.add(MM_Storm.class);
        mobMods.add(MM_Vengeance.class);
        mobMods.add(MM_Weakness.class);
        mobMods.add(MM_Webber.class);
        mobMods.add(MM_Wither.class);
        mobMods.add(MM_Arsonist.class);
        mobMods.add(MM_Bomber.class);

        Iterator<Class<? extends MobModifier>> iter = mobMods.iterator();
        while (iter.hasNext())
        {
            Class<?> c = iter.next();
            if (!config.get(Configuration.CATEGORY_GENERAL, c.getSimpleName() + " enabled", true).getBoolean(true))
            {
                iter.remove();
            }
        }
        
        //if(config.hasChanged()) config.save();
    }

    /**
     * Forge Config file
     */
    
    private void loadConfig()
    {
        //config.load();
    	
    	String categoryGeneral = Configuration.CATEGORY_GENERAL;

        eliteRarity =
                Integer.parseInt(config.get(categoryGeneral, "eliteRarity", 15, "One in THIS many Mobs will become atleast rare")
                        .getString());
        ultraRarity =
                Integer.parseInt(config.get(categoryGeneral, "ultraRarity", 7,
                        "One in THIS many already rare Mobs will become atleast ultra").getString());
        infernoRarity =
                Integer.parseInt(config.get(categoryGeneral, "infernoRarity", 7,
                        "One in THIS many already ultra Mobs will become infernal").getString());
        
        double rarityFactorNpcAlly = config.get(categoryGeneral, "rarityFactorNpcAlly", 0.2,
                "How much percent of the rare NPCs can be allies").getDouble();
        
        rarityNpcAlly = rarityFactorNpcAlly > 0F ? (int) (eliteRarity / rarityFactorNpcAlly) : 0;
        
        double rarityFactorNpcEnemy = config.get(categoryGeneral, "rarityFactorNpcEnemy", 1.0,
                "How much percent of the rare NPCs can be enemies").getDouble();
        
        rarityNpcEnemy = rarityFactorNpcEnemy > 0F ? (int) (eliteRarity / rarityFactorNpcEnemy) : 0;
        
        
        
        useSimpleEntityClassNames =
            config.get(categoryGeneral, "useSimpleEntityClassnames", true,
                        "Use Entity class names instead of ingame Entity names for the config").getBoolean();
        disableHealthBar =
            config.get(categoryGeneral, "disableGUIoverlay", false, "Disables the ingame Health and Name overlay").getBoolean();
       
        modHealthFactor =
            config.get(categoryGeneral, "mobHealthFactor", 1.0D, "Multiplier applied ontop of all of the modified Mobs health").getDouble();

        parseItemsForList(
                config.get(
                		categoryGeneral,
                        "droppedItemIDsElite",
                        // 
                        "lotr:item.ironCrossbow,lotr:item.bronzeCrossbow,lotr:item.legsBronze,lotr:item.helmetBronze,bow,iron_shovel,iron_pickaxe,iron_axe,iron_sword,iron_hoe,chainmail_helmet,chainmail_chestplate,chainmail_leggings,chainmail_boots,iron_helmet,iron_chestplate,iron_leggings,iron_boots,cookie-0-6,enchanted_book"
                       // lotr drops
                        +"lotr:item.modTemplate"
                        ,
                        "List of equally likely to drop Items for Elites, seperated by commas, syntax: ID-meta-stackSize-stackSizeRandomizer, everything but ID is optional, see changelog")
                        .getString(), instance.dropIdListElite);

        parseItemsForList(
                config.get(
                		categoryGeneral,
                        "droppedItemIDsUltra",
                        // vanilla only items
                        //"golden_apple,blaze_powder-0-3"
                        "bow,iron_hoe,chainmail_helmet,chainmail_chestplate,chainmail_leggings,chainmail_boots,iron_helmet,iron_chestplate,iron_leggings,iron_boots,golden_helmet,golden_chestplate,golden_leggings,golden_boots,enchanted_book"
                        +"lotr:item.ironCrossbow,lotr:item.modTemplate,lotr:item.mithrilNugget-0-1-2,lotr:item.mithrilMail-0-0-1,lotr:item.spearMithril-1000,lotr:item.mithrilRing,lotr:item.daggerMithril-1000,lotr:item.shovelMithril-1000,lotr:item.diamond-0-2-2,lotr:item.opal-0-3-9,lotr:item.sapphire-0-2-2,lotr:item.ruby-0-2-2,lotr:item.coin-2-1",
                        
                		"List of equally likely to drop Items for Ultras, seperated by commas, syntax: ID-meta-stackSize-stackSizeRandomizer, everything but ID is optional, see changelog")
                        .getString(), instance.dropIdListUltra);

        parseItemsForList(
                config.get(
                		categoryGeneral,
                        "droppedItemIDsInfernal",
                        // vanilla only items
                        //"diamond-0-3,diamond_sword,diamond_shovel,diamond_pickaxe,diamond_axe,diamond_hoe,ender_pearl,diamond_helmet,diamond_chestplate,diamond_leggings,diamond_boots"
                       "bow,chainmail_helmet,chainmail_chestplate,chainmail_leggings,chainmail_boots,enchanted_book,"
                       // lotr
                       +"lotr:item.ironCrossbow,lotr:item.modTemplate,lotr:item.mithrilNugget-0-1-2,lotr:item.mithrilMail-0-1,lotr:item.spearMithril-1000,lotr:item.mithrilRing,lotr:item.daggerMithril-1000,lotr:item.shovelMithril-1000,lotr:item.diamond-0-2-2,lotr:item.opal-0-3-9,lotr:item.sapphire-0-2-2,lotr:item.ruby-0-2-2,lotr:item.coin-2-1-1",
                        "List of equally likely to drop Items for Infernals, seperated by commas, syntax: ID-meta-stackSize-stackSizeRandomizer, everything but ID is optional, see changelog")
                        .getString(), instance.dropIdListInfernal);

        maxDamage =
             config.get(categoryGeneral, "maxOneShotDamage", 10.0D,
                       "highest amount of damage an Infernal Mob or reflecting Mod will do in a single strike").getDouble();

        
	        // workaround for some issues with mobs that have random health scaling
	        String key = "entitybasehealth";
	       
	        config.get(key, useSimpleEntityClassNames ? "LOTREntityMirkwoodSpider" : "lotr.common.entity.npc.LOTREntityMirkwoodSpider", 18D);
	        config.get(key, useSimpleEntityClassNames ? "LOTREntityUtumnoIceSpider" : "lotr.common.entity.npc.LOTREntityUtumnoIceSpider", 21D);
	        config.get(key, useSimpleEntityClassNames ? "LOTREntityMordorSpider" : "lotr.common.entity.npc.LOTREntityMordorSpider", 24D);
	        config.get(key, useSimpleEntityClassNames ? "LOTREntityDesertScorpion" : "lotr.common.entity.animal.LOTREntityDesertScorpion", 15D);
	        config.get(key, useSimpleEntityClassNames ? "LOTREntityJungleScorpion" : "lotr.common.entity.animal.LOTREntityJungleScorpion", 18D);
        
	        
	    String categoryLoot = "loot";
	    lootMaxDamagePercent = config.get(categoryLoot, "maxDamagePercent", 0.1D, "How much the loot may be damaged").getDouble();
	    lootEnchantChance = config.get(categoryLoot, "enchantChance", 0.5D, "Chance of whether loot will get enchanted").getDouble();
	    modifiersPerDrop = config.get(categoryLoot, "modifiersPerDrop", 8, "how many modifiers will allow a second drop").getInt();
	    lootEnchantPowerChange = config.get(categoryLoot, "enchantPowerChange", -1, "reduce or increase max enchant level from the current item, usually 30 (like enchanting table has 30 for example)").getInt();
	    lootRequireDirectPlayerKill = config.get(categoryLoot, "requireDirectPlayerKill", false).getBoolean();
	    lootChance = config.get(categoryLoot, "dropChance", 1.0D, "whether something drops at all").getDouble(); // 0.6
	    
	    abilityRewardChance = (float) config.get(categoryGeneral, "abilityRewardChance", 0.05D, "Chance for Hired Units to learn abilities when killing a boss").getDouble();
	    allowDestructiveAbilityRewards = config.get(categoryGeneral, "canLearnDestructiveAbilities", true, "Whether Hired Units can learn destructive Abilities").getBoolean();
	    
	    if(config.hasChanged()) config.save();
    }

    private void parseItemsForList(String itemIDs, ArrayList<ItemStack> list)
    {
        Random rand = new Random();
        itemIDs = itemIDs.trim();
        for (String s : itemIDs.split(","))
        {
            String[] meta = s.split("-");

            Object itemOrBlock = parseOrFind(meta[0]);
            if (itemOrBlock != null)
            {
                int imeta = (meta.length > 1) ? Integer.parseInt(meta[1]) : 0;
                int stackSize = (meta.length > 2) ? Integer.parseInt(meta[2]) : 1;
                int randomizer = (meta.length > 3) ? Integer.parseInt(meta[3]) + 1 : 1;
                if (randomizer < 1)
                {
                    randomizer = 1;
                }

                if (itemOrBlock instanceof Block)
                {
                    list.add(new ItemStack(((Block) itemOrBlock), stackSize + rand.nextInt(randomizer), imeta));
                }
                else
                {
                    list.add(new ItemStack(((Item) itemOrBlock), stackSize + rand.nextInt(randomizer), imeta));
                }
            }
        }
    }

    private Object parseOrFind(String s)
    {
        Item item = GameData.getItemRegistry().getObject(s);
        if (item != null)
        {
            return item;
        }

        Block block = GameData.getBlockRegistry().getObject(s);
        if (block != Blocks.air)
        {
            return block;
        }
        return null;
    }
    
    public static EntityPlayer getNearestPlayer(Entity entity) {
		EntityPlayer result = null;
		int lowestdistance = Integer.MAX_VALUE;
		@SuppressWarnings("unchecked")
		List<EntityPlayer> list = entity.worldObj.playerEntities;
		for(EntityPlayer player : list) {
			int distance = (int) player.getDistanceSqToEntity(entity);
			if(distance < lowestdistance) {
				lowestdistance = distance;
    			result = player;
			}
		}
		return result;
		
	}
    
    
    
    public int getRarityForSpawning(Entity entity) {
    	if(lotr && entity instanceof LOTREntityNPC && !((LOTREntityNPC) entity).hiredNPCInfo.isActive && !((LOTREntityNPC) entity).isCivilianNPC()) {
    		// factor = 1F // could make npcs uncontrolled rare
    		
    		EntityPlayer player = getNearestPlayer(entity);
    		if(player != null) {
    			boolean isEnemy = LOTRLevelData.getData(player).getAlignment(((LOTREntityNPC) entity).getFaction()) < 0F;
				if(rarityNpcAlly > 0 || isEnemy) {
					return isEnemy ? rarityNpcEnemy : rarityNpcAlly;
				}
    		}
    	}
    	else if (entity instanceof EntityMob || entity instanceof IMob) {
    		return eliteRarity;
    	}
    	
    	return 0;
    }
    
    public boolean shouldMakeInfernal(EntityLivingBase entity) {
    	int rarity = getRarityForSpawning(entity);
    	return rarity > 0 && instance.checkEntityClassAllowed(entity) && (instance.checkEntityClassForced(entity) || entity.worldObj.rand.nextInt(rarity) == 0);
    }

    /**
     * Called when an Entity is spawned by natural (Biome Spawning) means, turn
     * them into Elites here
     * 
     * @param entity
     *            Entity in question
     */
    
    public void processEntitySpawn(EntityLivingBase entity)
    {
        if (!entity.worldObj.isRemote)
        {
            if (!getIsRareEntity(entity, false))
            {
            	if(shouldMakeInfernal(entity)) {
                
                
                
                    MobModifier mod = instance.createMobModifiers(entity);
                    if (mod != null)
                    {
                        proxy.getRareMobs(false).put(entity, mod);
                        mod.onSpawningComplete(entity);
                        
                    }
                }
            }
        }
    }

    private String getEntityNameSafe(Entity entity)
    {
        String result;
        try
        {
            result = EntityList.getEntityString(entity);
        }
        catch (Exception e)
        {
            result = entity.getClass().getSimpleName();
            System.err.println("Entity of class " + result
                    + " crashed when EntityList.getEntityString was queried, for shame! Using classname instead.");
            System.err.println("If this message is spamming too much for your taste set useSimpleEntityClassnames true in your Infernal Mobs config");
        }
        return result;
    }

    private boolean checkEntityClassAllowed(EntityLivingBase entity)
    {
        String entName = useSimpleEntityClassNames ? entity.getClass().getSimpleName() : getEntityNameSafe(entity);
        if (classesAllowedMap.containsKey(entName))
        {
            return classesAllowedMap.get(entName);
        }

        boolean result = config.get("permittedentities", entName, true).getBoolean(true);
        if(config.hasChanged()) config.save();
        classesAllowedMap.put(entName, result);

        return result;
    }

    private boolean checkEntityClassForced(EntityLivingBase entity)
    {
        String entName = useSimpleEntityClassNames ? entity.getClass().getSimpleName() : getEntityNameSafe(entity);
        if (classesForcedMap.containsKey(entName))
        {
            return classesForcedMap.get(entName);
        }

        boolean result = config.get("entitiesalwaysinfernal", entName, false).getBoolean(false);
        if(config.hasChanged()) config.save();
        classesForcedMap.put(entName, result);

        return result;
    }

    public float getMobClassMaxHealth(EntityLivingBase entity)
    {
        String entName = useSimpleEntityClassNames ? entity.getClass().getSimpleName() : getEntityNameSafe(entity);
        if (classesHealthMap.containsKey(entName))
        {
            return classesHealthMap.get(entName);
        }

        float result = (float) config.get("entitybasehealth", entName, entity.getMaxHealth()).getDouble(entity.getMaxHealth());
        if(config.hasChanged()) config.save();
        classesHealthMap.put(entName, result);

        return result;
    }

    /**
     * Allows setting Entity Health past the hardcoded getMaxHealth() constraint
     * 
     * @param entity
     *            Entity instance whose health you want changed
     * @param amount
     *            value to set
     */
    public void setEntityHealthPastMax(EntityLivingBase entity, float amount)
    {
        entity.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(amount);
        entity.setHealth(amount);
        instance.sendHealthPacket(entity, amount); // jonosa test was disabled
    }

    /**
     * Decides on what, if any, of the possible Modifications to apply to the
     * Entity
     * 
     * @param entity
     *            Target Entity
     * @return null or the first linked MobModifier instance for the Entity
     */
    @SuppressWarnings("unchecked")
    private MobModifier createMobModifiers(EntityLivingBase entity)
    {
        /* 2-5 modifications standard */
        int number = 2 + entity.worldObj.rand.nextInt(3);
        /* lets just be lazy and scratch mods off a list copy */
        ArrayList<Class<? extends MobModifier>> possibleMods = (ArrayList<Class<? extends MobModifier>>) mobMods.clone();

        if (entity.worldObj.rand.nextInt(ultraRarity) == 0) // ultra mobs
        {
            number += 3 + entity.worldObj.rand.nextInt(2);

            if (entity.worldObj.rand.nextInt(infernoRarity) == 0) // infernal
                                                                  // mobs
            {
                number += 3 + entity.worldObj.rand.nextInt(2);
            }
        }

        MobModifier lastMod = null;
        while (number > 0 && !possibleMods.isEmpty()) // so long we need more
                                                      // and have some
        {
            /* random index of mod list */
            int index = entity.worldObj.rand.nextInt(possibleMods.size());
            MobModifier nextMod = null;

            /*
             * instanciate using one of the two constructors, chainlinking
             * modifiers as we go
             */
            try
            {
                if (lastMod == null)
                {
                    nextMod = possibleMods.get(index).getConstructor(new Class[] { EntityLivingBase.class }).newInstance(entity);
                }
                else
                {
                    nextMod =
                            possibleMods.get(index).getConstructor(new Class[] { EntityLivingBase.class, MobModifier.class })
                                    .newInstance(entity, lastMod);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            boolean allowed = true;
            if (nextMod != null && nextMod.getBlackListMobClasses() != null)
            {
                for (Class<?> cl : nextMod.getBlackListMobClasses())
                {
                    if (entity.getClass().isAssignableFrom(cl))
                    {
                        allowed = false;
                        break;
                    }
                }
            }
            if (lastMod != null)
            {
                if (lastMod.getModsNotToMixWith() != null)
                {
                    for (Class<?> cl : lastMod.getModsNotToMixWith())
                    {
                        if (lastMod.containsModifierClass(cl))
                        {
                            allowed = false;
                            break;
                        }
                    }
                }
            }

            /* scratch mod off list */
            possibleMods.remove(index);

            if (allowed) // so can we use it?
            {
                // link it, note that we need one less, repeat
                lastMod = nextMod;
                number--;
            }
        }

        return lastMod;
    }

    /**
     * Converts a String to MobModifier instances and connects them to an Entity
     * 
     * @param entity
     *            Target Entity
     * @param savedMods
     *            String depicting the MobModifiers, equal to the ingame Display
     */
    public void addEntityModifiersByString(EntityLivingBase entity, String savedMods, boolean client)
    {
    	// only duplicates possilbe would be from server to client, and those we want to override anyway
        //if (!getIsRareEntity(entity, client))
        //{
            MobModifier mod = stringToMobModifiers(entity, savedMods);
            if (mod != null)
            {
            	Map<EntityLivingBase, MobModifier> rareMobs = proxy.getRareMobs(client);
            	if(rareMobs.containsKey(entity)) {
            		//System.out.println("removed "+rareMobs.remove(entity));
            	}
            	
            	rareMobs.put(entity, mod);
                //System.out.println(proxy.toString());
                //System.out.println(FMLCommonHandler.instance().getEffectiveSide().name());
                mod.onSpawningComplete(entity);
                mod.setHealthAlreadyHacked(entity);
            }
            else
            {
                System.err.println("Infernal Mobs error, could not instantiate modifier "+savedMods);
            }
        //}
    }

    private MobModifier stringToMobModifiers(EntityLivingBase entity, String buffer)
    {
        MobModifier lastMod = null;

        String[] tokens = buffer.split("\\s");
        for (int j = tokens.length - 1; j >= 0; j--)
        {
            String modName = tokens[j];

            MobModifier nextMod = null;
            for (Class<? extends MobModifier> c : mobMods)
            {
                /*
                 * instanciate using one of the two constructors, chainlinking
                 * modifiers as we go
                 */
                try
                {
                    if (lastMod == null)
                    {
                        nextMod = c.getConstructor(new Class[] { EntityLivingBase.class }).newInstance(entity);
                    }
                    else
                    {
                        nextMod = c.getConstructor(new Class[] { EntityLivingBase.class, MobModifier.class }).newInstance(entity, lastMod);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if (nextMod != null && nextMod.modName.equals(modName))
                {
                    /*
                     * Only actually keep the new linked instance if it's what
                     * we wanted
                     */
                    lastMod = nextMod;
                    break;
                }
            }
        }

        return lastMod;
    }

    public static MobModifier getMobModifiers(EntityLivingBase ent, boolean client)
    {
        return proxy.getRareMobs(client).get(ent);
    }

    public static boolean getIsRareEntity(EntityLivingBase ent, boolean client)
    {
        return proxy.getRareMobs(client).containsKey(ent);
    }

    public static void removeEntFromElites(EntityLivingBase entity, boolean client)
    {
        proxy.getRareMobs(client).remove(entity);
    }

    /**
     * Used by the client side to answer to a server packet carrying the Entity
     * ID and mod string
     * 
     * @param world
     *            World the client is in, and the Entity aswell
     * @param entID
     *            unique Entity ID
     * @param mods
     *            MobModifier compliant data String from the server
     */
    public void addRemoteEntityModifiers(World world, int entID, String mods)
    {
        Entity ent = world.getEntityByID(entID);
        if (ent != null)
        {
            addEntityModifiersByString((EntityLivingBase) ent, mods, true);
            // System.out.println("Client added remote infernal mod on entity "+ent+", is now "+mod.getModName());
        }
        else {
        	System.out.println("received for unknown entity");
        }
    }

    public void dropLootForEnt(EntityLivingBase mob, MobModifier mods)
    {
    	if(!lotr || LOTRConfig.enchantingVanilla) {
	        int xpValue = 25;
	        while (xpValue > 0)
	        {
	            int xpDrop = EntityXPOrb.getXPSplit(xpValue);
	            xpValue -= xpDrop;
	            
	            mob.worldObj.spawnEntityInWorld(new EntityXPOrb(mob.worldObj, mob.posX, mob.posY, mob.posZ, xpDrop));
	        }
    	}

        dropRandomEnchantedItems(mob, mods);
    }
    
    private void dropRandomEnchantedItems(EntityLivingBase mob, MobModifier mods)
    {
        int modStr = mods.getModSize();
        /* 0 for elite, 1 for ultra, 2 for infernal */
        int prefix = (modStr <= 5) ? 0 : (modStr <= 10) ? 1 : 2;
        
        boolean enchVanilla = !lotr || LOTRConfig.enchantingVanilla;
        boolean enchLOTR = lotr && LOTRConfig.enchantingLOTR;
        Random rand = mob.getRNG();
        
        while (modStr > 0)
        {
            ItemStack itemStack = getRandomItem(mob, prefix);
            if (itemStack != null)
            {
                Item item = itemStack.getItem();
                
                if (item != null && item instanceof Item)
                {
                    if (enchVanilla && item instanceof ItemEnchantedBook)
                    {
                        itemStack = ((ItemEnchantedBook) item).func_92114_b(rand).theItemId;
                    }
                    else if (enchLOTR && item instanceof LOTRItemModifierTemplate)
                    {
                        itemStack = LOTRItemModifierTemplate.getRandomCommonTemplate(rand);
                    }
                    else
                    {
                    	if(enchVanilla) {
	                        if(rand.nextFloat() <= lootEnchantChance) {
	                        	int usedStr = Math.min(modStr, 5);
	                        	enchantRandomly(rand, itemStack, item.getItemEnchantability(), usedStr);
	                        }
                        
                    	}
                        // damage item
                        if(itemStack.isItemStackDamageable())
                        	itemStack.setItemDamage(itemStack.getItemDamage() + rand.nextInt((int) ((itemStack.getMaxDamage() - itemStack.getItemDamage()) * lootMaxDamagePercent)));
                        
                    }
                }
                EntityItem itemEnt = new EntityItem(mob.worldObj, mob.posX, mob.posY, mob.posZ, itemStack);
                mob.worldObj.spawnEntityInWorld(itemEnt);
                modStr -= modifiersPerDrop; // how many modifiers needed for second drop
            }
            else
            {
                // fixes issue with empty drop lists
                modStr--;
            }
        }
    }

    /**
     * Custom Enchanting Helper
     * 
     * @param rand
     *            Random gen to use
     * @param itemStack
     *            ItemStack to be enchanted
     * @param itemEnchantability
     *            ItemStack max enchantability level
     * @param modStr
     *            MobModifier strength to be used. Should be in range 2-5
     */
    private void enchantRandomly(Random rand, ItemStack itemStack, int itemEnchantability, int modStr)
    {
        int remainStr = (modStr + 1) / 2; // should result in 1-3
        List<?> enchantments = EnchantmentHelper.buildEnchantmentList(rand, itemStack, itemEnchantability - lootEnchantPowerChange);
        if (enchantments != null)
        {
            Iterator<?> iter = enchantments.iterator();
            while (iter.hasNext() && remainStr > 0)
            {
                remainStr--;
                EnchantmentData eData = (EnchantmentData) iter.next();
                itemStack.addEnchantment(eData.enchantmentobj, eData.enchantmentLevel);
            }
        }
    	
    }

    /**
     * @param mob
     *            Infernal Entity
     * @param prefix
     *            0 for Elite rarity, 1 for Ultra and 2 for Infernal
     * @return ItemStack instance to drop to the World
     */
    private ItemStack getRandomItem(EntityLivingBase mob, int prefix)
    {
        ArrayList<ItemStack> list = (prefix == 0) ? instance.dropIdListElite : (prefix == 1) ? instance.dropIdListUltra : instance.dropIdListInfernal;
        return list.size() > 0 ? list.get(mob.worldObj.rand.nextInt(list.size())).copy() : null;
    }

    public void sendVelocityPacket(EntityPlayerMP target, float xVel, float yVel, float zVel)
    {
        if (getIsEntityAllowedTarget(target))
        {
            networkHelper.sendPacketToPlayer(new VelocityPacket(xVel, yVel, zVel), target);
        }
    }

    public void sendKnockBackPacket(EntityPlayerMP target, float xVel, float zVel)
    {
        if (getIsEntityAllowedTarget(target))
        {
            networkHelper.sendPacketToPlayer(new KnockBackPacket(xVel, zVel), target);
        }
    }

    public void sendHealthPacket(EntityLivingBase mob, float health)
    {
        networkHelper.sendPacketToAllAroundPoint(new HealthPacket("", mob.getEntityId(), mob.getHealth(), mob.getMaxHealth()), new TargetPoint(
                mob.dimension, mob.posX, mob.posY, mob.posZ, 32d));
    }

    public void sendHealthRequestPacket(EntityLivingBase mob)
    {
        networkHelper.sendPacketToServer(new HealthPacket(FMLClientHandler.instance().getClient().thePlayer.getGameProfile().getName(), mob
                .getEntityId(), 0f, 0f));
    }
    
    public void sendAirPacket(EntityPlayerMP target, int lastAir, boolean keep)
    {
        if (getIsEntityAllowedTarget(target))
        {
            networkHelper.sendPacketToPlayer(new AirPacket(lastAir, keep), target);
        }
    }

    public void checkExist(boolean client) {

    		
		Map<EntityLivingBase, MobModifier> mobsmap = InfernalMobsCore.proxy.getRareMobs(client);
		List<Entity> removes = new ArrayList<>();
        for (EntityLivingBase mob : mobsmap.keySet()) {
            if (mob.isDead || !mob.worldObj.loadedEntityList.contains(mob))
            {
            	removes.add(mob);
                
            }
        }
        
        for(Entity mob : removes) {
        	//System.out.println("Remove "+mob.getCommandSenderName()+" with ID "+mob.getEntityId()+(client ? " CLIENT" : " SERVER"));
            removeEntFromElites((EntityLivingBase) mob, client);
        }
    }
    
    public static List<TrackInfo> toRemove;
    
    @EventHandler
    public void onServerStop(FMLServerStoppingEvent event) {
    	if(toRemove != null) {
	    	for(TrackInfo info : toRemove) {
	    		if(info.pos != null) {
		    		int x = info.pos.chunkPosX;
					int y = info.pos.chunkPosY;
					int z = info.pos.chunkPosZ;
					
					World world = info.world;
					if(world.getChunkProvider().chunkExists(x >> 4, z >> 4) && world.getBlock(x, y, z) == info.compare) {
						world.setBlockToAir(x, y, z);
					}	
	    		}
	    		else if(info.entity != null) {
    				info.entity.setDead();
    			}
	    	}
	    	toRemove.clear();
    	}
    }
    
    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
    	if(System.currentTimeMillis() > nextExistCheckTimeServer) {
    		nextExistCheckTimeServer = System.currentTimeMillis() + existCheckDelay;
    		checkExist(false);
    	}
    	
    	
        if(event.phase == Phase.START) {
        	infCheckA = null;
            infCheckB = null;
        	
        	if(toRemove != null) {
	        	int ticks = MobModifier.getTickTime();
	        	if((ticks & 16) == 0) { // every 16 ticks
	        		Iterator<TrackInfo> it = toRemove.iterator();
		        	while(it.hasNext()) {
		        		TrackInfo info = it.next();
		        		if(ticks >= info.timeTicks) {
		        			if(info.pos != null) {
			        			int x = info.pos.chunkPosX;
			        			int y = info.pos.chunkPosY;
			        			int z = info.pos.chunkPosZ;
			        			World world = info.world;
			        			if(world.getChunkProvider().chunkExists(x >> 4, z >> 4) && world.getBlock(x, y, z) == info.compare) {
			        				world.setBlockToAir(x, y, z);
			        			}
		        			}
		        			else if(info.entity != null) {
		        				info.entity.setDead();
		        			}
		        			
		        			it.remove();
		        		}
		        	}
	        	}
        	}
        }
    }
    
    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
    	if(System.currentTimeMillis() > nextExistCheckTimeClient) {
    		nextExistCheckTimeClient = System.currentTimeMillis() + existCheckDelay;
    		checkExist(true);
    	}
    }

    public boolean getIsHealthBarDisabled()
    {
        return disableHealthBar;
    }

    public double getMobModHealthFactor()
    {
        return modHealthFactor;
    }
    
    public float getLimitedDamage(float test)
    {
        return (float) Math.min(test, maxDamage);
    }

    public boolean getIsEntityAllowedTarget(Entity entity)
    {
        return !(entity instanceof FakePlayer);
    }
    
    /**
     * By caching the last reflection pairing we make sure it doesn't trigger more than once (reflections battling each other, infinite loop, crash)
     * @return true when inf loop is suspected, false otherwise
     */
    public boolean isInfiniteLoop(EntityLivingBase mob, Entity entity)
    {
        if ((mob == infCheckA && entity == infCheckB) || (mob == infCheckB && entity == infCheckA))
        {
            return true;
        }
        infCheckA = mob;
        infCheckB = entity;
        return false;
    }
    
    public static void attemptUnitReward(Entity killer, MobModifier mod) {
    	if(InfernalMobsCore.lotr && killer instanceof LOTREntityNPC) {
        	LOTREntityNPC npc = (LOTREntityNPC) killer;
        	if(npc.hiredNPCInfo.isActive) {
        		// average should have atleast 5 mods to be worth
        		float chance = (abilityRewardChance / 5F) * mod.getModSize();
        		// chance gets greater the more modifiers, with limit tho
        		if(npc.getRNG().nextFloat() < chance) {
        			MobModifier result = InfernalMobsCore.applyRandomModifierToNpc(npc, mod);
        			if(result != null) {
        				EntityPlayer player = npc.hiredNPCInfo.getHiringPlayer();
        				player.addChatComponentMessage(new ChatComponentText("Your Unit \u00a76"+npc.getCommandSenderName()+"\u00a7r learned the ability \u00a7c"+result.modName+"\u00a7r by killing a Boss"));
        			}
        		}
        	}
        }
    }
    
    public static MobModifier applyRandomModifierToNpc(LOTREntityNPC npc, MobModifier nextMod) {
    	MobModifier lastMod = InfernalMobsCore.getMobModifiers(npc, false);
    	
    	List<MobModifier> list = new ArrayList<>(nextMod.getModSize());
		
		
		while(nextMod != null) {
			boolean allowed = allowDestructiveAbilityRewards || !(nextMod instanceof MM_Ghastly || nextMod instanceof MM_Bomber || nextMod instanceof MM_Arsonist || nextMod instanceof MM_Storm);
			
			if (nextMod != null && nextMod.getBlackListMobClasses() != null)
	         {
	             for (Class<?> cl : nextMod.getBlackListMobClasses())
	             {
	                 if (npc.getClass().isAssignableFrom(cl))
	                 {
	                     allowed = false;
	                     break;
	                 }
	             }
	         }
	         if (lastMod != null)
	         {
	        	 // already in there?
	        	 if(lastMod.containsModifierClass(nextMod.getClass())) {
	        		 allowed = false;
	        	 }
	        	 
	        	 // not compatible?
	             if (lastMod.getModsNotToMixWith() != null)
	             {
	                 for (Class<?> cl : lastMod.getModsNotToMixWith())
	                 {
	                     if (cl.equals(nextMod.getClass()))
	                     {
	                         allowed = false;
	                         break;
	                     }
	                 }
	             }
	         }
			
			if(allowed) {
				list.add(nextMod);
			}
			nextMod = nextMod.nextMod;
		}
		
		if(!list.isEmpty()) {
			MobModifier mod = list.get(npc.getRNG().nextInt(list.size()));
			// make new mod with the oldmod as nextmod in the chain
			try
            {
				mod = mod.getClass().getConstructor(new Class[] { EntityLivingBase.class, MobModifier.class })
	            	.newInstance(npc, lastMod);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
			
			InfernalMobsCore.proxy.getRareMobs(false).put(npc, mod);
			mod.onSpawningComplete(npc);
			// make packet and send to all watching players
			String stringData = mod.getLinkedModNameUntranslated();
			MobModsPacket packet = new MobModsPacket(stringData, npc.getEntityId(), (byte) 1);
			
			int x = MathHelper.floor_double(npc.posX) >> 4;
		    int z = MathHelper.floor_double(npc.posZ) >> 4;
		    PlayerManager playerManager = ((WorldServer) npc.worldObj).getPlayerManager();

			@SuppressWarnings("unchecked")
			List<EntityPlayerMP> players = npc.worldObj.playerEntities;
			for(EntityPlayerMP player : players) {
				if(playerManager.isPlayerWatchingChunk(player, x, z)) {
					InfernalMobsCore.instance().networkHelper.sendPacketToPlayer(packet, player);
					InfernalMobsCore.instance().sendHealthPacket(npc, mod.getActualHealth(npc));
					System.out.println("Add "+npc.getCommandSenderName()+" with ID "+npc.getEntityId());
				}  
			} 

		    return mod;
		}
		return null;
    }

}
