package zork;

import java.util.ArrayList;

public class Inventory {
  private ArrayList<Item> items;
  private int maxWeight;
  public int currentWeight;

  public Inventory(int maxWeight) {
    this.items = new ArrayList<Item>();
    this.maxWeight = maxWeight;
    this.currentWeight = 0;
  }

  public void printContents(){
    for (Item item : items) {
      System.out.println("Item Name: "+item.getName());
      System.out.println("Item ID: "+item.getId());
      System.out.println("Item Description: "+item.getDesc());
      System.out.println("Item Weight: "+item.getWeight());
    }
  }

  public void checkBackpack(){
    for (Item item : items) {
      if(item.isWeapon()){
        Weapon weapon = (Weapon) item;
        System.out.print("Weapon Name: "+ weapon.getName());
        System.out.println("   Damage: "+ weapon.getDamage());
      }
    }
  }

  public boolean checkWeapons(){
    int counter = 0;
    for (Item item : items) {
      if(item.isWeapon()){
        counter++;
      }
    }

    if(counter>0)
      return true;
    else
      return false;
  }

  public int getMaxWeight() {
    return maxWeight;
  }

  public int getCurrentWeight() {
    return currentWeight;
  }

  public boolean addItem(Item item) {
    if ((item.getWeight() + currentWeight) <= maxWeight)
      return items.add(item);
    else {
      System.out.println("There is no room to add the item.");
      return false;
    }
  }

  public Item removeItem(String itemName){
    int ind = -1;
    for(int i = 0; i < items.size(); i++){
      if(itemName.equals(items.get(i).getName())){
        ind = i;
      }
    }
    // serach for the ite name AND DELETE A THAT INDEX AND RETURN THE ITEM
    if(ind<0){
      return null;
    }
    return items.remove(ind);
  }

  public boolean checkItem(String itemName){
    boolean there = false;
    for(int i = 0; i < items.size(); i++){
      if(itemName.equals(items.get(i).getName())){
        there = true;
      }
    }
    return there;
  }

}
