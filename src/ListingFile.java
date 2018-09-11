import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListingFile {
	private List<String[]> listings = new ArrayList<>();
	private List<String> sources = new ArrayList<>();

	private static final int LISTING_INCLUDE = 0;
	private static final int LISTING_LINE = 1;
	private static final int LISTING_BLOCK = 2;
	private static final int LISTING_LINE_CONTENT = 3;

	public ListingFile(String source, String file) throws IOException {
		this.readListingFile(source, file);
	}

	private void readListingFile(String source, String file) throws IOException {
		ArrayList<String> listingFile = this.adjustListingFile(file);

		int src = 0;
		int srcOld = -1;

		String line;
		List<String> stack = new ArrayList<String>();

		String col1;
		String col2;
		String col3;
		
		System.out.println("** Reading listing file \"" + file + "\" **");
		
		for (int i = 0; i < listingFile.size(); i++) {
			line = listingFile.get(i);
			
			if (!line.startsWith(" ") || line.length() <= 0) {
				continue;
			}

			col1 = line.substring(0, 2).trim(); // Source number
			col2 = line.substring(3, 7).trim(); // Line number
			col3 = line.substring(8, 11).trim(); // Block number

			col1 = (col1.equals("") ? "0" : col1);
			col2 = (col2.equals("") ? "0" : col2);
			col3 = (col3.equals("") ? "0" : col3);

			if (!col1.matches("-?\\d+(\\.\\d+)?")
			 || !col2.matches("-?\\d+(\\.\\d+)?")
			 || !col3.matches("-?\\d+(\\.\\d+)?")) {
				continue;
			}

			src = Integer.parseInt(col1);
			srcOld = (srcOld == -1 ? src : srcOld);

			if (srcOld != src) {
				if (srcOld < src) {
					int close = 0;
					int start = 0;

					String last;
					String include = "";

					for (int j = i - 1; j >= 0; j--) {
						last = listingFile.get(j);
						start = 0;

						while (last.indexOf('}', start) >= 0) {
							start = last.indexOf('}', start) + 1;
							close++;
						}

						start = 0;

						while (last.indexOf('{', start) >= 0) {
							start = last.indexOf('{', start) + 1;
							close--;
						}

						if(close > 0) { 
							listings.remove(listings.size() - 1);
							sources.remove(sources.size() - 1);
						}

						include = last + include;
						if (close == 0) break;
					}

					include = include.substring(12).trim();
					include = include.substring(include.indexOf('{'));

					String[] token = include.split(" ");
					
					for (String item : token) {
						if (item.indexOf("{") != -1 && !item.contains("&") && item.contains("/")) {
							stack.add(item.replace("{", "").replace("}", ""));
						}
					}
				} else {
					stack.remove(stack.size() - 1);
				}
			}
			
			String listing[] = new String[4];
			listing[LISTING_INCLUDE] = Integer.toString(src);
			listing[LISTING_LINE] = col2;
			listing[LISTING_BLOCK] = col3;
			listing[LISTING_LINE_CONTENT] = line.substring(12).replaceAll("^\\s+","").replaceAll("\\s+$","");

			listings.add(listing);
			sources.add(stack.isEmpty() ? source : stack.get(stack.size() - 1));

			srcOld = src;
		}
	}

	/**
	 * Adjust the Progress Listing file removing the unnecessary text and breaking
	 * lines.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private ArrayList<String> adjustListingFile(String file) throws IOException {
		ArrayList<String> newFile = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line;
		String colLine = "";

		while ((line = reader.readLine()) != null) {
			if (line.indexOf(12) == 0) {
				break;
			} else if (line.startsWith(" ") && line.length() > 1) {
				colLine = line.substring(3, 7).trim();

				if (colLine.equals("")) colLine = "0";

				if (!colLine.matches("-?\\d+(\\.\\d+)?")) {
					continue;
				}

				newFile.add(line);
			}
		}

		reader.close();

		return newFile;
	}

	public int getOriginalLine(int line) {
		line--;
		return line < listings.size() ? Integer.parseInt(listings.get(line)[LISTING_LINE]) : 0;
	}

	public String getOriginalSource(int line) {
		line--;
		return line < sources.size() ? sources.get(line) : "";
	}

	public boolean isInclude(int line) {
		line--;
		return line < listings.size() ? Integer.parseInt(listings.get(line)[LISTING_INCLUDE]) > 0 : false;
	}
	
	public boolean isLineValid(int line) {
		String content;
		line--;
		
		if (line < 0 || line > (listings.size() - 1)) {
			content = "";
		} else {
			content = listings.get(line)[LISTING_LINE_CONTENT].toUpperCase();
		}
		
		return !content.isEmpty() && (content.contains("IF") || !content.contains("DO:")) && !content.startsWith("END");
	}
}
