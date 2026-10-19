package com.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.KeywordField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.Parser.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Indexer {
    public record IndexResult(ByteBuffersDirectory directory, Analyzer analyzer) {}
    
    private static final int numresults = 10;
    private static final Logger logger = LogManager.getLogger(Indexer.class);

    private static final Map<String, Float> boosts = Map.of(
        "anchor", 3.0f,
        "name", 4.0f,
        "parent", 2.0f,
        "keywords", 2.5f,
        "signature",1.5f,
        "definition", 1.0f
    );

    private IndexResult index;


    public final void indexEntries() throws IOException {
        try {
            List<Entry> entries = readJSON();
            index = readIndex(entries);
            logger.info("indexing finished with {} entries", entries.size());
        } catch (IOException exc) {
            logger.error("failed to build search index", exc);
            throw new UncheckedIOException(exc);
        }
    }

    private IndexResult readIndex(List<Entry> entries) throws IOException {
        ByteBuffersDirectory directory = new ByteBuffersDirectory();
        CustomAnalyzer analyzer = CustomAnalyzer.builder()
            .withTokenizer("standard")
            .addTokenFilter("lowercase")
            .addTokenFilter("edgeNGram", "minGramSize", "1", "maxGramSize", "10")
            .build();

        try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
            for (Entry def : entries) {
                Document doc = new Document();
                doc.add(new StoredField("location", def.location()));
                doc.add(new StoredField("id", def.id()));
                
                doc.add(new TextField("name", def.name(), Field.Store.YES));
                doc.add(new TextField("parent", def.parent(), Field.Store.YES));

                doc.add(new TextField("anchor", def.anchor(), Field.Store.YES));
                doc.add(new StringField("anchorExact", def.anchor().toLowerCase(), Field.Store.NO));

                for (String keyword : def.keywords()) {
                    doc.add(new KeywordField("keywords", keyword, Field.Store.YES));
                }

                for (String signature : def.signature()) {
                    doc.add(new TextField("signature", signature, Field.Store.YES));
                }

                doc.add(new TextField("definition", def.definition(), Field.Store.YES));
                writer.addDocument(doc);
            }
            writer.commit();
        }

        return new IndexResult(directory, analyzer);
    }

    public List<Entry> readJSON() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        List<Entry> entries = mapper.readValue(
                Path.of(Main.getAppDirectory(), "entries.json").toFile(),
                new TypeReference<List<Entry>>() { }
        );
        return entries;
    }

    public Entry getEntryFromID(List<Entry> entries, String id) {
        for (Entry entry : entries) {
            if (id.equals(entry.id())) {
                return new Entry(
                    entry.location(), entry.linenum(), entry.type(), entry.id(), entry.anchor(), entry.name(),
                    entry.parent(), entry.keywords(), entry.signature(), entry.definition()
                );
            }
        }

        return null;
    }

    private List<Entry> search(String search, ByteBuffersDirectory directory, Analyzer analyzer) throws IOException, ParseException {
        Query query;
        List<Entry> searchresults = new ArrayList<>();
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        MultiFieldQueryParser parser = new MultiFieldQueryParser(
            new String[]{"anchor", "name", "parent", "keywords", "signature", "definition"}, analyzer, boosts
        );

        try {
            query = parser.parse(search);
        } catch (ParseException err) {
            query = parser.parse(MultiFieldQueryParser.escape(search));
        }

        BooleanQuery.Builder querybuilder = new BooleanQuery.Builder();
        querybuilder.add(query, BooleanClause.Occur.SHOULD);

        String identifier = search.toLowerCase(Locale.ROOT);
        querybuilder.add(
            new BoostQuery(
                new PrefixQuery(new Term("anchorExact", identifier)),
                100.0f
            ),
            BooleanClause.Occur.SHOULD
        );

        TopDocs results = searcher.search(querybuilder.build(), numresults);
        StoredFields storedFields = reader.storedFields();

        for (ScoreDoc hit : results.scoreDocs) {
            Document doc = storedFields.document(hit.doc);
            searchresults.add(getEntryFromID(readJSON(), doc.get("id")));
        }

        return searchresults;
    }

    public final List<Entry> searchTerm(String search) throws IOException, ParseException {
        return search(search, index.directory, index.analyzer);
    }
}
