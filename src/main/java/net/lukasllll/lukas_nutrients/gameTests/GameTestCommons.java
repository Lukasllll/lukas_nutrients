package net.lukasllll.lukas_nutrients.gameTests;

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
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.Resource;
import java.util.Optional;

@PrefixGameTestTemplate(false)
@GameTestHolder(LukasNutrients.MOD_ID)
public class GameTestCommons {
  protected final String modID = LukasNutrients.MOD_ID;

  // !!! DEFINE TEMPLATES HERE !!! For now we need to manually check if they exist
  // to avoid a world of pain!
  // Base templates only. Preferably don't call these directly when setting the
  // template for a Gametest
  protected final String twoXtwo = "2x2empty";

  // fixed template names for actual Tests
  private final String testTemplateTemplate = twoXtwo;

  public GameTestCommons() {
  }

  private static boolean checkTemplateExists(String tName, String modID) {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    var locInRes = new String("structures/" + tName + ".nbt");
    var templateLoc = new ResourceLocation(modID, locInRes);
    ResourceManager resManager = server.getResourceManager();
    Optional<Resource> template = resManager.getResource(templateLoc);
    return template.isPresent();
  }

  protected static void checkTemplate(String tName, String modID, GameTestHelper helper) {
    /**
     * Checks if the given template file actually exists.
     * Fails the test if none is found.
     */
    if (!checkTemplateExists(tName, modID)) {
      helper.fail(String.format("Template %s not found !", tName));
    }
  }

  @GameTest(template = testTemplateTemplate)
  public void testTemplateExisting(GameTestHelper helper) {

    if (!checkTemplateExists(testTemplateTemplate, modID)) {
      helper.fail(String.format("%s not found!", testTemplateTemplate));
    }
    if (checkTemplateExists("wrongtemplate", modID)) {
      helper.fail("Non existing template passed!");
    }
    helper.succeed();
  }

  protected static ServerPlayer getServerPlayerByName(String playerName) {
    /** Returns a ServerPlayer by name */
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
    return player;
  }

  protected static ServerPlayer findAnOp() {
    /**
     * Returns an OP ServerPlayer or null if none is found
     */
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

  protected static int runMockPlayerCommand(Player mockplayer, String command) {
    CommandSourceStack commandSourceStack = mockplayer.createCommandSourceStack().withSuppressedOutput()
        .withPermission(4);
    CommandDispatcher<CommandSourceStack> commanddispatcher = mockplayer.getServer().getCommands().getDispatcher();
    ParseResults<CommandSourceStack> results = commanddispatcher.parse(command, commandSourceStack);
    int result = mockplayer.getServer().getCommands().performCommand(results, command);
    LukasNutrients.LOGGER.info("ran \'" + command + "\' for mockplayer");
    return result;
  }

  protected static String getServerPlayerGamemode(ServerPlayer player) {
    ServerPlayerGameMode spg = player.gameMode;
    return spg.getGameModeForPlayer().getName();
  }

  protected static ServerPlayer getSuitablePlayer(GameTestHelper helper) {
    /**
     * Tries creating a mock server player. If this doesn't work,
     * we try finding an OP player.
     * If this also doesn't work we fail the test.
     */
    ServerPlayer mockplayer = null;
    try {
      mockplayer = helper.makeMockServerPlayerInLevel();
    } catch (Exception e) {
      LukasNutrients.LOGGER.info("Could not initalize a  mock player! Will try an OP now");
      mockplayer = findAnOp();
    }
    if (mockplayer == null) {
      helper.fail("No suitable (mock)player created/found!");
    }
    return mockplayer;
  }

}