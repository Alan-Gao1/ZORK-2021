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

  private Parser parser;
  private Room currentRoom;
  private int peoplePickpocketed;
  public boolean finished = false;
  private boolean winCondition = false;

  /**
   * Create the game and initialise its internal map.
   */
  public Game() {
    try {
      initRooms("src\\zork\\data\\rooms.json");
      //initItems("src\\zork\\data\\items.json");
      currentRoom = roomMap.get("Lobby");
    } catch (Exception e) {
      e.printStackTrace();
    }
    parser = new Parser();
  }

  private void initItems(String string) throws Exception{
    //TBD - must intialize objects
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
    else if (commandWord.equals("use"))
      useItem(command);
    else if (commandWord.equals("solve"))
      solveLock(command, in);
    else if (commandWord.equals("open")){
      openItem(command);
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

  private void useItem(Command command) {
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
  }

  private void read(Command command) {
    //print the clues for the lock/locker/key
  }

  private void untie(Command command) {
    //untie a kid 
  }

  private void openItem(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("Open what?");
      return;
    }else if(currentRoom.getRoomName().equals("Room212")){
      if(command.getSecondWord().equals("Chest1")){
        System.out.println("You opened Chest1. There is a sword in the chest. ");
      }else if(command.getSecondWord().equals("Chest2")){
        System.out.println("You opened Chest2. There is the upper part of the costume. The costume has a tag that reads \"from BVG shop \".");
      }else if(command.getSecondWord().equals("Chest3")){
        System.out.println("You opened Chest3, and a bomb exploded.");
        //return false;
      }else if(command.getSecondWord().equals("Chest4")){
        System.out.println("You opened Chest4. There is $100!");
      }else if(command.getSecondWord().equals("Chest5")){
        System.out.println("You opened Chest5, and a bomb exploded.");
        //return false;
      }
    }
    //opens a different items (chest, locker, microwave, curtains, door, backpack)
    //check if you can open the item
  }

  // implementations of user commands:
  private void drop(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("Drop what?");
      return;
    }else
      System.out.println("You have dropped " + command.getSecondWord() + ".");
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
      System.out.println("You pickpocketed "+rand+" from a random person.");
    }else if(counter<=chance){
      finished = true;
    }
  }

  private void takeItem(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("Take what?");
      return;
    }

    String item = command.getSecondWord();
    Item newItem = new Item(10, item, true); //** this is hardcoded but retrieve the values from the json */
    Inventory backpack = new Inventory(10);
    //check to see if item exists in the json file

    /*if(item can be moved)
       System.out.println("You cannot move the " + command.getSecondWord() + "!");
    else{
       */if(backpack.addItem(newItem)){
       System.out.println("You took the " + command.getSecondWord() + ".");
       }
     //}

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