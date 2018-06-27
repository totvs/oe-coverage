import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfilerCoverage {
	private Map<String, Map<Integer, Boolean>> sources = new HashMap<String, Map<Integer, Boolean>>();
	private Map<Integer, String> dbg = new HashMap<Integer, String>();
	
	private static final int PROFILER_BLOCK_INFO = 0;
	private static final int PROFILER_BLOCK_SOURCES = 1;
	private static final int PROFILER_BLOCK_COVERAGE = 3;
	private static final int PROFILER_BLOCK_SOURCES_LINES = 4;

	/**
	 * Reads the provided profiler file and extracts the coverage information.
	 *  
	 * @param file
	 * @throws IOException
	 */
	public void readProfiler(String file) throws IOException {
		String line = null;
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Map<Integer, Boolean> source = null;

		int blocks = 0;
		boolean readingLines = false;
		
		// Empty the source list.
		dbg.clear();

		while ((line = reader.readLine()) != null) {
			if (line.equals(".")) {
				blocks++;
				
				source = null;
				readingLines = false;
			} else if (!line.isEmpty()){
				if (blocks == PROFILER_BLOCK_INFO) {
					System.out.println("** Reading profiler file \"" + file + "\" **");
				} else if (blocks == PROFILER_BLOCK_SOURCES) {
					this.parseSource(line);
				} else if (blocks == PROFILER_BLOCK_COVERAGE) {
					this.parseCoverage(line);
				} else if (blocks > PROFILER_BLOCK_SOURCES_LINES) {
					if (!readingLines) {
						readingLines = true;
						source = this.getLinesSource(line);
					} else if (source != null) {
						this.parseLines(line, source);
					}
				}
			}
		}

		reader.close();
	}

	public Set<String> getSources() {
		return sources.keySet();
	}

	public Map<Integer, Boolean> getCoverageInfo(String source) {
		return sources.get(source);
	}
	
	/**
	 * Parses the source line extracted from the profiler.
	 * @param sourceLine Line extracted from the profiler containing the source code information.
	 * 
	 * {@code example: "698 "remove-all-links adm/objects/broker.p" "" 0"}
	 */
	private void parseSource(String sourceLine) {
		Matcher matcher = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(sourceLine);

		// Get the source ID information.
		matcher.find();
		int codeno = Integer.valueOf(matcher.group(2));

		// Get the source path information.
		matcher.find();
		String list[] = matcher.group(1).split(" ");
		
		// Add the found source information to the list.
		dbg.put(codeno, list[list.length - 1].replace("\\", "/"));
	}
	
	/**
	 * Parses the coverage line extracted from the profiler.
	 * @param coverageLine Line extracted from the profiler containing the coverage information.
	 * 
	 * {@code example: "32 1974 1 0.000496 0.000496"}
	 */
	private void parseCoverage(String coverageLine) {
		String filename;
		String[] splitted = coverageLine.split(" ");
		
		Map<Integer, Boolean> source = null;
		
		int lineno = 0;
		int codeno = 0;
		
		if (splitted.length == 5) {
			codeno = Integer.valueOf(splitted[0].trim());
			lineno = Integer.valueOf(splitted[1].trim());

			if (lineno > 0) {
				// Recover the covered source from the list.
				filename = dbg.get(codeno);
				source = sources.get(filename);
				
				// Creates the source covered lines list and adds the covered line.
				if (source == null) {
					source = new TreeMap<Integer, Boolean>();
					sources.put(filename, source);
				}
				
				// If the coverage line still doesn't exist or it's false, creates the new as true.
				if (!source.containsKey(lineno) || !source.get(lineno)) {
					source.put(lineno, true);
				}
			}
		}
	}
	
	/**
	 * Returns the source object encountered for the informed line.
	 * 
	 * @param line Line containing the information about the source ID.
	 * @return Source object for the informed line.
	 * 
	 * {@code example: "319 "" 22"}
	 */
	private Map<Integer, Boolean> getLinesSource(String line) {
		Matcher matcher = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(line);
		Map<Integer, Boolean> source = null;
		
		int codeno;
		String filename;

		if (matcher.find()) {
			// Get the source ID information.
			codeno = Integer.valueOf(matcher.group(2));
			filename = dbg.get(codeno);
			
			// Get the source from the list.
			source = sources.get(filename);
			
			if (source == null) {
				source = new TreeMap<Integer, Boolean>();
				sources.put(filename, source);
			}
		}
		
		return source;
	}
	
	/**
	 * Parses the executable source code line encountered for the informed line.
	 * @param line Line containing the information about the executable source code line.
	 * @param source List of the source's executable lines for the current source.
	 * 
	 * {@code example: "38"}
	 */
	private void parseLines(String line, Map<Integer, Boolean> source) {
		int lineno = Integer.valueOf(line);

		if (lineno > 0) {
			if (!source.containsKey(lineno)) {
				source.put(lineno, false);
			}
		}
	}
}
