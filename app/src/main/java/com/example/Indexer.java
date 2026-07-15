package com.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.example.model.Definition;

public class Indexer {
    public record IndexResult(ByteBuffersDirectory directory, StandardAnalyzer analyzer) {}
    public record SearchResult(String[] term, String[] definition) {}

    public void searchTerm (String search) throws Exception {
        List<Definition> entries = readJSON(Path.of("app/src/main/java/com/example/entries.json").toFile());
        IndexResult result = readIndex(entries);
        search(search, result.directory, result.analyzer);
        

        
    }

    private List<Definition> readJSON(File filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        List<Definition> entries = mapper.readValue(
                Path.of("app/src/main/java/com/example/entries.json").toFile(),
                new TypeReference<List<Definition>>() {}
        );

        return entries;
    }

    private IndexResult readIndex(List<Definition> entries) throws IOException {
        ByteBuffersDirectory directory = new ByteBuffersDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer();

        try (IndexWriter writer = new IndexWriter(
                directory,
                new IndexWriterConfig(analyzer))) {

            for (Definition def : entries) {

                Document doc = new Document();

                // Add every term
                for (String term : def.getSignature()) {
                    doc.add(new TextField(
                            "term",
                            term,
                            Field.Store.YES
                    ));
                }

                // Add definition
                doc.add(new TextField(
                        "definition",
                        def.getDefinition(),
                        Field.Store.YES
                ));

                writer.addDocument(doc);
            }

            writer.commit();
        }

        return new IndexResult(directory, analyzer);
    }

    public static void main(String[] args) throws Exception {

    }



    private List<SearchResult> search(String search, ByteBuffersDirectory directory, StandardAnalyzer analyzer) throws Exception {

    List<SearchResult> searchresults = new ArrayList<>();
    DirectoryReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);
    MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"term", "definition"}, analyzer);

    Query query = parser.parse(search);
    TopDocs results = searcher.search(query, 10);
    StoredFields storedFields = reader.storedFields();

    for (ScoreDoc hit : results.scoreDocs) {
        Document doc = storedFields.document(hit.doc);

        searchresults.add(new SearchResult(doc.getValues("term"), doc.getValues("definition")));

        }
    return searchresults;
    }
}
