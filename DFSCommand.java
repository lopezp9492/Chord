
import java.io.*;
import java.util.*;
import com.google.gson.Gson;

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
                initialized = true;
            }

            if (result[0].equals("list"))
            {
                    System.out.println("List of files: ");
                    System.out.println(dfs.lists());
            }

            if (result[0].equals("delete"))
            {
                dfs.delete("music.json");
            }

            if(result[0].equals("read"))
            {
                //TEST

                //Remote Input File Stream
                RemoteInputFileStream dataraw = dfs.read("music.json", 1);
                System.out.println("\t"+ TAG+":connecting."); // DEBUG
                dataraw.connect();

                //Scanner
                System.out.println("\t" + TAG+":scanning."); // DEBUG
                Scanner scan = new Scanner(dataraw);
                scan.useDelimiter("\\A");
                String data = scan.next();
                System.out.println(data); // DEBUG

                //Convert from json to ArrayList
                System.out.println("\t" + TAG + ":converting json to CatalogPage.");
                //ArrayList<CatalogItem> pageItems = new ArrayList<CatalogItem>();// Data //OLD
                CatalogPage page = new CatalogPage();
                Gson gson = new Gson();
                page = gson.fromJson(data, CatalogPage.class);
                //final ArrayList<?> jsonArray = new Gson().fromJson(array.toString(), ArrayList.class);

                System.out.println("\t" + TAG + ":Read Complete.");

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

        System.out.println("\tcreate");
        System.out.println("\tlist");
        System.out.println("\tdelete (not implemented)");

        System.out.println("\tread");
        System.out.println("\tmove  (not implemented)");
        System.out.println("\append (not implemented)");




        System.out.println("\tquit (crashes?)");
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
