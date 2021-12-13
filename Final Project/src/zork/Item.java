package zork;

public class Item{
  private int weight;
  private String name;
  private String id;
  private boolean isOpenable;
  private String description;

  public Item(){
    this.weight = 0;
    this.name = "DEFAULT_NAME";
    this.isOpenable = false;
    this.id = "";
    this.description = "";
  }
  
  public Item(int weight, String name, boolean isOpenable) {
    this.weight = weight;
    this.name = name;
    this.isOpenable = isOpenable;
    this.id = "";
    this.description = "";
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

}
