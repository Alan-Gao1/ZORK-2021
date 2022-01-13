package zork;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Scanner;

import javax.sound.sampled.BooleanControl;
  

public class Game {
  private Scanner in;

  public static HashMap<String, Room> roomMap = new HashMap<String, Room>();
  public static ArrayList<Item> itemList = new ArrayList<>();
  public static HashMap<String, Item> itemMap = new HashMap<String, Item>();
  public static ArrayList<characters> characterList = new ArrayList<>();
  public static HashMap<String, characters> characterMap = new HashMap<String, characters>();

  private Parser parser;
  private Room currentRoom;
  private int peoplePickpocketed;
  public boolean finished = false;
  private boolean winCondition = false;
  private Inventory backpack = new Inventory(15);
  private double wallet;
  private characters enemy;
  private int playerHP = 100;

  private ArrayList<Exit> exits;

  /**
   * Create the game and initialise its internal map.
   */ 
   public Game() {
    try {
      initRooms("src\\zork\\data\\rooms.json");
      currentRoom = roomMap.get("Lobby");
      initItems("src\\zork\\data\\items.json");
      initCharacters("src\\zork\\data\\characters.json");
      System.out.println(itemList);
    } catch (Exception e) {
      e.printStackTrace();
    }
    parser = new Parser();
  }

  private void initCharacters(String fileName) throws Exception{
    Path path = Path.of(fileName);
    String jsonString = Files.readString(path);
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(jsonString);
    JSONArray jsonCharacters = (JSONArray) json.get("items");
    for(Object itemObj : jsonCharacters){
      String characterId = (String) ((JSONObject) itemObj).get("id");
      String characterName = (String) ((JSONObject) itemObj).get("name");
      String characterHPS = (String) ((JSONObject) itemObj).get("hp");
      int characterHP = Integer.parseInt(characterHPS);
      Boolean characterIsFightable = (Boolean) ((JSONObject) itemObj).get("isFightable");
      String characterStartingRoom = (String) ((JSONObject) itemObj).get("room");
      JSONArray jsonUse = (JSONArray) ((JSONObject) itemObj).get("use");
      int damage = 0;
      for (Object itemUse : jsonUse) {
        String sdamage = (String) ((JSONObject) itemUse).get("damage");
        damage = Integer.parseInt(sdamage);
      }
      characters character = new characters(characterHP, characterName, characterIsFightable, characterId, characterStartingRoom);
      character.setDamage(damage);
      characterList.add(character);
      characterMap.put(characterId, character);
    }
  }

  private void displayInfo(){
    System.out.println("Money: $"+wallet);
    System.out.println();
    backpack.printContents();
  }

