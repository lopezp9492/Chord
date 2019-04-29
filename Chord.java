/**
* Chord implements Chord P2P
*
* @author  Oscar Morales-Ponce
* @version 0.15
* @since   03-3-2019
*/

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;
import java.io.*;

/**
 * Chord extends from UnicastRemoteObject to support RMI.
 * It implements the ChordMessageInterface
 *
 */
public class Chord extends java.rmi.server.UnicastRemoteObject implements ChordMessageInterface
{
    // Numbers of fingers
    public static final int M = 2;

     // rmi registry for lookup the remote objects.
    Registry registry;

    // Successor peeer
    ChordMessageInterface successor;

    // Predecessor peeer
    ChordMessageInterface predecessor;

    // array of fingers
    ChordMessageInterface[] finger;

    // it is used to keep the fingers updated
    int nextFinger;

    // GUID
    long guid;

    // path prefix
    String prefix;
    String mapPrefix;

    // chord size
    int chordSize;

    //Extra Variables
    TreeMap <String, CatalogPage> tm;

    //-----MY METHODS------
    public void map(long guid)
    {

      //---Outline---
      //load CatalogPage

      //for each catalog item in the page

        //for each word in the "title", "album", "artist"

          //store catalog item in  


      //---Implementation---

      //load CatalogPage

      RemoteInputFileStream rawdata = null;
      CatalogPage catalogPage = new CatalogPage();
      try {
          rawdata = new RemoteInputFileStream(prefix + guidObject);
          rawdata.connect();
          Scanner scan = new Scanner(rawdata);
          scan.useDelimiter("\\A");
          String data = scan.next();
          Gson gson = new Gson();
          catalogPage = gson.fromJson(data, CatalogPage.class);


      } catch (IOException e)
      {
          throw(new RemoteException("File does not exists"));
      }


      //for each catalog item in the page
      for(int i = 0 ; i < catalogPage.size(); i++ )
      {

        //Get all words in artist, album and song title
        String line = catalog.getItem(i).artist.name;
        line = line + " " + catalog.getItem(i).release.name; // album
        line = line + " " + catalog.getItem(i).song.title;

        //System.out.println("line: (" + line +")"); // DEBUG - Print Line
        line = line.toLowerCase();

        //Separate line into words
        String[] words = line.split("\\s+");


        //TODO
        //Remove punctuation // Remove Special Symbols
        /**
        for (int j = 0; j < words.length; j++) {
          // You may want to check for a non-word character before blindly
          // performing a replacement
          // It may also be necessary to adjust the character class
          words[i] = words[i].replaceAll("[^\\w]", "");
        }
        **/

        //For each word
        for (int k = 0; k < words.length; k++) {
          //System.out.println("\t"+ "word: (" + words[k] +")"); // DEBUG - Print each word

          //Get key
          String key = words[k];
          if(key.length()>2)
          {
            key = key.substring(0,2);//get first 2 characters only
          }

          //if first 2 chars of word exists in Hash (Reverse Index)
          if (reverseIndex.containsKey(key))  
          { 
            //add song to CatalogPage
              //System.out.println("\t" + "add song to CatalogPage"); // DEBUG

              reverseIndex.get(key).addItem(catalog.getItem(i));      //Uses 2 chars as key
              //reverseIndex.get(words[k]).addItem(catalog.getItem(i)); //Uses Full word as Key

          }
          else // this is a new key, add it to the HashMap
          {
            //System.out.println("\t"+ "add word to Hash ("+ words[k] +")"); // DEBUG
            sortedKeys.add(key);//used later when saving the files to the peers.


            //add song to new CatalogPage
            CatalogPage capa = new CatalogPage();
            capa.addItem(catalog.getItem(i));

            //add first two chars of word to Hash ()
            reverseIndex.put(key, capa);

          }
            
        }

      }

      System.out.println("Indexing Done.");

      // At this point the local file has been indexed
      // and stored in the TreeMap.

      // This process will be repeated for each page file in this peer.
      // Once all local page files have been processed...

      //Send() the the nodes to the correct peer 

    }

