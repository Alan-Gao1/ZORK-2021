package zork;

import java.util.ArrayList;

public class Room {

  private String roomName;
  private String description;
  private ArrayList<Exit> exits;
  private boolean isLocked;
  //make this locked/not locked
  private Inventory items;

  public ArrayList<Exit> getExits() {
    return exits;
  }

  public boolean addItem(Item item){
     return items.addItem(item);
  }

  public Item removeItem(String itemName){
    return items.removeItem(itemName);
  }

  public void setExits(ArrayList<Exit> exits) {
    this.exits = exits;
  }

  /**
   * Create a room described "description". Initially, it has no exits.
   * "description" is something like "a kitchen" or "an open court yard".
   */
  public Room(String description) {
    this.description = description;
    exits = new ArrayList<Exit>();
    this.items = new Inventory(1000);
  }

  public Room() {
    roomName = "DEFAULT ROOM";
    description = "DEFAULT DESCRIPTION";
    exits = new ArrayList<Exit>();
    this.items = new Inventory(1000);
  }

  public void addExit(Exit exit) throws Exception {
    exits.add(exit);
  }

  /**
   * Return the description of the room (the one that was defined in the
   * constructor).
   */
  public String shortDescription() {
    return "Room: " + roomName + "\n\n" + description;
  }

  /**
   * Return a long description of this room, on the form: You are in the kitchen.
   * Exits: north west
   */
  public String longDescription() {

    return "Room: " + roomName + "\n\n" + description + "\n\n" + exitString();
  }

  /**
   * Return a string describing the room's exits, for example "Exits: north west
   * ".
   */
  public String exitString() {
    String returnString = "Exits: \n";
    for (Exit exit : exits) {
      returnString += "Exiting "+exit.getDirection() + " will lead to room "+exit.getAdjacentRoom()+"\n";
    }

    return returnString;
  }

  /**
   * Return the room that is reached if we go from this room in direction
   * "direction". If there is no room in that direction, return null.
   */
  public Room nextRoom(String direction) {
    try {
      for (Exit exit : exits) {
        if (exit.getDirection().equalsIgnoreCase(direction)) {
          String adjacentRoom = exit.getAdjacentRoom();

          return Game.roomMap.get(adjacentRoom);
        }

      }
    } catch (IllegalArgumentException ex) {
      System.out.println(direction + " is not a valid direction.");
      return null;
    }
    System.out.println(direction + " is not a valid direction.");
    return null;
  }

  /*
   * private int getDirectionIndex(String direction) { int dirIndex = 0; for
   * (String dir : directions) { if (dir.equals(direction)) return dirIndex; else
   * dirIndex++; }
   * 
   * throw new IllegalArgumentException("Invalid Direction"); }
   */
  public String getRoomName() {
    return roomName;
  }

  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean getLocked(){
    return isLocked;
  }

  public Inventory getInv(){
    return items;
  }

  public void printRoomContents(){
    if(roomName.equals("BVG shop")){
      return;
    }
    System.out.println("This Room contains:");
    for (Item item : items.getInventory()) {
      System.out.println("'"+item.getName()+"' with ID: '"+item.getId()+"' is present in this room");
    }
  }
}