  private void initItems(String fileName) throws Exception{
    Path path = Path.of(fileName);
    String jsonString = Files.readString(path);
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(jsonString);
    JSONArray jsonItems = (JSONArray) json.get("items");

    for(Object itemObj : jsonItems){
      String itemName = (String) ((JSONObject) itemObj).get("name");
      String itemId = (String) ((JSONObject) itemObj).get("id");
      boolean isChest = false;
      int chestNum = 0 ;
      String object = "";
      String contentDesc = "";
      if(itemId.equals("chestOne")||itemId.equals("chestTwo")||itemId.equals("chestThree")||itemId.equals("chestFour")||itemId.equals("chestFive")){
        isChest = (Boolean) ((JSONObject) itemObj).get("isChest");
        String chestNum1 = Long.toString((long) ((JSONObject) itemObj).get("chestNum"));
        chestNum = Integer.parseInt(chestNum1);
        object = (String) ((JSONObject) itemObj).get("object");
        contentDesc = (String) ((JSONObject) itemObj).get("contents");
      }
      String itemDescription = (String) ((JSONObject) itemObj).get("description");
      String itemStartingRoom = (String) ((JSONObject) itemObj).get("startingRoom");
      String itemWeight = (String) ((JSONObject) itemObj).get("weight");
      int iWeight = Integer.parseInt(itemWeight);
      Boolean itemIsOpenable = (Boolean) ((JSONObject) itemObj).get("isOpenable");
      Boolean isLocked = false;
      Boolean itemIsWeapon = (Boolean) ((JSONObject) itemObj).get("isWeapon");
      if(itemIsOpenable&&!isChest){
        isLocked = (Boolean) ((JSONObject) itemObj).get("isLocked");
        OpenableObject openableObject;
        if(isLocked){
          String itemKey = (String) ((JSONObject) itemObj).get("keyId");
          openableObject = new OpenableObject(iWeight, itemName, true, itemId, itemDescription, itemStartingRoom, isLocked, itemKey, false);
        }else{
          openableObject = new OpenableObject(iWeight, itemName, true, itemId, itemDescription, itemStartingRoom, isLocked, false);
        }
        itemList.add(openableObject);
        itemMap.put(itemId, openableObject);
        putIteminRoom(itemStartingRoom, itemId);
      }else if(isChest){
        Chest chest = new Chest(iWeight, itemName, itemIsOpenable, itemId, itemDescription, itemStartingRoom, isLocked, "0", false, chestNum, contentDesc);
        if(!object.equals("money")){
          chest.addItem(findContents(object));
        }
        itemList.add(chest);
        itemMap.put(itemId, chest);
        putIteminRoom(itemStartingRoom, itemId);
      }else{
        Item item = new Item(iWeight, itemName, itemIsOpenable, itemId, itemDescription, itemStartingRoom, itemIsWeapon);
        if(itemIsWeapon){
          item = new Weapon(iWeight, itemName, itemIsOpenable, itemId, itemDescription, itemStartingRoom, 0, 0, itemIsWeapon);
          item.setDamage(itemId);
          if(itemId.equals("slingshot")){
            item.setAmmo(5);
          }
        }
        itemList.add(item);
        itemMap.put(itemId, item);
        putIteminRoom(itemStartingRoom, itemId);
      }   
    }
  }

  private Item findContents(String object) {
    for (Item item : itemList) {
      if(item.getId().equals(object)){
        Item returnItem;
        returnItem = item;
        return returnItem;
      }
    }
    return null;
  }

  private static void putIteminRoom(String insideName, String itemId){
    int ind = 0;
    if(!insideName.equals("item")){
      roomMap.get(insideName).addItem(itemMap.get(itemId));
    }
  }

  private void initRooms(String fileName) throws Exception {
    Path path = Path.of(fileName);
    String jsonString = Files.readString(path);
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(jsonString);

    JSONArray jsonRooms = (JSONArray) json.get("rooms");

    for (Object roomObj : jsonRooms) {
      Room room = new Room();
      String roomName = (String) ((JSONObject) roomObj).get("name");
      String roomId = (String) ((JSONObject) roomObj).get("id");
      String roomDescription = (String) ((JSONObject) roomObj).get("description");
      room.setDescription(roomDescription);
      room.setRoomName(roomName);

      JSONArray jsonExits = (JSONArray) ((JSONObject) roomObj).get("exits");
      /*ArrayList<Exit>*/ exits = new ArrayList<Exit>();
      for (Object exitObj : jsonExits) {
        String direction = (String) ((JSONObject) exitObj).get("direction");
        String adjacentRoom = (String) ((JSONObject) exitObj).get("adjacentRoom");
        String keyId = (String) ((JSONObject) exitObj).get("keyId");
        Boolean isLocked = (Boolean) ((JSONObject) exitObj).get("isLocked");
        Boolean isOpen = (Boolean) ((JSONObject) exitObj).get("isOpen");
        Exit exit = new Exit(direction, adjacentRoom, isLocked, keyId, isOpen);
        exits.add(exit);
      }
      room.setExits(exits);
      roomMap.put(roomId, room);
    }
  }

  