    //Send all TreeMap nodes that dont belong in this peer.
    public void sendAll()
    {

      //-----OutLine-----
      //for each node in TreeMap
        //determine guid
        //if successor is not this peer
          //send to proper peer
        //else skip to next node

      //-----Implementation----- WIP
      //for each node in TreeMap
      for(Map.Entry<String,CatalogPage> entry : tm.entrySet()) 
      {
        //determine guid
        String k = entry.getKey();
        Long guid = md5( k + "reverseIndex" + k );
        ChordMessageInterface peer = chord.locateSuccessor(guid); // locate successor

        //compare guids (compare long values)
        int result = Long.compare(peer.getId(), this.guid);

        //if successor is this peer
        if(result == 0)
        {
          //skip to next node
        }
        else
        {
          //send to proper peer
        }

      }


    }

    //Send an item  "key, <s1, s2, s3...> " aka CatalogPage
    public void send()
    {

    }

    //place received file into TreeMap
      //if tree map already contains the key then combine received page with current page
      //else just store the new page
    public void store(long guid, RemoteInputFileStream rawdata) throws RemoteException
    {
      //-----OutLine-----
      //receive data
      //place received file into TreeMap
        //if tree map already contains the key then combine received page with current page
        //else just store the new page

      //-----Implementation-----
      //receive data
      CatalogPage catalogPage = new CatalogPage();
      try {
        rawdata.connect();
        Scanner scan = new Scanner(rawdata);
        scan.useDelimiter("\\A");
        String data = scan.next();
        Gson gson = new Gson();
        catalogPage = gson.fromJson(data, CatalogPage.class);
      } catch (IOException e)
      {
          throw(new RemoteException(":error in store(): \\_(^_^)_/"));
      }
      //place received file into TreeMap

      //if tree map already contains the key 
      if(tm.containsKey(catalogPage.getKey))
      {
        //then combine received page with current page
        for(int i = 0; i < catalogPage.size(); i ++)
        {
          tm.get(catalogPage.getKey()).addItem(catalogPage.getItem(i) );
        }
      }
      else
      {
        //else just store the new page
        tm.put(catalogPage.getKey, catalogPage);
      }
    }

    public void emit()
    {
        //for each key in the TreeMap emit "CatalogPage"

          //locate peer 

          //peer.store(CatalogPage)
    } 

    public void bulk()
    {
      //for each CatalogPage in TreeMap

        //generate guid

        //save to file
    }

    public int getChordSize()
    {
      return this.chordSize;
    }

    //-----END MY METHODS-----

    //-----NEW METHODS-----
    public void onChordSize(long source, int n) throws RemoteException
    {

      //System.out.println("source id: " + source);
      //System.out.println("this   id: " + this.guid);

      if(n==0)
      {
        successor.onChordSize(source, ++n);
      }
      else
      {
        int result = Long.compare(source, this.guid);

        if(result == 0)// if result == 0 they are equal
        {
          chordSize = n;
        }
        else
        {
          successor.onChordSize(source, ++n);
        }
      }


    }
    public void onPageCompleted(String file) throws RemoteException
    {

    }
    //public void mapContext(int page, Mapper mapper, ChordMessageInterface coordinator, String file) throws RemoteException
    //{

    //}
    
    //public void reduceContext(int page, Mapper reducer, ChordMessageInterface coordinator, String file) throws RemoteException
    //{

    //}

