import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class SonarCoverage {
	private ProfilerCoverage profiler = new ProfilerCoverage();
	private String listingPath;
	private String sourcePath;
	
	/**
	 * Generate a SONARQube Coverage XML from the provided information.
	 *  
	 * @param args List of the required information to generate the XML file. 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length < 3) {
			System.out.println("Usage: SonarCoverage <profiler> <listing path> <sonar data file> [sonar source path]");
		} else {
			
			SonarCoverage sonar;
			
			if (args.length < 4) {
				sonar = new SonarCoverage(args[0], args[1]);
			} else {
				sonar = new SonarCoverage(args[0], args[1], args[3]);
			}
			
			sonar.createDataTest(args[2]);
		}
	}

	/**
	 * Generate a SONARQube Coverage XML from the provided information.
	 * 
	 * @param profSource File or path that contains the Progress Coverage Profiler (.out files).
	 * @param listingPath Path that contains the Source Listings files (Preprocessed source code files).
	 * 
	 * @throws IOException
	 */
	public SonarCoverage(String profSource, String listingPath) throws IOException {
		this(profSource, listingPath, "");
	}

	/**
	 * Generate a SONARQube Coverage XML from the provided information.
	 * 
	 * @param profSource File or path that contains the Progress Coverage Profiler (.out files).
	 * @param listingPath Path that contains the Source Listings files (Preprocessed source code files).
	 * @param sourcePath SONAR source path that will be used in the coverage XML file.
	 * 
	 * @throws IOException
	 */
	public SonarCoverage(String profSource, String listingPath, String sourcePath) throws IOException {
		File file = new File(profSource);
		List<File> files = new ArrayList<File>();
		
		if (!file.exists()) {
			throw new RuntimeException("Informed file or path \"" + file + "\" does not exists");
		} else if (file.isDirectory()) {
			files = Arrays.asList(file.listFiles());
		} else {
			files.add(file);
		}
		
		// Read all profilers files found in the provided path.
		for (int i = 0; i < files.size(); i++) {
			if (files.get(i).getName().indexOf(".out") > 0) {
				this.profiler.readProfiler(files.get(i).getAbsolutePath());
			}
		}
		
		this.listingPath = listingPath;
		this.sourcePath = sourcePath;
	}

	/**
	 * Creates the SONARQube Coverage XML file based on the provided profilers and
	 * listings files.
	 *  
	 * @param output Absolute file name where the XML will be created.
	 * @throws IOException 
	 */
	public void createDataTest(String output) throws IOException {
		Map<String, Map<Integer, Boolean>> data = new HashMap<String, Map<Integer, Boolean>>();

		for (String source : profiler.getSources()) {
			
			String file = getAbsolutePath(listingPath, source);

			if(file == null) {
				// Class - Ex.: com.totvs.sample (com/totvs/sample.cls)
				file = getAbsolutePath(listingPath, source.replace(".", "/"));
			}
			
			if (file != null) {				
				
				ListingFile list = new ListingFile(source, file);
				Map<Integer, Boolean> coverage = profiler.getCoverageInfo(source);

				for (Integer line : coverage.keySet()) {
					if (list.isLineValid(line)) {
						boolean covered = coverage.get(line);
						String sourceR = list.getOriginalSource(line);
						int lineR = list.getOriginalLine(line);

						Map<Integer, Boolean> lines = data.get(sourceR);

						if (lines == null) {
							lines = new TreeMap<Integer, Boolean>();
							data.put(sourceR, lines);
						}

						lines.put(lineR, covered);
					}
				}
			}
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
		writer.write("<coverage version=\"1\">\n");

		for (String source : data.keySet()) {
			
			String sourceP = source;
			
			int pivorSource = source.lastIndexOf(".");
			
			String extSource = "";
			
			if(pivorSource != -1)					
				extSource = source.substring(pivorSource);
			
			if(!extSource.contains("i")) {
				
				String file = getAbsolutePath(listingPath, source);
				
				if(file == null) {
					// Class - Ex.: com.totvs.sample (com/totvs/sample.cls)
					file = getAbsolutePath(listingPath, source.replace(".", "/"));
				}
				
				if(file != null) {
					
					String ext = file.substring(file.lastIndexOf("."));
					
					int pivor = source.lastIndexOf(".");
						
					if(ext.equalsIgnoreCase(".cls"))
						sourceP = source.replace(".", "/") + ext;
					else {
						if(pivor != -1)
							sourceP = source.substring(0, pivor) + ext;
						else
							sourceP = source + ext;
					}
				}
			}
			
			writer.write("\t<file path=\"" + this.sourcePath + sourceP.replaceAll("\\\\", "/") + "\">\n");

			Map<Integer, Boolean> coverage = data.get(source);
			Set<Integer> lines = coverage.keySet();

			for (Integer lineno : lines) {
				Boolean covered = coverage.get(lineno);
				
				if (source.indexOf("ut0666.i") > 0) {
					System.out.println();
				}
				
				writer.write("\t\t<lineToCover lineNumber=\"" + lineno + "\" covered=\"" + covered + "\"/>\n");
			}

			writer.write("\t</file>\n");
		}

		writer.write("</coverage>\n");
		writer.close();
	}
	
	public String getAbsolutePath(String path, String name) throws IOException {
		
		String absolute = null;
				
		Path p = Paths.get(path);
		
		if (p.toFile().exists()) {
			PropathVisitor visitor = new PropathVisitor(name);
			Files.walkFileTree(p, visitor);
			
			if (visitor.getFile() != null) {
				absolute = visitor.getFile().getAbsolutePath();
			}
		}		
	
		return absolute;
	}
}
