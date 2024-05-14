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
import net.minecraft.network.protocol.PacketFlow;
import java.util.UUID;
import java.util.function.Consumer;
import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import java.time.Duration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryPropertyMap.Builder;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;

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
     * we try finding an OP player instead.
     * If this also doesn't work we fail the test.
     * Reason for this is Mojang's function for creating a
     * mockServerPlayer currently not working and
     * our own may break on newer versions. Since some
     * test could also be run on an OP player we do that as
     * a fallback.
     */

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
    ServerPlayer mockplayer = null;
    try {
      // mockplayer = helper.makeMockServerPlayerInLevel();
      mockplayer = createMockServerPlayer(helper);
    } catch (Exception e) {
      LukasNutrients.LOGGER.info("Could not initalize a  mock player! Will try an OP now");
      mockplayer = findAnOp();
    }
    if (mockplayer == null) {
      helper.fail("No suitable (mock)player created/found!");
    }
    return mockplayer;
  }

  protected static ServerPlayer createMockServerPlayer(GameTestHelper helper) {
    /**
     * Creates an actual Server Mock Player
     */
    try {

      String mockPlayerName = "test-mock-player";
      var gameProfile = new GameProfile(UUID.randomUUID(), mockPlayerName);
      ServerPlayer mockPlayer = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), gameProfile);

      var playerList = helper.getLevel().getServer().getPlayerList();

      // Get rid of existing mock players with the same name.
      while (getServerPlayerByName(mockPlayerName) != null) {
        getServerPlayerByName(mockPlayerName).connection.disconnect(Component.literal("Whoever reads this is an NPC"));
      }

      // init stuff for ClientPacketListener
      Minecraft minecraft = Minecraft.getInstance();

      Screen callbackScreen = null; // Should not be null but seems to work lol
      ServerData serverData = null;

      // Feels janky, but sticks so far TODO: acutally figure stuff out
      // ------------------------
      TelemetryEventSender telemetryEventSender = new TelemetryEventSender() {
        @Override
        public void send(TelemetryEventType pEventType, Consumer<Builder> p_262079_) {
          LukasNutrients.LOGGER.info("mockServerPlayer Telemetry event send");
        }
      };

      WorldSessionTelemetryManager telemetryManager = new WorldSessionTelemetryManager(
          telemetryEventSender,
          true, // Assume it's a new world
          Duration.ofMinutes(5), // Example duration, can be null
          null // Minigame name, can be null
      );

      // Connection stuff
      var connection = new Connection(PacketFlow.SERVERBOUND);
      new EmbeddedChannel(new ChannelInitializer<EmbeddedChannel>() {
        @Override
        protected void initChannel(EmbeddedChannel ch) throws Exception {
          ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext cHc) throws Exception {
              connection.channelActive(cHc);
            }
          });
        }
      });
      var cPl = new ClientPacketListener(minecraft, callbackScreen, connection, serverData, gameProfile,
          telemetryManager);

      connection.setListener(cPl);

      // ---------------------------------------------------

      playerList.placeNewPlayer(connection, mockPlayer);

      return getServerPlayerByName(mockPlayerName);

    } catch (Exception e) {
      helper.fail("ServerMockPlayer creation failed!");
      return null;
    }

  }

}