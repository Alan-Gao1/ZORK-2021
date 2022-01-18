package zork;

public class Chest extends OpenableObject{

    private Inventory chestInv;
    private String contentDescription;

    public Chest(int weight, String name, boolean isOpenable, String id, String description, String startingRoom, Boolean isLocked, String keyId, Boolean isOpen, String contentDescription){
        super(weight, name, isOpenable, id, description, startingRoom, isLocked, keyId, isOpen);
        this.setContentDescription(contentDescription);
        this.chestInv = new Inventory(100);
    }
    
    public String getContentDescription() {
        return contentDescription;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    public Chest(){
        super();
        this.setContentDescription("DEFAULT_CONTENT_DESCRIPTION");
        this.chestInv = new Inventory(100);
    }

    public void setContents(String desc){
        this.setContentDescription(desc);
    }

    public void addItem(Item item){
        chestInv.addItem(item);
    }

    public void setOpen(boolean bool){
        this.isOpen = bool;
    }
}
