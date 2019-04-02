import java.rmi.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.math.BigInteger;
import java.security.*;
import com.google.gson.Gson;
import java.io.InputStream;
import java.util.*;

//import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
//import CatalogItem.*;



/* Metadata JSON Format
{"file":
  [
     {"name":"MyFile",
      "size":128000000,
      "pages":
      [
         {
            "guid":11,
            "size":64000000
         },
         {
            "guid":13,
            "size":64000000
         }
      ]
      }
   ]
} 
*/


public class DFS
{
    
    // METADATA CLASSES---------------------------

    public class PagesJson
    {
        Long guid;
        int size;
        public PagesJson()
        {
            guid = (long) 0;
            size = 0;
        }

        // getters
        public Long getGUID()
        {
            return guid;
        }

        public int getSize()
        {
            return size;
        }

        // setters
        public void setGUID(Long guid)
        {
            this.guid = guid;
        }
        public void setSize(int size)
        {
            this.size = size;
        }

    };

    public class FileJson 
    {
        String name;
        Long   size;
        int   numberOfItems;
        int   itemsPerPage;
        ArrayList<PagesJson> pages;
        public FileJson()
        {
            this.name = "not set";
            this.size = (long) 0;
            this.numberOfItems = 0;
            this.itemsPerPage =  0;
            this.pages = new ArrayList<PagesJson>();
        }
        // getters
        public String getName()
        {
            return this.name;
        }
        public Long getSize()
        {
            return this.size;
        }
        public int getNumberOfItems()
        {
            return this.numberOfItems;
        }
        public int getItemsPerPage()
        {
            return this.itemsPerPage;
        }
         public int getNumberOfPages()
        {
            return this.pages.size();
        }
        public ArrayList<PagesJson> getPages()
        {
            return this.pages;
        } 
        public PagesJson getPage(int i)
        {
            return pages.get(i);
        }


        // setters
        public void setName(String name)
        {
            this.name = name;
        }
        public void setSize(Long size)
        {
            this.size = size;
        }
        public void setPages(ArrayList<PagesJson> pages)
        {
            //TODO Is this a shallow copy?
            this.pages = pages;
        } 
        public void addPage(PagesJson page)
        {
            this.pages.add(page);
            this.size += page.getSize();
        }
		public void addPage(Long guid, int page_size)
        {
            PagesJson page = new PagesJson();		//metadata
            page.setGUID(guid);         			//metadata
            page.setSize(page_size);    			//metadata

            this.addPage(page);
        }
    };
    
    public class FilesJson 
    {
         List<FileJson> files;
         public FilesJson() 
         {
             files = new ArrayList<FileJson>();
         }

        // getters
         public FileJson getFile(int i)
         {
            return this.files.get(i);
         }

        // setters
         public void addFile(FileJson file)
         {
            this.files.add(file);
         }

         public int size()
         {
            return files.size();
         }

         public void deleteFile(String fileName)
         {
         	int index_to_remove = 0;
         	for(int i = 0 ; i < files.size(); i++)
         	{
         		if(files.get(i).getName().equals(fileName))
         		{
					index_to_remove = i;
         		}
         	}

         	files.remove(index_to_remove);
         }
    };
    // END METADATA CLASSES---------------------------


    //HASH FUNCTION
    private long md5(String objectName)
    {
        try
        {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(objectName.getBytes());
            BigInteger bigInt = new BigInteger(1,m.digest());
            return Math.abs(bigInt.longValue());
        }
        catch(NoSuchAlgorithmException e)
        {
                e.printStackTrace();
                
        }
        return 0;
    }

    //END HELPER CLASSES---------------------------------------
    
    //DFS Variables

    ArrayList<CatalogItem> catalogItems;
    int port;
    Chord  chord;
    
    //END DFS Variables
    
    public DFS(int port) throws Exception
    {
        catalogItems = new ArrayList<CatalogItem>();

        
        this.port = port;
        long guid = md5("" + port);
        chord = new Chord(port, guid);
        Files.createDirectories(Paths.get(guid+"/repository"));
        Files.createDirectories(Paths.get(guid+"/tmp"));
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                chord.leave();
            }
        });
        
    }
    
  
/**
 * Join the chord
  *
 */
    public void join(String Ip, int port) throws Exception
    {
        chord.joinRing(Ip, port);
        chord.print();
    }
    
    
   /**
 * leave the chord
  *
 */ 
    public void leave() throws Exception
    {        
       chord.leave();
    }
  
   /**
 * print the status of the peer in the chord
  *
 */
    public void print() throws Exception
    {
        chord.print();
    }
    
