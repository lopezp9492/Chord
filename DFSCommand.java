
import java.io.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class DFSCommand
{
    DFS dfs;
    Boolean initialized = false;
    Boolean running = true;
    String TAG = "DFSCommand"; // DEBUG
        
    public DFSCommand(int p, int portToJoin) throws Exception {
        dfs = new DFS(p);
        
        if (portToJoin > 0)
        {
            System.out.println("Joining "+ portToJoin);
            dfs.join("127.0.0.1", portToJoin);            
        }

        printMenu();
        
        BufferedReader buffer=new BufferedReader(new InputStreamReader(System.in));
        String line = buffer.readLine();  
        //while (!line.equals("quit"))
        while(running)
        {
            String[] result = line.split("\\s");
            if (result[0].equals("join")  && result.length > 1)
            {
                dfs.join("127.0.0.1", Integer.parseInt(result[1]));     
            }

            if (result[0].equals("print"))
            {
                dfs.print();     
            }

            if (result[0].equals("leave"))
            {
                dfs.leave();     
            }

            //pedro
            if (result[0].equals("create"))
            {
                dfs.create("music.json");
            }

            //
            if (result[0].equals("ci"))//createIndex
            {
                dfs.createIndex();
                dfs.reverseIndexStats();
                dfs.saveReverseIndexToPeers();
            }

            if (result[0].equals("list"))
            {
                System.out.println("List of files: ");
                System.out.println(dfs.lists());
            }

            if (result[0].equals("delete"))
            {

                String file_to_delete = "?";
                if(result.length > 1)
                {
                    file_to_delete = result[1];
                    dfs.delete(file_to_delete);
                } 
                else
                {
                    dfs.delete("music.json");
                }
            }

            if(result[0].equals("read"))
            {

                //default page number = 1
                int pageNumber = 1;

                //If page specified update pageNumber
                if(result.length > 1)
                {
                    pageNumber = Integer.parseInt(result[1]);

                } 
                System.out.println("reading page #" + pageNumber);
                Long startTime = System.currentTimeMillis();

                //Remote Input File Stream
                RemoteInputFileStream dataraw = dfs.read("music.json", pageNumber);
                System.out.println("\t"+ TAG+":connecting."); // DEBUG
                dataraw.connect(); //NEW RFIS

                //Scanner
                CatalogPage page = new CatalogPage();
                //try{
                System.out.println("\t" + TAG+":scanning."); // DEBUG
                Scanner scan = new Scanner(dataraw);
                scan.useDelimiter("\\A");
                String data = scan.next();
                //System.out.println(data); // DEBUG

                //Convert from json to ArrayList
                System.out.println("\t" + TAG + ":converting json to CatalogPage.");
                //CatalogPage page = new CatalogPage();
                Gson gson = new Gson();
                try{
                    page = gson.fromJson(data, CatalogPage.class);

                }catch(com.google.gson.JsonSyntaxException e)
                {
                    System.out.println("JsonSyntaxException: not enough time to read buffer.");
                }

                Long endTime = System.currentTimeMillis();
                Long runTime = (endTime-startTime);

                System.out.println("\t" + TAG + ":Read Complete.");
                System.out.println("\t" + TAG + ":runTime: " + runTime);


                //DEBUG
                //print each catalogItem song.title
                for(int i = 0 ; i < page.size(); i++)
                {
                    System.out.println("\t\t" + page.getItem(i).song.title);
                }
                System.out.println("\t" + TAG + ":Print Complete.");
                System.out.println("\t" + TAG + ":runTime: " + runTime);



            }
            if(result[0].equals("search"))
            {
                String filter = "happy";    // default filter
                int count = 20;             // default count

                //If filter is specified update the filter
                if(result.length > 1)
                {
                    filter = result[1];
                } 
                //If count is specified update count
                if(result.length > 2)
                {
                    count = Integer.parseInt(result[2]);
                } 
                System.out.println("Searching...");
                System.out.println("filter: " + filter);
                System.out.println("count: " + count);

                Long startTime = System.currentTimeMillis();
                JsonObject jo = dfs.search(filter, count);

                Long endTime = System.currentTimeMillis();
                Long runTime = endTime-startTime;


                Gson gson = new Gson();
                String jsonString = gson.toJson(jo);
                System.out.println(jsonString);

                System.out.println("search complete.");
                System.out.println("runTime: "+ runTime);

            }
            if(result[0].equals("is"))// is = Index Searchhk
            {
                String filter = "happy";    // default filter
                int count = 20;             // default count

                //If filter is specified update the filter
                if(result.length > 1)
                {
                    filter = result[1];
                } 
                //If count is specified update count
                if(result.length > 2)
                {
                    count = Integer.parseInt(result[2]);
                } 
                System.out.println("Searching...");
                System.out.println("filter: " + filter);
                System.out.println("count: " + count);

                //Start Timer
                Long startTime = System.currentTimeMillis();

                JsonObject jo = dfs.indexSearch(filter, count);

                //End Timer
                Long endTime = System.currentTimeMillis();
                Long runTime = endTime-startTime;


                Gson gson = new Gson();
                String jsonString = gson.toJson(jo);
                System.out.println(jsonString);

                System.out.println("search complete.");
                System.out.println("runTime: "+ runTime + " miliseconds");

            }
            
            if(result[0].equals("g"))// This command is for testing only
            {
                dfs.generateKeyGuid("moon");
            }


            if(result[0].equals("chord"))// This command is for testing only
            {
                dfs.determineChordSize();
            }

            if(result[0].equals("quit"))
            {
                System.out.println("stoping...");
                running = false;
                return;
            }

            line=buffer.readLine();  
        }
            // User interface:
            // join, ls, touch, delete, read, tail, head, append, move
    }

    public void printMenu()
    {
        System.out.println("---Options---");
        System.out.println("\tjoin");
        System.out.println("\tprint");
        System.out.println("\tleave");

        System.out.println("\tcreate (loads music.json into DFS)");
        System.out.println("\tlist   (displays files in DFS)");
        System.out.println("\tread   (reads page 1 of music.json)");

        System.out.println("\tdelete (deletes music.json)");
        System.out.println("\tmove   (not implemented)");
        System.out.println("\tappend (not implemented)");

        System.out.println("\tsearch (filter, count)(\"name or artist or album\" , count)");
        System.out.println("\tis     (Index Search)");

        System.out.println("\t");
        System.out.println("\tquit (freezes?)");
    }

    
    static public void main(String args[]) throws Exception
    {
        if (args.length < 1 ) {
            throw new IllegalArgumentException("Parameter: <port> <portToJoin>");
        }
        if (args.length > 1 ) {
            DFSCommand dfsCommand=new DFSCommand(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        }
        else
        {
            DFSCommand dfsCommand=new DFSCommand( Integer.parseInt(args[0]), 0);
        }
     } 
}
