package zork;

public class OpenableObject extends Item{
  private Boolean isLocked;
  private String keyId;
  public Boolean isOpen;

  public OpenableObject(){
    super();
    this.isLocked = true;
    this.keyId = "DEFAULT_ID";
    this.isOpen = false;
  }
  
  public OpenableObject(int weight, String name, boolean isOpenable, String id, String description, String startingRoom, Boolean isLocked, String keyId, Boolean isOpen){
    super(weight, name, isOpenable, id, description, startingRoom);
    this.isLocked = isLocked;
    this.keyId = keyId;
    this.isOpen = isOpen;
  }

  public OpenableObject(int weight, String name, boolean isOpenable, String id, String description, String startingRoom, Boolean isLocked, Boolean isOpen){
    super(weight, name, isOpenable, id, description, startingRoom);
    this.isLocked = isLocked;
    this.keyId = "";
    this.isOpen = isOpen;
  }

  public OpenableObject(boolean isLocked, String keyId){
    super();
    this.keyId = keyId;
    this.isLocked = isLocked;
  }

  public OpenableObject(boolean isLocked){
    super();
    this.isLocked = isLocked;
    this.keyId = null;
    this.isOpen = false;
  }
  
  public OpenableObject(boolean isLocked, String keyId, Boolean isOpen) {
    super();
    this.isLocked = isLocked;
    this.keyId = keyId;
    this.isOpen = isOpen;
  }

public boolean isLocked() {
    return isLocked;
  }

  public void setLocked(boolean isLocked) {
    this.isLocked = isLocked;
  }

  public String getKeyId() {
    return keyId;
  }

  public void setKeyId(String id){
    this.keyId = id;
  }

  public boolean isOpen() {
    return isOpen;
  }

  public void setOpen(boolean isOpen) {
    this.isOpen = isOpen;
  }

  public boolean getOpen(){
    return isOpen;
  }
}