    public void addKeyValue(long key, int value) throws RemoteException
    {

    }
    public void emit(long key, int value, String file) throws RemoteException// Sends
    {

    }
    /**
    * Save tree to page
    **/
    public void bulk(int page) throws RemoteException
    {

    }
    //-----END NEW METHODS-----



/**
 * Constructor of the Chord.
 * <p>
 * The function is used to debug if the ring is correctly formed
 * </p>
 *
 * @param  port it is the port where it listen. If the port is being used
 * for another process, it throw RemoteException.
 * @param guid the global unique id of the peer.
 */
    public Chord(int port, long guid) throws RemoteException {

      chordSize = 1;
      tm = new TreeMap<String, CatalogPage>();


        int j;
        // Initialize the variables
        prefix = "./" + guid + "/repository/";
        mapPrefix = prefix + "/map/";
	    finger = new ChordMessageInterface[M];
        for (j=0;j<M; j++){
	       finger[j] = null;
     	}
        this.guid = guid;
	    nextFinger = 0;
        predecessor = null;
        successor = this;
        Timer timer = new Timer();
        // It sets the timer to self stabilize the chord when nodes leave or join
        timer.scheduleAtFixedRate(new TimerTask() {
	    @Override
	    public void run() {
            stabilize();
            fixFingers();
            checkSuccessor();
            checkPredecessor();
            }
        }, 1000, 1000);   // Every second
        try{
            // create the registry and bind the name and object.
            System.out.println(guid + " is starting RMI at port="+port);
            registry = LocateRegistry.createRegistry( port );
            registry.rebind("Chord", this);
        }
        catch(RemoteException e){
	       throw e;
        }
    }



/**
 * return true if the key is in the open interval (key1, key2)
 */
    public Boolean isKeyInOpenInterval(long key, long key1, long key2)
    {
      if (key1 < key2)
          return (key > key1 && key < key2);
      else
          return (key > key1 || key < key2);
    }


/**
 *return true if the key is in the semi-open interval (key1, key2]
 */
    public Boolean isKeyInSemiCloseInterval(long key, long key1, long key2)
    {
      return isKeyInOpenInterval(key, key1, key2) || key == key2;
    }

/**
 *  put a file in the repository
  * @param guidObject  GUID of the object to store
  * @param stream  File to store
 */
    public void put(long guidObject, RemoteInputFileStream stream) throws RemoteException {
        stream.connect();
        try {
          String fileName = prefix + guidObject;
          FileOutputStream output = new FileOutputStream(fileName);
          while (stream.available() > 0)
              output.write(stream.read());
          output.close();
      }
      catch (IOException e) {
          System.out.println(e);
      }
    }

    /**
 *  put a text in guidObject
 * @param guidObject  GUID of the object to store
 * @param text text to store
 */
    public void put(long guidObject, String text) throws RemoteException {
        try {
          String fileName = prefix + guidObject;
          FileOutputStream output = new FileOutputStream(fileName);
          output.write(text.getBytes());
          output.close();
      }
      catch (IOException e) {
          System.out.println(e);
      }
    }
/**
 * return guidObject
  * @param guidObject  GUID of the object to return
 */
    public RemoteInputFileStream get(long guidObject) throws RemoteException {
        RemoteInputFileStream file = null;
        try {
             file = new RemoteInputFileStream(prefix + guidObject);
        } catch (IOException e)
        {
            throw(new RemoteException("File does not exists"));
        }
        return file;
    }


/**
 * return len bytes of guidObject from offset
  * @param guidObject  GUID of the object to return
 */
    public byte[] get(long guidObject, long offset, int len) throws RemoteException {
        byte[] buf = new byte[len];

        try {
            File file = new File(prefix + guidObject);
            FileInputStream inputStream = new FileInputStream(file);
            inputStream.skip(offset);
            inputStream.read(buf);
            inputStream.close();
        } catch (IOException e)
        {
            throw(new RemoteException("File does not exists"));
        }
        return buf;
    }


/**
 * deletes a file with guidObject from the repository
 * @param guidObject  GUID of the object to delete
 */
    public void delete(long guidObject) throws RemoteException {
        File file = new File(prefix + guidObject);
        file.delete();
    }

/**
 * returns the id of the peer
 */
    public long getId() throws RemoteException {
        return guid;
    }

/**
 * It is used to detect that the peer is still alive
 * <p>
 * return true
 */
    public boolean isAlive() throws RemoteException {
	    return true;
    }

/**
 * return the predecessor
 * <p>
 * return the Chord Interface of the predecessor
 */
    public ChordMessageInterface getPredecessor() throws RemoteException {
	    return predecessor;
    }

/**
 * locates the successor of key
 * <p>
 * @param key
 * return the Chord Interface of the successor of key
 */
    public ChordMessageInterface locateSuccessor(long key) throws RemoteException {
	    if (key == guid)
            throw new IllegalArgumentException("Key must be distinct that  " + guid);
	    if (successor.getId() != guid)
	    {
	      if (isKeyInSemiCloseInterval(key, guid, successor.getId()))
	        return successor;
	      ChordMessageInterface j = closestPrecedingNode(key);

          if (j == null)
	        return null;
	      return j.locateSuccessor(key);
        }
        return successor;
    }

/**
 * Returns the closest preceding node for the key
 * <p>
 * @param key
 * return the Chord Interface of the closet preceding node
 */
    public ChordMessageInterface closestPrecedingNode(long key) throws RemoteException {
        if(key != guid) {
            int i = M - 1;
            while (i >= 0) {
                try{

                    // It verifies from the largest interval
                    if(finger[i] != null && isKeyInSemiCloseInterval(finger[i].getId(), guid, key)) {
                        if(finger[i].getId() != key)
                            return finger[i];
                        else {
                            return successor;
                        }
                    }
                }
                catch(Exception e)
                {
                    // Skip ;
                }
                i--;
            }
        }
        return successor;
    }

/**
 * It joins the ring in the peer (ip,port). The peer must exist
 * <p>
 * @param ip of the peer
 * @param port of the peer
 */
    public void joinRing(String ip, int port)  throws RemoteException {
        try{
            System.out.println("Get Registry to joining ring");
            Registry registry = LocateRegistry.getRegistry(ip, port);
            ChordMessageInterface chord = (ChordMessageInterface)(registry.lookup("Chord"));
            predecessor = null;
            successor = chord.locateSuccessor(this.getId());
            System.out.println("Joining ring");
        }
        catch(RemoteException | NotBoundException e){
            successor = this;
        }
    }

