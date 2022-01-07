package zork;

import java.util.ArrayList;

public class Chest extends OpenableObject{

    // private ArrayList<Item> contents = new ArrayList<>();
    private Item contents = new Item();
    private int chestNum;
    private String contentDescription;

    public Chest(int weight, String name, boolean isOpenable, String id, String description, String startingRoom, Boolean isLocked, String keyId, Boolean isOpen, int chestNum, String contentDescription){
        super(weight, name, isOpenable, id, description, startingRoom, isLocked, keyId, isOpen);
        this.chestNum = chestNum;
        this.contentDescription = contentDescription;
    }
    
    public Chest(){
        super();
        this.chestNum = 0;
        this.contentDescription = "DEFAULT_CONTENT_DESCRIPTION";
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
