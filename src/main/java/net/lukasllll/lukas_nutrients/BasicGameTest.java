package net.lukasllll.lukas_nutrients;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.lukasllll.lukas_nutrients.nutrients.player.PlayerNutrientProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.Resource;
import java.util.Optional;

@PrefixGameTestTemplate(false)
@GameTestHolder(LukasNutrients.MOD_ID)
public class BasicGameTest {
  private final String modID = LukasNutrients.MOD_ID;

  // !!! DEFINE TEMPLATES HERE !!! For now we need to manually check if they exist
  // to avoid a world of pain!
  // Base templates only. Preferably don't call these directly when setting the
  // template for a Gametest
  private final String twoXtwo = "2x2empty";

  // fixed template names for actual Tests
  private final String testTemplateTemplate = twoXtwo;
  private final String testSetCommandTemplate = twoXtwo;
  private final String testGetCommandTemplate = twoXtwo;

  private static boolean checkTemplateExists(String tName, String modID) {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    var locInRes = new String("structures/" + tName + ".nbt");
    var templateLoc = new ResourceLocation(modID, locInRes);
    ResourceManager resManager = server.getResourceManager();
    Optional<Resource> template = resManager.getResource(templateLoc);

    return template.isPresent();
  }

  @GameTest(template = testTemplateTemplate)
  public void testTemplateExisting(GameTestHelper helper) {

    if (!checkTemplateExists(testTemplateTemplate, modID)) {
      helper.fail(missingTemplatString(testTemplateTemplate));
    }
    if (checkTemplateExists("wrongtemplate", modID)) {
      helper.fail("Non existing template passed!");
    }
    helper.succeed();
  }

  @GameTest(template = testSetCommandTemplate)
  public void TestSetCommand(GameTestHelper helper) {
    LukasNutrients.LOGGER.info("Initialising TestSetCommand gametest");

    /**
     * MockPlayer creation failing might be out of our control.
     * So far I have only found one repo actually using it (WilderWild and it's the
     * fabric variant of GameTestHelper), but versions >1.20.1 (which also won't
     * build for me). Additionally NeoForge marked makeMockServerPlayerInLevel as
     * obsolete. Instead the Test will be ran on the first connected OP player that
     * is in the Player list.
     * 
     * makeMockServerPlayerInLevel():
     * - Fails on MC 1.20.1
     * - Fails on Forge 47.2.0 to 47.2.30
     * - Fails in Singleplayer and Multiplayer
     **/
    // ServerPlayer mockplayer = helper.makeMockServerPlayerInLevel();

    if (!checkTemplateExists(testSetCommandTemplate, modID)) {
      helper.fail(missingTemplatString(testSetCommandTemplate));
    }

    ServerPlayer mockplayer = findAnOp();
    if (mockplayer == null) {
      helper.fail("TestSetCommand failed! No OP player connected!");
    }
    String opPlayer = (mockplayer.getName()).getString();

    // Get gamemode
    ServerPlayerGameMode spg = mockplayer.gameMode;
    String gameMode = spg.getGameModeForPlayer().getName();
    runMockPlayerCommand(mockplayer, "gamemode survival");

    String nutrientID = "fruits";

    LukasNutrients.LOGGER.info("TestSetCommand: Testing valid values");
    for (int i = 1; i <= 24; i += 1) {
      final int nurishmentVal = i;

      String command = String.format("nutrients set %s %s %s", opPlayer, nutrientID, nurishmentVal);
      runMockPlayerCommand(mockplayer, command);

      mockplayer.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
        double actualNurishment = nutrients.getNutrientAmount(nutrientID);
        if (actualNurishment != Double.valueOf(nurishmentVal)) {
          helper.fail(String.format("Failed TestSetCommand. Expected %s, actual %s", Double.toString(nurishmentVal),
              Double.toString(actualNurishment)));
        runMockPlayerCommand(mockplayer, "gamemode " + gameMode);
        }
      });
    }

    LukasNutrients.LOGGER.info("TestSetCommand: Testing invalid value");
    int nurishmentVal = 10;
    String command = String.format("nutrients set %s %s %s", opPlayer, nutrientID, nurishmentVal);
    runMockPlayerCommand(mockplayer, command);
    Object[] badArray = { 99, 2.0, "I'm a String" };

    for (Object arrEntry : badArray) {
      command = String.format("nutrients set Dev %s %s", nutrientID, arrEntry.toString());
      runMockPlayerCommand(mockplayer, command);
      mockplayer.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
        double actualNurishment = nutrients.getNutrientAmount(nutrientID);
        if (actualNurishment != Double.valueOf(nurishmentVal)) {
          helper.fail(String.format("Failed TestSetCommand. Expected %s, actual %s", Double.toString(nurishmentVal),
              Double.toString(actualNurishment)));
          runMockPlayerCommand(mockplayer, "gamemode " + gameMode);
        }
      });
    }

    runMockPlayerCommand(mockplayer, "gamemode " + gameMode);
    helper.succeed();
  }

  @GameTest(template = testGetCommandTemplate)
  public void testGetCommand(GameTestHelper helper){
    if (!checkTemplateExists(testGetCommandTemplate, modID)) {
      helper.fail(missingTemplatString(testGetCommandTemplate));
    }

    ServerPlayer mockplayer = findAnOp();
    if(mockplayer==null){helper.fail("No OP connected!");} 

    String opPlayer = (mockplayer.getName()).getString();
    String gameMode=getMockplayerGamemode(mockplayer);
    runMockPlayerCommand(mockplayer, "gamemode survival");



    helper.fail("not implemente fail");
  }

  private static ServerPlayer getServerPlayerByName(String playerName) {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
    return player;
  }

  private static ServerPlayer findAnOp() {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    PlayerList playerList = server.getPlayerList();
    for (ServerPlayer player : playerList.getPlayers()) {
      if (player.hasPermissions(4)) {
        return player;
      }
    }
    ServerPlayer notThere = null;
    return notThere;
  }

  private static int runMockPlayerCommand(Player mockplayer, String command) {
    CommandSourceStack commandSourceStack = mockplayer.createCommandSourceStack().withSuppressedOutput()
        .withPermission(4);
    CommandDispatcher<CommandSourceStack> commanddispatcher = mockplayer.getServer().getCommands().getDispatcher();
    ParseResults<CommandSourceStack> results = commanddispatcher.parse(command, commandSourceStack);
    int result = mockplayer.getServer().getCommands().performCommand(results, command);
    LukasNutrients.LOGGER.info("ran \'" + command + "\' for mockplayer");
    return result;
  }

  private static String missingTemplatString(String templatename) {
    return String.format("Template %s not found", templatename);
  }

  private static String getMockplayerGamemode(ServerPlayer player){
   ServerPlayerGameMode spg = player.gameMode;
   return spg.getGameModeForPlayer().getName();
  }

}