    /**
 * It joins the ring in the peer ChordMessageInterface. The peer must exist
 * <p>
 * @param s is the successor
 */
    public void joinRing(ChordMessageInterface s)  throws RemoteException {
            predecessor = null;
            successor = s;
    }

/**
 *  If the successor fails, it tries to handle the failure using the
 * first finger available
 * <p>
 */
    public void findingNextSuccessor()
    {
        int i;
        successor = this;
        for (i = 0;  i< M; i++)
        {
            try
            {
                if (finger[i].isAlive())
                {
                    successor = finger[i];
                }
            }
            catch(RemoteException | NullPointerException e)
            {
                finger[i] = null;
            }
        }
    }

/**
 * Stabilizes the chord
 * <p>
 * It verifies if the peer is in the right interval. If it is not
 * in the interval, it corrects the interval
 * This method executed by the timer.
 */
    public void stabilize() {
      try {
          if (successor != null)
          {
              ChordMessageInterface x = successor.getPredecessor();

              // It verifies if the predecessor is in the correct interval
              // x.getId() != this.getId() is used for for the trivial case
              // where only one peer exists
              if (x != null && x.getId() != this.getId() && isKeyInOpenInterval(x.getId(), this.getId(), successor.getId()))
              {
                  successor = x;
              }
              // The if statament is to handle the trivial case where only
              // one peer exists
              if (successor.getId() != getId())
              {
                  // We notified the successor that there It verifies if the predecessor is in the correct interval
                  successor.notify(this);
              }
          }
      } catch(RemoteException | NullPointerException e1) {
          findingNextSuccessor();

      }
    }

/**
 * A node notifies that it has a new predecessor j. It also moves all the
 * files that the predecessor must handle
 * </p>
 * @param j the new predecessor
 */
    public void notify(ChordMessageInterface j) throws RemoteException {
         if (predecessor == null || (predecessor != null
                    && isKeyInOpenInterval(j.getId(), predecessor.getId(), guid)))
             predecessor = j;
            try {
                File folder = new File(prefix);
                // It reads all the files in repository
                File[] files = folder.listFiles();

                for (File file : files) {
                    try{
                        long guidObject = Long.valueOf(file.getName());
                        // If the guidObject is less than the new predecessor
                        if(!isKeyInSemiCloseInterval(guidObject, j.getId(), getId())) {
                            predecessor.put(guidObject, new RemoteInputFileStream(file.getPath(), true));

                        }
                    }
                    catch (NumberFormatException e)
                    {
                        //skip;
                    }
                }
                } catch (ArrayIndexOutOfBoundsException e) {
                //happens sometimes when a new file is added during the loop
            } catch (IOException e) {
            e.printStackTrace();
        }

    }

/**
 * Fixes the fingers
 * <p>
 * Every time that is executed is fixing the Finger nextFinger.
 * This method executed by the timer.
 */
    public void fixFingers() {

        long id= guid;

        try {
            if (successor != null && successor.getId() != getId())
            {
                // The finger is at distance 2^(nextFinger) of this.getId()
                // We use a shift to the left to perform the operation
                long nextId = this.getId() + (1<< (nextFinger+1));
                finger[nextFinger] = locateSuccessor(nextId);

                // The same process cannot be a finger
                if (finger[nextFinger].getId() == guid)
                    finger[nextFinger] = null;
                else
                    nextFinger = (nextFinger + 1) % M;
            }
            else{
                if (successor != null)
                {
                    finger[nextFinger] = null;
                    nextFinger = (nextFinger + 1) % M;
                }
            }
        }
        catch(RemoteException | NullPointerException e){
             //System.out.println(e.message());
             e.printStackTrace();
        }
    }

