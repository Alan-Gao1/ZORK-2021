package zork;

public class CommandWords {
  // a constant array that holds all valid command words
  private static final String validCommands[] = { "go", "quit", "info", "help", "solve", "take", "drop", "fight","open", "untie", "listen", "play", "pickpocket", "use", "buy", "exits"};

  /**
   * Constructor - initialise the command words.
   */
  public CommandWords() {
    // nothing to do at the moment...
  }

  /**
   * Check whether a given String is a valid command word. Return true if it is,
   * false if it isn't.
   **/
  public boolean isCommand(String aString) {
    for (String c : validCommands) {
      if (c.equals(aString))
        return true;
    }
    // if we get here, the string was not found in the commands
    return false;
  }

  /*
   * Print all valid commands to System.out.
   */
  public void showAll() {
    System.out.println("This is all of the availible command words you can use.");
    for (String c : validCommands) {
      System.out.print(c + "  ");
    }
    System.out.println();
    System.out.println("Type \"listen 'kidname'\" to listen to the hints and clues the kids have.");
    System.out.println("Use \"pickpocket\" to try and get money. If you get caught, the game ends. The more times you succesfully pickpocket, the more likely it is you will be caught");
  }
}
