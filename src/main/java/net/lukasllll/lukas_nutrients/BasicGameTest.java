package net.lukasllll.lukas_nutrients;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.lukasllll.lukas_nutrients.nutrients.player.PlayerNutrientProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;



@PrefixGameTestTemplate(false)
@GameTestHolder(LukasNutrients.MOD_ID)
public class BasicGameTest {
  // @PrefixGameTestTemplate(false)
  // @GameTest(template ="faildemo")
  @GameTest(template ="faildemo")
  public static void demoTest(GameTestHelper helper){
    LukasNutrients.LOGGER.info("shits fucked");
    helper.succeedIf(() -> helper.assertBlock(new BlockPos(1, 1, 1), b -> b == Blocks.AIR, "Block was not air"));
  }

   @GameTest(template="2x2empty") 
   public static void doTest(GameTestHelper helper){
      // throw new UnsupportedOperationException();
      LukasNutrients.LOGGER.info("Initialising basic gametest");
        // var mockplayer = helper.makeMockSurvivalPlayer();
        var mockplayer = helper.makeMockSurvivalPlayer();

        // this apperently should run a command
      String command="nutriens set @s fruits 21";

      // trying to get the nutrientvalue of player
      var hey=runMockPlayerCommand(mockplayer, "say hey");
      var reload=runMockPlayerCommand(mockplayer, "nutrients reload");
      var setOut = runMockPlayerCommand(mockplayer, command);
      //belongs to runnign command
      
      // trying to get some entitydata. maybe nutrients need to be manually intialized for th emock player?
      var test = mockplayer.getEntityData();

      var ugly=runMockPlayerCommand(mockplayer, "nutrients get @s fruits");
      var nutrientID="fruits";
      mockplayer.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
        double amount = nutrients.getNutrientAmount(nutrientID);
        LukasNutrients.LOGGER.info(Double.toString(amount));
      });

      helper.fail("designated epic fail :)");
      // currently still garbage
        // helper.succeedIf(() -> helper.assertEntityProperty(mockplayer, null,"lukas_nutrients_nutrients_sugar");, b -> b == Blocks.AIR, "Block was not air"));

   }

   private static int runMockPlayerCommand(Player mockplayer, String command){
      CommandSourceStack commandSourceStack = mockplayer.createCommandSourceStack().withSuppressedOutput().withPermission(4);
      CommandDispatcher<CommandSourceStack> commanddispatcher = mockplayer.getServer().getCommands().getDispatcher();
      ParseResults<CommandSourceStack> results = commanddispatcher.parse(command, commandSourceStack);
      int result = mockplayer.getServer().getCommands().performCommand(results, command);
      LukasNutrients.LOGGER.info("ran \'"+command+"\' for mockplayer");
      return result;
   }
}
