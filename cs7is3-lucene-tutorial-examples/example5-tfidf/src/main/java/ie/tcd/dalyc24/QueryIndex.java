package ie.tcd.dalyc24;

import java.io.IOException;

import java.util.Scanner;

import java.nio.file.Paths;
import java.nio.file.Files;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.util.BytesRef;

import org.apache.lucene.document.Document;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.DocIdSetIterator;

public class QueryIndex
{
    
    // Directory where the search index will be saved
    private static String INDEX_DIRECTORY = "../index";

    private Analyzer analyzer;
    private Directory directory;

    public QueryIndex() throws IOException
    {
        // Need to use the same analyzer and index directory throughout, so
        // initialize them here
        this.analyzer = new StandardAnalyzer();
        this.directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
    }

    public void buildIndex(String[] args) throws IOException
    {

        // Create a new field type which will store term vector information
        FieldType ft = new FieldType(TextField.TYPE_STORED);
        ft.setTokenized(true); //done as default
        ft.setStoreTermVectors(true);
        ft.setStoreTermVectorPositions(true);
        ft.setStoreTermVectorOffsets(true);
        ft.setStoreTermVectorPayloads(true);

        // create and configure an index writer
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter iwriter = new IndexWriter(directory, config);  

        // Add all input documents to the index
        for (String arg : args)
        {
            System.out.printf("Indexing \"%s\"\n", arg);
            String content = new String(Files.readAllBytes(Paths.get(arg)));
            Document doc = new Document();
            doc.add(new StringField("filename", arg, Field.Store.YES));
            doc.add(new Field("content", content, ft));
            iwriter.addDocument(doc);
        }
        
        // close the writer
        iwriter.close();
    }

    public void postingsDemo() throws IOException
    {
        DirectoryReader ireader = DirectoryReader.open(directory);
    
        // Use IndexSearcher to retrieve some arbitrary document from the index        
        IndexSearcher isearcher = new IndexSearcher(ireader);
        Query queryTerm = new TermQuery(new Term("content","raven"));
        ScoreDoc[] hits = isearcher.search(queryTerm, 1).scoreDocs;
        
        // Make sure we actually found something
        if (hits.length <= 0)
        {
            System.out.println("Failed to retrieve a document");
            return;
        }

        // get the document ID of the first search result
        int docID = hits[0].doc;

        // Get the fields associated with the document (filename and content)
        Fields fields = ireader.getTermVectors(docID);

        for (String field : fields)
        {
            // For each field, get the terms it contains i.e. unique words
            Terms terms = fields.terms(field);

            // Iterate over each term in the field
            BytesRef termByte = null;
            TermsEnum termsEnum = terms.iterator();

            while ((termByte = termsEnum.next()) != null)
            {                                
                int id;

                // for each term retrieve its postings list
                PostingsEnum posting = null;
                posting = termsEnum.postings(posting, PostingsEnum.FREQS);

                // This only processes the single document we retrieved earlier
                while ((id = posting.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS)
                {
                    // convert the term from a byte array to a string
                    String termString = termByte.utf8ToString();
                    
                    // extract some stats from the index
                    Term term = new Term(field, termString);
                    long freq = posting.freq();
                    long docFreq = ireader.docFreq(term);
                    long totFreq = ireader.totalTermFreq(term);

                    // print the results
                    System.out.printf(
                        "%-16s : freq = %4d : totfreq = %4d : docfreq = %4d\n",
                        termString, freq, totFreq, docFreq
                    );
                }
            }
        }

        // close everything when we're done
        ireader.close();
    }

    public void shutdown() throws IOException
    {
        directory.close();
    }

    public static void main(String[] args) throws IOException
    {
        
        if (args.length <= 0)
        {
            System.out.println("Expected corpus as input");
            System.exit(1);            
        }

        QueryIndex qi = new QueryIndex();
        qi.buildIndex(args);
        qi.postingsDemo();
        qi.shutdown();
    }
}
