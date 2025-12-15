package shadowbuddy.ml;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.IteratedLovinsStemmer;
import weka.core.stopwords.Rainbow;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * Trains an intent classification model using Weka, a Java-based machine learning library.
 * This utility class loads training data from an ARFF file, Weka's native dataset format.
 * Raw user text input is then preprocessed using the {@code StringToWordVector} filter, which
 * converts string-based input into numeric attributes suitable for machine learning algorithms.
 * A {@code NaiveBayes} classification algorithm is then trained on the filtered data to predict
 * the intent associated with each user input. Finally, the trained classifier and the text filter
 * are saved to disk for future use.
 */
public class IntentTrainer {
    private static final String TRAINING_FILE_PATH = "src/main/resources/dataset/intent.arff";
    private static final String MODEL_FILE_PATH = "src/main/resources/models/shadow.model";
    private static final String FILTER_FILE_PATH = "src/main/resources/models/filter.model";

    /**
     * Provides the main entry point of the IntentTrainer program.
     *
     * @throws Exception If reading data, building the classifier and filter, or persisting files fails.
     */
    public static void main(String[] args) throws Exception {
        Instances data = DataSource.read(TRAINING_FILE_PATH); // loads ARFF dataset into a Weka Instances object

        /* Sets the class attribute of dataset to be the last attribute in the dataset. */
        data.setClassIndex(data.numAttributes() - 1); // Weka uses 0-based indexing

        StringToWordVector filter = new StringToWordVector();
        filter.setLowerCaseTokens(true);
        filter.setOutputWordCounts(true); // outputs raw word counts instead of presence/absence booleans
        filter.setWordsToKeep(5000); // keeps only the top 5000 most frequent or important words

        /* Applies the filter globally to the entire dataset, instead of separately for each class. */
        filter.setDoNotOperateOnPerClassBasis(true); // ensures a single consistent shared vocabulary

        /* Enables term frequency transformation, counting relative proportion of each word, instead of raw count. */
        filter.setTFTransform(true); // normalises text attributes relative to document length

        /* Enables inverse document frequency transformation to distinguish between important and common words. */
        filter.setIDFTransform(true); // gives higher weighted importance to rare, informative words than common words

        /* Configures filter to use a Stopwords handler to remove common, uninformative words. */
        filter.setStopwordsHandler(new Rainbow()); // Rainbow is Weka's built-in Stopwords list

        /* Configures filter to use a stemmer to reduce words to their base forms, improving generalisation. */
        IteratedLovinsStemmer stemmer = new IteratedLovinsStemmer(); // IteratedLovinsStemmer is Weka's built-in stemmer
        filter.setStemmer(stemmer);

        /* Initialises the filter with the dataset structure in Weka. */
        filter.setInputFormat(data); // includes information on the type and number of attributes, class index, etc.
        Instances filteredData = Filter.useFilter(data, filter); // applies filter to produce a new, filtered dataset

        /* Resets the class attribute of filtered dataset to be the attribute named "class" in the filtered dataset. */
        filteredData.setClassIndex(filteredData.attribute("class").index());

        /* Instantiates the chosen classifier algorithm, NaiveBayes, and trains it on the filtered dataset. */
        Classifier classifier = new NaiveBayes();
        classifier.buildClassifier(filteredData);

        /* Persists the trained classifier and filter so they can be loaded during future use. */
        SerializationHelper.write(MODEL_FILE_PATH, classifier);
        SerializationHelper.write(FILTER_FILE_PATH, filter);
    }
}
