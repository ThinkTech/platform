package app;

import java.io.InputStream;
import java.io.OutputStream;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

public class FileManager {
	
    private static final String ACCESS_TOKEN = "pQXsY7k7n6AAAAAAAAAACCeARGCUI6ZaOF4vho8GFb39O6lv-8wWo2LqYRm1ShIY";
    private DbxClientV2 client;
    
    public FileManager(){
    	DbxRequestConfig config = new DbxRequestConfig("dropbox/thinktech-portal");
        client = new DbxClientV2(config, ACCESS_TOKEN);
    }
    
    public void upload(String name,InputStream in) throws Exception {
    	client.files().uploadBuilder("/"+name).withMode(WriteMode.OVERWRITE).uploadAndFinish(in);
    }
    
    public void download(String name,OutputStream out) throws Exception {
    	client.files().downloadBuilder("/"+name).start().download(out);
    }
}