 /**
 * It checks if the predecessor is still alive.
 * <p>
 * It checks if the predecessor is still alive. If the predecessor
 * is not present it sets its predecessor to null. This method executed
 * by the timer.
 */
    public void checkPredecessor() {
      try {
          if (predecessor != null && !predecessor.isAlive())
              predecessor = null;
      }
      catch(RemoteException e)
      {
          predecessor = null;
      }
    }

       /**
 * It checks if the successor is still alive.
 * <p>
 * It checks if the successor is still alive. If the succesor
 * is not present it joins its successor. This method executed
 * by the timer.
 */
    public void checkSuccessor() {
      try {
          successor.isAlive();
      }
      catch(Exception e)
      {
            successor = null;
      }


         if (successor == null)
         {
                try{
                      for (int i=1; i<M; i++)
                      {
                        joinRing(finger[i]);
                        break;
                      }
                }catch(Exception e)
                {

                }
        }
    }

/**
 * Leaves the ring. It move all files to the successor
 */
    public void leave()  {
        if (predecessor == null) return;
        try
        {
            ChordMessageInterface suc = successor;
            if (suc != null && suc.getId() != getId())
            {
                successor = null;
                predecessor.joinRing(suc);
                predecessor = null;

                try {
                    File folder = new File(prefix);
                    // It reads all the files in repository
                    File[] files = folder.listFiles();

                    for (File file : files) {
                        try{
                            long guidObject = Long.valueOf(file.getName());
                            suc.put(guidObject, new RemoteInputFileStream(file.getPath()));
                            //file.delete();
                        }
                        catch (Exception   e)
                        {
                            //skip;
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    //happens sometimes when a new file is added during the loop
                }
            }
        }
        catch (RemoteException e)
        {

        }
    }

/**
 * Prints the successor, predecessor and fingers if they are not null.
 * <p>
 * The function is used to debug if the ring is correctly formed
 * </p>
 */
    void print()
    {
        int i;
        try {
            if (successor != null)
                System.out.println("successor "+ successor.getId());
            if (predecessor != null)
                System.out.println("predecessor "+ predecessor.getId());
            for (i=0; i<M; i++)
            {
                try {
                    if (finger[i] != null)
                        System.out.println("Finger "+ i + " " + finger[i].getId());
                } catch(NullPointerException e)
                {
                    System.out.println("Cannot retrive id of the finger " + i);
                }
            }
        }
        catch(RemoteException e){
	       System.out.println("Cannot retrive id of successor or predecessor");
        }
    }
}
