package zork;

public class characters {
    private int hp;
  private String name;
  private String id;
  private boolean isFightable;
  private boolean isDefeated;
  private String room;
  private int damage;


  public characters(){
    this.hp = 0;
    this.name = "DEFAULT_NAME";
    this.isFightable= false;
    this.id = "";
    this.room = "DEFAULT_ROOM";
  }
  
  public characters(int hp, String name, boolean isFightable, boolean isDefeated, String id, String room) {
    this.hp = hp;
    this.name = name;
    this.isFightable = isFightable;
    this.isDefeated = isDefeated;
    this.id = id;
    this.room = room;
  }

  public characters(int hp, String name, boolean isFightable){
    this.hp = hp;
    this.name = name;
    this.isFightable = isFightable;
  }

  public void fight() {
    if (!isFightable)
      System.out.println("The " + name + " cannot be damaged.");
  }

  public boolean isDefeated(){
    return isDefeated;
  }

  public void setDefeated(int gethp){
    if(gethp()<=0){
      isDefeated = true;
    }else{
      isDefeated = false;
    }
  }

  public int gethp() {
    return hp;
  }

  public void sethp(int hp) {
    this.hp = hp;
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

  public boolean isFightable() {
    return isFightable;
  }

  public void setFightable(boolean isFightable) {
    this.isFightable = isFightable;
  }

  public void setId(String id){
    this.id = id;
  }

  public void setroom(String room){
    this.room = room;
  }

  public String getRoom(){
    return room;
  }

  public void setDamage(int dmg){
    this.damage = dmg;
  }

  public int getDamage(){
    return damage;
  }
  
}
