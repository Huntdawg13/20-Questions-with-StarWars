import java.util.*;
import java.io.*;


/**
 * Class used to manage and call all the actions that 
 * can be done using a tree for 20 questions. 
 *  
 * @author Hunter J. McClure
 * @version (June 6 2019)
 */
public class QuestionTree
{

  private QuestionNode root;
  private UserInterface my;

  private int GamesPlayed;
  private int GamesWon;
  
  
  /**
    * Constructor that creates a object which holds 
    * information of the given user interface. Also 
    * creates a starting root node when starting fresh.  
    * 
    * @param ui The object holding/representing the user interface
    * @return nothing
    * @throw IllegalArgumentException if the user interface object is null
    */
  public QuestionTree (UserInterface ui)
  {
    if(ui == null) throw new IllegalArgumentException();
    
    my = ui;
    
    root = new QuestionNode("computer");
  }
  
  /**
    * Starts the game of 20 questions by using recursion 
    * and sends it into another method where the main program 
    * for running the actual playing game is made. Also increases
    * the number of games played in that session.
    * 
    * @param nothing
    * @return nothing
    */
  public void play()
  {
      //my.println("Play Game here");
   
      root = CurrentNode(root);
      ++GamesPlayed;
  }
  
  /**
    * Starts the recursive process of saving a game. It takes any 
    * added objects and adds them into the chosen text file.  
    * 
    * @param outout The information being sent through the method which is added into a text file
    * @return nothing
    * @throw IllegalArgumentException if the parameter is null
    */
  public void save(PrintStream output)
  {
    //my.println("Save the current tree here");
     if(output == null) throw new IllegalArgumentException();
      save(output, root);
  }
  
  /**
    * Starts the recursive process of loading a game. It takes a
    * text file and creates nodes out of the info and adds them into the tree.
    * @param input The information being sent from the text file and sent to 
    *    the method which is added into a tree in the program
    * @return nothing
    * @throw IllegalArgumentException if the parameter is null
    */
  public void load(Scanner input)
  {
    //my.println("Save the current file here");
      if(input == null) throw new IllegalArgumentException();
      root = loading(input);
  }
  
  /**
    * Gets the total number of games played that session  
    * 
    * @param nothing
    * @return number of games that session
    */
  public int totalGames()
  {
    return GamesPlayed;
  }
  
  /**
    * Gets the total number of wins that the computer has gained in that session  
    * 
    * @param nothing
    * @return number of wins from the computer that session
    */
  public int gamesWon()
  {
    return GamesWon;
  }
  
  
  //private helper methods below
  
  //Takes current node and determines if it is a question or answer.
  //If computer loses, then sends to the method that creates new object
  private QuestionNode CurrentNode(QuestionNode Current)
  {
      if(Current == null) throw new IllegalArgumentException();
      
      if (Current.Yes_Left == null && Current.No_Right == null) 
      {
         my.print("Would your object happen to be " + Current.data + "?");

         if (my.nextBoolean()) 
         {
            my.println("I win!");
            
            ++GamesWon;
          }
          
          else 
          {
              Current = NewSection(Current);
          }
      } 
        
      else
      {
           my.print(Current.data.toString());
           if (my.nextBoolean()) 
           {
               Current.Yes_Left = CurrentNode(Current.Yes_Left);
           }
         
           else
           {
               Current.No_Right = CurrentNode(Current.No_Right);
           }
        }
        
        return Current;
    }
    
    // Creates a new object, the question to compare the object to another, 
    // and which side of the branch of the tree that it belongs on
    private QuestionNode NewSection(QuestionNode guessed)
    {
        if(guessed == null) throw new IllegalArgumentException();
        
        my.print("I lose. What is your object? ");
        
        QuestionNode theObject = new QuestionNode (my.nextLine());

        my.print("Type a yes/no question to distinguish " + "your item from " 
                  + guessed.data + ":");
        
        String question = my.nextLine();

        my.print("And what is the answer for your object?");
        
        if(my.nextBoolean())
        {
            return new QuestionNode(question, theObject, guessed);
        }
        
        else return new QuestionNode(question, guessed, theObject);
    }
    
    //The recursive method which saves the new info of the game into a chosen file
    private void save(PrintStream output, QuestionNode current)
    {
        if(current == null) throw new IllegalArgumentException();
        
        if (current.Yes_Left == null && current.No_Right == null)
        {
            output.print("A:" + current.data);
        } 
        
        else 
        {
            output.println("Q:" + current.data);

            save(output, current.Yes_Left);

            output.println();
            
            save(output, current.No_Right);
        }
    }
     
     //The recursive method which loads a text file into the game 
     //which holds the info for the given game
     private QuestionNode loading(Scanner input)
     {
        if(input == null) throw new IllegalArgumentException();
        
        QuestionNode placeHolder = null;

        if (input.hasNext())
        {
            String[] information = input.nextLine().split(":",2);
            
            if (information[0].equals("A"))
            {
                placeHolder = new QuestionNode(information[1]);
            } 
            
            else
            {
                placeHolder = new QuestionNode(information[1], loading(input), loading(input));
            }
        }
        
        if(placeHolder == null) 
        {
            placeHolder = new QuestionNode("computer");
        }
      
        return placeHolder;
    }  
  
}

/**
 * Class used to create a new node for an object or a question in the game
 *  
 * @author Hunter J. McClure
 * @version (June 4 2019)
 */
class QuestionNode
{
   public String data;
   public QuestionNode Yes_Left;
   public QuestionNode No_Right;
   
   /**
    * Constructor that creates a object/leaf in the question tree.  
    * 
    * @param info The information/object name that is going to be stored in the node
    * @return nothing
    */
   public QuestionNode(String info)
   {
      this(info, null, null);
   }
   
   /**
    * Constructor that creates a question/branch in the question tree.  
    * 
    * @param info The information/question that is going to be stored in the node
    * @param Left The node under the question which goes left if the question's answer is yes
    * @param Right The node under the question which goes right if the question's answer is no  
    * @return nothing
    */
   public QuestionNode(String info, QuestionNode Left, QuestionNode Right)
   {
      data = info;
      Yes_Left = Left;
      No_Right = Right;
   }
}