package zork;

public class Item{
  private int weight;
  private String name;
  private String id;
  private boolean isOpenable;
  private String description;
  private String startingRoom;

  public Item(){
    this.weight = 0;
    this.name = "DEFAULT_NAME";
    this.isOpenable = false;
    this.id = "";
    this.description = "";
    this.startingRoom = "DEFAULT_STARTING_ROOM";
  }
  
  public Item(int weight, String name, boolean isOpenable, String id, String description, String startingRoom) {
    this.weight = weight;
    this.name = name;
    this.isOpenable = isOpenable;
    this.id = id;
    this.description = description;
    this.startingRoom = startingRoom;
  }

  public Item(int weight, String keyName, boolean isOpenable){
    this.weight = weight;
    this.name = keyName;
    this.isOpenable = isOpenable;
  }

public void open() {
    if (!isOpenable)
      System.out.println("The " + name + " cannot be opened.");
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public String getName() {
    return name;
  }

  public String getId(){
    return id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isOpenable() {
    return isOpenable;
  }

  public void setOpenable(boolean isOpenable) {
    this.isOpenable = isOpenable;
  }

  public void setId(String id){
    this.id = id;
  }

  public void setDescription(String description){
    this.description = description;
  }

  public void setStartingRoom(String startingRoom){
    this.startingRoom = startingRoom;
  }

  public void setDamage(String id){
  }

  public void setAmmo(int id){
  }
  
}
