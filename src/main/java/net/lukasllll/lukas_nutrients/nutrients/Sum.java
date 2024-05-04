package net.lukasllll.lukas_nutrients.nutrients;

import net.lukasllll.lukas_nutrients.client.ClientNutrientData;
import net.lukasllll.lukas_nutrients.client.graphics.gui.DisplayElement;
import net.minecraft.network.FriendlyByteBuf;

import java.nio.charset.Charset;

public class Sum implements DisplayElement {

    private final String id;
    private String displayname;
    private int[] pointRanges;
    private int max;
    private int basePoint;

    private String[] summandIDs;

    public Sum(String id, String displayname, int[] pointRanges, String[] summandIDs) {
        this.id = id;
        this.displayname = displayname;
        this.pointRanges = pointRanges;
        this.summandIDs = summandIDs;
        this.basePoint = (pointRanges[1] + pointRanges[0])/2;
        this.max = -1;
    }

    /*
    read relevant data from a ByteBuffer. This constructor is called when reading a packet send from the server to the client
    containing the servers Nutrients and Sums for rendering
     */
    public Sum(FriendlyByteBuf buf) {
        int stringLength = buf.readInt();
        this.id = (String) buf.readCharSequence(stringLength, Charset.defaultCharset());
        stringLength = buf.readInt();
        this.displayname = (String) buf.readCharSequence(stringLength, Charset.defaultCharset());
        this.pointRanges = buf.readVarIntArray();
        int tempArrayLength = buf.readInt();
        summandIDs = new String[tempArrayLength];
        for(int i=0; i<tempArrayLength; i++) {
            stringLength = buf.readInt();
            summandIDs[i] = (String) buf.readCharSequence(stringLength, Charset.defaultCharset());
        }
        this.basePoint = (pointRanges[1] + pointRanges[0])/2;
        this.max = -1;
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
        buf.writeInt(summandIDs.length);
        for(int i=0; i<summandIDs.length; i++) {
            buf.writeInt(summandIDs[i].length());
            buf.writeCharSequence(summandIDs[i], Charset.defaultCharset());
        }
    }

    public String[] getSummandIDs() { return summandIDs; }
    public String getID() {return this.id;}
    public String getDisplayname() {return this.displayname;}
    public int[] getPointRanges() {return this.pointRanges;}
    public int getBasePoint() {return this.basePoint; }
    public int getMax(boolean onClient) {
        if(max == -1) {
            max = 0;
            if(onClient) {
                for (String id : this.getSummandIDs()) {
                    Nutrient nutrient = ClientNutrientData.getNutrient(id);
                    if(nutrient == null) continue;
                    max += nutrient.getMaxScore();
                }
            } else {
                for (String id : this.getSummandIDs()) {
                    int arrayIndex = NutrientManager.getNutrientArrayIndex(id);
                    if(arrayIndex == -1) continue;
                    Nutrient nutrient = NutrientManager.getNutrients()[arrayIndex];
                    max += nutrient.getMaxScore();
                }
            }
        }
        return max;
    }
}
