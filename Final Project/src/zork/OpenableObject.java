package zork;

public class OpenableObject extends Item{
  private Boolean isLocked;
  private String keyId;
  private Boolean isOpen;

  public OpenableObject() {
    super();
    this.isLocked = false;
    this.keyId = null;
    this.isOpen = false;
  }

  public OpenableObject(boolean isLocked, String keyId, Boolean isOpen) {
    super();
    this.isLocked = isLocked;
    this.keyId = keyId;
    this.isOpen = isOpen;
  }

  public OpenableObject(boolean isLocked, String keyId) {
    super();
    this.isLocked = isLocked;
    this.keyId = keyId;
    this.isOpen = false;
  }

  public OpenableObject(boolean isLocked){
    super();
    this.isLocked = isLocked;
    this.keyId = null;
    this.isOpen = false;
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
}