  /**
   * Main play routine. Loops until end of play.
   */
  public void play() {
    printWelcome();

    finished = false;
    while (!finished) {
      Command command;
      try {
        command = parser.getCommand();
        finished = processCommand(command);
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
    if(winCondition){
      System.out.println("Thank you for playing. Good bye.");
    }else{
      System.out.println("Game over.");
    }
  }

  /**
   * Print out the opening message for the player.
   */
  private void printWelcome() {
    System.out.println();
    System.out.println("Welcome to Zork!");
    System.out.println("Zork is a new, incredibly boring adventure game.");
    System.out.println("Type 'help' if you need help.");
    System.out.println();
    System.out.println(currentRoom.longDescription());
  }

  /**
   * Given a command, process (that is: execute) the command. If this command ends
   * the game, true is returned, otherwise false is returned.
   */
  private boolean processCommand(Command command) {
    in = new Scanner(System.in);
    if (command.isUnknown()) {
      System.out.println("I don't know what you mean...");
      return false;
    }

    String commandWord = command.getCommandWord();
    if (commandWord.equals("help"))
      printHelp();
    else if (commandWord.equals("go"))
      goRoom(command);
    else if (commandWord.equals("take"))
      takeItem(command);
    else if (commandWord.equals("pickpocket")){
      if(pickpocket(command)){
        System.out.println("You got caught pickpocketing. Get good.");
        return true;
      }
    }else if (commandWord.equals("drop"))
      drop(command);
    else if (commandWord.equals("untie"))
      untie(command);
    else if (commandWord.equals("read"))
      read(command);
    else if (commandWord.equals("listen"))
      listen(command);
    else if (commandWord.equals("wear"))
      wear(command);
    else if (commandWord.equals("fight"))
      fight(command);
    else if (commandWord.equals("play"))
      playVideo(command);
    else if(commandWord.equals("info"))
      displayInfo();
    else if (commandWord.equals("use")){
      if(!command.hasSecondWord())
        System.out.println("Use what?");
      else
        return useItem(command);
    }else if (commandWord.equals("solve"))
      solveLock(command, in, (OpenableObject) itemMap.get("locker"));
    else if (commandWord.equals("open")){
      if(!command.hasSecondWord())
        System.out.println("Open what?");
      else
        return openItem(command);
      //might be true of false
    }else if (commandWord.equals("quit")) {
      if (command.hasSecondWord())
        System.out.println("Quit what?");
      else
        return true; // signal that we want to quit
    } else if (commandWord.equals("eat")) {
      System.out.println("Do you really think you should be eating at a time like this?");
    }
    return false;
  }

  private boolean useItem(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("Use what?");
      return false;
    }
    String itemName = command.getSecondWord();
    Item item = itemMap.get(itemName);
    if(currentRoom.getRoomName().equals("Cafeteria") && item.getName().equals("microwave") /*&& microwave.isLocked()*/){
      System.out.println("You turned on the microwave and all of a sudden you feel full. You ate the kid inside the microwave, which was crucial to your mission.");
      return true;
    }
    //use key
    return false;
    //use items from json file
  }

  private void playVideo(Command command) {
    //play the video in the robotics room
    if(!command.hasSecondWord()){
      System.out.println("Play what?");
      return;
    }else if(!command.getSecondWord().equals("video")){
      System.out.println("You can only play videos!");
    }else if(command.getSecondWord().equals("video") && currentRoom.getRoomName().equals("Room 109")){
      System.out.println("The video has started playing...");
      System.out.println("The short 10 second video shows fellow BVG students playing baseball in a gym.");
    }else{
      System.out.println("Invalid request.");
    }
  }

  public void checkBackpack(){
    //iterate through backpack
  }

  private void fight(Command command) {
    String enemyName = command.getSecondWord();
    enemy = characterMap.get(enemyName);
    if(enemy == null){
      System.out.println("You cannot fight " + enemyName);
    }else if(!(enemy.getRoom().equals(currentRoom.getRoomName()))){
      System.out.println(enemyName + " is nowhere in sight.");
    }else{
      while(playerHP>=0 && enemy.gethp()>=0){
        System.out.println("What weapon do you want to use? (If you want to see your weapons, type \"check backpack\"");
        System.out.print("> "); 
        String inputLine = in.nextLine();
        String itemName = inputLine.substring(inputLine.indexOf(" ")+1);
        if(inputLine.equals("check backpack")){
          backpack.checkBackpack();
        }else if(!backpack.checkItem(itemName)){
          System.out.println("You do not have that weapon to use!");
        }else{
          Item item = itemMap.get(itemName);
          Weapon weapon = (Weapon) item;
          if(weapon == null){
            System.out.println("You cannot use " + itemName);
          }else{
            System.out.println("You use the " + weapon.getName() + ".");
            enemy.sethp(enemy.gethp() - weapon.getDamage());
            System.out.println("Attack successful, Enemy -" + weapon.getDamage() + " health.");
            if(currentRoom.getRoomName().equals("Gym")){
                System.out.println("Enemy Attacks, player -10 health");
                playerHP -= 10;
            }else if(currentRoom.getRoomName().equals("FedericoOffice")){
                System.out.println("Enemy Attacks, player -15 health");
                playerHP -= 15;
            }else if(currentRoom.getRoomName().equals("Room106")){
                System.out.println("Enemy Attacks, player -25 health");
                playerHP -= 25;
            }
            System.out.println("Enemy health remaining: " + enemy.gethp());
            System.out.println("Player health remaining: " + playerHP);
            System.out.println();
          }
        }
      }
      if(enemy.gethp() <= 0){
        System.out.println("Enemy defeated");
      }else if(playerHP<=0){
        System.out.println("You have been defeated by " + enemy.getName() + "!");
      }
  }   
}

  private void wear(Command command) {
    //put on the costume
  }

  private void listen(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("Listen to what?");
      return;
    }

    String x = command.getSecondWord();
    x = x.toLowerCase();
    OpenableObject microwave = (OpenableObject) itemMap.get("microwave");
    
    //listen to what one of the kids has to say
    //print the dialogue/information from kids
      
    if(x.equals("alex")&& microwave.isOpen()){
        System.out.println();
        System.out.println("\"Hi friend. Thanks for saving me, I am Alex. There is a great conspiracy here at Bayview Glen, and I'm not sure if you want to uncover it. If you're in, take me with you to find more hints in Room 203. Oh, and beware if you like baseball, you're in danger.\" ");
    }else if(x.equals("maya")/**&& Room203 is unlocked */){
        System.out.println();
        System.out.println("\"Hi friend, I'm Maya. I'm guessing Kid#1 sent you here. The truth is, something terrible has happened at this school. Go to the theatre to learn more and remember the number 2.\"");
    }else if(x.equals("justin") /**&& kid#3 is untied, theatre is unlocked*/){
        System.out.println();
        System.out.println("\"Thanks for saving me, I'm Justin. They will call you crazy, but it is true. Kids are indeed disappearing from our school. Turn back now, or rise to the challenge, you will find my friend where people make robots.\"");
    }else if(x.equals("trevor")/**&& mr.cardon has been defeated, kid#4 has been freed*/){
        System.out.println();
        System.out.println("\"Thanks for your help, I'm Trevor. I believe my last friend is in Mr. Federico's office, please help him!\"");
    }else{
      System.out.println("What do you want to listen to? the floor?");
    }
  }

