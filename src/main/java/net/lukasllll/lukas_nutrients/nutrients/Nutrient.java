package net.lukasllll.lukas_nutrients.nutrients;

import net.lukasllll.lukas_nutrients.client.graphics.gui.IDisplayElement;
import net.lukasllll.lukas_nutrients.nutrients.operators.ICalcElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

public class Nutrient implements IDisplayElement, ICalcElement {

    public static int MAX_AMOUNT = 24;

    private final String id;
    private String displayname;
    private int[] pointRanges;
    private ItemStack displayItemStack;
    private double defaultAmount;

    public Nutrient(String id, String displayname, String item, int[] ranges, double defaultAmount) {
        this.id=id;
        this.displayname=displayname;
        this.pointRanges=ranges;
        this.defaultAmount = defaultAmount;

        RegistryObject<Item> DisplayItemRegistry = RegistryObject.create(new ResourceLocation(item), ForgeRegistries.ITEMS);

        displayItemStack = new ItemStack(DisplayItemRegistry.get());
    }

    /*
    read relevant data from a ByteBuffer. This constructor is called when reading a packet send from the server to the client
    containing the servers Nutrients and Sums for rendering
     */
    public Nutrient(FriendlyByteBuf buf) {
        int stringLength = buf.readInt();
        this.id = (String) buf.readCharSequence(stringLength, Charset.defaultCharset());
        stringLength = buf.readInt();
        this.displayname = (String) buf.readCharSequence(stringLength, Charset.defaultCharset());
        this.pointRanges = buf.readVarIntArray();
        this.displayItemStack = buf.readItem();
        this.defaultAmount = buf.readDouble();
    }

    /*
    Write the objects relevant data to a ByteBuffer to be sent from the server to the client.
     */
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(id.length());
        buf.writeCharSequence(id, Charset.defaultCharset());
        buf.writeInt(displayname.length());
        buf.writeCharSequence(displayname, Charset.defaultCharset());
        buf.writeVarIntArray(pointRanges);
        buf.writeItem(displayItemStack);
        buf.writeDouble(defaultAmount);
    }

    public int getCurrentRange(double amount) {
        //sets the range to -1 to mark, that it is currently not determined.
        int range = -1;
        //the range is determined by looping through the segment end points. If the nutrient amount is smaller than the end point value,
        //the value falls inside that segment.
        for(int i=0; i<pointRanges.length; i++) {
            if(Math.max(0, amount-1) < pointRanges[i]) {
                range = i;
                break;
            }
        }
        //if the range has not yet been determined, the amount must fall in the last segment
        if(range == -1) {
            range = 4;
        }

        return range;
    }

    public int getMaxScore() {
        if(pointRanges[0] < MAX_AMOUNT && pointRanges[1] < 24 && pointRanges[2] > pointRanges[1] && pointRanges[2] > pointRanges[0]) return 2;
        if(pointRanges[0] < MAX_AMOUNT && pointRanges[3] > pointRanges[0]) return 1;
        return 0;
    }
    public double getMaxAmount() { return MAX_AMOUNT; }

    public String getID() {return this.id;}
    public String getDisplayname() {return this.displayname;}
    public ItemStack getDisplayItemStack() {return this.displayItemStack;}
    public int[] getPointRanges() {return this.pointRanges;}
    public double getDefaultAmount() {return this.defaultAmount;}

    public List<Component> getTooltip() {
        LinkedList<Component> out = new LinkedList<>();
        out.add(Component.literal(getDisplayname()).append(Component.literal(" (Nutrient)").withStyle(ChatFormatting.GRAY)));
        return out;
    }
}
