package Maria_Hameed.Spider_V3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.*;
import java.sql.*;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App
{
    public static void main(String args[]) throws IOException, NullPointerException{

        myFile mf = new myFile();						//makes an instance of myFile
    }
}

class myFile {

    File f;
    String ouputStr = "";

    myFile() throws IOException, NullPointerException {            //constructor

        Connection connection = null;
        Statement statement = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost/spider_db", "root", "");

            statement = connection.createStatement();

            String sql = "SELECT * FROM file_data";

            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                System.out.println("Data already stored in db...");
            } else {
                f = new File("F:\\");                            //makes a file
                if (f.exists())                            //check whether the file exists
                {
                    if (f.isDirectory())                            //checks if it is a directory
                    {
                        getFileName("F:\\");                    //if the file is a directory then send it to the method getFileName to list the files
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        get_userInput();
    }

    private void get_userInput() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter the key:");
        String key_val = sc.nextLine();
        getResult gr = new getResult(key_val);
        gr.start();

        System.out.println("\nEnter size:");
        String key_val_2 = sc.nextLine();
        getResult gr_2 = new getResult(key_val_2);
        gr_2.start();
    }

    public void getFileName(String s) throws IOException, NullPointerException {
        File[] fileArray;
        File newFile = new File(s);                        //makes
        fileArray = newFile.listFiles();                //makes a file Array to store files in a directory to find out the required one

        if (fileArray == null)
            return;                                        //if the directory is empty then it returns


        int size = fileArray.length;
        if (newFile.isDirectory())
            storeDirectoryData(s, size);
        for (int i = 0; i < fileArray.length; i++) {
            if (fileArray[i].isDirectory())                //checks if it if a directory
            {
                getFileName(fileArray[i].getAbsolutePath());                        //if it is a directory then it again send the name to same method for recursive searching
            }
            ouputStr += fileArray[i].getAbsolutePath();


            if (fileArray[i].getPath().endsWith(".txt")) {
                String[] filePaths = fileArray[i].getPath().split("\\\\");//split(File.pathSeparator);

                String fileName = filePaths[filePaths.length - 1];

                //System.out.println("--"+fileName+"--");


                String[] fileNameParts = fileName.split(" ");
                //System.out.println("creating thread + "+fileArray[i].getPath());
                readFile rf = new readFile(fileArray[i].getPath(), fileNameParts, fileNameParts.length);
                rf.start();

            }
        }
    }//

    private void storeDirectoryData(String s, int size) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost/spider_db", "root", "");
            PreparedStatement store_file_details = con.prepareStatement
                    ("insert into folder_details values(?,?,?,?,?,?,?,?)");


            Path path = Paths.get(s);
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

            System.out.println("-----------------------------------------");

            System.out.println("creationTime: " + attr.creationTime());
            System.out.println("lastAccessTime: " + attr.lastAccessTime());
            System.out.println("lastModifiedTime: " + attr.lastModifiedTime());

            store_file_details.setString(4, attr.lastAccessTime().toString());
            store_file_details.setString(5, attr.creationTime().toString());
            store_file_details.setString(6, attr.lastModifiedTime().toString());

            System.out.println("size: " + attr.size());
            store_file_details.setInt(3, size);

            FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
            UserPrincipal owner = ownerAttributeView.getOwner();
            System.out.println("owner: " + owner.getName());
            store_file_details.setString(7, owner.getName());

            String permissions_ = "";
            try {
                AclFileAttributeView aclView = Files.getFileAttributeView(path,
                        AclFileAttributeView.class);
                if (aclView == null) {
                    System.out.format("ACL view  is not  supported.%n");
                    return;
                }
                List<AclEntry> aclEntries = aclView.getAcl();
                for (AclEntry entry : aclEntries) {
                    System.out.format("Principal: %s%n", entry.principal());
                    System.out.format("Type: %s%n", entry.type());
                    System.out.format("Permissions are:%n");

                    Set<AclEntryPermission> permissions = entry.permissions();
                    for (AclEntryPermission p : permissions) {
                        permissions_ += p.toString();
                        //System.out.format("%s %n", p);
                    }
                }


                File fff = new File(s);

                store_file_details.setString(1, fff.getPath());
                store_file_details.setString(2, s);
                store_file_details.setString(8, permissions_);

            } catch (Exception e1) {
                e1.printStackTrace();
            }

            store_file_details.executeUpdate();
            store_file_details.close();
            con.close();

        } catch (Exception e) {
        }

    }//end class

}
    class readFile extends Thread {

        String fileName;
        String fileNameArr[];
        int nameLen;

        readFile(String filePath, String nameArr[], int len) {
            fileName = filePath;
            fileNameArr = nameArr;
            nameLen = len;
        }

        public void run() {
            //System.out.println("thread Started");

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/spider_db", "root", "");
                PreparedStatement updateemp = con.prepareStatement
                        ("insert into file_data(key_, file_name) values(?,?)");
                PreparedStatement store_file_details = con.prepareStatement
                        ("insert into file_details values(?,?,?,?,?,?,?,?)");


                Path path = Paths.get(fileName);
                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

                System.out.println("-----------------------------------------");

                System.out.println("creationTime: " + attr.creationTime());
                System.out.println("lastAccessTime: " + attr.lastAccessTime());
                System.out.println("lastModifiedTime: " + attr.lastModifiedTime());

                store_file_details.setString(4, attr.lastAccessTime().toString());
                store_file_details.setString(5, attr.creationTime().toString());
                store_file_details.setString(6, attr.lastModifiedTime().toString());

                System.out.println("size: " + attr.size());
                store_file_details.setInt(3, (int) attr.size());

                FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
                UserPrincipal owner = ownerAttributeView.getOwner();
                System.out.println("owner: " + owner.getName());
                store_file_details.setString(7, owner.getName());

                String permissions_ = "";
                try {
                    AclFileAttributeView aclView = Files.getFileAttributeView(path,
                            AclFileAttributeView.class);
                    if (aclView == null) {
                        System.out.format("ACL view  is not  supported.%n");
                        return;
                    }
                    List<AclEntry> aclEntries = aclView.getAcl();
                    for (AclEntry entry : aclEntries) {
                        System.out.format("Principal: %s%n", entry.principal());
                        System.out.format("Type: %s%n", entry.type());
                        System.out.format("Permissions are:%n");

                        Set<AclEntryPermission> permissions = entry.permissions();
                        for (AclEntryPermission p : permissions) {
                            permissions_ += p.toString();
                            //System.out.format("%s %n", p);
                        }
                    }
                    store_file_details.setString(1, fileNameArr[fileNameArr.length - 1]);
                    store_file_details.setString(2, fileName);
                    store_file_details.setString(8, permissions_);

                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                for (String tempName : fileNameArr) {
                    updateemp.setString(1, tempName);
                    updateemp.setString(2, fileName);
                    updateemp.executeUpdate();
                }

                store_file_details.executeUpdate();
                store_file_details.close();
                updateemp.close();
                con.close();

                storeFileContent(fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public void storeFileContent(String name) {
            BufferedReader br = null;

            try {

                String line;

                br = new BufferedReader(new FileReader(name));

                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/spider_db", "root", "");

                PreparedStatement updateemp = con.prepareStatement
                        ("insert into file_data(key_, file_name) values(?,?)");


                while ((line = br.readLine()) != null) {
                    //System.out.println(sCurrentLine);

                    String[] fileNameParts = line.split(" ");

                    for (String tempVal : fileNameParts) {

                        updateemp.setString(1, tempVal);
                        updateemp.setString(2, fileName);
                        updateemp.executeUpdate();

                    }
                }

                updateemp.close();
                con.close();

            } catch (IOException e) {
                try {
                    if (br != null) br.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


class getResult extends Thread
{
    public static String search_key;
    private static Pattern pattern;
    private static Matcher matcher;

    getResult(String key_)
    {
        search_key = key_;
        pattern = Pattern.compile(search_key);
    }

    public void run()
    {
        PreparedStatement preparedStatement = null;

        String selectSQL = "SELECT file_name, key_ FROM file_data";

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost/spider_db", "root", "");
            preparedStatement = con.prepareStatement(selectSQL);

            ResultSet rs = preparedStatement.executeQuery();

            System.out.println("For key "+search_key+ " result: ");

            int zz=0;
            while (rs.next()) {
                matcher = pattern.matcher(rs.getString("key_"));
                if(matcher.find()) {
                    System.out.println(rs.getString("file_name"));
                    zz++;
                }
            }



            preparedStatement = con.prepareStatement("select * from file_details");

            //preparedStatement.setString(1, search_key);

            // execute select SQL stetement
            rs = preparedStatement.executeQuery();

            System.out.println("For key "+search_key+ " result: ");

            int found =0;
//            ResultSetMetaData resultSetMetaData = rs.getMetaData();

            while (rs.next()) {
                //System.out.print("    from details  ");
                matcher = pattern.matcher(rs.getString("lastAccessTime"));
                if(matcher.find()) {
                    System.out.println(rs.getString("name"));
                    zz++;
                    continue;
                }
                matcher = pattern.matcher(rs.getString("lastModifiedTime"));
                if(matcher.find()) {
                    System.out.println(rs.getString("name"));
                    zz++;
                    continue;
                }
                matcher = pattern.matcher(rs.getString("creationTime"));
                if(matcher.find()) {
                    System.out.println(rs.getString("name"));
                    zz++;
                    continue;
                }
                matcher = pattern.matcher(rs.getString("owner"));
                if(matcher.find()) {
                    System.out.println(rs.getString("name"));
                    zz++;
                    continue;
                }
                matcher = pattern.matcher(rs.getString("permissions"));
                if(matcher.find()) {
                    System.out.println(rs.getString("name"));
                    zz++;
                    continue;
                }

                matcher = pattern.matcher(rs.getString("key_"));
                if(matcher.find()) {
                    System.out.println(rs.getString("name"));
                    zz++;
                    continue;
                }

                matcher = pattern.matcher(rs.getString("size"));
                if(matcher.find()) {
                    System.out.println(rs.getString("name"));
                    zz++;

                }
            }










            if (zz ==0)
                System.out.println("No result found matching the key");
            preparedStatement.close();
            con.close();
        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}