  private void read(Command command) {
    
  }

  private void untie(Command command) {
    String item = command.getSecondWord();
    item = item.toLowerCase();
    //using kid is too vague
    if(item.equals("kid")){
      System.out.println("You untied the kid.");
      //itemMap.get("kidOne")
      //release kid 
    }
  }

  private boolean openItem(Command command) {
    String item = command.getSecondWord();
    OpenableObject newItem2 = (OpenableObject) itemMap.get(item);
    if(currentRoom.getRoomName().equals("Room 212")){
      if(item.equals("chestOne")){
        System.out.println("You opened Chest1. There is a sword in the chest. ");
        currentRoom.addItem(itemMap.get("sword"));
        newItem2.setOpen(true);
      }else if(item.equals("chestTwo")){
        System.out.println("You opened Chest2. There is the upper part of the costume. The costume has a tag that reads \"from BVG shop \".");
        currentRoom.addItem(itemMap.get("costumeOne"));
        newItem2.setOpen(true);
      }else if(item.equals("chestThree")){
        System.out.println("You opened Chest3, and a bomb exploded.");
        currentRoom.addItem(itemMap.get("bomb"));
        newItem2.setOpen(true);
        return true;
      }else if(item.equals("chestFour")){
        System.out.println("You opened Chest4. There is $100!");
        wallet+=100;
        newItem2.setOpen(true);
      }else if(item.equals("chestFive")){
        System.out.println("You opened Chest5, and a bomb exploded.");
        newItem2.setOpen(true);
        return true;
      }
    }else if(currentRoom.getRoomName().equals("Cafeteria")){
      if(item.equals("microwave")){
        System.out.println("You opened the microwave. Alex hops out of the microwave and looks at you.");
        currentRoom.addItem(itemMap.get("kidOne"));
        newItem2.setOpen(true);
          //if(itemMap.get("microwave").isOpenable()) //index 20
            //itemMap.get("microwave").isOpenable();//open the microwave (set it to an opened state)
      }
    //write this open thing for locker
    }else if(currentRoom.getRoomName().equals("Hallway 3")){
      if(item.equals("locker")){
        if(newItem2.isOpen){
          newItem2.setOpen(true);
          System.out.println("You opened the locker.");
          read(command);
        }else{
          newItem2.setOpen(false);
          System.out.println("This locker is locked");
        }
      }
    }else{
      System.out.println("You cannot open a " + command.getSecondWord() + ". You can only open chests, microwaves, lockers, curtains, doors, and backpacks");
    }
    return false;
    //opens a different items (chest, locker, microwave, curtains, door, backpack)
    //check if you can open the item
  }

