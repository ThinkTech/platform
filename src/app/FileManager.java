package app;

import java.io.InputStream;
import java.io.OutputStream;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

public class FileManager {
	
	private static final String ACCESS_TOKEN = "pQXsY7k7n6AAAAAAAAAAPWBEAjxZy5Xcgtsd8kyz9VsKOIrEzorICsW29BV4bspM";
    private final DbxClientV2 client;
    private final String folder;
    
    public FileManager(String folder) {
    	this.folder = folder;
    	client = new DbxClientV2(new DbxRequestConfig("dropbox/thinktech-app"), ACCESS_TOKEN);
    }
    
    public void upload(String name,InputStream in) throws Exception {
    	client.files().uploadBuilder("/"+folder+"/"+name).withMode(WriteMode.OVERWRITE).uploadAndFinish(in);
    }
    
    public void download(String name,OutputStream out) throws Exception {
    	client.files().downloadBuilder("/"+folder+"/"+name).start().download(out);
    }
}