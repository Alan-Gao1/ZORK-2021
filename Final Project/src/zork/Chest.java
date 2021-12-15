package zork;

import java.util.ArrayList;

public class Chest extends OpenableObject{

    // private ArrayList<Item> contents = new ArrayList<>();
    private Item contents = new Item();
    private int chestNum;
    private String contentDescription;

    public Chest(){
        super();
        this.chestNum = 0;
    }

    public void setChestNum(int num){
        this.chestNum = num;
    }

    public void addContentsChest(Item item){
        this.contents = item;
    }

    public void setContents(String desc){
        this.contentDescription = desc;
    }
}
