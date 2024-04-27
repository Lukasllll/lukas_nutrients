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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;




// @PrefixGameTestTemplate(false)
@GameTestHolder(LukasNutrients.MOD_ID)
public class BasicGameTest {
  // @PrefixGameTestTemplate(false)
  // @GameTest(template ="faildemo")
  @GameTest(template ="faildemo")
  public static void demoTest(GameTestHelper helper){
    LukasNutrients.LOGGER.info("shits fucked");
    helper.succeedIf(() -> helper.assertBlock(new BlockPos(1, 1, 1), b -> b == Blocks.AIR, "Block was not air"));
  }

   @GameTest 
   public static void doTest(GameTestHelper helper){
      // throw new UnsupportedOperationException();
      LukasNutrients.LOGGER.info("Initialising basic gametest");
        var mockplayer = helper.makeMockSurvivalPlayer();

        // this apperently should run a command
      CommandSourceStack commandSourceStack = mockplayer.createCommandSourceStack().withSuppressedOutput().withPermission(4);
      String command="nutriens set 21";

      // trying to get the nutrientvalue of player


      //belongs to runnign command
      CommandDispatcher<CommandSourceStack> commanddispatcher = mockplayer.getServer().getCommands().getDispatcher();
      ParseResults<CommandSourceStack> results = commanddispatcher.parse(command, commandSourceStack);
      int result = mockplayer.getServer().getCommands().performCommand(results, command);

      LukasNutrients.LOGGER.info("end of gametest");

      // currently still garbage
        // helper.succeedIf(() -> helper.assertEntityProperty(mockplayer, null,"lukas_nutrients_nutrients_sugar");, b -> b == Blocks.AIR, "Block was not air"));
   }
}