/**
 * readMetaData read the metadata from the chord
  *
 */
    public FilesJson readMetaData() throws Exception
    {
        //DEBUG
        //
        String TAG = "readMetaData";
        //System.out.println(TAG+"()");

        FilesJson filesJson = null;
        try {
            Gson gson = new Gson();
            long guid = md5("Metadata");
            ChordMessageInterface peer = chord.locateSuccessor(guid);
            RemoteInputFileStream metadataraw = peer.get(guid);
            metadataraw.connect();
            Scanner scan = new Scanner(metadataraw);
            scan.useDelimiter("\\A");
            String strMetaData = scan.next();

            //System.out.println(strMetaData); // DEBUG
            filesJson= gson.fromJson(strMetaData, FilesJson.class);
        } catch (NoSuchElementException ex)
        {
            filesJson = new FilesJson();
        }
        return filesJson;
    }
    
/**
 * writeMetaData write the metadata back to the chord
  *
 */
    public void writeMetaData(FilesJson filesJson) throws Exception
    {
        long guid = md5("Metadata");
        ChordMessageInterface peer = chord.locateSuccessor(guid);
        System.out.println("\tSaving Metadata to peer: " + peer.getId()); // DEBUG
        
        Gson gson = new Gson();
        peer.put(guid, gson.toJson(filesJson));
    }

    /**
 * writePageData write the page data to the chord
  *
 */
    public void writePageData(CatalogPage catalogpage, Long guid) throws Exception
    {
        ChordMessageInterface peer = chord.locateSuccessor(guid);
        System.out.println("\tSaving Page to peer: " + peer.getId()); // DEBUG
        
        Gson gson = new Gson();
        String jsonString = gson.toJson(catalogpage); // Convert CatalogPage to Json
        peer.put(guid, jsonString); // send page
    }
   
/**
 * Change Name
  *
 */
    public void move(String oldName, String newName) throws Exception
    {
        // TODO:  Change the name in Metadata
        // Write Metadata
    }

  
/**
 * List the files in the system
 *
 * @param filename Name of the file
 */
    public String lists() throws Exception
    {
        //DEBUG 
        String TAG = "lists";
        //System.out.println( TAG + "()");


        String listOfFiles = "";
        FilesJson files = readMetaData();
        for(int i = 0 ; i < files.size(); i++)
        {
            listOfFiles += files.getFile(i).name + "\n";
        }

        //System.out.println(TAG + ":files.size() == " + files.size());//DEBUG
        if(files.size() == 0 ){return "Empty";}
        
 
        return listOfFiles;
    }