  // implementations of user commands:
  private void drop(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("Drop what?");
      return;
    }

    String x = command.getSecondWord();
    String item = "";
    x = x.toLowerCase();
    switch(x){
      case "alex": 
        item = "Kid #1";
        break;
      case "maya":
        item = "Kid #2";
        break;
      case "justin":
        item = "Kid #3";
        break;
      case "trevor":
        item = "Kid #4";
        break;
      case "aaron":
        item = "Kid #5";
        break;
      case "upper-costume":
        item = "Upper Costume piece";
        break;
      case "lower-costume":
        item = "Lower Costume piece";
        break;
      default:
        item = command.getSecondWord();
        break;
    }

    if(item == null)
      System.out.println("Drop what?");
    else{
      Item newItem = backpack.removeItem(item);
      if(backpack.getCurrentWeight()<=0){
        System.out.println("You have nothing to drop!");
      }else if(newItem == null){
        System.out.println("Drop what?");
      }else{
        backpack.currentWeight -= newItem.getWeight();
        currentRoom.addItem(newItem);
        System.out.println("You dropped the " + command.getSecondWord());
      }
  }
  }

  private boolean pickpocket(Command command) {
    //check to see if there is someone to pickpocket money from
    //take a random sum of money from a randomly generated person
    double rand = (Math.random()*11);
    rand += 5.0;
    rand*=100;
    rand = (int)rand;
    rand = (Double)rand;
    rand/=100;
    peoplePickpocketed++;
    double chance = peoplePickpocketed*3;
    double counter = (int)(Math.random()*101);
    if(counter>chance){
      System.out.println("You pickpocketed $"+rand+" from a random person.");
      wallet+=rand;
      return false;
    }else if(counter<=chance){
      return true;
    }
    return false;
  }

  private void takeItem(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("Take what?");
      return;
    }

    //if the item is inside of an openable object, you must open the object inorder to access the item inside

    String x = command.getSecondWord();
    String item = "";
    x = x.toLowerCase();
    switch(x){
      case "alex": 
        item = "Kid #1";
        break;
      case "maya":
        item = "Kid #2";
        break;
      case "justin":
        item = "Kid #3";
        break;
      case "trevor":
        item = "Kid #4";
        break;
      case "aaron":
        item = "Kid #5";
        break;
      case "upper-costume":
        item = "Upper Costume piece";
        break;
      case "lower-costume":
        item = "Lower Costume piece";
        break;
      default:
        item = command.getSecondWord();
        break;
    }
    // if(x.equals("kid#1")){
    //     item = "kidOne";
    // }else if(x.equals("kid#2")){
    //     item = "kidTwo";
    // }else if(x.equals("kid#3")){
    //     item = "kidThree";
    // }else if(x.equals("kid#4")){
    //     item = "kidFour";
    // }else if(x.equals("kid#5")){
    //     item = "kidFive";
    // }else{
    //   item = command.getSecondWord();
    // }

    if(item==null){
      System.out.println("Take what?");
    }else{
      Item newItem = currentRoom.removeItem(item);
      if(itemMap.get(item) instanceof OpenableObject)
        System.out.println("You cannot move the " + command.getSecondWord() + "!");
      else if(newItem == null)
        System.out.println("There is no " + item);
      else if(backpack.addItem(newItem)){
        System.out.println("You took the " + command.getSecondWord() + ".");
        backpack.currentWeight += newItem.getWeight();
       }else{
         currentRoom.addItem(newItem);
        System.out.println("You cannot take " + command.getSecondWord());
      }
  }

  }

  private void solveLock(Command command, Scanner in, OpenableObject openableObject) {
    if(!command.hasSecondWord()){
      System.out.println("Solve what?");
      return;
    }

    if(!command.getSecondWord().equals("lock")){
      System.out.println("You can not solve " + command.getSecondWord() + "! You can only solve locks.");
    }else{
      System.out.println("Input the correct lock passcode (Enter '-' inbetween numbers)");
      System.out.print("> "); 
      String inputLine = in.nextLine();
      if(inputLine.equals("0-13-20")){
        System.out.println("Passcode is correct!");
        openableObject.setOpen(true);
      }else{
        System.out.println("Incorrect passcode!");
        openableObject.setOpen(false);
      }
    }
  }

  /**
   * Print out some help information. Here we print some stupid, cryptic message
   * and a list of the command words.
   */
  private void printHelp() {
    System.out.println("You are lost. You are alone. You wander");
    System.out.println("around at Monash Uni, Peninsula Campus.");
    System.out.println();
    System.out.println("Your command words are:");
    parser.showCommands();
  }

  /**
   * Try to go to one direction. If there is an exit, enter the new room,
   * otherwise print an error message.
   */
  private void goRoom(Command command) {
    if (!command.hasSecondWord()) {
      // if there is no second word, we don't know where to go...
      System.out.println("Go where?");
      return;
    }

    String direction = command.getSecondWord();

    // Try to leave current room.
    Room nextRoom = currentRoom.nextRoom(direction);
    int ind = -1;
    for(int i = 0; i<currentRoom.getExits().size(); i++){
      if(direction.equals(currentRoom.getExits().get(i).getDirection().toLowerCase())){
        ind = i;
      }
    }

    if(nextRoom.getRoomName().equals("Room 203")){
      Exit exit = null;
      for(Exit exit1:currentRoom.getExits()){
        if(exit1.getAdjacentRoom().equals("Room203"))
          exit = exit1;
      }
      if(exit==null){
        return;
      }else if(exit.isLocked()){
        System.out.println("Room 203 is locked! There is a slip of paper that reads \"Someone has been abducting children. The key to this door can be found in locker #121, in the third hallway.\"");
        return;
      }
    }

    if (nextRoom == null)
      System.out.println("There is no door!");
    else {
      if(currentRoom.getExits().get(ind).getLocked()){
        if((nextRoom.getRoomName().equals("Theatre")||nextRoom.getRoomName().equals("Upper Theatre"))&&(!backpack.checkItem("Upper Costume piece")||!backpack.checkItem("Lower Costume piece"))){
          System.out.println("You do not have a full costume, the theatre is only for members of the play");
        }else if((nextRoom.getRoomName().equals("Theatre")||nextRoom.getRoomName().equals("Upper Theatre"))&&(backpack.checkItem("Upper Costume piece")&&backpack.checkItem("Lower Costume piece"))){
          System.out.println("Welcome member of the play!");
          currentRoom = nextRoom;
          System.out.println(currentRoom.longDescription());
        }
      }else{
        currentRoom = nextRoom;
        System.out.println(currentRoom.longDescription());
      }
    }
  }
}