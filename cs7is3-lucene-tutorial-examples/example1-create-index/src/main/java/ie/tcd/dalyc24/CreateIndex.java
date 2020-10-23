package ie.tcd.dalyc24;

import java.io.IOException;

import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
// import org.apache.lucene.store.RAMDirectory;
 
public class CreateIndex
{
    
    // Directory where the search index will be saved
    private static String INDEX_DIRECTORY = "../index";

    public static void main(String[] args) throws IOException
    {
        // Analyzer that is used to process TextField
        Analyzer analyzer = new StandardAnalyzer();
        
        // To store an index in memory
        // Directory directory = new RAMDirectory();
        // To store an index on disk
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        
        // Index opening mode
        // IndexWriterConfig.OpenMode.CREATE = create a new index
        // IndexWriterConfig.OpenMode.APPEND = open an existing index
        // IndexWriterConfig.OpenMode.CREATE_OR_APPEND = create an index if it
        // does not exist, otherwise it opens it
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        
        IndexWriter iwriter = new IndexWriter(directory, config);  
        
        // Create a new document
        Document doc = new Document();
        doc.add(new TextField("super_name", "Spider-MAN1", Field.Store.YES));
        doc.add(new TextField("name", "Peter ParkER1", Field.Store.YES));
        doc.add(new TextField("category", "superheRO0", Field.Store.YES));

        // Save the document to the index
        iwriter.addDocument(doc);

        // Commit changes and close everything
        iwriter.close();
        directory.close();
    }
}
