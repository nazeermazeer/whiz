package com.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.model.Definition;

public class Indexer {

    public record IndexResult(
        ByteBuffersDirectory directory, StandardAnalyzer analyzer
    ) { }

    public record SearchResult(
        String[] location, String[] term, String[] definition
    ) {
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SearchResult result = (SearchResult) o;
            return Arrays.equals(location, result.location)
                && Arrays.equals(term, result.term)
                && Arrays.equals(definition, result.definition);
        }

        @Override
        public int hashCode() {
            int result = Arrays.hashCode(location);
            result = MULTIPLER * result + Arrays.hashCode(term);
            result = MULTIPLER * result + Arrays.hashCode(definition);
            return result;
        }

        @Override
        public String toString() {
            return "SearchResult["
                + "location=" + Arrays.toString(location) + ", "
                + "term=" + Arrays.toString(term) + ", "
                + "definition=" + Arrays.toString(definition)
                + "]";
        }
    }
    private static final int MULTIPLER = 31;
    private static final int NUMRESULTS = 10;

    private IndexResult index;


    public final void indexEntries() throws IOException {
        try {
            List<Definition> entries = readJSON();
            index = readIndex(entries);
        } catch (IOException exc) {
            throw new UncheckedIOException(exc);
        }
    }

    private IndexResult readIndex(List<Definition> entries) throws IOException {
        ByteBuffersDirectory directory = new ByteBuffersDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer();

        try (IndexWriter writer = new IndexWriter(
            directory, new IndexWriterConfig(analyzer)
        )) {
            for (Definition def : entries) {
                Document doc = new Document();
                doc.add(
                    new StringField("location", def.getLocation(),
                    Field.Store.YES
                ));
                for (String term : def.getSignature()) {
                    doc.add(new TextField("term", term, Field.Store.YES));
                }
                doc.add(
                    new TextField("definition", def.getDefinition(), Field.Store.YES)
                );
                writer.addDocument(doc);
            }
            writer.commit();
        }

        return new IndexResult(directory, analyzer);
    }

    private List<Definition> readJSON() throws IOException {
        ObjectMapper mapper = new ObjectMapper();


        return mapper.readValue(
                Path.of("app/src/main/java/com/example/entries.json").toFile(),
                new TypeReference<List<Definition>>() { }
        );
    }



    private List<SearchResult> search(
        String search, ByteBuffersDirectory directory, StandardAnalyzer analyzer
    ) throws IOException, ParseException {
        List<SearchResult> searchresults = new ArrayList<>();
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        MultiFieldQueryParser parser = new MultiFieldQueryParser(
            new String[]{"term", "definition"}, analyzer
        );

        Query query = parser.parse(search);
        TopDocs results = searcher.search(query, NUMRESULTS);
        StoredFields storedFields = reader.storedFields();

        for (ScoreDoc hit : results.scoreDocs) {
            Document doc = storedFields.document(hit.doc);

            searchresults.add(
                new SearchResult(doc.getValues("location"),
                doc.getValues("term"), doc.getValues("definition")
            ));

            }

        return searchresults;
    }

    public final List<SearchResult> searchTerm(String search)
    throws IOException, ParseException {
        return search(search, index.directory, index.analyzer);
    }
}