/**
 * create an empty file 
  *
 * @param filename Name of the file
 */
    public void create(String fileName) throws Exception
    {

    	//TODO:
    	//Accept .mp3 files

        // DONE:
        // Accept music.json
        // Write Metadata
        // Write Pages

        //DEBUG
        String TAG = "DFS.create";
        System.out.println(TAG + "(" + fileName + ")");
        


        // LoadCatalog
        loadCatalog(fileName);
        System.out.println(TAG + ":catalogItems.size() = " + catalogItems.size()); //DEBUG 

        // Variables
        int songs_per_page = 50;
        CatalogPage catalogpage = new CatalogPage(); // Data
        //ArrayList<CatalogItem> pageItems = new ArrayList<CatalogItem>();// Data // OLD
        FileJson file = new FileJson();      	// metadata
        FilesJson metadata = new FilesJson();	// metadata
        int page_size = 0;
        Long file_size = (long) 0;


        // Split file into n pages
        // for each item in catalog save it to a "page"
        for(int i = 0 ; i < catalogItems.size(); i++ )
        {


            // Page groups of size "songs_per_page"
            page_size =  page_size + 1;

            //get item from catalog and add it to the page
            catalogpage.addItem(catalogItems.get(i));
            
            //if page size reaches "songs_per_page" save the page
            if( (i+1)%songs_per_page == 0)
            {

                // DEBUG
                //System.out.println("i + 1 = " + (i+1) );
                System.out.println("\tpage_size = " + page_size);


                // Hash each page (name + time stamp) to get its GUID
                Long timeStamp = System.currentTimeMillis();
                Long guid = md5(fileName + timeStamp);
                System.out.println("\tguid = "  + guid ); // DEBUG

                // Update MetaData
                file.addPage(guid, page_size); // metadata

                // Save page at its corresponding node
                writePageData(catalogpage, guid);

                //reset page
                catalogpage = new CatalogPage();
                page_size = 0; // metadata

            }

            //Save Last Page if its smaller than "songs_per_page"
            else if(i == catalogItems.size()-1 )
            {

                // DEBUG
                System.out.println("\tLast Page: smaller than " + songs_per_page); // DEBUG
                //System.out.println("i + 1 = " + (i+1) );
                System.out.println("\tpage_size = " + page_size); // DEBUG


                // Hash each page (name + time stamp) to get its GUID
                Long timeStamp = System.currentTimeMillis();
                Long guid = md5(fileName + timeStamp);
                System.out.println("\tguid = "  + guid ); // DEBUG

                //Update MetaData
                file.addPage(guid, page_size); // metadata


                // Save page at its corresponding node
                writePageData(catalogpage, guid);

                //reset page
                catalogpage = new CatalogPage();
                page_size = 0;
            }
        }

        // Save metadata.json to Chord
        file.setName(fileName);
        file.setSize(file_size);
        metadata.addFile(file);
        writeMetaData(metadata);
    }

    public void loadCatalog(String fileName)
    {

        //DEBUG
        String TAG = "loadCatalog";
        System.out.println(TAG + "(" + fileName + ")" );

        //String full_path = "./assets/music.json"

        // Try to open the file for reading
        try {
            
            //JSON
            JsonReader jsonReader = new JsonReader(new FileReader(fileName));
            
            //GSON
            Gson gson = new Gson();

            jsonReader.beginArray();

            while (jsonReader.hasNext()) {
                CatalogItem item = gson.fromJson(jsonReader, CatalogItem.class);
                catalogItems.add(item);
            }
            jsonReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
/**
 * delete file 
  *
 * @param filename Name of the file
 */
    public void delete(String fileName) throws Exception
    {
     	//TODO: 
     	//Read metadata
     	//find filename in metadata
     	//for each page of file 
     		//delete page

    	String TAG = "delete";

    	FilesJson metadata = readMetaData();
    	FileJson  file = new FileJson();

    	//find file
    	for(int i = 0 ; i < metadata.size(); i++)
    	{
    		if(metadata.getFile(i).getName().equals(fileName))
    		{
    			file = metadata.getFile(i);
    			System.out.println(TAG+": file size: " + file.getSize()); // DEBUG
    			System.out.println(TAG+": numberOfPages: " + file.getNumberOfPages()); // DEBUG


    			// delete all pages of file
    			for(int j = 0 ; j < file.getNumberOfPages()-1; j++)
    			{
    			
    				Long guid  = file.getPage(j).getGUID();
    				System.out.println("\tdeleting page: " + j); 	// DEBUG
    				System.out.println("\tguid: " + guid ); 		// DEBUG
    				ChordMessageInterface peer = chord.locateSuccessor(guid); // locate successor
    				peer.delete(guid);
    			}

    			//Update metadata
    			//TODO
    			metadata.deleteFile(fileName);
    			System.out.println("delete done." ); // DEBUG
    			writeMetaData(metadata);
    			return;
    		}
    	}
    	System.out.println("file not found: " + fileName); // DEBUG
    }
    
/**
 * Read block pageNumber of fileName //read catalogpage
  *
 * @param filename Name of the file
 * @param pageNumber number of block. 
 */
    public RemoteInputFileStream read(String fileName, int pageNumber) throws Exception
    {
    	//TODO: 
    	//TEST different pageNumbers


    	//DONE:
     	//Read metadata
     	//find filename in metadata
     	//find guid of pageNumber in metadata
     	//request page 

     	//Debug 
     	String TAG = "read";
     	System.out.println(TAG + "(fileName, pageNumber)");
     	System.out.println(TAG + "(" + fileName + ", " + pageNumber);

    	//Read Metadata 
    	FilesJson metadata = readMetaData();
    	long guid = (long) 0;

    	//Find File in metadata
    	//for(FileJson filejson : metadata)
    	for(int i = 0; i < metadata.size(); i++)
		{
			FileJson filejson = metadata.getFile(i);
			System.out.println("filejson.getName: " + filejson.getName()+")"); // DEBUG

			//if x.getName == filename
			if(filejson.getName().equals(fileName) )
			{
				System.out.println("name matched"); // DEBUG
				//get guid of page with "pageNumber"
				guid = filejson.getPage(pageNumber).getGUID();
				System.out.println("guid retrieved"); // DEBUG
				break;
			}
		}

        ChordMessageInterface peer = chord.locateSuccessor(guid);
		return peer.get(guid);
    }
    
 /**
 * Add a page to the file                
  *
 * @param filename Name of the file
 * @param data RemoteInputStream. 
 */
    public void append(String filename, RemoteInputFileStream data) throws Exception
    {
        
    }


    
}
