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

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import com.example.model.Definition;

public class Indexer {

    public static void main(String[] args) throws Exception {

        // Read JSON
        ObjectMapper mapper = new ObjectMapper();

        List<Definition> entries = mapper.readValue(
                Path.of("app/src/main/java/com/example/functions.json").toFile(),
                new TypeReference<List<Definition>>() {}
        );

        // Create Lucene index
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

        // Search
        Scanner myscanner = new Scanner(System.in);
        String search = myscanner.nextLine();

        try (DirectoryReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);

            MultiFieldQueryParser parser =
                    new MultiFieldQueryParser(
                            new String[]{"term", "definition"},
                            analyzer
                    );

            Query query = parser.parse(search);

            TopDocs results = searcher.search(query, 10);

            StoredFields storedFields = reader.storedFields();

            for (ScoreDoc hit : results.scoreDocs) {

                Document doc = storedFields.document(hit.doc);

                System.out.println("Terms:");

                for (String term : doc.getValues("term")) {
                    System.out.println("  - " + term);
                }

                System.out.println("Definition:");
                System.out.println("  " + doc.get("definition"));

                System.out.println("-------------------------");
            }
        }

        myscanner.close();
    }
}