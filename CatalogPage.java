
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

enum Order
{
    byArtist, byAlbum, bySong, UNSORTED;
}


public class CatalogPage
{
     List<CatalogItem> items;
     Order order;
     
     
     public CatalogPage() 
     {
         items = new ArrayList<CatalogItem>();
         order = Order.UNSORTED;
     }

    // getters-----------------------------
     public CatalogItem getItem(int i)
     {
        return this.items.get(i);
     }

     public int size()
     {
        return items.size();
     }
     //returns the first letter of the first item
     public String getFirstLetter()
     {
        switch(order)
            {
            case byArtist:
                return items.get(0).getArtist().substring(0,0);
            case byAlbum:
                return items.get(0).getAlbum().substring(0,0);
            case bySong:
                return items.get(0).getTitle().substring(0,0);
            default:
                //System.out.println("Unsorted: " + items.get(i).artist.name);
                return "?";
            }
     }

    // setters-------------------------------
     public void addItem(CatalogItem item)
     {
        this.items.add(item);
     }
     
     public void clear()
     {
         items.clear();
     }
     
     // Sorting Functions----------------------------------------
     public void sortByArtist()
     {
        Collections.sort(items, CatalogItem.ArtistNameComparator);
        order = Order.byArtist;
     }
     
     public void sortBySong()
     {
        Collections.sort(items, CatalogItem.SongNameComparator);
        order = Order.bySong;
     }
     
     public void sortByAlbum()
     {
        Collections.sort(items, CatalogItem.AlbumNameComparator);
        order = Order.byAlbum;
     }
     
     //FILE IO --------------------------------------------------
     
     // Write simplified catalog
    public void writeJsonFile(String fileName)
    {       
        //String TAG = "writeJsonFile";
        
         Gson gson = new Gson();
         String jsonString = gson.toJson(this); // Convert CatalogPage to Json
         
         
         //peer.put(fileName, jsonString);
         try {
             //String fileName = "test_write_CatalogPage";
             FileOutputStream output = new FileOutputStream(fileName);
             output.write(jsonString.getBytes());
             output.close();
         }
         catch (IOException e) {
             System.out.println(e);
         }
    }
    
    // Read simplified catalog
    public void readJsonFile(String fileName)
    {
        String TAG = "readJsonFile";
        
        //read peer.get
        try {
             FileInputStream dataraw = new FileInputStream(fileName);
            
             System.out.println("\t" + TAG+": scanning."); // DEBUG
             Scanner scan = new Scanner(dataraw);
             scan.useDelimiter("\\A");
             String data = scan.next();
             
             System.out.println("\t" + TAG + ": converting json to CatalogPage.");
             //CatalogPage page = new CatalogPage();
             Gson gson = new Gson();
             CatalogPage c = new CatalogPage();
             c = gson.fromJson(data, CatalogPage.class);
             
             //Copy all items from c to self
             items.clear();
             for(int i = 0 ; i < c.size(); i++)
             {
                 items.add(c.getItem(i));
             }
             scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        setOrder(fileName);
    }

    public void setOrder(String fileName)
    {
        //Set Order 
        switch (fileName)
        {
            case "artist.json"
                this.order = byArtist;
                break;
            case "album.json"
                this.order = byAlbum;
                break;
            case "songs.json"
                this.order = bySong;
                break;
            default:
                this.order = UNSORTED;
            break;
        }
    }
    
    // loads the original music.json file that has Professor Ponce's Format
    public void loadCatalog(String fileName)
    {
        // Try to open the file for reading
        try {
            
            //JSON
            JsonReader jsonReader = new JsonReader(new FileReader(fileName));
            
            //GSON
            Gson gson = new Gson();

            jsonReader.beginArray();

            while (jsonReader.hasNext()) {
                CatalogItem item = gson.fromJson(jsonReader, CatalogItem.class);
                items.add(item);
            }
            jsonReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // PRINT --------------------------------------------------

    public void println()
    {
        this.println(items.size());
    }

    
    public void println(int count)
    {
        // Set limit of items to print
        int limit = items.size();
        if(count < limit)
        {
            limit = count;
        }
        
        // Print n items
        for(int i = 0 ; i < limit; i ++)
        {
            switch(order)
            {
            case byArtist:
                System.out.println("Artist: " + items.get(i).artist.name);
                break;
            case byAlbum:
                System.out.println("Album: " + items.get(i).release.name);
                break;
            case bySong:
                System.out.println("Tittle: " + items.get(i).song.title);
                break;
            default:
                System.out.println("Unsorted: " + items.get(i).artist.name);
                break;
            }
        }
    }
};