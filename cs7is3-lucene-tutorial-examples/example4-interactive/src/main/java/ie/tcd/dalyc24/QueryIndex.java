package ie.tcd.dalyc24;

import java.io.IOException;

import java.util.Scanner;

import java.nio.file.Paths;
import java.nio.file.Files;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.Document;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.DirectoryReader;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

public class QueryIndex
{

	// the location of the search index
	private static String INDEX_DIRECTORY = "../index";
	
	// Limit the number of search results we get
	private static int MAX_RESULTS = 10;
	
	public static void main(String[] args) throws IOException, ParseException
	{
		// Analyzer used by the query parser.
		// Must be the same as the one used when creating the index
		Analyzer analyzer = new StandardAnalyzer();
		
		// Open the folder that contains our search index
		Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
		
		// create objects to read and search across the index
		DirectoryReader ireader = DirectoryReader.open(directory);
		IndexSearcher isearcher = new IndexSearcher(ireader);
		
		// Create the query parser. The default search field is "content", but
		// we can use this to search across any field
		QueryParser parser = new QueryParser("content", analyzer);
		
		String queryString = "";
		Scanner scanner = new Scanner(System.in);
		do
		{
			// trim leading and trailing whitespace from the query
			queryString = queryString.trim();

			// if the user entered a querystring
			if (queryString.length() > 0)
			{
				// parse the query with the parser
				Query query = parser.parse(queryString);

				// Get the set of results
				ScoreDoc[] hits = isearcher.search(query, MAX_RESULTS).scoreDocs;

				// Print the results
				System.out.println("Documents: " + hits.length);
				for (int i = 0; i < hits.length; i++)
				{
					Document hitDoc = isearcher.doc(hits[i].doc);
					System.out.println(i + ") " + hitDoc.get("filename") + " " + hits[i].score);
				}

				System.out.println();	
			}
			
			// prompt the user for input and quit the loop if they escape
			System.out.print(">>> ");
			queryString = scanner.nextLine();
		} while (!queryString.equals("\\q"));
		
		// close everything and quit
		ireader.close();
		directory.close();
	}
}
