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
        Long size;
        public PagesJson()
        {
            guid = (long) 0;
            size = (long) 0;
        }

        // getters
        public Long getGUID()
        {
            return guid;
        }

        public Long getSize()
        {
            return size;
        }

        // setters
        public void setGUID(Long guid)
        {
            this.guid = guid;
        }
        public void setSize(Long size)
        {
            this.size = size;
        }

    };

    public class FileJson 
    {
        String name;
        Long   size;
        ArrayList<PagesJson> pages;
        public FileJson()
        {
            this.name = "not set";
            this.size = (long) 0;
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
    };
    
    //DFS Variables

    ArrayList<CatalogItem> catalogItems;
    int port;
    Chord  chord;
    
    //END DFS Variables
    
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

    //END METADATA CLASSES---------------------------------------
    
    
    
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
        System.out.println(TAG+"()");

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

            System.out.println(strMetaData); // DEBUG
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
        System.out.println("\tSaving Metadata to guid: " + guid);//DEBUG
        
        Gson gson = new Gson();
        peer.put(guid, gson.toJson(filesJson));
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
        System.out.println( TAG + "()");


        String listOfFiles = "";
        FilesJson files = readMetaData();
        for(int i = 0 ; i < files.size(); i++)
        {
            listOfFiles += files.getFile(i).name + "\n";
        }
        
 
        return listOfFiles;
    }

/**
 * create an empty file 
  *
 * @param filename Name of the file
 */
    public void create(String fileName) throws Exception
    {

        // TODO:
        // Write Metadata
        // Write Pages

        //DEBUG
        String TAG = "DFS.create";
        System.out.println(TAG + "(" + fileName + ")");
        


        // LoadCatalog
        loadCatalog(fileName);
        System.out.println(TAG + ":catalogItems.size() = " + catalogItems.size()); //DEBUG 

        // Variables
        int songs_per_page = 1000;
  
        PagesJson page = new PagesJson();     // metadata
        FileJson file = new FileJson();       // metadata
        FilesJson metadata = new FilesJson(); // metadata
        Long page_size = (long) 0;
        Long file_size = (long) 0;


        // Split file into n pages
        // for each item in catalog save it to a "page"
        for(int i = 0 ; i < catalogItems.size(); i++ )
        {
            //Add catalog item to JsonObject


            // Page groups of size "songs_per_page"
            page_size =  page_size + 1;
            //page_size = content.getContentLength();
            
            if( (i+1)%songs_per_page == 0)
            {
                page = new PagesJson(); 

                // DEBUG
                //System.out.println("i + 1 = " + (i+1) );
                System.out.println("\tpage_size = " + page_size);


                // Hash each page (name + time stamp) to get its GUID
                Long timeStamp = System.currentTimeMillis();
                Long guid = md5(fileName + timeStamp);
                System.out.println("\tguid = "  + guid ); // DEBUG


                page.setGUID(guid);         //add page_guid to metadata
                page.setSize(page_size);    //add page size to metadata
                file.addPage(page);         //add page to file in metadata


                file_size += page_size;
                page_size = (long) 0;

                //4) Save page at its corresponding node
                //ChordMessageInterface peer = chord.locateSuccessor(guid); // locate successor
                //Gson gson = new Gson();
                //peer.put(guid, gson.toJson(filesJson));// send page
            }


            //Save Last Page if its smaller than "songs_per_page"
            else if(i == catalogItems.size()-1 )
            {
                page = new PagesJson(); 

                // DEBUG
                System.out.println("\tLast Page: smaller than " + songs_per_page); // DEBUG
                //System.out.println("i + 1 = " + (i+1) );
                System.out.println("\tpage_size = " + page_size); // DEBUG


                //2)Hash each page (name + time stamp) to get its GUID
                Long timeStamp = System.currentTimeMillis();
                Long guid = md5(fileName + timeStamp);
                System.out.println("\tguid = "  + guid ); // DEBUG


                page.setGUID(guid);         //add page_guid to metadata 
                page.setSize(page_size);    //add page size to metadata
                file.addPage(page);         //add page to file


                file_size += page_size;
                page_size = (long) 0;


                //4) Save page at its corresponding node
                //ChordMessageInterface peer = chord.locateSuccessor(guid); // locate successor
                //Gson gson = new Gson();
                //peer.put(guid, gson.toJson(filesJson));// send page
            }
        }

        //3 save metadata.json to Chord
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
     
        
    }
    
/**
 * Read block pageNumber of fileName 
  *
 * @param filename Name of the file
 * @param pageNumber number of block. 
 */
    public RemoteInputFileStream read(String fileName, int pageNumber) throws Exception
    {
        return null;
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
