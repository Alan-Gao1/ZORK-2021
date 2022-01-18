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
  private int slingshotAmmo = 0;
  private boolean shoheiUntied = false;
  private boolean oneReturned = false;
  private boolean twoReturned = false;
  private boolean threeReturned = false;
  private boolean fourReturned = false;
  private boolean fiveReturned = false;

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
      Boolean characterIsDefeated = (Boolean) ((JSONObject) itemObj).get("isDefeated");
      String characterStartingRoom = (String) ((JSONObject) itemObj).get("room");
      JSONArray jsonUse = (JSONArray) ((JSONObject) itemObj).get("use");
      int damage = 0;
      for (Object itemUse : jsonUse) {
        String sdamage = (String) ((JSONObject) itemUse).get("damage");
        damage = Integer.parseInt(sdamage);
      }
      characters character = new characters(characterHP, characterName, characterIsFightable, characterIsDefeated, characterId, characterStartingRoom);
      character.setDamage(damage);
      characterList.add(character);
      characterMap.put(characterId, character);
    }
  }

  private void displayInfo(){
    System.out.println("HP: "+playerHP+"HP");
    System.out.println("Money: $"+wallet);
    System.out.println(backpack.getCurrentWeight()+"/"+backpack.getMaxWeight()+" weight used");
    System.out.println();
    backpack.printContents();
    System.out.println("Room Info: ");
    currentRoom.printExitInfo();
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
      String object = "";
      String contentDesc = "";
      if(itemId.equals("chestOne")||itemId.equals("chestTwo")||itemId.equals("chestThree")||itemId.equals("chestFour")||itemId.equals("chestFive")||itemId.equals("microwave")){
        isChest = (Boolean) ((JSONObject) itemObj).get("isChest");
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
        Chest chest = new Chest(iWeight, itemName, itemIsOpenable, itemId, itemDescription, itemStartingRoom, isLocked, "0", false, contentDesc);
        if(!object.equals("money")){
          chest.addItem(findContents(object));
        }
        itemList.add(chest);
        itemMap.put(itemId, chest);
        putIteminRoom(itemStartingRoom, itemId);
      }else{
        Item item = new Item(iWeight, itemName, itemIsOpenable, itemId, itemDescription, itemStartingRoom, itemIsWeapon);
        if(itemIsWeapon){
          item = new Weapon(iWeight, itemName, itemIsOpenable, itemId, itemDescription, itemStartingRoom, 0, itemIsWeapon);
          item.setDamage(itemId);
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
        wallet = round(wallet);
        winCondition = checkWin();
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
    if(winCondition){
      System.out.println();
      System.out.println("You have successfully found all abducted children and defeated the final boss!");
      System.out.println("Thank you for playing. Good bye.");
    }else{
      System.out.println("Game over.");
    }
  }

  private boolean checkWin() {
    if(characterMap.get("MrDesLauriers").isDefeated()&&characterMap.get("MrCardone").isDefeated()&&characterMap.get("MrFederico").isDefeated()&&shoheiUntied&&oneReturned&&twoReturned&&threeReturned&&fourReturned&&fiveReturned){
      finished = true;
      return true;
    }else{
      return false;
    }
  }

  private double round(double wallet) {
    wallet*=100;
    wallet = (int) wallet;
    wallet = (double) wallet;
    wallet/=100;
    return wallet;
  }

  /**
   * Print out the opening message for the player.
   */
  private void printWelcome() {
    System.out.println();
    System.out.println("Welcome to Zork!");
    System.out.println("Zork is a new, incredibly boring adventure game.");
    System.out.println("Type \"help\" if you need help.");
    System.out.println("Pro Tip: Use \"info\" frquently to see availible exits and where they lead to!");
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
    else if (commandWord.equals("listen"))
      listen(command);
    else if (commandWord.equals("wear"))
      wear(command);
    else if (commandWord.equals("fight"))
      return fight(command);
    else if (commandWord.equals("buy")){
      if(currentRoom.getRoomName().equals("BVG shop")){
        buy(command);
      }else{
        System.out.println("You can't buy stuff in places that aren't the shop. Go to the shop to buy stuff.");
      }
    }
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

  private void buy(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("What do you even wanna buy? actually specify something to buy");
      return;
    }

    boolean validObject = false;
    String objectId = command.getSecondWord();
    if(objectId.equals("lower-costume")){
      objectId = "costumeTwo";
    }
    for (Item item : currentRoom.getInv().getInventory()) {
      if(objectId.equals(item.getId())){
        validObject = true;
      }
    }

    if(validObject){
      int price = 0;
      switch (objectId) {
        case "dagger":
          price = 10;
          break;
        case "slingshot":
          price = 25;
          break;
        case "pellet":
          price = 25;
          break;
        case "healthJar":
          price = 50;
          break;
        case "costumeTwo":
          price = 10;
          break;
        default:
          break;
      }
      backpack.addItem(currentRoom.removeItem(itemMap.get(objectId).getName()));
      wallet -= price;
      if(wallet<0){
        System.out.println("You do not have enough money to buy " + itemMap.get(objectId).getName() + ". You can get money by pickpocketing or finding cash.");
        wallet += price;
        currentRoom.addItem(backpack.removeItem(itemMap.get(objectId).getName()));
      }else if(backpack.currentWeight + itemMap.get(objectId).getWeight() >= backpack.getMaxWeight()){
        currentRoom.addItem(backpack.removeItem(itemMap.get(objectId).getName()));
      }else if(objectId.equals("healthJar")){
        playerHP += 100;
        System.out.println("You now have " + playerHP+"HP");
      }else if(objectId.equals("pellet")){
        slingshotAmmo += 5;
        System.out.println("You now have " + slingshotAmmo + " pellets to use for your slingshot");
      }else{
        System.out.println("You bought the "+ itemMap.get(objectId).getName());
      }
    }else{
      System.out.println("Not a valid object ID! You can only buy a dagger, slingshot, pellet, healthJar, or lower-costume once.");
    }
  }

  private boolean useItem(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("Use what?");
      return false;
    }
    String itemName = command.getSecondWord();
    Item item = itemMap.get(itemName);
    if(item == null){
      System.out.println("You cannot use that item!");
    }else if(currentRoom.getRoomName().equals("Cafeteria") && item.getName().equals("microwave")){
      if(!(backpack.checkItem("Alan"))){
        System.out.println("You turned on the microwave and all of a sudden you feel full. You ate the kid inside the microwave, which was crucial to your mission.");
        return true;
      }else{
        System.out.println("You turned the microwave on. Thank goodness you took Alan out of the microwave.");
        return false;
      }
    }else if(currentRoom.getRoomName().equals("Hallway 3") && backpack.checkItem(itemName) && item.getName().equals("key")){
      System.out.println("Room 203 is now open!");
      return false;
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

  private boolean fight(Command command) {
    String enemyId = command.getSecondWord();
    enemy = characterMap.get(enemyId);
    if(enemy == null){
      System.out.println("You cannot fight " + enemy.getName());
      return false;
    }else if(!(enemy.getRoom().equals(currentRoom.getRoomName()))){
      System.out.println(enemy.getName() + " is nowhere in sight.");
      return false;
    }else if(!backpack.checkWeapons()){
      System.out.println("You do not have any weapons in your inventory! Go find weapons before fighting.");
      return false;
    }else{
      while(playerHP>0 && enemy.gethp()>0){
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
          }else if(weapon.getName().equals("slingshot")&&slingshotAmmo==0){
            System.out.println("You have no slingshotAmmo. You must buy some at the shop. You cannot use your slingshot without ammo.");
          }else{
            System.out.println("You use the " + weapon.getName() + ".");
            enemy.sethp(enemy.gethp() - weapon.getDamage());
            System.out.println("Attack successful, Enemy -" + weapon.getDamage() + " health.");
            if(!weapon.getName().equals("slingshot")){
              if(currentRoom.getRoomName().equals("Gym")){
                  System.out.println("Enemy Attacks, player -10 health");
                  playerHP -= 10;
              }else if(currentRoom.getRoomName().equals("Mr.Federico's Office")){
                  System.out.println("Enemy Attacks, player -15 health");
                  playerHP -= 20;
              }else if(currentRoom.getRoomName().equals("Room 106")){
                  System.out.println("Enemy Attacks, player -25 health");
                  playerHP -= 25;
              }
            }else{
              if(currentRoom.getRoomName().equals("Room 106")&&slingshotAmmo>0){
                System.out.println("You were not able to avoid the ranged attacks of Mr. DesLauriers' robot lasers. However, you were able to avoid some of the damage.");
                System.out.println("Enemy Attacks, player -5 health");
                playerHP -= 5;
                slingshotAmmo--;
              }else if(slingshotAmmo>0){
                System.out.println("Because you were using the slingshot, you have avoided all attacks!");
                slingshotAmmo--;
              }else{
                System.out.println("You have no more slingshot ammo left!");
              }
            }
            if(enemy.gethp()>0)
              System.out.println("Enemy health remaining: " + enemy.gethp());
            else
              System.out.println("Enemy health remaining: 0");
            if(playerHP>0)
              System.out.println("Player health remaining: " + playerHP);
            else
              System.out.println("Player health remaining: 0");
            System.out.println();
          }
        }
        if(backpack.isOnlySlingshot()){
          System.out.println("You have no more slingshotAmmo left!");
          playerHP = 0;
        }
      }
      if(enemy.gethp() <= 0){
        System.out.println("Enemy defeated!");
        enemy.setDefeated(enemy.gethp());
        if(playerHP>0)
          System.out.println("Player health remaining: " + playerHP);
        else
          System.out.println("Player health: 0");
        if(currentRoom.getRoomName().equals("Gym")){
          System.out.println("You gained $20!");
          wallet += 20.0;
        }else if(currentRoom.getRoomName().equals("Mr.Federico's Office")){
          System.out.println("You gained $50!");
          wallet += 50.0;
          System.out.println("You have defeated Mr.Federico and he tells you the following. \"I am not part of this kidnapping. The truth is, Mr.DesLauriers is the mastermind behind this whole happening. He has been kidnapping children to form an elite BVG baseball team! You'll probably find him in his office. Best of luck...\".");
        }
        return false;
      }else if(playerHP<=0){
        System.out.println("You have been defeated by " + enemy.getName() + "!");
        return true;
      }
  }
  return false;   
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
    Item kid = itemMap.get(x);
    //listen to what one of the kids has to say
    //print the dialogue/information from kids

    if(kid == null){
      System.out.println("What do you want to listen to? the floor?");
    }else if(backpack.checkItem(kid.getName())){
      if(x.equals("alan")&& microwave.isOpen()){
        System.out.println();
        System.out.println("\"Hi friend. Thanks for saving me, I am Alan. There is a great conspiracy here at Bayview Glen, and I'm not sure if you want to uncover it. If you're in, take me with you to find more hints in Room 203.\" ");
      }else if(x.equals("elly")){
        System.out.println();
        System.out.println("\"Hi friend, I'm Elly. I'm guessing Alan sent you here, but if you haven't found him, he is in the cafeteria. The truth is, something terrible has happened at this school. Go to the theatre to learn more and remember: choose number 2. Hopefully you'll find Shohei.\"");
      }else if(x.equals("shohei") /**&& kid#3 is untied, theatre is unlocked*/){
        System.out.println();
        System.out.println("\"Thanks for saving me, I'm Shohei. They will call you crazy, but it is true. Kids are disappearing from our school. You will find your next friend where you find robots.\"");
      }else if(x.equals("trevor")){/**&& mr.cardone has been defeated, kid#4 has been freed*/
        if(characterMap.get("MrCardone").isDefeated()){
          System.out.println();
          System.out.println("\"Thanks for your help, I'm Trevor. I believe my last friend is in Mr. Federico's office, please help him!\"");
        }else
          System.out.println("You must fight and defeat Mr. Cardone in order to listen to Trevor!");
      }else if(x.equals("lucas")){
        System.out.println();
        System.out.println("\"Hi, I'm Lucas. Thanks for saving me and my friends! You have to put all of my friends in your backpack in order to win.\"");
        if(!backpack.checkItem("alan")){
          System.out.println("You haven't found Alan yet. Find him where we eat food!");
        }else if(!backpack.checkItem("elly")){
          System.out.println("You haven't found Elly yet. She's in Room 203.");
        }else if(!backpack.checkItem("shohei")){
          System.out.println("You haven't found Shohei yet. He's in the Upper Theatre.");
        }else if(!backpack.checkItem("trevor")){
          System.out.println("You haven't found Trevor yet. He's in the Gym.");
        }else if(!backpack.checkItem("lucas")){
          System.out.println("Save me to win!");
        }
      }
    }else{
      System.out.println("Invalid second command word. Ensure that the you are listening to children who can speak, you have taken, and are in the same room as you");
    }
    
  }

  private void untie(Command command) {
    String item = command.getSecondWord();
    item = item.toLowerCase();
    if(item.equals("shohei")){
      if(currentRoom.getRoomName().equals("Upper Theatre")){
        System.out.println("You untied the kid.");
        Item kid = itemMap.get("shohei");
        kid.setDescription("This is shohei. This kid has been untied.");
        shoheiUntied = true;
      }else if(currentRoom.getRoomName().equals("Theatre")){
        System.out.println("Shohei is not here!");
      }else{
        System.out.println("what...? what and who are you untying? you're delusional.");
      }
    }
  }

  private boolean openItem(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("Open what?");
      return false;
    }
    String item = command.getSecondWord();
    if(itemMap.get(item) == null || !itemMap.get(item).isOpenable()||!currentRoom.getInv().checkItem(itemMap.get(item).getName())){
      System.out.println("You cannot open a " + command.getSecondWord() + ". You can only open chests, microwaves, lockers, curtains, doors, and backpacks in your current room");
      return false;
    }else{
      OpenableObject newItem2 = (OpenableObject) itemMap.get(item);
      if(newItem2.getOpen()){
        System.out.println(item + " is already open! You cannot open it again.");
      }else if(currentRoom.getRoomName().equals("Room 212")){
        if(item.equals("chestOne")){
          System.out.println(((Chest)itemMap.get("chestOne")).getContentDescription());
          newItem2.setOpen(true);
        }else if(item.equals("chestTwo")){
          System.out.println(((Chest)itemMap.get("chestTwo")).getContentDescription());
          currentRoom.addItem(itemMap.get("costumeOne"));
          newItem2.setOpen(true);
        }else if(item.equals("chestThree")){
          System.out.println(((Chest)itemMap.get("chestThree")).getContentDescription());
          currentRoom.addItem(itemMap.get("bomb"));
          newItem2.setOpen(true);
          return true;
        }else if(item.equals("chestFour")){
          System.out.println(((Chest)itemMap.get("chestFour")).getContentDescription());
          wallet+=100;
          System.out.println("You now have $" + wallet + " in your wallet. ");
          newItem2.setOpen(true);
        }else if(item.equals("chestFive")){
          System.out.println(((Chest)itemMap.get("chestFive")).getContentDescription());
          newItem2.setOpen(true);
          return true;
        }
      }else if(currentRoom.getRoomName().equals("Cafeteria")){
        if(item.equals("microwave")){
          System.out.println("You opened the microwave. Alan climbs out of the microwave and looks at you.");
          currentRoom.addItem(itemMap.get("alan"));
          newItem2.setOpen(true);
          itemMap.get("alan").setStartingRoom("Cafeteria");
        }
      }else if(currentRoom.getRoomName().equals("Hallway 3")&&item.equals("locker")){
          if(!newItem2.isLocked()){
            newItem2.setOpen(true);
            System.out.println("You opened the locker. There is a key inside. ID: \"key\"");
          }else{
            newItem2.setOpen(false);
            System.out.println("This locker is locked. Use \"solve lock\".");
          }
      }else if(currentRoom.getRoomName().equals("Hallway 3")&&item.equals("lock")){
        System.out.println("Use \"solve lock\" to open a lock");
      }else{
        System.out.println("You cannot open a " + command.getSecondWord() + ". You can only open chests, microwaves, lockers, curtains, doors, and backpacks");
      }
      return false;
    }
  }

  private void drop(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("Drop what?");
      return;
    }

    String x = command.getSecondWord();
    String item = "";
    x = x.toLowerCase();
    switch(x){
      case "alan": 
        item = "Alan";
        break;
      case "elly":
        item = "Elly";
        break;
      case "shohei":
        item = "Shohei";
        break;
      case "trevor":
        item = "Trevor";
        break;
      case "lucas":
        item = "Lucas";
        break;
      case "costumeone":
        item = "Upper Costume piece";
        break;
      case "costumetwo":
        item = "Lower Costume piece";
        break;
      case "arm":
        item = "arm armour";
        break;
      case "chestplate":
        item = "chestplate armour";
        break;
      case "feet":
        item = "feet armour";
        break;
      case "helmet":
        item = "helmet armour";
        break;
      default:
        item = command.getSecondWord();
        break;
    }

    if(item == null)
      System.out.println("Drop what?");
    else{
      Item newItem = backpack.removeItem(item);
      if(newItem == null){
        System.out.println("You cannot drop " + item);
      }else if(backpack.getCurrentWeight()<=0&&newItem.getWeight()!=0){
        System.out.println("You have nothing to drop!");
      }else if(x.equals("helmet")||x.equals("arm")||x.equals("feet")||x.equals("chestplate")){
        backpack.currentWeight -= newItem.getWeight();
        int healthAdd = 0;
        if(x.equals("helmet")){
          healthAdd = 50;
          System.out.println("Your health has been decreased by 50 due to dropping the helmet!");
        }else if(x.equals("chestplate")){
          healthAdd = 50;
          System.out.println("Your health has been decreased by 50 due to dropping the chestplate!");
        }else if(x.equals("feet")){
          healthAdd = 25;
          System.out.println("Your health has been decreased by 25 due to dropping feet armour");
        }else if(x.equals("arm")){
          healthAdd = 25;
          System.out.println("Your health has been decreased by 25 due to dropping arm armour");
        }
        playerHP -= healthAdd;
      }else if(x.equals("alan")||x.equals("shohei")||x.equals("elly")||x.equals("lucas")||x.equals("elly")){
        if(currentRoom.getRoomName().equals("Lobby")){
          switch(x){
            case "alan": 
              System.out.println("Alan has been rescued!");
              oneReturned = true;
              break;
            case "elly":
              System.out.println("Elly has been rescued!");
              twoReturned = true;
              break;
            case "shohei":
              System.out.println("Shohei has been rescued!");
              threeReturned = true;
              break;
            case "trevor":
              System.out.println("Trevor has been rescued!");
              fourReturned = true;
              break;
            case "lucas":
              System.out.println("Lucas has been rescued!");
              fiveReturned = true;
              break;
          }
          backpack.currentWeight -= newItem.getWeight();
          currentRoom.addItem(newItem);
        }else{
          System.out.println("You can only drop the kids in the lobby to return them.");
        }
      }else{
        backpack.currentWeight -= newItem.getWeight();
        currentRoom.addItem(newItem);
        System.out.println("You dropped the " + command.getSecondWord() + ".");
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
      case "alan": 
        item = "Alan";
        break;
      case "elly":
        item = "Elly";
        break;
      case "shohei":
        item = "Shohei";
        break;
      case "trevor":
        item = "Trevor";
        break;
      case "lucas":
        item = "Lucas";
        break;
      case "upper-costume":
        item = "Upper Costume piece";
        break;
      case "key":
        item = "key";
        break;
      case "lower-costume":
        item = "Lower Costume piece";
        break;
      case "arm":
        item = "arm armour";
        break;
      case "chestplate":
        item = "chestplate armour";
        break;
      case "feet":
        item = "feet armour";
        break;
      case "helmet":
        item = "helmet armour";
        break;
      default:
        item = command.getSecondWord();
        break;
    }

    if(item==null){
      System.out.println("Take what?");
    }else{
      Item newItem = currentRoom.removeItem(item);
      if(itemMap.get(item) instanceof OpenableObject)
        System.out.println("You cannot move the " + command.getSecondWord() + "!");
      else if(newItem == null)
        System.out.println("There is no " + item);
      else if(item.equals("Lower Costume piece")){
        System.out.println("You must buy the lower-costume from the shops.");
        currentRoom.addItem(newItem);
      }else if(item.equals("key")){
        if(((OpenableObject) itemMap.get("locker")).isLocked()){
          System.out.println("The key is inside the locked locker!");
          currentRoom.addItem(newItem);
        }else{
          System.out.println("You took the key. This key unlocks Room 203");
          backpack.addItem(newItem);
        }
      }else if(item.equals("Trevor") && !characterMap.get("MrCardone").isDefeated()){
        System.out.println("Mr. Cardone is not defeated. You cannot take Trevor!");
        currentRoom.addItem(newItem);
      }else if(x.equals("helmet")||x.equals("arm")||x.equals("feet")||x.equals("chestplate")){
        backpack.addItem(newItem);
        backpack.currentWeight += newItem.getWeight();
        int healthAdd = 0;
        if(x.equals("helmet")){
          healthAdd = 50;
          System.out.println("Your health has been increased by 50 due to wearing the helmet!");
        }else if(x.equals("chestplate")){
          healthAdd = 50;
          System.out.println("Your health has been increased by 50 due to wearing the chestplate!");
        }else if(x.equals("feet")){
          healthAdd = 25;
          System.out.println("Your health has been increased by 25 due to wearing feet armour");
        }else if(x.equals("arm")){
          healthAdd = 25;
          System.out.println("Your health has been increased by 25 due to wearing arm armour");
        }
        playerHP += healthAdd;
      }else if((x.equals("alan")||x.equals("shohei")||x.equals("trevor")||x.equals("elly")||x.equals("lucas"))&&backpack.addItem(newItem)){
        if(x.equals("shohei")&&!shoheiUntied){
          System.out.println("Shohei is still tied up! Untie shohei to take him.");
        }else{
          System.out.println("You took the " + command.getSecondWord() + ". In order to win, you must drop each child with the drop command in the lobby.");
          System.out.println("You may also listen to the kid to gain more information!");
          backpack.currentWeight += newItem.getWeight();
        }
      }else if(backpack.addItem(newItem)){
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
    }else if(currentRoom.getRoomName().equals("Hallway 3")){
      System.out.println();
      System.out.println("The passcode has 3 numbers from 0-99. Do not enter in 0s before single digit numbers");
      System.out.println("Passcode Hint: ");
      System.out.println("The first number is neither positive nor negative");
      System.out.println("The second number is commonly recognized as unlucky");
      System.out.println("The third numbers is 5*4/2*3-10");
      System.out.println("Input the correct lock passcode (Enter '-' inbetween numbers)"); 
      boolean solved = false;
      while(!solved){
        System.out.print("> ");
        String inputLine = in.nextLine();
        if(inputLine.equals("0-13-20")){
          System.out.println("Passcode is correct! Open the locker see what's inside!");
          solved = true;
          openableObject.setLocked(false);
        }else{
          System.out.println("Incorrect passcode!");
          openableObject.setLocked(true);
        }
      }
    }else{
      System.out.println("Cannot unlock lock in a room other than Hallway 3");
    }
  }

  /**
   * Print out some help information. Here we print some stupid, cryptic message
   * and a list of the command words.
   */
  private void printHelp() {
    System.out.println();
    System.out.println("Your command words are:");
    parser.showCommands();
    System.out.println("Player health: " + playerHP);
    System.out.println("Wallet: $" + wallet);
    System.out.println("'info' will display all items in your backpack.");
  }
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

    if (nextRoom == null)
      System.out.println("There is no door!");
    else {
      if(currentRoom.getExits().get(ind).getLocked()){
        if((nextRoom.getRoomName().equals("Theatre")||nextRoom.getRoomName().equals("Upper Theatre"))&&(!backpack.checkItem("Upper Costume piece")||!backpack.checkItem("Lower Costume piece"))){
          System.out.println("You do not have a full costume, the theatre is only for members of the play.");
          System.out.println("Hint: costumes can be found in chests. It is rumored that Room 212 has a lot of spare equipment for the play");
        }else if((nextRoom.getRoomName().equals("Theatre")||nextRoom.getRoomName().equals("Upper Theatre"))&&(backpack.checkItem("Upper Costume piece")&&backpack.checkItem("Lower Costume piece"))){
          System.out.println("Welcome member of the play!");
          currentRoom = nextRoom;
          System.out.println(currentRoom.longDescription());
        }else if(nextRoom.getRoomName().equals("Room 203") && !backpack.checkItem("key")){
          System.out.println("Room 203 is locked! There is a slip of paper that reads \"Someone has been abducting children. The key to this door can be found in locker #121, in the third hallway.\" You must solve the lock.");
        }else if(nextRoom.getRoomName().equals("Room 203") && backpack.checkItem("key")){
          System.out.println("Room 203 is unlocked!");
          currentRoom = nextRoom;
          System.out.println(currentRoom.longDescription());
          currentRoom.printRoomContents();
        }else if(nextRoom.getRoomName().equals("Room 106") && !characterMap.get("MrFederico").isDefeated()){
          System.out.println("Room 106 is locked! Come back to check after.");
        }else if(nextRoom.getRoomName().equals("Room 106") && characterMap.get("MrFederico").isDefeated()){
          currentRoom = nextRoom;
          System.out.println(currentRoom.longDescription());
          currentRoom.printRoomContents();
        }else{
          currentRoom = nextRoom;
          System.out.println(currentRoom.longDescription());
          currentRoom.printRoomContents();
        }
      }else{
        currentRoom = nextRoom;
        System.out.println(currentRoom.longDescription());
        currentRoom.printRoomContents();
      }
    }
  }
}