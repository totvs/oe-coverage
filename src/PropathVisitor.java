import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class PropathVisitor extends SimpleFileVisitor<Path> {
	
	public static final String[] EXTENSIONS = new String[] { "p", "py", "w", "cls" };
	
	private String name;
	private File   file;
		 
	public PropathVisitor(String name) {
		this.name = name;
	}
		 
	public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
		return FileVisitResult.CONTINUE;
	}
		 
	public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
		File file = path.toFile();
		
		String ext = "";
		
		int i = file.getName().lastIndexOf('.');
		if (i > 0) ext = file.getName().substring(i + 1).toLowerCase();
		
		boolean v = false;
		
		for (int j = 0; j < PropathVisitor.EXTENSIONS.length && !v; j++)
			if(ext.matches(PropathVisitor.EXTENSIONS[j] + "[0-9]*")) v = true;
		
		if (!v) return FileVisitResult.CONTINUE;
		
		String split[] = name.replaceAll("\\\\","/").toLowerCase().split("/");
		
		String f = file.getName().toLowerCase().split("\\.")[0];
		String n = split[split.length - 1].split("\\.")[0];
		
		if (f.equals(n)) {
			this.file = file;
			
			return FileVisitResult.TERMINATE;
		} else {
			return FileVisitResult.CONTINUE;
		}
	}
	
	public File getFile() {
		return file;
	}
}
