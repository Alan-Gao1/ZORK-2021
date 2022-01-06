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

  private Parser parser;
  private Room currentRoom;;
  private int peoplePickpocketed;
  public boolean finished = false;
  private boolean winCondition = false;
  private Inventory backpack = new Inventory(10);

  /**
   * Create the game and initialise its internal map.
   */
  public Game() {
    try {
      initRooms("src\\zork\\data\\rooms.json");
      initItems("src\\zork\\data\\items.json");
      System.out.println(itemList);
      currentRoom = roomMap.get("Lobby");
    } catch (Exception e) {
      e.printStackTrace();
    }
    parser = new Parser();
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
      if(itemIsOpenable&&!isChest){
        OpenableObject openableObject = new OpenableObject();
        isLocked = (Boolean) ((JSONObject) itemObj).get("isLocked");
        openableObject.setLocked(isLocked);
        openableObject.setOpenable(true);
        if(openableObject.isLocked()){
          String itemKey = (String) ((JSONObject) itemObj).get("keyId");
          openableObject.setKeyId(itemKey);
        }
        openableObject.setName(itemName);
        openableObject.setWeight(iWeight);
        openableObject.setId(itemId);
        openableObject.setDescription(itemDescription);
        itemList.add(openableObject);
        itemMap.put(itemId, openableObject);
      }else if(isChest){
        Chest chest = new Chest();
        chest.setLocked(isLocked);
        chest.setOpenable(true);
        chest.setName(itemName);
        chest.setWeight(iWeight);
        chest.setId(itemId);
        chest.setDescription(itemDescription);
        chest.setChestNum(chestNum);
        chest.addContentsChest(findContents(object));
        chest.setContents(contentDesc);
        itemList.add(chest);
        //once added, the item stored in chest isnt there anymore
        itemMap.put(itemId, chest);
      }else{
        Item item = new Item();
        item.setName(itemName);
        item.setWeight(iWeight);
        item.setOpenable(itemIsOpenable);
        item.setId(itemId);
        item.setDescription(itemDescription);
        item.setStartingRoom(itemStartingRoom);
        itemList.add(item);
        itemMap.put(itemId, item);
      }   
    }
  }

  private Item findContents(String object) {
    for (Item item : itemList) {
      if(item.getId().equals(object)){
        Item returnItem = new Item();
        returnItem = item;
        return returnItem;
      }
    }
    return null;
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
      ArrayList<Exit> exits = new ArrayList<Exit>();
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
      System.out.println("Thank you for playing.  Good bye.");
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
    else if (commandWord.equals("pickpocket"))
      pickpocket(command);
    else if (commandWord.equals("drop"))
      drop(command);
    else if (commandWord.equals("untie"))
      untie(command);
    else if (commandWord.equals("read"))
      read(command);
    else if (commandWord.equals("listen"))
      listen(command);
    else if (commandWord.equals("wear"))
      wear(command);
    else if (commandWord.equals("play"))
      playVideo(command);
    else if (commandWord.equals("use")){
      if(!command.hasSecondWord())
        System.out.println("Use what?");
      else
        return useItem(command);
    }else if (commandWord.equals("solve"))
      solveLock(command, in);
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
    if(currentRoom.getRoomName().equals("Cafeteria") && command.getSecondWord().equals("microwave") /*&& microwave.isLocked()*/){
        System.out.println("You turned on the microwave and all of a sudden you feel full. You killed the kid inside the microwave, which was crucial to your mission.");
        return true;
    }
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

  private void wear(Command command) {
    //put on the costume
  }

  private void listen(Command command) {
    //listen to what one of the kids has to say
    //print the dialogue/information from kids
    
    if(command.getSecondWord().equals("kid")){
      if(currentRoom.getRoomName().equals("Cafeteria")/*&& !microwave.isLocked*/){
        System.out.println();
        System.out.println("\"Hi friend. Thanks for saving me, I am Kid#1. There is a great conspiracy here at Bayview Glen, and I'm not sure if you want to uncover it. If you're in, take me with you to find more hints in Room 203. Oh, and beware if you like baseball, you're in danger.\" ");
      }else if(currentRoom.getRoomName().equals("Room 203")/**&& Room203 is unlocked */){
        System.out.println();
        System.out.println("\"Hi friend, I'm Kid#2. I'm guessing Kid#1 sent you here. The truth is, something terrible has happened at this school. Go to the theatre to learn more and remember the number 2.\"");
      }else if(currentRoom.getRoomName().equals("Theatre") /**&& kid#3 is untied, theatre is unlocked*/){
        System.out.println();
        System.out.println("\"Thanks for saving me, I'm Kid#3. They will call you crazy, but it is true. Kids are indeed disappearing from our school. Turn back now, or rise to the challenge, you will find my friend where people make robots.\"");
      }else if(currentRoom.getRoomName().equals("Gym")/**&& mr.cardon has been defeated, kid#4 has been freed*/){
        System.out.println();
        System.out.println("\"Thanks for your help, I'm Kid#4. I believe my last friend is in Mr. Federico's office, please help him!\"");
      }
    }
  }

  private void read(Command command) {
    //print the clues for the lock/locker/key
  }

  private void untie(Command command) {
    String item = command.getSecondWord();
    if(item.equals("kid")||item.equals("Kid")){
      System.out.println("You untied the kid.");
      //itemMap.get("kidOne")
      //release kid 
    }
  }

  private boolean openItem(Command command) {
    String item = command.getSecondWord();
    if(currentRoom.getRoomName().equals("Room 212")){
      if(item.equals("Chest1")){
        System.out.println("You opened Chest1. There is a sword in the chest. ");
        
      }else if(item.equals("Chest2")){
        System.out.println("You opened Chest2. There is the upper part of the costume. The costume has a tag that reads \"from BVG shop \".");
      }else if(item.equals("Chest3")){
        System.out.println("You opened Chest3, and a bomb exploded.");
        return true;
      }else if(item.equals("Chest4")){
        System.out.println("You opened Chest4. There is $100!");
      }else if(item.equals("Chest5")){
        System.out.println("You opened Chest5, and a bomb exploded.");
        return true;
      }
    }else if(currentRoom.getRoomName().equals("Cafeteria")){
      if(item.equals("microwave")){
        System.out.println("You opened the microwave. A kid hops out of the microwave and looks at you.");
          //if(itemMap.get("microwave").isOpenable()) //index 20
            //itemMap.get("microwave").isOpenable();//open the microwave (set it to an opened state)
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

    String item = "";
    if(command.getSecondWord().equals("kid")){
      if(currentRoom.getRoomName().equals("Cafeteria")){
        item = "kidOne";
      }else if(currentRoom.getRoomName().equals("Room203")){
        item = "kidTwo";
      }else if(currentRoom.getRoomName().equals("UpperTheatre")){
        item = "kidThree";
      }else if(currentRoom.getRoomName().equals("Gym")){
        item = "kidFour";
      }else if(currentRoom.getRoomName().equals("Room106")){
        item = "kidFive";
      }
    }else{
      item = command.getSecondWord();
    }

    Item newItem = itemMap.get(item);

    if(itemMap.get(item)!=null){
      backpack.remove(newItem);
      System.out.println("You took the " + command.getSecondWord() + ".");
    }else{
      System.out.println("You cannot take " + command.getSecondWord());
    }
    //remove an item from inventory (ex. remove kid or item)

    //you do not have anything to drop
  }

  private void pickpocket(Command command) {
    //check to see if there is someone to pickpocket money from
    //take a random sum of money from a randomly generated person
    double rand = (int)(Math.random()*11);
    rand += 5;
    peoplePickpocketed++;
    double chance = peoplePickpocketed*3;
    double counter = (int)(Math.random()*101);
    if(counter>chance){
      System.out.println("You pickpocketed $"+rand+" from a random person.");
    }else if(counter<=chance){
      finished = true;
    }
  }

  private void takeItem(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("Take what?");
      return;
    }

    String item = "";
    if(command.getSecondWord().equals("kid")){
      if(currentRoom.getRoomName().equals("Cafeteria")){
        item = "kidOne";
      }else if(currentRoom.getRoomName().equals("Room 203")){
        item = "kidTwo";
      }else if(currentRoom.getRoomName().equals("UpperTheatre")){
        item = "kidThree";
      }else if(currentRoom.getRoomName().equals("Gym")){
        item = "kidFour";
      }else if(currentRoom.getRoomName().equals("Room106")){
        item = "kidFive";
      }
    }else{
      item = command.getSecondWord();
    }

    Item newItem = itemMap.get(item);
    //Item newItem = new Item(10, item, true); //** this is hardcoded but retrieve the values from the json */
    //check to see if item exists in the json file

    //if(item can be moved)
      // System.out.println("You cannot move the " + command.getSecondWord() + "!");
    //else{
    if(itemMap.get(item)!=null){
      if(backpack.addItem(newItem)){
        System.out.println("You took the " + command.getSecondWord() + ".");
      }
    }else{
      System.out.println("You cannot take " + command.getSecondWord());
    }
     //}
     
     //public static HashMap<String, Item> itemMap = new HashMap<String, Item>();
     //itemMap.put(itemId, item);
  }

  private void solveLock(Command command, Scanner in) {
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
      }else{
        System.out.println("Incorrect passcode!");
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

    if (nextRoom == null)
      System.out.println("There is no door!");
    else {
      currentRoom = nextRoom;
      System.out.println(currentRoom.longDescription());
    }
  }
}