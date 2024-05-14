package net.lukasllll.lukas_nutrients.gameTests.tests;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.gameTests.GameTestCommons;
import net.lukasllll.lukas_nutrients.nutrients.player.PlayerNutrientProvider;

@PrefixGameTestTemplate(false)
@GameTestHolder(LukasNutrients.MOD_ID)
public class CommandTests extends GameTestCommons {

    public CommandTests() {
        super();
    }

    // fixed template names for actual Tests
    private final String testSetCommandTemplate = twoXtwo;
    private final String testGetCommandTemplate = twoXtwo;
    private final String testReloadTemplate = twoXtwo;

    @GameTest(template = testSetCommandTemplate)
    public void TestSetCommand(GameTestHelper helper) {
        LukasNutrients.LOGGER.info("Initialising TestSetCommand gametest");

        checkTemplate(testSetCommandTemplate, modID, helper);

        ServerPlayer mockplayer = getSuitablePlayer(helper);
        String opPlayer = (mockplayer.getName()).getString();

        // Get gamemode
        String gameMode = getServerPlayerGamemode(mockplayer);
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
                    helper.fail(String.format("Failed TestSetCommand. Expected %s, actual %s",
                            Double.toString(nurishmentVal),
                            Double.toString(actualNurishment)));
                    runMockPlayerCommand(mockplayer, "gamemode " + gameMode);
                }
            });
        }

        LukasNutrients.LOGGER.info("TestSetCommand: Testing invalid value");
        int nurishmentVal = 10;
        String command = String.format("nutrients set %s %s %s", opPlayer, nutrientID, nurishmentVal);
        runMockPlayerCommand(mockplayer, command);
        Object[] badArray = { 99, -1, 2.0, "I'm a String" };

        for (Object arrEntry : badArray) {
            command = String.format("nutrients set Dev %s %s", nutrientID, arrEntry.toString());
            runMockPlayerCommand(mockplayer, command);
            mockplayer.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
                double actualNurishment = nutrients.getNutrientAmount(nutrientID);
                if (actualNurishment != Double.valueOf(nurishmentVal)) {
                    helper.fail(String.format("Failed TestSetCommand. Expected %s, actual %s",
                            Double.toString(nurishmentVal),
                            Double.toString(actualNurishment)));
                    runMockPlayerCommand(mockplayer, "gamemode " + gameMode);
                }
            });
        }

        runMockPlayerCommand(mockplayer, "gamemode " + gameMode);
        helper.succeed();
    }

    @GameTest(template = testGetCommandTemplate)
    public void testGetCommand(GameTestHelper helper) {
        checkTemplate(testGetCommandTemplate, modID, helper);

        // ServerPlayer mockplayer = findAnOp();
        ServerPlayer mockplayer = getSuitablePlayer(helper);
        // if(mockplayer==null){helper.fail("No OP connected!");}

        String opPlayer = (mockplayer.getName()).getString();
        String gameMode = getServerPlayerGamemode(mockplayer);
        runMockPlayerCommand(mockplayer, "gamemode survival");
        String nutrientID = "fruits";

        helper.fail("not implemented fail");
    }

    @GameTest(template = testReloadTemplate)
    public void testReloadCommand(GameTestHelper helper) {

        helper.fail("not implemented fail");

    }

}
