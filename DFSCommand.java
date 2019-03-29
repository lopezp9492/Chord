
import java.io.*;


public class DFSCommand
{
    DFS dfs;
    Boolean initialized = false;
        
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
        while (!line.equals("quit"))
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
            if(result[0].equals("quit"))
            {
                System.out.println("ending...");
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
        System.out.println("\tdelete");
        System.out.println("\tquit");


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
