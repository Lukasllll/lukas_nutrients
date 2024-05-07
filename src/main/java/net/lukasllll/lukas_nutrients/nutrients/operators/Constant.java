package net.lukasllll.lukas_nutrients.nutrients.operators;

public class Constant implements ICalcElement{

    public static final String DISPLAY_NAME = "CONSTANT";
    public static final String ID = "const";

    private double value;

    public Constant(String inputID) {
        String[] splitID = inputID.split("=");
        if(splitID.length != 2) {
            value = 0;
        } else {
            value = Double.parseDouble(splitID[1]);
        }
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getDisplayname() {
        return DISPLAY_NAME;
    }

    @Override
    public double getMaxAmount() {
        return value;
    }

    @Override
    public int getMaxScore() {
        return (int) value;
    }

    public double getValue() { return value; }
}
