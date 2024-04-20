package net.lukasllll.lukas_nutrients.nutrients;

import net.lukasllll.lukas_nutrients.config.NutrientGroupsConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.nio.charset.Charset;

public class NutrientGroup {

    public static NutrientGroup[] nutrientGroups = null;

    private final String id;
    private String displayname;
    private int[] pointRanges;
    private ItemStack displayItemStack;
    private double defaultAmount;

    public NutrientGroup(String id, String displayname, String item, int[] ranges, double defaultAmount) {
        this.id=id;
        this.displayname=displayname;
        this.pointRanges=ranges;
        this.defaultAmount = defaultAmount;

        RegistryObject<Item> DisplayItemRegistry = RegistryObject.create(new ResourceLocation(item), ForgeRegistries.ITEMS);

        displayItemStack = new ItemStack(DisplayItemRegistry.get());
    }

    /*
    read relevant data from a ByteBuffer. This constructor is called when reading a packet send from the server to the client
    containing the servers NutrientGroups
     */
    public NutrientGroup(FriendlyByteBuf buf) {
        int stringLength = buf.readInt();
        this.id = (String) buf.readCharSequence(stringLength, Charset.defaultCharset());
        stringLength = buf.readInt();
        this.displayname = (String) buf.readCharSequence(stringLength, Charset.defaultCharset());
        this.pointRanges = buf.readVarIntArray();
        this.displayItemStack = buf.readItem();
        this.defaultAmount = buf.readDouble();
    }

    public String getID() {return this.id;}
    public String getDisplayname() {return this.displayname;}
    public ItemStack getDisplayItemStack() {return this.displayItemStack;}
    public int[] getPointRanges() {return this.pointRanges;}
    public double getDefaultAmount() {return this.defaultAmount;}

    public static NutrientGroup[] getNutrientGroups() {
        if(nutrientGroups !=null) return nutrientGroups;

        getFromConfig();
        return nutrientGroups;
    }

    public static void getFromConfig() {
        nutrientGroups = NutrientGroupsConfig.getNutrientGroups();
    }

    //gets the array index from a nutrientID. Returns -1 if no such ID exists.
    public static int getArrayIndex(String nutrientID) {
        for(int i = 0; i< nutrientGroups.length; i++) {
            if(nutrientGroups[i].getID().equals(nutrientID)) {
                return i;
            }
        }
        return -1;
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
}
